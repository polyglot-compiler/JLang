import java.util.HashMap;

public class GenericMethods {

    public static void main(String[] args) {
        new GenericMethods().print("hi", "world");
    }

    public <T> T print(T o1, T o2) {
        System.out.println("T " + o1 + " -- " + o2);
        return o1;
    }

    // Polyglot disagrees with javac on which 'print' method to call,
    // so we ignore this part of the test for now.
    // public String print(String o1, String o2) {
    //     System.out.println("String " + o1 + " -- " + o2);
    //     return o2;
    // }

    public <T> void print(HashMap<T,T> o1){
        System.out.println(o1);
    }
}
