class ClassLiteral {

    class Inner {}

    interface It {}

    abstract class Abstract {}

    public static void main(String[] args) {
        // System.out.println(int.class); TODO
        System.out.println(ClassLiteral.Inner.class);
        System.out.println(Object.class.toString());
        // System.out.println(byte[].class); TODO
        // System.out.println(String[][].class); TODO
        System.out.println();

        System.out.println(new ClassLiteral().getClass());
        System.out.println(new Object().getClass());
        System.out.println("String".getClass());
        // System.out.println(new char[0].getClass()); // TODO
        System.out.println();

        System.out.println(Object.class == new Object().getClass());
        System.out.println(String.class == "String".getClass());
        System.out.println(String.class == new Object().getClass());
        System.out.println(ClassLiteral.Inner.class
                == new ClassLiteral().new Inner().getClass());
        // System.out.println(It.class); TODO
        System.out.println(Abstract.class);
        // System.out.println(short[].class == new short[] {1, 2, 3}.getClass()); TODO
    }
}
