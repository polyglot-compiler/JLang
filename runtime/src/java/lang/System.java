package java.lang;

public class System {
    // Placeholder until we have true JNI.
    public static PrintStream out;

    public static class PrintStream {
        public static native void println();
        public static native void println(String s);
        public static native void print(String s);
        public static native void println(boolean n);
        public static native void print(boolean n);
        public static native void println(int n);
        public static native void print(int n);
        public static native void println(long n);
        public static native void print(long n);
        public static native void println(float n);
        public static native void print(float n);
        public static native void println(double n);
        public static native void print(double n);
    }
}
