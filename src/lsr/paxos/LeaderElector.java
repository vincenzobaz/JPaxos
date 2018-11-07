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
    private int nextLeader;
    private int missing;
    private Storage storage;
    private boolean active;
    private int startView;

    public LeaderElector(Storage storage) {
        this.storage = storage;
        Network.addMessageListener(MessageType.Prepare, this);
        Network.addMessageListener(MessageType.PrepareOK, this);
        init();
        active = false;
    }

    public void start() {
        init();
        logger.info("LeaderElector asked to start");
        active = true;
    }

    public void stop() {
        logger.info("LeaderElector leader of {} is {}", storage.getLeader(), nextLeader);
        storage.setLeader(nextLeader);
        active = false;
    }

    private void init() {
        startView = storage.getView();
        nextLeader = storage.getLeader();
        missing = storage.getFirstUncommitted();
    }

    @Override
    public void onMessageReceived(Message msg, int sender) {
        if (!active || msg.getView() < startView) {
            return;
        }
        logger.info("LeaderElector processing {} from {}", msg, sender);
        switch (msg.getType()) {
            case Prepare:
                Prepare prep = (Prepare) msg;
                if (prep.getFirstUncommitted() >= missing) {
                    missing = prep.getFirstUncommitted();
                    nextLeader = sender;
                }
                break;
                // Stop is also called onMajorityPrepareOk, the moment where the decision is taken
        }
    }

    @Override
    public void onMessageSent(Message message, BitSet destinations) {

    }

    private final static Logger logger = LoggerFactory.getLogger(LeaderElector.class);
}
