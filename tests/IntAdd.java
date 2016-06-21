public class IntAdd {
    public static void main() {
        int x = 1+2;
        print(x+f(4,x));
        if(x == 3){
            print(1);
        } else if(false){
            print(0);
        } else {
            print(123);
        }
//        double y = 1.0;
    }
    
    public static int f(int w, int y) {
        int x = w+y;
        return x;
    }
    
    public static native void print(int i);

}
