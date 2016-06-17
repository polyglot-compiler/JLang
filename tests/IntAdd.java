public class IntAdd {
    public static void main() {
        int x = 1+2;
        print(x+f(4,x));
    }
    
    public static int f(int w, int y) {
        int x = w+y;
        return x;
    }

    public static native void print(int i);

}
