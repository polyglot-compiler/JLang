import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;

class MethodReflection {

    public static void main(String[] args) throws Exception {
        MethodReflection mr = new MethodReflection();
        Class cls = mr.getClass();
        MethodComparator mc = new MethodComparator();
        Method[] m = cls.getDeclaredMethods();
        Arrays.sort(m, mc);
        for (Method mtd : m) {
            if (mtd.getName().equals("<init>")) continue;
            System.out.println(mtd.getName());
            System.out.println(mtd.getModifiers());
            System.out.println(mtd.getReturnType());
            for (Class c : mtd.getParameterTypes()) {
                System.out.println(c);
            }

            if (mtd.getName().equals("ll")) {
                System.out.println(mtd.invoke(mr, "aa"));
            }

            if (mtd.getName().equals("ii")) {
                // TODO: support autoboxing
                // Currently, it is broken when mtc.invoke() returns an int.
                // or it takes in unboxed parameters/arguments.
                Object l = mtd.invoke(mr, new short[]{1, 2, 3}, new Long(100000),
                                         new String[]{"anc"});
                System.out.println(l);
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
