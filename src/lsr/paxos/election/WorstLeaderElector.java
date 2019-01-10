package lsr.paxos.election;

import lsr.paxos.storage.Storage;

public class WorstLeaderElector extends LeaderElector {
    public WorstLeaderElector(Storage storage) {
        super(storage);
    }

    @Override
    public int compare(int[] currHoles, int[] senderHoles) {
        int currMax = currHoles[0];
        int senderMax = senderHoles[0];

        if (senderHoles.length > currHoles.length || (senderHoles == currHoles && senderMax < currMax))
            return 1;
        else if (senderHoles.length == currHoles.length && senderMax == currMax)
            return 0;
        else
            return -1;
    }
}
