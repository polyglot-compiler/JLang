class ClassLiteral {

    class Inner {}

    public static void main(String[] args) {
        System.out.println(int.class);
        System.out.println(ClassLiteral.Inner.class);
        System.out.println(Object.class.toString());
        System.out.println(byte[].class);
        System.out.println(String[][].class);
    }
}