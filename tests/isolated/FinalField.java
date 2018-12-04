public class FinalField {

    static final int b = calculate();
    static final int a = 3;
    static int d = calculate2();
    static final boolean c = true;
    final int x;
    final int y;
    static final int i3 = testString();
    static final String message = (new FinalField()).toString();

    static final String m = "this is a message";

    private static int calculate2() { return c ? a : b; }
    private static int calculate() { return a; }

    private static int testString() { return m.length(); }

    public FinalField(){

        System.out.println(this);
        x = 3;

        System.out.println(this);
        y = 4;
        System.out.println(this);

    }

    @Override
    public String toString() {
        return "(" + x + ", " + y  + ")";
    }

    public static void main(String[] args) {
	System.out.println("Printing non-static message");
        System.out.println(new FinalField().toString());
	System.out.println("---------------------------");
	System.out.println(a == b);
	System.out.println(a == d);
	System.out.println(m);
	System.out.println(m == "this is a message");
	System.out.println(m == "this is a message".intern());
    }

    static {
	System.out.println("Printing static message");
	System.out.println(message);
	System.out.println("-----------------------");
    }

}
