package lsr.paxos.core;

import lsr.paxos.messages.PrepareOK;

public interface Proposer {
    public enum ProposerState {
        INACTIVE, PREPARING, PREPARED
    }

    public void start();

    public ProposerState getState();

    public void ballotFinished();

    public void stopProposer();

    public void onPrepareOK(PrepareOK msg, int sender);

    public void propose(byte[] value);

    public void prepareNextView();

    /**
     * After reception of majority accepts, we suppress propose messages.
     * 
     * @param instanceId no. of instance, for which we want to stop
     *            retransmission
     */
    public void stopPropose(int instanceId);

    /**
     * If retransmission to some process for certain instance is no longer
     * needed, we should stop it
     * 
     * @param instanceId no. of instance, for which we want to stop
     *            retransmission
     * @param destination number of the process in processes PID list
     */
    public void stopPropose(int instanceId, int destination);

    interface Task {
        void onPrepared();

        void onFailedToPrepare();
    }

    /**
     * Schedules a task to be executed as soon as the proposer is prepared
     */
    public void executeOnPrepared(final Task task);

}