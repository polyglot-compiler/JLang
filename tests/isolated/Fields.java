import java.lang.reflect.Method; 
import java.lang.reflect.Field; 
import java.util.concurrent.atomic.AtomicLong;
import java.util.Random;

public class Fields {
  public String c;
  public int a;
  public boolean bb;
  public Boolean bbb;
  static Integer b;
  static String s = "AAAA";

  public static void main(String[] args) throws Exception {
    Fields aaa = new Fields(1534,2);
    Class cls = aaa.getClass();
    Field[] f = cls.getDeclaredFields();
    System.out.println(f.length);
    for (Field fld : f) {
      System.out.println(fld.getName());
      System.out.println(fld.getModifiers());
      System.out.println(fld.getType());
      System.out.println(fld.get(aaa));
      System.out.println(fld.getGenericType());
    }
  }

  Fields(int i, int j) {
    a = i;
    c = "A string";
    b = j;
  }
}
