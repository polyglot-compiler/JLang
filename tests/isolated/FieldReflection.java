import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;

public class FieldReflection {
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

  public static void main(String[] args) throws Exception {
    System.out.println(Long.class);
    System.out.println(Short.class);
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
      System.out.println(fld.getGenericType());

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
      } else if (fld.getName().equals("ll")) {
        fld.setLong(aaa, 12998769931L);
      } else if (fld.getName().equals("ch")) {
        fld.setChar(aaa, 'h');
      }

      // should work with static arguments as well
      if (fld.getName().equals("b")) {
        fld.set(aaa, 5533);
      } else if (fld.getName().equals("ss")) {
        fld.set(aaa, "ABCD");
      } else if (fld.getName().equals("ii")) {
        fld.setInt(aaa, 88991);
      } else if (fld.getName().equals("sb")) {
        fld.setBoolean(aaa, true);
      } else if (fld.getName().equals("sc")) {
        fld.setChar(aaa, 'o');
      } else if (fld.getName().equals("sby")) {
        fld.setByte(aaa, (byte) 55);
      } else if (fld.getName().equals("ssh")) {
        fld.setShort(aaa, (short) 1351);
      } else if (fld.getName().equals("sf")) {
        fld.setFloat(aaa, 998.43211f);
      } else if (fld.getName().equals("sd")) {
        fld.setDouble(aaa, 10985373.12312414);
      } else if (fld.getName().equals("sl")) {
        fld.setLong(aaa, 1L);
      }
    }

    System.out.println(aaa.a);
    System.out.println(aaa.bb);
    System.out.println(aaa.cc);
    System.out.println(aaa.ff);
    System.out.println(aaa.d);
    System.out.println(aaa.j);
    System.out.println(aaa.ll);
    System.out.println(aaa.ch);

    System.out.println(FieldReflection.b);
    System.out.println(FieldReflection.ss);
    System.out.println(FieldReflection.ii);
    System.out.println(FieldReflection.sb);
    System.out.println(FieldReflection.sc);
    System.out.println(FieldReflection.sby);
    System.out.println(FieldReflection.sf);
    System.out.println(FieldReflection.sd);
    System.out.println(FieldReflection.sl);
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
