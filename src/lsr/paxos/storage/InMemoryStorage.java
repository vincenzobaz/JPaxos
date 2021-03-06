package lsr.paxos.storage;

import static lsr.common.ProcessDescriptor.processDescriptor;

import java.util.ArrayList;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.IntStream;

import lsr.paxos.Snapshot;
import lsr.paxos.storage.ConsensusInstance.LogEntryState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InMemoryStorage implements Storage {
    // Must be volatile because it is read by other threads
    // other than the Protocol thread without locking.
    private volatile int view;
    private volatile int leader = 0;
    private final ConcurrentMap<Integer, Integer> leaders = new ConcurrentHashMap<>();
    private volatile int firstUncommitted = 0;

    protected Log log;
    private Snapshot lastSnapshot;

    private ArrayList<ViewChangeListener> viewChangeListeners = new ArrayList<Storage.ViewChangeListener>();

    // must be non-null for proper serialization - NOPping otherwise
    private long[] epoch = new long[0];

    /**
     * Initializes new instance of <code>InMemoryStorage</code> class with empty
     * log.
     */
    public InMemoryStorage() {
        log = new Log();
        leaders.put(0, 0);
    }

    /**
     * Initializes new instance of <code>InMemoryStorage</code> class with
     * specified log.
     * 
     * @param log - the initial content of the log
     */
    public InMemoryStorage(Log log) {
        this.log = log;
        leaders.put(0, 0);
    }

    public Log getLog() {
        return log;
    }

    public Snapshot getLastSnapshot() {
        return lastSnapshot;
    }

    public void setLastSnapshot(Snapshot snapshot) {
        assert lastSnapshot == null || lastSnapshot.compareTo(snapshot) <= 0;
        lastSnapshot = snapshot;
    }

    public int getView() {
        return view;
    }

    @Override
    public int getLeader() {
        return leader;
    }

    @Override
    public int getLeaderOfView(int viewId) {
        if (viewId < 0) return 0;
        if (!leaders.containsKey(viewId)) {
            logger.info("No leader for the provided view ({})", viewId);
        }
        return leaders.getOrDefault(viewId, getLeaderOfView(viewId - 1));
    }

    @Override
    public boolean isLocalProcessLeader() {
        return leader == processDescriptor.localId;
    }

    @Override
    public boolean isLocalProcessLeader(int viewId) {
        return getLeaderOfView(viewId) == processDescriptor.localId;
    }

    @Override
    public void setView(int view) throws IllegalArgumentException {
        assert view > this.view : "Cannot set smaller or equal view.";
        this.view = view;
        updateHistoryAndNotify();
        logger.info("InMemoryStorage: setting view {}", view);
    }

    @Override
    public void setLeader(int id) {
        this.leader = id;
        updateHistoryAndNotify();
        logger.info("InMemoryStorage: setting leader {}", id);
    }

    @Override
    public void setLeaderAndView(int leader, int view) {
        this.view = view;
        this.leader = leader;
        updateHistoryAndNotify();
    }

    @Override
    public void fillLeader(int view, int leader) {
        leaders.put(view, leader);
    }

    private void updateHistoryAndNotify() {
        this.leaders.put(view, leader);
        fireViewChangeListeners();
    }

    public int getFirstUncommitted() {
        return firstUncommitted;
    }

    public void updateFirstUncommitted() {
        if (lastSnapshot != null) {
            firstUncommitted = Math.max(firstUncommitted, lastSnapshot.getNextInstanceId());
        }

        SortedMap<Integer, ConsensusInstance> logs = log.getInstanceMap();
        while (firstUncommitted < log.getNextId() &&
               logs.get(firstUncommitted).getState() == LogEntryState.DECIDED) {
            firstUncommitted++;
        }
    }

    public long[] getEpoch() {
        return epoch;
    }

    public void setEpoch(long[] epoch) {
        this.epoch = epoch;
    }

    public void updateEpoch(long[] epoch) {
        assert epoch.length == this.epoch.length : "Incorrect epoch length";

        for (int i = 0; i < epoch.length; i++) {
            this.epoch[i] = Math.max(this.epoch[i], epoch[i]);
        }
    }

    public void updateEpoch(long newEpoch, int id) {
        assert id < epoch.length : "Incorrect id";

        epoch[id] = Math.max(epoch[id], newEpoch);
    }

    public boolean isInWindow(int instanceId) {
        return instanceId < firstUncommitted + processDescriptor.windowSize;
    }

    public int getWindowUsed() {
        return getLog().getNextId() - getFirstUncommitted();
    }

    public boolean isWindowFull() {
        return getWindowUsed() == processDescriptor.windowSize;
    }

    public boolean isIdle() {
        return getLog().nextId == firstUncommitted;
    }

    public boolean addViewChangeListener(ViewChangeListener l) {
        if (viewChangeListeners.contains(l))
            return false;
        return viewChangeListeners.add(l);
    }

    public boolean removeViewChangeListener(ViewChangeListener l) {
        return viewChangeListeners.remove(l);
    }

    protected void fireViewChangeListeners() {
        for (ViewChangeListener l : viewChangeListeners)
            l.viewChanged(view, leader);
    }

    public long getRunUniqueId() {
        long base = 0;
        switch (processDescriptor.crashModel) {
            case FullSS:
                base = getEpoch()[0];
                break;
            case ViewSS:
                base = getView();
                break;
            case EpochSS:
                base = getEpoch()[processDescriptor.localId];
                break;
            case CrashStop:
                break;
            default:
                throw new RuntimeException();
        }
        return base;
    }

    @Override
    public int[] getHolesIDs() {
        int nextID = log.getNextId();
        if (firstUncommitted < nextID) {
            return IntStream.range(firstUncommitted, log.getNextId())
                    .filter(id -> log.getInstance(id).getState() != LogEntryState.DECIDED)
                    .toArray();
        } else {
            return new int[]{firstUncommitted};
        }
    }

    private final static Logger logger = LoggerFactory.getLogger(InMemoryStorage.class);
}
