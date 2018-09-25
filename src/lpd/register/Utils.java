package lpd.register;

public interface Utils {
    static byte[] serializeInt(int value) {
        byte[] result = new byte[4];
        for (int i = 0; i < 4; i++)
            result[i] = ((byte) (value >> (24 - i * 8) & 0xFF));

        return result;
    }

    static int deserializeInt(byte[] bytes) {
        return (0xFF & (bytes[0] << 24)) |
                (0xFF & (bytes[1] << 16)) |
                (0xFF & (bytes[2] << 8)) |
                (0xFF & bytes[3]);
    }
}
