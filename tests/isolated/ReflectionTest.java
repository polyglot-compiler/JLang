import java.lang.reflect.Method; 
import java.lang.reflect.Field; 

public class ReflectionTest {
    private int a;
    private String c;
    // static int b;

    public static void main(String[] args) throws Exception {
        f(1);
    }

    ReflectionTest(int i, int j) {
        a = i;
    }

    public int ll() {
        return 1;
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
        System.out.println(long.class);
        for (Field fld : f) {
            System.out.println(fld.getName());
            System.out.println(fld.getModifiers());
            System.out.println(fld.getType());
            // System.out.println(fld.get(aaa));
            // System.out.println(fld.getGenericType());
        }

        Method[] m = cls.getDeclaredMethods();
        // System.out.println(m.length);
        // for (Method mtd : m) {
        //     System.out.println(mtd.getName());
        //     System.out.println(mtd.getModifiers());
        // }
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
