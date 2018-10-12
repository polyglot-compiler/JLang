//Copyright (C) 2018 Cornell University

package jlang.runtime;

// Helps native code construct common Java objects.
class Factory {

    static boolean[] BooleanArray (int len) { return new boolean[len]; }
    static byte   [] ByteArray    (int len) { return new byte   [len]; }
    static char   [] CharArray    (int len) { return new char   [len]; }
    static short  [] ShortArray   (int len) { return new short  [len]; }
    static int    [] IntArray     (int len) { return new int    [len]; }
    static long   [] LongArray    (int len) { return new long   [len]; }
    static float  [] FloatArray   (int len) { return new float  [len]; }
    static double [] DoubleArray  (int len) { return new double [len]; }

    static String String(char[] chars) { return new String(chars); }

    static Object[] ObjectArray(int len) {
        return new Object[len];
    }

    static Integer autoBoxInt(int i) {
        return new Integer(i);
    }

    static void printString(String s) {
        System.out.println(s);
    }

}
