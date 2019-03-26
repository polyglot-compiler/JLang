import java.lang.reflect.Array;

public class ArrayReflection {

    public static void main(String[] args) {
        System.out.println("--> int array test begins");
        intArrayTest();
        System.out.println("<-- int array test ends");
        System.out.println("--> boolean array test begins");
        booleanArrayTest();
        System.out.println("<-- boolean array test ends");
        System.out.println("--> byte array test begins");
        byteArrayTest();
        System.out.println("<-- byte array test ends");
        System.out.println("--> char array test begins");
        charArrayTest();
        System.out.println("<-- char array test ends");
        System.out.println("--> double array test begins");
        doubleArrayTest();
        System.out.println("<-- double array test ends");
        System.out.println("--> float array test begins");
        floatArrayTest();
        System.out.println("<-- float array test ends");
        System.out.println("--> long array test begins");
        longArrayTest();
        System.out.println("<-- long array test ends");
        System.out.println("--> short array test begins");
        shortArrayTest();
        System.out.println("<-- short array test ends");
        System.out.println("<-- object array test starts");
        objectArrayTest();
        System.out.println("<-- object array test ends");
        System.out.println("<-- inheritance array test starts");
        inheritanceArrayTest();
        System.out.println("<-- inheritance array test ends");

    }

    private static void intArrayTest() {
        // TODO: handle array larger than limit
        // Object arr = Array.newInstance(int.class, Integer.MAX_VALUE);
        Object arr = Array.newInstance(int.class, 4);
        System.out.println(arr.toString());
        System.out.println(arr.getClass().getName());
        System.out.println(arr.getClass().getComponentType());

        Array.set(arr, 0, 1);
        Array.set(arr, 1, 2019);
        Array.set(arr, 2, 100);
        System.out.println(Array.get(arr, 0));
        System.out.println(Array.getInt(arr, 1));
        System.out.println(Array.get(arr, 2));

        Array.setInt(arr, 0, 1019);
        Array.setInt(arr, 1, 330);
        Array.setInt(arr, 2, 9102);
        System.out.println(Array.getInt(arr, 0));
        System.out.println(Array.get(arr, 1));
        System.out.println(Array.getInt(arr, 2));

        // default value
        System.out.println(Array.get(arr, 3));
        System.out.println(Array.getInt(arr, 3));

        // implicit conversion
        System.out.println(Array.getDouble(arr, 1));
        System.out.println(Array.getFloat(arr, 1));

        try {
            Array.set(arr, 1, false);
        } catch (IllegalArgumentException e) {
            // expected exception: argument type mismatch
            System.out.println(e.toString());
        }

        try {
            Array.getBoolean(arr, 2);
        } catch (IllegalArgumentException e) {
            // expected exception: argument type mismatch
            System.out.println(e.toString());
        }

        try {
            Array.set(arr, 5, 3);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println(e.toString());
        }

        try {
            Array.set(arr, 5, false);
        } catch (Exception e) {
            // expected exception: ArrayIndexOutOfBoundsException
            System.out.println(e.toString());
        }

        // test Integer array vs int array
        Object integerArr = Array.newInstance(Integer.class, 3);
        System.out.println(integerArr.toString());
        System.out.println(integerArr.getClass().getName());
        System.out.println(integerArr.getClass().getComponentType());

        Array.set(integerArr, 0, 3);
        Array.set(integerArr, 1, Integer.MAX_VALUE);
        try {
            Array.setInt(integerArr, 2, 1000);
        } catch (IllegalArgumentException e) {
            // expected exception: argument is not an array
            System.out.println(e.toString());
        }
        try {
            System.out.println(Array.getInt(integerArr, 0));
        } catch (IllegalArgumentException e) {
            // expected exception: argument is not an array
            System.out.println(e.toString());
        }
        System.out.println(Array.get(integerArr, 1));
        System.out.println(Array.get(integerArr, 2));
        try {
            Array.setInt(integerArr, 1, Integer.MAX_VALUE);
        } catch (IllegalArgumentException e) {
            // expected exception: argument is not an array
            System.out.println(e.toString());
        }
    }

