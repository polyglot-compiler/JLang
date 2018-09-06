//Copyright (C) 2018 Cornell University

package java.lang;

public
class IllegalArgumentException extends RuntimeException {

    public IllegalArgumentException() {
        super();
    }

    public IllegalArgumentException(String s) {
        super(s);
    }

    public IllegalArgumentException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalArgumentException(Throwable cause) {
        super(cause);
    }

}
