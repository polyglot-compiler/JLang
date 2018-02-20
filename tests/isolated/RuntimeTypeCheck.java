public class RuntimeTypeCheck {

    static class A{}
    static class B{}

    private static B createB() {
        System.out.println("Creating B");
        return new B();
    }

    public static void main(String[] args) {
        try {
            A a = (A) (Object) createB();
        } catch (ClassCastException e){
            System.out.println("Correct");
        }
        System.out.println("Done");

    }
}
