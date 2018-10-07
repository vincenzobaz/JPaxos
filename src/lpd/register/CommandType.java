package lpd.register;

public enum CommandType {
    READ, WRITE;

    public static CommandType fromInt(int i) {
        switch (i) {
            case 0:
                return READ;
            case 1:
                return WRITE;
            default:
                throw new IllegalArgumentException();
        }
    }
}