    private static void booleanArrayTest() {
        System.out.println("boolean array test begins");
        Object arr = Array.newInstance(boolean.class, 2);
        System.out.println(arr.toString());
        System.out.println(arr.getClass().getName());
        System.out.println(arr.getClass().getComponentType());
        System.out.println(Array.getLength(arr));

        Array.set(arr, 0, false);
        Array.set(arr, 1, true);
        System.out.println(Array.get(arr, 0));
        System.out.println(Array.getBoolean(arr, 1));

        Array.setBoolean(arr, 0, true);
        Array.setBoolean(arr, 1, false);
        System.out.println(Array.getBoolean(arr, 0));
        System.out.println(Array.get(arr, 1));

        try {
            Array.set(arr, 1, 1);
        } catch (IllegalArgumentException e) {
            // expected exception: argument type mismatch
            System.out.println(e.toString());
        }

        try {
            Array.getInt(arr, 0);
        } catch (IllegalArgumentException e) {
            // expected exception: argument type mismatch
            System.out.println(e.toString());
        }

        try {
            Array.set(arr, 5, false);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println(e.toString());
        }

        try {
            Array.set(arr, 5, 3);
        } catch (Exception e) {
            // expected exception: ArrayIndexOutOfBoundsException
            System.out.println(e.toString());
        }
    }

    private static void byteArrayTest() {
        Object arr = Array.newInstance(byte.class, 2);
        System.out.println(arr.toString());
        System.out.println(arr.getClass().getName());
        System.out.println(arr.getClass().getComponentType());
        System.out.println(Array.getLength(arr));

        Array.set(arr, 0, (byte)5);
        Array.set(arr, 1, (byte)65);
        System.out.println(Array.get(arr, 0));
        System.out.println(Array.getByte(arr, 1));

        Array.setByte(arr, 0, (byte)0x12);
        Array.setByte(arr, 1, (byte)0x122);
        System.out.println(Array.getByte(arr, 0));
        System.out.println(Array.get(arr, 1));

        // implicit conversion
        System.out.println(Array.getInt(arr, 0));
        System.out.println(Array.getDouble(arr, 1));

        try {
            Array.set(arr, 1, 1);
        } catch (IllegalArgumentException e) {
            // expected exception: argument type mismatch
            System.out.println(e.toString());
        }

        try {
            Array.getChar(arr, 0);
        } catch (IllegalArgumentException e) {
            // expected exception: argument type mismatch
            System.out.println(e.toString());
        }

        try {
            Array.set(arr, 5, (byte)2);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println(e.toString());
        }

        try {
            Array.set(arr, 5, 3);
        } catch (Exception e) {
            // expected exception: ArrayIndexOutOfBoundsException
            System.out.println(e.toString());
        }
    }

    private static void charArrayTest() {
        Object arr = Array.newInstance(char.class, 2);
        System.out.println(arr.toString());
        System.out.println(arr.getClass().getName());
        System.out.println(arr.getClass().getComponentType());
        System.out.println(Array.getLength(arr));

        Array.set(arr, 0, '1');
        Array.set(arr, 1, (char)65);
        System.out.println(Array.get(arr, 0));
        System.out.println(Array.getChar(arr, 1));

        Array.setChar(arr, 0, (char)90);
        Array.setChar(arr, 1, '%');
        System.out.println(Array.getChar(arr, 0));
        System.out.println(Array.get(arr, 1));

        // implicit conversion
        System.out.println(Array.getInt(arr, 0));
        System.out.println(Array.getDouble(arr, 1));

        try {
            Array.set(arr, 1, 1);
        } catch (IllegalArgumentException e) {
            // expected exception: argument type mismatch
            System.out.println(e.toString());
        }

        try {
            Array.getBoolean(arr, 0);
        } catch (IllegalArgumentException e) {
            // expected exception: argument type mismatch
            System.out.println(e.toString());
        }

        try {
            Array.set(arr, 5, 'a');
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println(e.toString());
        }

        try {
            Array.set(arr, 5, 3);
        } catch (Exception e) {
            // expected exception: ArrayIndexOutOfBoundsException
            System.out.println(e.toString());
        }
    }

    private static void doubleArrayTest() {
        Object arr = Array.newInstance(double.class, 2);
        System.out.println(arr.toString());
        System.out.println(arr.getClass().getName());
        System.out.println(arr.getClass().getComponentType());
        System.out.println(Array.getLength(arr));

        Array.set(arr, 0, 0.32);
        Array.set(arr, 1, 7.54121);
        System.out.println(Array.get(arr, 0));
        System.out.println(Array.getDouble(arr, 1));

        Array.setDouble(arr, 0, 43.1234);
        Array.setDouble(arr, 1, 123.123456);
        System.out.println(Array.getDouble(arr, 0));
        System.out.println(Array.get(arr, 1));

        // implicit conversion
        Array.setInt(arr, 0, 3);
        Array.setFloat(arr, 1, 34f);

        try {
            Array.set(arr, 1, false);
        } catch (IllegalArgumentException e) {
            // expected exception: argument type mismatch
            System.out.println(e.toString());
        }

        try {
            Array.getInt(arr, 0);
        } catch (IllegalArgumentException e) {
            // expected exception: argument type mismatch
            System.out.println(e.toString());
        }

        try {
            Array.set(arr, 5, 12.12);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println(e.toString());
        }

        try {
            Array.set(arr, 5, 3);
        } catch (Exception e) {
            // expected exception: ArrayIndexOutOfBoundsException
            System.out.println(e.toString());
        }
    }

