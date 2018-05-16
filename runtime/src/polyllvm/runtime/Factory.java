package polyllvm.runtime;

// Helps native code construct common Java objects.
class Factory {

    static boolean[] booleanArray (int len) { return new boolean[len]; }
    static byte   [] byteArray    (int len) { return new byte   [len]; }
    static char   [] charArray    (int len) { return new char   [len]; }
    static short  [] shortArray   (int len) { return new short  [len]; }
    static int    [] intArray     (int len) { return new int    [len]; }
    static long   [] longArray    (int len) { return new long   [len]; }
    static float  [] floatArray   (int len) { return new float  [len]; }
    static double [] doubleArray  (int len) { return new double [len]; }

    static String string(byte[] bytes) { return new String(bytes); }

    // TODO
    static Object[] objectArray(int len) {
        return new Object[len];
    }
}
