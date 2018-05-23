package java.lang;

public class Throwable {
    private String detailMessage;
    private Throwable cause;
    private Throwable suppressed;

    public Throwable() {}

    public Throwable(String s) { this(s, null); }

    public Throwable(String s, Throwable t) {
        this.detailMessage = s;
    }

    public Throwable(Throwable t) {
        this.detailMessage = t.detailMessage;
        this.cause = t;
    }

    void addSuppressed(Throwable t) {
        this.suppressed = t;
    }

    public String getMessage() { return detailMessage; }
    public String getLocalizedMessage() {return detailMessage;}
    public String toString() { return detailMessage; }
}