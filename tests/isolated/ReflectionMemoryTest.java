import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionMemoryTest {
    static boolean b;

    public static void main(String[] args) {
        ReflectionMemoryTest s = new ReflectionMemoryTest();
        Class cls = s.getClass();
        Field[] fields = cls.getDeclaredFields();
        Method[] methods = cls.getDeclaredMethods();
        System.out.println(fields.length);
        for (int i = 0; i < 100000; i++) {
            System.out.println(fields[0].getType());
            System.out.println(methods[0].getReturnType());
            System.out.println(i);
        }
    }
}
