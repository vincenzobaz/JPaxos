package lsr.paxos;

import lsr.paxos.messages.Message;
import lsr.paxos.messages.MessageType;
import lsr.paxos.messages.Prepare;
import lsr.paxos.network.MessageHandler;
import lsr.paxos.network.Network;
import lsr.paxos.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.BitSet;

public class LeaderElector implements MessageHandler {
    private Storage storage;

    // Replica to replace as leader
    private int toReplace;
    // The view in which the election starts
    private int startView;
    // Whether this elector is in action
    private boolean active;

    // Current candidate for next leader
    private int nextLeader;
    // Missing log entries for the current leader
    private int[] missingLogEntries;


    public LeaderElector(Storage storage) {
        this.storage = storage;
        Network.addMessageListener(MessageType.Prepare, this);
        init(-1);
        active = false;
    }

    /**
     * Start the election of a new leader
     *
     * @param toReplace the replica to be replaced as leader
     */
    public void start(int toReplace) {
        init(toReplace);
        logger.info("LeaderElector asked to start to replace {}", toReplace);
        active = true;
    }

    /**
     * Concludes the election and notify the replica of its result.
     * Called when a majority of PrepareOK is received
     */
    public void stop() {
        logger.info("LeaderElector leader of {} is {}", storage.getView(), nextLeader);
        storage.setLeader(nextLeader);
        for (int i = startView; i < storage.getView(); i++) {
            storage.fillLeader(i, nextLeader);
        }
        active = false;
    }

    private void init(int toReplace) {
        this.startView = storage.getView();
        this.toReplace = toReplace;
        this.nextLeader = storage.getLeader();
        this.missingLogEntries = storage.getHolesIDs();
    }

    @Override
    public void onMessageReceived(Message msg, int sender) {
        if (!active || msg.getView() < startView) {
            return;
        }
        logger.info("LeaderElector processing {} from {}", msg, sender);

        // Safe: the constructor only subscribes to prepares
        Prepare prep = (Prepare) msg;

        // TODO: Specify that they are sorted
        int currMax = missingLogEntries[0];
        int senderMax = prep.getHolesIDs()[0];
        int currHoles = missingLogEntries.length;
        int senderHoles = prep.getHolesIDs().length;

        // If he has less holes than me in his log, he's better!
        // If we have the same number of missing entries, but his smallest missing is higher than mine
        if ((senderHoles < currHoles || (senderHoles == currHoles && senderMax > currMax)) && sender != toReplace) {
            missingLogEntries = prep.getHolesIDs();
            nextLeader = sender;
            logger.info("LeaderElector finished processing, improvement: new candidate {}", nextLeader);
            return;
        }

        int nextL = nextLeader;
        int[] holes = missingLogEntries;

        // In case of ties, choose replica with smallest ID
        if (sender == currHoles && senderMax == currMax) {
            nextL = Math.min(sender, nextL);
            logger.info("LeaderElector TIE");
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

    public boolean isActive() {
        return active;
    }

    @Override
    public void onMessageSent(Message message, BitSet destinations) {

    }

    private final static Logger logger = LoggerFactory.getLogger(LeaderElector.class);
}
