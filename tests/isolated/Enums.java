class Enums {

    enum Num {
        ONE,
        TWO((short) 2, "Hi"),
        THREE(3, "Hi") {
            String override = "Override";
            @Override
            void printMyGreeting() {
                System.out.println(override);
            }
        };

        public static int count;
        public final int val;
        private final String greeting;
        public int mutable = 1;

        Num() {
            this(1);
        }

        Num(int val) {
            this(val, "Hello");
        }

        Num(int val, String greeting) {
            this.val = val;
            this.greeting = greeting;
        }

        static {
            for (Num n : Num.values()) {
                ++count;
            }
        }

        void printMyGreeting() {
            System.out.println(greeting);
        }
    }

    public static void main(String[] args) {
        System.out.println("begin");
        System.out.println(Num.ONE);
        System.out.println(Num.ONE == Num.ONE);
        System.out.println(Num.ONE.equals(Num.TWO));

        System.out.println(Num.ONE.val);
        System.out.println(Num.TWO.val);
        System.out.println("total " + Num.count);
        Num.ONE.printMyGreeting();
        Num.TWO.printMyGreeting();
        Num.THREE.printMyGreeting();

        System.out.println(Num.THREE.mutable++);
        System.out.println(Num.THREE.mutable++);
        System.out.println(Num.ONE.mutable + 2);

        // Switch statements.
        switch (Num.TWO) {
            case ONE: System.out.println("one"); break;
            case TWO: System.out.println("two"); break;
            case THREE: System.out.println("three"); break;
        }
        switch (three()) {
            case THREE: System.out.println("ok"); break;
            default: System.out.println("no"); break;
        }

        for (Num num : Num.values()) {
            System.out.println(num);
        }

        // TODO: This requires unimplemented library support.
        // System.out.println(Num.valueOf("ONE").val);
    }

    public static Num three() {
        return Num.THREE;
    }
}
