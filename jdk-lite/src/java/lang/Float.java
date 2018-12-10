//Copyright (C) 2018 Cornell University

package java.lang;

public final class Float extends Number {
    private final float value;

    public static String toString(float f) {
        return String.valueOf(f);
    }

    public Float(float value) {
        this.value = value;
    }

    public String toString() {
        return Float.toString(value);
    }

    public float floatValue() {
        return value;
    }

    public static boolean isNaN(float f) {
    	return f != f;
    }
}
