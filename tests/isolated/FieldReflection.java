import java.lang.reflect.Method; 
import java.lang.reflect.Field; 
import java.util.Arrays;
import java.util.Comparator;

public class FieldReflection {
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
    FieldReflection aaa = new FieldReflection(1534,2);
    Class cls = aaa.getClass();
    Field[] f = cls.getDeclaredFields();
    Arrays.sort(f, new FieldComparator());
    System.out.println(f.length);
    for (Field fld : f) {
      System.out.println(fld.getName());
      System.out.println(fld.getModifiers());
      System.out.println(fld.getType());
      System.out.println(fld.get(aaa));
      System.out.println(fld.getGenericType());
    }
  }

  FieldReflection(int i, int j) {
    a = i;
    s = "A string";
    b = j;
  }

    public static class FieldComparator implements Comparator<Field> {
	public int compare(Field f1, Field f2) {
	    return f1.getName().compareTo(f2.getName());
	}

    }
}
