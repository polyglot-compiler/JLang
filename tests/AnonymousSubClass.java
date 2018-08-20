import java.test.AbstractTest;
import java.test.Test;
import java.test.SubTest;

public class AnonymousSubClass {

    public static void main(String[] args) {
	AbstractTest at = new Test();
	at.printMessage();
	at = new SubTest();
	at.printMessage();
	SubTest st = new SubTest() {
		public void doNothing(){ System.out.print(""); };
	    };
	st.printMessage();

    }
}
