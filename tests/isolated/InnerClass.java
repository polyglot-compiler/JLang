class InnerClass {
    private static final String staticField = "static field";
    private final String finalField = "final field";
    private String field = "field";
    private String shadowed = "shadowed outer";

    void f() { System.out.println("f"); }

    class Inner extends InnerClass {
        private final String innerField;
        private final String shadowed = "shadowed inner";

        Inner(String innerField) {
            this.innerField = innerField;
        }

        @Override
        void f() { System.out.println("override f"); }

        void printFields() {
            System.out.println();
            System.out.println("printing fields");
            System.out.println(innerField);
            System.out.println(staticField);
            System.out.println(finalField);
            System.out.println(field);
            System.out.println(shadowed);
            System.out.println(Inner.super.shadowed);
            System.out.println(InnerClass.this.shadowed);
            field = "field already printed";
        }

        class InnerInner {
            private final String innerInnerField = "inner inner field";

            void printFields() {
                Inner.this.printFields();
                System.out.println(innerInnerField);
                System.out.println("from inner: " + shadowed);
                System.out.println("from inner: " + Inner.super.shadowed);
                System.out.println("from inner: " + InnerClass.this.shadowed);
                System.out.println("from inner: " + field);
                Inner.super.f();
                f();
            }
        }
    }

    class Strange extends Inner.InnerInner {
        private final int field;

        Strange(int field) {
            InnerClass.this.new Inner("strange").super();
            this.field = field;
        }

        void printFields() {
            super.printFields();
            System.out.println(field);
        }
    }

    Inner createInner(final String param) {
        final String localVar = "local var";

        class Local extends Inner {

            Local(String innerField) {
                super(innerField);
            }

            @Override
            void printFields() {
                super.printFields();
                System.out.println(localVar);
                System.out.println(param);
            }
        }

        return new Local("created by outer");
    }

    class Generic<T> {
        T field;

        Generic(T field) {
            this.field = field;
        }

        class Inner {
            T innerField;

            Inner(T innerField) {
                this.innerField = innerField;
            }

            void printFields() {
                System.out.println();
                System.out.println("printing fields");
                System.out.println(field);
                System.out.println(innerField);
            }
        }
    }

    public static void main(String[] args) {
        InnerClass outer = new InnerClass();
        Inner inner = outer.new Inner("created by main");
        inner.printFields();

        Inner innerFromOuter = outer.createInner("param");
        innerFromOuter.printFields();

        Inner.InnerInner innerInner = inner.new InnerInner();
        innerInner.printFields();

        new InnerClass().new Strange(42).printFields();

        new InnerClass()
                .new Generic<String>("generic")
                .new Inner("generic inner")
                .printFields();

        new ExternalInner().printFields();
    }
}

class ExternalInner extends InnerClass.Inner {

    ExternalInner() {
        new InnerClass().super("external sub");
    }

    @Override
    void printFields() {
        super.printFields();
    }
}
