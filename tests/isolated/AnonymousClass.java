public class AnonymousClass {

    public static void main(String[] args) {
        final int x = 32;//Integer.parseInt("32");
        final byte y = 93;
        final short z = 203;

        final int level1 = 1;
        final int level2 = 2;

        I i = new I() {

            int field;

            @Override
            public int f() {
                final int i = 3;
                int o = z*z+z + level1;
                int f = new I() {

                    @Override
                    public int f() {
                        return 10+i+x + level2 /*+ field*/;
                    }
                }.f();
                return x+i+y+f+o;
            }

            @Override
            public String toString() {
                return "Anonymous I";
            }
        };

        System.out.println(i.f());
    }

    private interface I {
        int f();
    }

}
