package unit;

class Assert {

    public static void main(String[] args) {
        try {
            assert true;
            System.out.println("After1");
        } catch (AssertionError e) {
            System.out.println("Bad");
        }
        try {
            assert false;
            System.out.println("After2");
        } catch (AssertionError e) {
            System.out.println("Good");
        }
    }
}
