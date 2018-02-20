class InnerClass {
    private static final String staticField = "static field";
    private final String finalField = "final field";
    private String field = "field";
    private String shadowed = "shadowed outer";

    class Inner extends InnerClass {
        private final String innerField;
        private final String shadowed = "shadowed inner";

        Inner(String innerField) {
            this.innerField = innerField;
        }

        void printFields() {
            System.out.println();
            System.out.println("printing fields");
            System.out.println(innerField);
            System.out.println(staticField);
            System.out.println(finalField);
            System.out.println(field);
            System.out.println(shadowed);
            field = "field already printed";
        }

        class InnerInner {
            private final String innerInnerField = "inner inner field";

            void printFields() {
                Inner.this.printFields();
                System.out.println(innerInnerField);
                System.out.println("from inner: " + shadowed);
                System.out.println("from inner: " + field);
            }
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

    public static void main(String[] args) {
        InnerClass outer = new InnerClass();
        Inner inner = outer.new Inner("created by main");
        inner.printFields();

        Inner innerFromOuter = outer.createInner("param");
        innerFromOuter.printFields();

        Inner.InnerInner innerInner = inner.new InnerInner();
        innerInner.printFields();

        class StaticLocal {

            void printFields() {
                System.out.println();
                System.out.println("printing fields");
                System.out.println(staticField);
            }
        }

        StaticLocal local = new StaticLocal();
        local.printFields();
    }
}
