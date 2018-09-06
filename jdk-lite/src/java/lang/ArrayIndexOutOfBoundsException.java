//Copyright (C) 2018 Cornell University

package java.lang;

public
class ArrayIndexOutOfBoundsException extends IndexOutOfBoundsException {

    public ArrayIndexOutOfBoundsException() {
        super();
    }

    public ArrayIndexOutOfBoundsException(int index) {
        super("Array index out of range: " );
    }

    public ArrayIndexOutOfBoundsException(String s) {
        super(s);
    }
}
