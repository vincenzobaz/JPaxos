package lpd.register;

import java.util.function.Function;

abstract class IntRegisterCommand implements Function<IntRegister, byte[]> {
    static IntRegisterCommand deserialize(byte[] bytes) {
        if (bytes == null) throw new IllegalArgumentException();
        return bytes.length == 0 ? new Read() : new Write(Utils.deserializeInt(bytes));
    }

    abstract byte[] serialize();

    public abstract String toString();
}

final class Read extends IntRegisterCommand {
    @Override
    public byte[] apply(IntRegister intRegister) {
        return Utils.serializeInt(intRegister.read());
    }

    @Override
    byte[] serialize() {
        return new byte[0];
    }

    @Override
    public String toString() {
        return "READ";
    }
}

final class Write extends IntRegisterCommand {
    private final int newValue;

    Write(int newValue) {
        this.newValue = newValue;
    }

    @Override
    public byte[] apply(IntRegister intRegister) {
        intRegister.write(newValue);
        return new byte [] {};
    }

    @Override
    byte[] serialize() {
        return Utils.serializeInt(newValue);
    }

    @Override
    public String toString() {
        return "WRITE(" + newValue +")";
    }
}
