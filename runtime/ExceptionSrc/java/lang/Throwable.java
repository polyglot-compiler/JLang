package java.lang;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;

public class Throwable implements Serializable {
	
    private transient Object backtrace;
    private Throwable cause;
    private String detailMessage;
    private StackTraceElement[] stackTrace;
    private java.util.List suppressedExceptions;

    public Throwable() {}
    public Throwable(String s){}
    public Throwable(String s, java.lang.Throwable t){}
    public Throwable(Throwable t) {}
    protected Throwable(String s, Throwable t, boolean b1, boolean b2) {}

    public String toString(){
        return "";
    }

    public final synchronized void addSuppressed(Throwable t){}
    public synchronized Throwable fillInStackTrace() {return null;}
    public synchronized Throwable getCause() {return null;}
    public String getLocalizedMessage() {return "";}
    public String getMessage() {return "";}
    public StackTraceElement[] getStackTrace() {return new StackTraceElement[0];}
    public final synchronized Throwable[] getSuppressed(){return new Throwable[0];}
    public synchronized Throwable initCause(Throwable t){return null;}
    public void printStackTrace() {}
    public void printStackTrace(java.io.PrintStream ps) {}
    public void printStackTrace(java.io.PrintWriter pw) {}
    public void setStackTrace(StackTraceElement[] st) {}
    /*native*/ int getStackTraceDepth() {return 0;}
    /*native*/ StackTraceElement getStackTraceElement(int i) {return null;}
    private /* native */ Throwable fillInStackTrace(int i) {return null;}
    private synchronized StackTraceElement[] getOurStackTrace() {return new StackTraceElement[0];}
    private void printEnclosedStackTrace(PrintStreamOrWriter psow, StackTraceElement[] stel, String s1, String s2, java.util.Set set){}
    private void printStackTrace(PrintStreamOrWriter psow) {}
    private void readObject(java.io.ObjectInputStream ois) throws java.io.IOException, ClassNotFoundException {}
    private synchronized void writeObject(java.io.ObjectOutputStream oos) throws java.io.IOException {}

    private class PrintStreamOrWriter {}

}