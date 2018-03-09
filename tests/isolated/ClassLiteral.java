class ClassLiteral {

    class Inner {}

    public static void main(String[] args) {
        System.out.println(int.class);
        System.out.println(ClassLiteral.Inner.class);
        System.out.println(Object.class.toString());
        System.out.println(byte[].class);
        System.out.println(String[][].class);

        System.out.println(Object.class == new Object().getClass());
        System.out.println(String.class == "String".getClass());
        System.out.println(String.class == new Object().getClass());
        System.out.println(ClassLiteral.Inner.class
                == new ClassLiteral().new Inner().getClass());
        System.out.println(short[].class == new short[] {1, 2, 3}.getClass());
    }
}