    private static void floatArrayTest() {
        Object arr = Array.newInstance(float.class, 2);
        System.out.println(arr.toString());
        System.out.println(arr.getClass().getName());
        System.out.println(arr.getClass().getComponentType());
        System.out.println(Array.getLength(arr));

        Array.set(arr, 0, 1.1f);
        Array.set(arr, 1, (float) 2.23);
        System.out.println(Array.get(arr, 0));
        System.out.println(Array.getFloat(arr, 1));

        Array.setFloat(arr, 0, 0.43f);
        Array.setFloat(arr, 1, (float) 1);
        System.out.println(Array.getFloat(arr, 0));
        System.out.println(Array.get(arr, 1));

        // implicit conversion
        Array.setChar(arr, 0, 'a');
        System.out.println(Array.getFloat(arr, 0));
        System.out.println(Array.getDouble(arr, 1));

        try {
            Array.set(arr, 1, false);
        } catch (IllegalArgumentException e) {
            // expected exception: argument type mismatch
            System.out.println(e.toString());
        }

        try {
            Array.getBoolean(arr, 0);
        } catch (IllegalArgumentException e) {
            // expected exception: argument type mismatch
            System.out.println(e.toString());
        }

        try {
            Array.set(arr, 5, 1.2f);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println(e.toString());
        }

        try {
            Array.set(arr, 5, 3);
        } catch (Exception e) {
            // expected exception: ArrayIndexOutOfBoundsException
            System.out.println(e.toString());
        }
    }

    private static void longArrayTest() {
        Object arr = Array.newInstance(long.class, 2);
        System.out.println(arr.toString());
        System.out.println(arr.getClass().getName());
        System.out.println(arr.getClass().getComponentType());
        System.out.println(Array.getLength(arr));

        Array.set(arr, 0, 1L);
        Array.set(arr, 1, 2L);
        System.out.println(Array.get(arr, 0));
        System.out.println(Array.getLong(arr, 1));

        Array.setLong(arr, 0, Long.MAX_VALUE);
        Array.setLong(arr, 1, Long.MIN_VALUE);
        System.out.println(Array.getLong(arr, 0));
        System.out.println(Array.get(arr, 1));

        // implicit conversion
        Array.setInt(arr, 0, 1);
        Array.setChar(arr, 1, 'a');
        System.out.println(Array.getLong(arr, 0));
        System.out.println(Array.get(arr, 1));

        try {
            Array.set(arr, 1, false);
        } catch (IllegalArgumentException e) {
            // expected exception: argument type mismatch
            System.out.println(e.toString());
        }

        try {
            Array.getInt(arr, 0);
        } catch (IllegalArgumentException e) {
            // expected exception: argument type mismatch
            System.out.println(e.toString());
        }

        try {
            Array.set(arr, 5, false);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println(e.toString());
        }

        try {
            Array.set(arr, 5, 3.0f);
        } catch (Exception e) {
            // expected exception: ArrayIndexOutOfBoundsException
            System.out.println(e.toString());
        }
    }

    private static void shortArrayTest() {
        Object arr = Array.newInstance(short.class, 2);
        System.out.println(arr.toString());
        System.out.println(arr.getClass().getName());
        System.out.println(arr.getClass().getComponentType());
        System.out.println(Array.getLength(arr));

        Array.set(arr, 0, (short) 2);
        Array.set(arr, 1, (short) 5);
        System.out.println(Array.get(arr, 0));
        System.out.println(Array.getShort(arr, 1));

        Array.setShort(arr, 0, (short) 10);
        Array.setShort(arr, 1, Short.MIN_VALUE);
        System.out.println(Array.getShort(arr, 0));
        System.out.println(Array.get(arr, 1));

        // implicit conversion
        Array.setByte(arr, 0, (byte) 5);
        System.out.println(Array.getInt(arr, 0));
        System.out.println(Array.getDouble(arr, 0));

        try {
            Array.setChar(arr, 0, 'a');
        } catch (IllegalArgumentException e) {
            // expected exception: argument type mismatch
            System.out.println(e.toString());
        }

        try {
            Array.getByte(arr, 0);
        } catch (IllegalArgumentException e) {
            // expected exception: argument type mismatch
            System.out.println(e.toString());
        }

        try {
            Array.set(arr, 5, false);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println(e.toString());
        }

        try {
            Array.set(arr, 5, 3);
        } catch (Exception e) {
            // expected exception: ArrayIndexOutOfBoundsException
            System.out.println(e.toString());
        }
    }

