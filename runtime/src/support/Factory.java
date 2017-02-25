package support;

// Helps native code construct common Java objects.
public class Factory {

    public static byte[] createByteArray(int len) {
        return new byte[len];
    }

    public static Object[] createObjectArray(int len) {
        return new Object[len];
    }

    public static String createString(byte[] bytes) {
        return new String(bytes);
    }
}
