//Copyright (C) 2018 Cornell University

package jlang.runtime;
import java.lang.reflect.Constructor;

class Exceptions {

    static void createClassNotFoundException(String name) throws ClassNotFoundException { throw new ClassNotFoundException(name) ; }
    static void throwNewThrowable(Class<? extends Throwable> clazz, String msg) throws Throwable {
	Constructor<? extends Throwable> ctor = clazz.getConstructor(String.class);
	throw ctor.newInstance(msg);
    }
    static void throwThrowable(Throwable t) throws Throwable {
	throw t;
    }
    static void throwInterruptedException() throws InterruptedException { throw new InterruptedException(); }
}