    private static class TestClass {
        int a, b;
        TestClass c;

        TestClass() {
            this.a = -1;
            this.b = -2;
        }

        TestClass(int a, int b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public String toString() {
            return super.toString() + "a: " + a + "b: " + b;
        }
    }

    private static void objectArrayTest() {
        Object arr = Array.newInstance(TestClass.class, 3);
        System.out.println(arr.toString());
        System.out.println(arr.getClass().getName());
        System.out.println(arr.getClass().getComponentType());
        System.out.println(Array.getLength(arr));

        Array.set(arr, 0, new TestClass());
        Array.set(arr, 1, new TestClass(123, 456));

        System.out.println(Array.get(arr, 0));
        System.out.println(Array.get(arr, 1));
        System.out.println(Array.get(arr, 2));

        Array.set(arr, 0, null);
        System.out.println(Array.get(arr, 0));

        try {
            Array.setInt(arr, 0, 0);
        } catch (IllegalArgumentException e) {
            // expected exception: Argument is not an array
            System.out.println(e.toString());
        }

        try {
            Array.set(arr, 0, new Object());
        } catch (IllegalArgumentException e) {
            // expected exception: array element type mismatch
            System.out.println(e.toString());
        }
    }

    private static class TestBaseClass {
        protected int a, b;

        TestBaseClass(int a, int b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public String toString() {
            return super.toString() + "a: " + a + "b: " + b;
        }
    }

    private static class TestDerivedClass extends TestBaseClass {
        int c;

        TestDerivedClass(int a, int b, int c) {
            super(a, b);
            this.c = c;
        }

        @Override
        public String toString() {
            return super.toString() + "c: " + c;
        }
    }

    private static void inheritanceArrayTest() {
        Object arrBase = Array.newInstance(TestBaseClass.class, 2);
        System.out.println(arrBase.toString());
        System.out.println(arrBase.getClass().getName());
        System.out.println(arrBase.getClass().getComponentType());
        System.out.println(Array.getLength(arrBase));

        Object arrDerived = Array.newInstance(TestDerivedClass.class, 2);
        System.out.println(arrDerived.toString());
        System.out.println(arrDerived.getClass().getName());
        System.out.println(arrDerived.getClass().getComponentType());
        System.out.println(Array.getLength(arrDerived));

        Array.set(arrBase, 0, new TestBaseClass(1, 5));
        Array.set(arrBase, 1, new TestDerivedClass(7, 9, 110));

        System.out.println(Array.get(arrBase, 0));
        System.out.println(Array.get(arrBase, 1));

        System.out.println((TestBaseClass[])arrBase);
        try {
            System.out.println((TestDerivedClass[])arrBase);
        } catch (ClassCastException e) {
            // expected exception: [LArrayReflection$TestBaseClass;
            //                     cannot be cast to [LArrayReflection$TestDerivedClass;
            System.out.println(e.toString());
        }

        TestBaseClass[] castArray = (TestBaseClass[])arrBase;
        castArray[0] = new TestBaseClass(12, 3);
        castArray[1] = new TestDerivedClass(1, 59, -2);
        System.out.println(castArray[0]);
        System.out.println(castArray[1]);

        try {
            Array.set(arrDerived, 0, new TestBaseClass(342, 351));
        } catch (IllegalArgumentException e) {
            // expected exception: array element type mismatch
            System.out.println(e.toString());
        }
        Array.set(arrDerived, 1, new TestDerivedClass(109, 2018, 2017));

        System.out.println(Array.get(arrDerived, 0));
        System.out.println(Array.get(arrDerived, 1));

        System.out.println((TestDerivedClass[])arrDerived);
        System.out.println((TestBaseClass[])arrDerived);

        castArray = (TestBaseClass[])arrDerived;

        System.out.println(castArray[0]);
        System.out.println(castArray[1]);

        try {
            castArray[0] = new TestBaseClass(1, 2);
        } catch (ArrayStoreException e) {
            // expected exception: java.lang.ArrayStoreException: ArrayReflection$TestBaseClass
            System.out.println(e.toString());
        }

        castArray[1] = new TestDerivedClass(123, 34, 123);
        System.out.println(castArray[0]);
        System.out.println(castArray[1]);
    }

}
