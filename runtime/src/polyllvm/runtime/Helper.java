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
    static final Class voidClassObject    = Class.forName("void");
    static final Class booleanClassObject = Class.forName("boolean");
    static final Class byteClassObject    = Class.forName("byte");
    static final Class charClassObject    = Class.forName("char");
    static final Class shortClassObject   = Class.forName("short");
    static final Class intClassObject     = Class.forName("int");
    static final Class longClassObject    = Class.forName("long");
    static final Class floatClassObject   = Class.forName("float");
    static final Class doubleClassObject  = Class.forName("double");
}
