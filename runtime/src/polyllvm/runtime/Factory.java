package polyllvm.runtime;

// Helps native code construct common Java objects.
class Factory {

    static byte[] createByteArray(int len) {
        return new byte[len];
    }

    static Object[] createObjectArray(int len) {
        return new Object[len];
    }

    static String createString(byte[] bytes) {
        return new String(bytes);
    }
}
