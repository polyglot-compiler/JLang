package java.lang;

/** A placeholder implementation of the String class. */
public final class String {
    private final char value[];

    public String(char value[]) {
        this.value = value;
    }

    public String(byte bytes[]) {
        // A naiive implementation for now.
        value = new char[bytes.length];
        for (int i = 0; i < bytes.length; ++i) {
            value[i] = (char) bytes[i];
        }
    }

    public String concat(String other){
        char value[] = new char[this.value.length + other.value.length];
        for (int i = 0; i < this.value.length; ++i) {
            value[i] = this.value[i];
        }
        for (int i = this.value.length; i <this.value.length + other.value.length; ++i) {
            value[i] = other.value[i-this.value.length];
        }
        return new String(value);
    }

    @Override
    public String toString() {
        return this;
    }
}
