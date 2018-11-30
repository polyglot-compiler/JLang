public class FinalField {

    static final int b = calculate();
    static final int a = 3;
    final int x;
    final int y;

    private static int calculate() { return a; }

    public FinalField(){

        System.out.println(this);
        x = 3;

        System.out.println(this);
        y = 4;
        System.out.println(this);
        //PUT Ds here

    }

    @Override
    public String toString() {
        return "(" /*+ x + ", " + y */ + ")";
    }

    public static void main(String[] args) {
        System.out.println(new FinalField().toString());
	System.out.println(a == b);
    }

}
