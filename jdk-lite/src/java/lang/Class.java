//Copyright (C) 2018 Cornell University

package java.lang;

public final class Class<T> {

    private static native void registerNatives();
    static {
        registerNatives();
    }

    public static Class forName(String name) {
        throw new RuntimeException("unimplemented");
    }

    public native String getName();

    public String toString() {
        String name = getName();
        switch (name) {
            case "void":
            case "boolean":
            case "byte":
            case "char":
            case "short":
            case "int":
            case "long":
            case "float":
            case "double":
                return name;
            default:
                return "class " + name;
        }
    }
}
