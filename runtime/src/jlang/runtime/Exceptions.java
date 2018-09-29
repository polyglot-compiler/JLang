//Copyright (C) 2018 Cornell University

package jlang.runtime;

class Exceptions {

    static void createClassNotFoundException(String name) throws ClassNotFoundException { throw new ClassNotFoundException(name) ; }

}
