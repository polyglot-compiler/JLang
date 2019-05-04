//Copyright (C) 2018 Cornell University

package jlang.runtime;

import java.io.Serializable;

/**
 * Constructs single- and multi-dimensional arrays, assisted by
 * the compiler. The constructors should never be called directly
 * from Java code, since we need the compiler to allocate the
 * correct amount of memory for an array instance.
 */
class Array implements Cloneable, Serializable {
    public final int length = 0;
    private int elementSize;
//    Array data is only visible to the compiler.
//    private byte[] data;

    /**
     * Single-dimensional arrays. To create a new array, the compiler will
     * invoke a runtime function `createArray`. It will allocate enough memory
     * for an instance of this class plus the array data, and
     * then set length and elementSize correctly.
     * <p>
     * Reference: https://docs.oracle.com/javase/specs/jls/se7/html/jls-10.html#jls-10.7
     * <p>
     * Reflection of Array has an unintuitive behavior in Java. Its fields and methods
     * cannot be found by invoking int[].class or arr.getClass(). To conform this behavior,
     * we erase all fields and methods information in runtime ClassInfo.
     * To use the reflection of Array, we should use methods in java.lang.reflect.Array instead.
     */

    @Override
    public Array clone() {
        try {
            return (Array) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Arrays should be cloneable");
        }
    }
}
