package java.lang;

public final class Integer extends Number {

    public static String toString(int i) {
        return String.valueOf(i);
    }

    public static Integer valueOf(int i) {
        return new Integer(i);
    }


    private final int value;

    public Integer(int value) {
        this.value = value;
    }

    public int intValue() {
        return value;
    }

    public float floatValue() {
        return (float) value;
    }

    public String toString() {
        return toString(value);
    }

    public int hashCode() {
        return value;
    }

    public boolean equals(Object obj) {
        if (obj instanceof Integer) {
            return value == ((Integer) obj).intValue();
        }
        return false;
    }
}
