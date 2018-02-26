class LocalClass {
    private static String staticField = "static field";

    public static void main(String[] args) {
        new LocalClass().f("param").f().f();
    }

    interface F { F f(); }

    public F f(final String param) {
        System.out.println("begin");
        final String local = "local";

        final String firstLocalCapture = "first local captures";
        class FirstLocal implements F {
            public F f() {
                System.out.println(firstLocalCapture);
                return this;
            }
        }

        class SecondLocal implements F {
            public final String innerField;

            SecondLocal(final String innerField) {
                this.innerField = innerField;

                // Test local inside constructor.
                class CtorLocal {
                    void f() {
                        System.out.println("from ctor local: " + innerField);
                    }
                }
                new CtorLocal().f();
            }

            String str() { return "str"; }

            // Infinite recursion here: just check that this compiles.
            class A { A() { new B(); System.out.println(param); } }
            class B { B() { new A(); System.out.println(local); } }

            // Test that we have access to the enclosing instance
            // when calling the super constructor.
            class C extends SecondLocal {
                C() {
                    super(SecondLocal.this.str());
                }
            }

            class D extends FirstLocal {}

            public void deeplyNested() {
                class InnerLocal {
                    class Member extends D {}
                }
                new InnerLocal().new Member().f();
            }

            public F f() {
                System.out.println("outer local begin");
                System.out.println(staticField);
                System.out.println(innerField);
                System.out.println(param);
                System.out.println(local);
                deeplyNested();
                System.out.println();

                final String innerLocal = "inner local";

                class InnerLocal extends C {

                    InnerLocal() { super(); }

                    public F f() {
                        String innerLocal = "inner local";
                        System.out.println("inner local begin");
                        System.out.println(staticField);
                        System.out.println(param);
                        System.out.println(local);
                        System.out.println(innerLocal);
                        return this;
                    }
                }

                return new InnerLocal();
            }
        }

        // Test field access in super class.
        class LocalSub extends SecondLocal {
            LocalSub() { super("subclass"); }
        }
        System.out.println(new LocalSub().innerField);

        class Local2 {
            Local2() { new LocalSub(); }
        }

        // Local3 should capture the variables captured by Local2, because
        // MemberClass will need them for super constructor calls.
        class Local3 {
            class MemberClass extends Local2 {}
        }
        new Local3().new MemberClass();


        // Test using captures before the constructor can initialize
        // the capture field.
        class Local4 {
            Local4(String arg) {
                System.out.println(arg);
            }
        }
        class Local5 extends Local4 {
            Local5() { this(local); }
            Local5(String arg) { super(arg); }
        }
        new Local5();

        // Test local class with duplicate name.
        g();

        // Bubble up this gigantic local class and print everything.
        return new SecondLocal("inner field");
    }

    void g() {
        class FirstLocal implements F {
            public F f() {
                System.out.println("duplicate name");
                return this;
            }
        }
        new FirstLocal().f();
    }
}
