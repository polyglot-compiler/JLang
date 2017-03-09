/**
 * Created by Daniel on 3/9/17.
 */
public class RuntimeTypeCheck {

    static class A{}
    static class B{}

    public static void main(String[] args) {
        try {
            A a = (A) (Object) new B();
        } catch (ClassCastException e){
            System.out.println("Correct");
        }
        System.out.println("Done");

    }
}
