package java.lang;

public final class String {
    private final char value[];

    public String(char value[]) {
        this(value, false);
    }

    public String(char value[], boolean share) {
        if(share) {
            this.value = value;
        } else {
            this.value = new char[value.length];
            charArrayCopy(value, this.value);
        }
    }

    public String(byte bytes[]) {
                value = new char[bytes.length];
        for (int i = 0; i < bytes.length; ++i) {
            value[i] = (char) bytes[i];
        }
    }

    public String concat(String other) {
        char value[] = new char[this.value.length + other.value.length];
        for (int i = 0; i < this.value.length; ++i) {
            value[i] = this.value[i];
        }
        for (int i = this.value.length; i <this.value.length + other.value.length; ++i) {
            value[i] = other.value[i-this.value.length];
        }
        return new String(value);
    }

    public boolean equals(Object o) {
        if (!(o instanceof String))
            return false;
        String s = (String) o;
        if (value.length != s.value.length)
            return false;
        for (int i = 0; i < value.length; ++i)
            if (value[i] != s.value[i])
                return false;
        return true;
    }

    public String toString() {
        return this;
    }

    public char[] toCharArray() {
                char result[] = new char[value.length];
        charArrayCopy(value, result);
        return result;
    }

    private void charArrayCopy(char[] source, char[] result) {
        for(int i=0; i< source.length; i++){
            result[i] = source[i];
        }
    }

    public static native String valueOf(byte i);
    public static native String valueOf(short i);
    public static native String valueOf(int i);
    public static native String valueOf(long i);
    public static native String valueOf(boolean i);
    public static native String valueOf(char i);
    public static native String valueOf(float i);
    public static native String valueOf(double i);
}
