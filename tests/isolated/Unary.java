class Unary {
    public static void main(String[] args) {
        System.out.println("begin");

        int i = 0;
        System.out.println(i++);
        System.out.println(++i);
        System.out.println(-i);
        System.out.println(--i);
        System.out.println(i--);
        System.out.println(+i);

        float f = 0;
        System.out.println(f++ == 0.0);
        System.out.println(f == 1.0);
        System.out.println(++f == 2.0);
        System.out.println(-f == -2.0);
        System.out.println(--f == 1.0);
        System.out.println(f-- == 1.0);
        System.out.println(+(-(++f)) == -1.0);

        Integer w = 5;
        System.out.println(w++);
        System.out.println(w);
        System.out.println(++w);
        System.out.println(-w);
        System.out.println(--w);
        System.out.println(w--);
        System.out.println(+w);

        System.out.println(!true);
        System.out.println(~24);
    }
}
