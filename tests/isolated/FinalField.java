public class FinalField {

    static final int b = calculate();
    static final int a = 3;
    static final int z = 1 + 2;
    static int d = calculate2();
    static final boolean c = true;
    final int x;
    final int y;

    static final int i3 = testString();
    static final int i4 = testString2();
    static final String message = (new FinalField()).toString();

    static final String m = "this is a message";
    static final String m2 = "this" + " is a " + "message";
    
    private static int calculate2() { return z; }
    private static int calculate() { return a; }

    private static int testString() { return m.length(); }
    private static int testString2() { return m.length(); }

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
	System.out.println(z == d);
	System.out.println(m);
	System.out.println(m == "this is a message");
	System.out.println(m == m2);
	System.out.println(m2);
    }

    static {
	System.out.println("Printing static message");
	System.out.println(message);
	System.out.println("-----------------------");
    }

}
