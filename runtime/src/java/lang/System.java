package java.lang;

public class System {
    // Placeholder until we have true JNI.
    public static PrintStream out;

    public static class PrintStream {
        public static void print(Object o) {
            print(o == null ? "null" : o.toString());
        }

        public static void println(Object o) {
            println(o == null ? "null" : o.toString());
        }

        public static native void println();
        public static native void println(String s);
        public static native void print(String s);
        public static native void println(boolean n);
        public static native void print(boolean n);
        public static native void println(byte n);
        public static native void print(byte n);
        public static native void println(char c);
        public static native void print(char c);
        public static native void println(short n);
        public static native void print(short n);
        public static native void println(int n);
        public static native void print(int n);
        public static native void println(long n);
        public static native void print(long n);
        public static native void println(float n);
        public static native void print(float n);
        public static native void println(double n);
        public static native void print(double n);
    }

    public static SecurityManager getSecurityManager() {
        return null;
    }

    public static native long currentTimeMillis();
    public static native long nanoTime();
    public static native int identityHashCode(Object x);


}
