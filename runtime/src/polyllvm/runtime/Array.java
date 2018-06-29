package polyllvm.runtime;

import java.io.Serializable;

/**
 * Constructs single- and multi-dimensional arrays, assisted by
 * the compiler. The constructors should never be called directly
 * from Java code, since we need the compiler to allocate the
 * correct amount of memory for an array instance.
 */
class Array implements Cloneable, Serializable {
    public final int length;
    private final int elementSize;
    // Array data is only visible to the compiler.
    
    /**
     * Single-dimensional arrays. To create a new array, the compiler will
     * allocate enough memory for an instance of this class plus the
     * array data, then emit a call to this constructor.
     */
    Array(int length, int elementSize) {
        // Note that entries have already been cleared,
        // since we use calloc to allocate memory.
        this.length = length;
	this.elementSize = elementSize;
    }

    enum Type { BOOLEAN, BYTE, CHAR, SHORT, INT, LONG, FLOAT, DOUBLE, OBJECT }

    /** Create a multidimensional array with the specified element type. */
    static Array createMultidimensional(Type type, int[] lens) {
        return createMultidimensional(type, lens, 0);
    }

    static Array createMultidimensional(Type type, int[] lens, int depth) {
        if (depth == lens.length - 1) {
            // Leaf array. Already zero-initialized through calloc.
            return create(type, lens[depth]);
        }
        else {
            // Recurse.
            Array[] res = (Array[]) (Object) create(Type.OBJECT, lens[depth]);
            for (int i = 0; i < res.length; ++i)
                res[i] = createMultidimensional(type, lens, depth + 1);
            return (Array) (Object) res;
        }
    }

    static Array create(Type type, int len) {
        // The casts succeeds because Array and type[] are
        // equivalent from the perspective of PolyLLVM.
        switch (type) {
            case BOOLEAN: return (Array) (Object) new boolean[len];
            case BYTE:    return (Array) (Object) new byte   [len];
            case CHAR:    return (Array) (Object) new char   [len];
            case SHORT:   return (Array) (Object) new short  [len];
            case INT:     return (Array) (Object) new int    [len];
            case LONG:    return (Array) (Object) new long   [len];
            case FLOAT:   return (Array) (Object) new float  [len];
            case DOUBLE:  return (Array) (Object) new double [len];
            case OBJECT:  return (Array) (Object) new Object [len];
            default:
                throw new Error("Unhandled array base type");
        }
    }

    public Array clone() {
        try {
            return (Array) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Arrays should be cloneable");
        }
    }
}
