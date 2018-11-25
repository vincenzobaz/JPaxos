package lsr.paxos.messages;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class RecoveryAnswer extends Message {
    private static final long serialVersionUID = 1L;

    private final long[] epoch;
    private final long nextId;
    private final int[] leaders;


    public RecoveryAnswer(int view, long[] epoch, long nextId, int[] leaders) {
        super(view);
        this.epoch = epoch;
        this.nextId = nextId;
        this.leaders = leaders;
    }

    public RecoveryAnswer(int view, long nextId, int[] leaders) {
        super(view);
        this.epoch = new long[0];
        this.nextId = nextId;
        this.leaders = leaders;
    }

    public RecoveryAnswer(DataInputStream input) throws IOException {
        super(input);

        int epochSize = input.readInt();
        epoch = new long[epochSize];
        for (int i = 0; i < epoch.length; ++i) {
            epoch[i] = input.readLong();
        }

        nextId = input.readLong();

        int leadersSize = input.readInt();
        leaders = new int[leadersSize];
        for (int i = 0; i < leadersSize; i++) {
            leaders[i] = input.readInt();
        }

    }

    protected void write(ByteBuffer bb) {
        bb.putInt(epoch.length);
        for (int i = 0; i < epoch.length; ++i) {
            bb.putLong(epoch[i]);
        }

        bb.putLong(nextId);

        bb.putInt(leaders.length);
        for (Integer l : leaders) {
            bb.putInt(l);
        }
    }

    public int byteSize() {
        int size = super.byteSize();
        size += 4 + epoch.length * 8; // epoch
        size += 8; // nextId
        size += (4 + 4 * leaders.length) ; // leaders
        return size;
    }

    public long[] getEpoch() {
        return epoch;
    }

    public MessageType getType() {
        return MessageType.RecoveryAnswer;
    }

    public long getNextId() {
        return nextId;
    }

    public int[] getLeaders() {
        return leaders;
    }

    public String toString() {
        return "RecoveryAnswer(" + super.toString() + ",e=" + Arrays.toString(epoch) + ",next=" +
                nextId + ",leaders=" + Arrays.toString(leaders) + ")";
    }
}
