package lpd.register;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.ByteArrayInputStream;

import java.nio.ByteBuffer;


class Response {
    private byte containsValue;
    private int value;

    private static byte boolToByte(boolean b) {
        int i = b ? 1 : 0;
        return (byte) i;
    }
    public Response(boolean isRead, int value) {
        this.containsValue = boolToByte(isRead);
        this.value = value;
    }

    public Response(byte[] bytes) throws IOException {
        DataInputStream dataInput = new DataInputStream(new ByteArrayInputStream(bytes));
        containsValue = dataInput.readByte(); 
        value = dataInput.readInt();
    }

    public byte[] toByteArray() {
        ByteBuffer buffer = ByteBuffer.allocate(5);
        buffer.put(containsValue);
        buffer.putInt(value);
        return buffer.array();
    }

    public boolean isRead() { return containsValue > 0; }

    public int getValue() { return value; }
}
