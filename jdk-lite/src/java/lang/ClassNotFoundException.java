package java.lang;

public class ClassNotFoundException extends Exception {

    public ClassNotFoundException() {
	super();
    }

    public ClassNotFoundException(String s) {
	super(s);
    }

    public ClassNotFoundException(String s, Throwable t) {
	super(s, t);
    }
}