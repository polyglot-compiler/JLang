class StringIntern {

    public static void main(String[] args) {
        System.out.println("hello" == "hello");
        // this should succeed once we add string constant folding
        System.out.println("hello" == "hel"+"l" + "o");
        System.out.println("hello" == ("hel"+"lo").intern());
        System.out.println(("he"+"llo").intern() == ("hel"+"lo").intern());
        String he = "he";
        System.out.println(he+"llo" == ("hel"+"lo").intern());
    }
}