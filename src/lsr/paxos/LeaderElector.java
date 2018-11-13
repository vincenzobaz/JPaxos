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
    private int toReplace;
    private int nextLeader;
    private int missingValues;
    private Storage storage;
    private boolean active;
    private int startView;

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
        logger.info("LeaderElector leader of {} is {}", storage.getLeader(), nextLeader);
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
        this.missingValues = storage.getFirstUncommitted();
    }

    @Override
    public void onMessageReceived(Message msg, int sender) {
        if (!active || msg.getView() < startView) {
            return;
        }
        logger.info("LeaderElector processing {} from {}", msg, sender);

        // Safe: the constructor only subscribes to prepares
        Prepare prep = (Prepare) msg;

        // He is better than me, new potential winner!
        if (prep.getFirstUncommitted() > missingValues) {
            missingValues = prep.getFirstUncommitted();
            nextLeader = sender;
            return;
        }

        // In case of ties, choose smallest ID
        if (prep.getFirstUncommitted() == missingValues) {
            nextLeader = Math.min(sender, nextLeader);
        }

        // If my current choice is the crushed replica, change it in any case
        if (nextLeader == toReplace) {
            nextLeader = sender;
        }
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public void onMessageSent(Message message, BitSet destinations) {

    }

    private final static Logger logger = LoggerFactory.getLogger(LeaderElector.class);
}
