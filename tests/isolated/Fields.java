import java.lang.reflect.Method; 
import java.lang.reflect.Field; 
import java.util.concurrent.atomic.AtomicLong;
import java.util.Random;

public class Fields {
  public int a;
  public boolean bb;
  public Boolean bbb;
  public byte j;
  public Byte jj;
  public Short c;
  public short cc;
  public Float f = 1.0f;
  public float ff = 512.322f;
  public double d = 0x44;
  public Double dd = 111.111111;
  public long ll = 1231231232;
  public Long L;
  public String s;

  static Integer b;
  static String ss = "AAAA";

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
    s = "A string";
    b = j;
  }
}
