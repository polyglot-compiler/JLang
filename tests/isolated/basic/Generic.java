package basic;

public class Generic<T>{
    public static void main(String[] args) {
        System.out.println(new Generic<String>("Hi There!").toString());
        System.out.println(new Generic<Object>("Hi There!").toString());
        System.out.println(new Generic<A>(new A()).toString());

        Generic<String> stringGeneric = new Generic<String>("Hi!");
        System.out.println(stringGeneric.toString());
        stringGeneric.obj = "Changed!";
        System.out.println(stringGeneric.toString());

        String s = stringGeneric.obj;
        System.out.println(s);

        String s1 = stringGeneric.retrieve();
        System.out.println(s1);

        A a = new Generic<A>(new A()).retrieve();
        System.out.println(a.toString());

    }

    public T obj;

    public Generic(T obj){
        this.obj = obj;
    }

    @Override
    public String toString() {
        return "Generic["+ obj +"]";
    }

    public T retrieve(){
        return obj;
    }

    private static class A{
        @Override
        public String toString() {
            return "A";
        }
    }
}
