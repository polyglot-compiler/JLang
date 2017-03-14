/**
 * Created by Daniel on 3/13/17.
 */
public class Generic<T>{
    public static void main(String[] args) {
        System.out.println(new Generic<String>("Hi There!").toString());
        System.out.println(new Generic<Object>("Hi There!").toString());
        System.out.println(new Generic<A>(new A()).toString());
    }

    T obj;

    public Generic(T obj){
        this.obj = obj;
    }

    @Override
    public String toString() {
        return "Generic["+ obj +"]";
    }

    private static class A{
        @Override
        public String toString() {
            return "A";
        }
    }
}
