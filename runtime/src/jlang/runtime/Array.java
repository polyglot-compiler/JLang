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
    private int length;
    private int elementSize;
    // Array data is only visible to the compiler.
    
    /**
     * Single-dimensional arrays. To create a new array, the compiler will
     * invoke a runtime function `createArray`. It will allocate enough memory
     * for an instance of this class plus the array data, and
     * then set length and elementSize correctly.
     */

    public Array clone() {
        try {
            return (Array) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Arrays should be cloneable");
        }
    }
}
