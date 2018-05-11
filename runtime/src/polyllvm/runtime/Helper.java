package polyllvm.runtime;

// Helper functions that implement Java semantics so that the compiler
// doesn't have to. That is, a PolyLLVM translation can sometimes call one of
// these helper methods rather than translating everything directly.
class Helper {

    // If o or o.toString() are null, we must substitute "null".
    static String toString(Object o) {
        if (o == null) return "null";
        String res = o.toString();
        return res == null ? "null" : res;
    }

    // These classes implicitly generate class objects,
    // which we use for primitive types.
    static final Class voidClassObject;
    static final Class booleanClassObject;
    static final Class byteClassObject;
    static final Class charClassObject;
    static final Class shortClassObject;
    static final Class intClassObject;
    static final Class longClassObject;
    static final Class floatClassObject;
    static final Class doubleClassObject;

    static {
        try {
            voidClassObject    = Class.forName("void");
            booleanClassObject = Class.forName("boolean");
            byteClassObject    = Class.forName("byte");
            charClassObject    = Class.forName("char");
            shortClassObject   = Class.forName("short");
            intClassObject     = Class.forName("int");
            longClassObject    = Class.forName("long");
            floatClassObject   = Class.forName("float");
            doubleClassObject  = Class.forName("double");
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
