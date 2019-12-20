import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class ConcurrentReflection {
    static class ClassReflection {

        public static void run(List<Object>output) {
            ClassReflectionE ce = new ClassReflectionE();
            Class clse = ce.getClass();
            Field[] fe = clse.getDeclaredFields();
            output.add(fe.length);
            output.add(clse.getSuperclass());
        }

        int k;

        public ClassReflection(int i, int j) {
            k = i + j;
        }

        static class GenericClassReflection<T> {
            T[] arr;

            public T get() {
                return arr[0];
            }
        }

        static class ClassReflectionE extends ClassReflection {
            public int gg;

            public ClassReflectionE() {
                super(641, 42);
            }
        }
    }

    static class MethodReflection {

        public static void run(List<Object>output) throws Exception {
            MethodReflection mr = new MethodReflection();
            Class cls = mr.getClass();
            MethodComparator mc = new MethodComparator();
            Method[] m = cls.getDeclaredMethods();
            Arrays.sort(m, mc);
            for (Method mtd : m) {
                if (mtd.getName().equals("<init>")) continue;
                output.add(mtd.getName());
                output.add(mtd.getModifiers());
                output.add(mtd.getReturnType());
                for (Class c : mtd.getParameterTypes()) {
                    output.add(c);
                }

                if (mtd.getName().equals("ll")) {
                    output.add(mtd.invoke(mr, "aa"));
                }

                if (mtd.getName().equals("ii")) {
                    Object l = mtd.invoke(mr, new short[]{1, 2, 3}, new Long(100000),
                            new String[]{"anc"});
                    output.add(l);
                }
            }
        }

        private String ii(short[] ss, Long l, String[] strs) {
            return "Called ii with " + Arrays.toString(ss) + " : " + l + " : " + Arrays.toString(strs);
        }

        public String ll(String s) {
            return "Called LL with " + s;
        }

        public static class MethodComparator implements Comparator<Method> {

            public int compare(Method m1, Method m2) {
                return m1.getName().compareTo(m2.getName());
            }

        }
    }


    static class FieldReflection {
        public int a;
        public Integer ai;
        public boolean bb;
        public Boolean bbb;
        public byte j;
        public Byte jj;
        public Short c;
        public short cc;
        public char ch;
        public Character Chr = 'a';
        public Float f = 1.0f;
        public float ff = 512.322f;
        public double d = 0x44;
        public Double dd = 111.111111;
        public long ll = 1231231232;
        public Long L;
        public String s;
        // TODO test these
        public String[] sarr;
        public int[] iarr;
        // TODO once inner classes have correct signatures
        public FieldReflectionGen<String> frGeneric;
        public ArrayList<String> strArrayList;

        static Integer b;
        static String ss = "AAAA";
        static int ii = 12;
        static boolean sb = false;
        static char sc;
        static short ssh;
        static byte sby;
        static float sf;
        static double sd = 0.544;
        static long sl = 1234449L;

        public static void run(List<Object>output) throws Exception {
            output.add(Long.class);
            output.add(Short.class);
            FieldReflection aaa = new FieldReflection(1534,2);
            Class cls = aaa.getClass();
            Field[] f = cls.getDeclaredFields();
            Arrays.sort(f, new FieldComparator());
            output.add(f.length);
            for (Field fld : f) {
                output.add(fld.getName());
                output.add(fld.getModifiers());
                output.add(fld.getType());
                output.add(fld.get(aaa));
            }

            output.add(aaa.a);
            output.add(aaa.bb);
            output.add(aaa.cc);
            output.add(aaa.ff);
            output.add(aaa.d);
            output.add(aaa.j);
            output.add(aaa.ll);
            output.add(aaa.ch);

            output.add(FieldReflection.b);
            output.add(FieldReflection.ss);
            output.add(FieldReflection.ii);
            output.add(FieldReflection.sb);
            output.add(FieldReflection.sc);
            output.add(FieldReflection.sby);
            output.add(FieldReflection.sf);
            output.add(FieldReflection.sd);
            output.add(FieldReflection.sl);
        }

        FieldReflection(int i, int j) {
            a = i;
            s = "A string";
            b = j;
        }

        static class FieldReflectionGen<T> {
            T[] arr;
        }

        public static class FieldComparator implements Comparator<Field> {
            public int compare(Field f1, Field f2) {
                return f1.getName().compareTo(f2.getName());
            }

        }
    }

    static int THREADS = 2;

    static class PrimitiveReflection {

        public static void run(List<Object>output) {
            output.add(int.class);
            output.add(int.class.getSuperclass());
            output.add(long.class);
            output.add(short.class);
            output.add(char.class);
            output.add(byte.class);
            output.add(float.class);
            output.add(double.class);
            output.add(boolean.class);
            output.add(void.class);
        }
    }

    static List<Object> output = Collections.synchronizedList(new ArrayList<>());

    static class A extends Thread {
        @Override
        public void run() {
            try {
                ClassReflection.run(output);
                MethodReflection.run(output);
                FieldReflection.run(output);
                PrimitiveReflection.run(output);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    public static void main(String[] args) {
        A[] pool = new A[THREADS];
        for (int i = 0; i < THREADS; i++) {
            pool[i] = new A();
            pool[i].start();
        }
        for (int i = 0; i < THREADS; i++) {
            try {
                pool[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Collections.sort(output, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                return Objects.toString(o1).compareTo(Objects.toString(o2));
            }
        });

        for (Object o : output) {
            System.out.println(o);
        }
    }
}
