package java.io;

// Hack to easily get console I/O.
public class PrintStream {

    public void print(Object o) {
        print(o == null ? "null" : o.toString());
    }

    public void println(Object o) {
        println(o == null ? "null" : o.toString());
    }

    public native void println();

    public native void print(String s);
    public native void println(String s);

    public native void print(boolean n);
    public native void println(boolean n);

    public native void print(byte n);
    public native void println(byte n);

    public native void print(char c);
    public native void println(char c);

    public native void print(short n);
    public native void println(short n);

    public native void print(int n);
    public native void println(int n);

    public native void print(long n);
    public native void println(long n);

    public native void print(float n);
    public native void println(float n);

    public native void print(double n);
    public native void println(double n);

    public native void flush();
}
