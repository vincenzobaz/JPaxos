package lsr.paxos.election;

import lsr.paxos.messages.Message;
import lsr.paxos.messages.MessageType;
import lsr.paxos.messages.Prepare;
import lsr.paxos.network.MessageHandler;
import lsr.paxos.network.Network;
import lsr.paxos.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.BitSet;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import static lsr.common.ProcessDescriptor.processDescriptor;

public abstract class LeaderElector implements MessageHandler, Comparator<int[]> {
    private Storage storage;
    // Replica to replace as leader
    private int toReplace;
    // The view in which the election starts
    private int startView;
    // Whether this elector is in action
    private boolean active;
    private Set<Integer> intervenedReplicas;
    private int nextLeader;
    private int[] missingLogEntries;

    LeaderElector(Storage storage) {
        this.storage = storage;
        Network.addMessageListener(MessageType.Prepare, this);
        init(-1);
        active = false;
        this.intervenedReplicas = new HashSet<>();

    }

    private void init(int toReplace) {
        this.startView = storage.getView();
        this.toReplace = toReplace;
        this.nextLeader = storage.getLeader();
        this.missingLogEntries = storage.getHolesIDs();
    }

    /**
     * Start the election of a new leader
     *
     * @param toReplace the replica to be replaced as leader
     */
    final public void start(int toReplace) {
        init(toReplace);
        logger.info("LeaderElector asked to start to replace {}", toReplace);
        active = true;
    }

    /**
     * Concludes the election and notify the replica of its result.
     * Called when a majority of PrepareOK is received
     */
    final public void stop() {
        if (intervenedReplicas.size() == 1 && intervenedReplicas.contains(processDescriptor.localId)) {
            // I am the only one to have intervened, continue election
            logger.info("LeaderElector asked to stop only after local vote. Continuing");
            return;
        }
        logger.info("LeaderElector leader of {} is {}", storage.getView(), nextLeader);
        storage.setLeader(nextLeader);
        for (int i = startView; i < storage.getView(); i++) {
            storage.fillLeader(i, nextLeader);
        }
        storage.setLeader(nextLeader);
        active = false;
        intervenedReplicas.clear();
    }

    final public boolean isActive() {
        return active;
    }


    final public void setCandidate(int candidate) {
        this.nextLeader = candidate;
    }

    @Override
    final public void onMessageReceived(Message msg, int sender) {
        if (!active || msg.getView() < startView) {
            return;
        }
        logger.info("LeaderElector processing {} from {}", msg, sender);

        // Safe: the constructor only subscribes to prepares
        Prepare prep = (Prepare) msg;

        intervenedReplicas.add(sender);

        int comparison = compare(missingLogEntries, prep.getHolesIDs());
        // If he has less holes than me in his log, he's better!
        // If we have the same number of missing entries, but his smallest missing is higher than mine
        if (comparison >= 1 && sender != toReplace) {
            missingLogEntries = prep.getHolesIDs();
            nextLeader = sender;
            logger.info("LeaderElector finished processing, improvement: new candidate {}", nextLeader);
            return;
        }

        int nextL = nextLeader;
        int[] holes = missingLogEntries;

        // In case of ties, choose replica with smallest ID
        if (comparison == 0) {
            nextL = Math.min(sender, nextL);
            logger.info("LeaderElector TIE, breaking it with {}", nextL);
            if (nextL == sender) holes = prep.getHolesIDs();
        }

        // If my current choice is the crashed replica, change it in any case
        if (nextL == toReplace) {
            if (nextL == sender) {
                // The current choice is crashed and is the sender, restore previous candidate
                nextL = nextLeader;
                holes = missingLogEntries;
            } else {
                // The current choice is crashed and is the current candidate, pick the sender
                nextL = sender;
                holes = prep.getHolesIDs();
            }
            logger.info("LeaderElector had elected toReplace ({}), fixing mistake with {}", toReplace, nextL);
        }

        nextLeader = nextL;
        missingLogEntries = holes;
        logger.info("LeaderElector finished processing, new candidate {}", nextLeader);
    }

    final public void onMessageSent(Message message, BitSet destinations) {

    }

    protected final static Logger logger = LoggerFactory.getLogger(LeaderElector.class);
}