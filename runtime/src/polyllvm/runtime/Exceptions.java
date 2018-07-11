package polyllvm.runtime;

class Exceptions {

    static void createClassNotFoundException(String name) throws ClassNotFoundException { throw new ClassNotFoundException(name) ; }

}