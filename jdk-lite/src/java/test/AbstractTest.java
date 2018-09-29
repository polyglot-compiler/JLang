//Copyright (C) 2018 Cornell University

package java.test;

public abstract class AbstractTest {

    public abstract String message();
    
    public void printMessage() {
	System.out.println("My message is: " + message());
    }

}
