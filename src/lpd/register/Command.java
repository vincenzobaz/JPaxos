package lpd.register;

import java.io.DataInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.nio.ByteBuffer;

public class Command {
    private final CommandType type;
    private final int value;

    public Command(CommandType type, int value) {
        this.type = type;
        this.value = value;
    }

    public Command(byte[] bytes) throws IOException {
        DataInputStream dataInput = new DataInputStream(new ByteArrayInputStream(bytes));
        type = CommandType.fromInt(dataInput.readInt());
        value = dataInput.readInt();
    }

    public int getValue() { return value; }
    public CommandType getType() { return type; }

    public byte[] toByteArray() {
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.putInt(type.ordinal());
        bb.putInt(value);
        return bb.array();
    }

    @Override
    public String toString() {
        if (getType() == CommandType.READ) return "READ";
        else return "WRITE(" + value + ")";
    }
}
