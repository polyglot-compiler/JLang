package java.lang;

import java.io.Serializable;

public abstract class Enum<E extends Enum<E>> implements Serializable {
    private final String name;

    public final String name() {
        return name;
    }

    private final int ordinal;

    public final int ordinal() {
        return ordinal;
    }

    protected Enum(String name, int ordinal) {
        this.name = name;
        this.ordinal = ordinal;
    }

    public String toString() {
        return name;
    }

    public final boolean equals(Object other) {
        return this == other;
    }

    public final int hashCode() {
        return super.hashCode();
    }

    protected final Object clone() {
        return this;
    }

    public final int compareTo(E o) {
        return 0;
    }

    public final Class<E> getDeclaringClass() {
                                return null;
    }

    public static <T extends Enum<T>> T valueOf(Class<T> enumType,
                                                String name) {
        throw new RuntimeException("java.lang.Enum#valueOf unimplemented");
    }

    private void readObject( Object in) {}

    private void readObjectNoData() {}
}
