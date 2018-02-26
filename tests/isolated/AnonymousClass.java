public class AnonymousClass {
    private final String field = "outer field";

    private interface I {
        void f();
    }

    void member() {
        final int local = 42;
        I i = new I() {
            private final String field = "inner field";

            @Override
            public void f() {
                System.out.println(AnonymousClass.this.field);
                System.out.println(field);
                System.out.println(local);
            }
        };
        i.f();
    }

    public static void main(String[] args) {
        final String local = "local";

        I i = new I() {
            int field = 3;

            @Override
            public void f() {
                System.out.println(local);
                System.out.println(field);

                I j = new I() {
                    int field = 4;

                    @Override
                    public void f() {
                        System.out.println(local);
                        System.out.println(field);
                    }
                };
                j.f();
            }
        };
        i.f();

        i = new I() { public void f() { System.out.println(5); } };
        i.f();

        new AnonymousClass().member();
    }
}
