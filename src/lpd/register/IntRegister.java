package lpd.register;

public class IntRegister {
    private int value;

    IntRegister(int value) {
        this.value = value;
    }

    public void write(int newValue) {
        value = newValue;
    }

    public int read() {
        return value;
    }
}
