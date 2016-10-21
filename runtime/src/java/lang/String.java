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
}
