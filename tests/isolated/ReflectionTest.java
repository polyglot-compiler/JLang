import java.lang.reflect.Method; 
import java.lang.reflect.Field; 
import java.util.concurrent.atomic.AtomicLong;
import java.util.Random;

public class ReflectionTest {
    public String c;
    public int a;
    static Integer b;

    static class ReflectionTestE extends ReflectionTest {
        public int gg;

        public ReflectionTestE() {
            super(641,42);
        }
    }

    public static void main(String[] args) throws Exception {
        f(1);
    }

    ReflectionTest(int i, int j) {
        a = i;
        c = "A string";
    }

    public String ll(String s) {
        return "Called LL with "+s;
    }

    public static int f(int i) throws Exception {
        int c = 1;
        int s = 2;
        ReflectionTest aaa = new ReflectionTest(1534,2);
        int aa = aaa.hashCode();
        // System.out.println(aa);
        Class cls = aaa.getClass();
        String str = cls.getName();
        // int ii = 	cls.getModifiers();
        Field[] f = cls.getDeclaredFields();
        System.out.println(f.length);
        System.out.println(int.class);
        System.out.println(int.class.getSuperclass());
        System.out.println(long.class);
        System.out.println(AtomicLong.class.getDeclaredField("value"));
        System.out.println(Random.class.getDeclaredField("seed"));
        System.out.println("hello" == "hello");
        // this should succeed once we add compile time string interning
        // System.out.println("hello" == "hel"+"lo");
        System.out.println("hello".intern() == ("hel"+"lo").intern());
        for (Field fld : f) {
            System.out.println(fld.getName());
            System.out.println(fld.getModifiers());
            System.out.println(fld.getType());
            if (fld.getModifiers() != 8)
                System.out.println(fld.get(aaa));
            System.out.println(fld.getGenericType());
        }

        ReflectionTestE re = new ReflectionTestE();
        Class clse = re.getClass();
        Field[] fe = clse.getDeclaredFields();
        System.out.println(fe.length);
        System.out.println(clse.getSuperclass());

        Method[] m = cls.getDeclaredMethods();
        for (Method mtd : m) {
            if (mtd.getName().equals("<init>")) continue;
            System.out.println(mtd.getName());
            System.out.println(mtd.getModifiers());

            if (mtd.getName().equals("ll")) {
                System.out.println(mtd.invoke(aaa, "aa"));
            }
        }
        // Method[] methods = cls.getMethods();
        // for (Method mtd : methods) {
        //     System.out.println(mtd.getName());
        //     System.out.println(mtd.getModifiers());
        // }
        // System.out.println(f[0].getName().intern() == "");
        // Field fld = cls.getDeclaredField("a");
        // System.out.println(fld.getName());
        
        boolean b = aaa instanceof Object;
        return c + s + i;
    }
}
