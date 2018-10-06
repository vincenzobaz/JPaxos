package lpd.register;

import lsr.service.SimplifiedService;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.ByteArrayInputStream;

import java.nio.ByteBuffer;


public class IntRegisterService extends SimplifiedService {
    private int value;  

    @Override
    protected byte[] execute(byte[] bytes) {
        Command command;
        try {
            command = new Command(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Response r;
        switch (command.getType()) {
            case READ:
                r = new Response(true, value);
                break;
            case WRITE:
                r = new Response(false, -1);
                value = command.getValue();
                break;
            default:
                throw new IllegalArgumentException();
        }
        System.out.println("Asked to perform " + command.toString());
        return r.toByteArray();
    }

    @Override
    protected byte[] makeSnapshot() {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(value);
        return buffer.array();
    }

    @Override
    protected void updateToSnapshot(byte[] snapshot) {
        DataInputStream dataInput = new DataInputStream(new ByteArrayInputStream(snapshot));
        try {
        value = dataInput.readInt(); 
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
}
