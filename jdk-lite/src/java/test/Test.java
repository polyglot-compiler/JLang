package java.test;

public class Test extends AbstractTest {

    private String message;

    public Test() {
	init();
    }

    public void init() {
	this.message = "Base Message";
    }

    public void setMessage(String msg) {
	this.message = msg;
    }

    public String message() {
	return this.message;
    }
    
    
}
