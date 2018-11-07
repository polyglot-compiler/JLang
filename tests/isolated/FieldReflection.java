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
  public String[] sarr;
  public int[] iarr;
  public FieldReflectionGen<String> frGeneric;

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
      // TODO fix something with generics
      // System.out.println(fld.getGenericType());

      if (fld.getName().equals("a")) {
        fld.setInt(aaa, 123412);
      } else if (fld.getName().equals("bb")) {
        fld.setBoolean(aaa, true);
      } else if (fld.getName().equals("cc")) {
        fld.setShort(aaa, new Short((short)17));
        fld.set(aaa, new Short((short)34));
      } else if (fld.getName().equals("ff")) {
        fld.setFloat(aaa, 0.3432f);
      } else if (fld.getName().equals("d")) {
        fld.set(aaa, new Double(1234544.92));
      } else if (fld.getName().equals("j")) {
        fld.setByte(aaa, (byte) 11);
      }
    }

    System.out.println(aaa.a);
    System.out.println(aaa.bb);
    System.out.println(aaa.cc);
    System.out.println(aaa.ff);
    System.out.println(aaa.d);
    System.out.println(aaa.j);
  }

  FieldReflection(int i, int j) {
    a = i;
    s = "A string";
    b = j;
    sarr = new String[]{"one", "two"};
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
