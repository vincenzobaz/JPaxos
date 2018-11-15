package lsr.paxos.messages;

import lsr.paxos.storage.ConsensusInstance;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Represents request to prepare consensus instances higher or equal to
 * <code>firstUncommitted</code>.
 * 
 * @param view - the view being prepared
 * @param firstUncommitted - the first consensus instance for which this process
 *            doesn't know the decision.
 */
public final class Prepare extends Message {
    private static final long serialVersionUID = 1L;
    private final int[] holesId;

    /**
     * Creates new <code>Prepare</code> message. This is request to preparing
     * new view number.
     * 
     * @param view - the view being prepared
     * @param holes - the IDs of the missing entries in the log
     */
    public Prepare(int view, int[] holes) {
        super(view);
        this.holesId = holes;
    }

    /**
     * Creates new <code>Prepare</code> message from serialized stream.
     * 
     * @param input - the input stream with serialized message inside
     * @throws IOException if I/O error occurs
     */
    public Prepare(DataInputStream input) throws IOException {
        super(input);
        this.holesId = new int[input.readInt()];
        for (int i = 0; i < holesId.length; i++) {
            this.holesId[i] = input.readInt();
        }
    }

    public int[] getHolesIDs() {
        return holesId;
    }

    public MessageType getType() {
        return MessageType.Prepare;
    }

    public int byteSize() {
        return super.byteSize() + 4 + (holesId.length * 4);
    }

    public String toString() {
        return "Prepare(" + super.toString() + ", id of log holes " + Arrays.toString(holesId) + ")";
    }

    protected void write(ByteBuffer bb) {
        bb.putInt(holesId.length);
        for (int i = 0; i < holesId.length; i++) {
            bb.putInt(holesId[i]);
        }
    }
}
