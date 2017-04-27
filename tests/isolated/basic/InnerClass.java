package basic;

public class InnerClass {
    public static void main(String[] args) {
        System.out.println(new StaticInner().toString());

        InnerClass innerClass = new InnerClass();
        NonStaticInner nonStaticInner = innerClass.new NonStaticInner();
        System.out.println(nonStaticInner);

        NonStaticInner.InnerInner innerInner = nonStaticInner.new InnerInner();
        System.out.println(innerInner);

    }

    public int outer = 100;

    private static class StaticInner {
        @Override
        public String toString() {
            return "StaticInner";
        }
    }

    public class NonStaticInner {

        public class InnerInner{

            @Override
            public String toString() {
                return "InnerInner"  + outer;
            }
        }

        @Override
        public String toString() {
            return "NonStaticInner" + outer;
        }
    }
}
