import java.lang.reflect.Field; 

class ClassReflection {

    public static void main(String[] args) {
        ClassReflectionE ce = new ClassReflectionE();
        Class clse = ce.getClass();
        Field[] fe = clse.getDeclaredFields();
        System.out.println(fe.length);
        System.out.println(clse.getSuperclass());
    }

    int k;

    public ClassReflection(int i, int j) {
        k = i + j;
    }

    static class GenericClassReflection<T> {
        T[] arr;

        public T get(){
            return arr[0];
        }
    }

    static class ClassReflectionE extends ClassReflection {
        public int gg;

        public ClassReflectionE() {
            super(641,42);
        }
    }
}