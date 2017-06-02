package basic;

public class Nesting {
    public static void main(String[] args) {
        System.out.println(new StaticNested().toString());

        InnerClass innerClass = new InnerClass();
        Inner inner = innerClass.new Inner();
        System.out.println(inner);

        Inner.InnerInner innerInner = inner.new InnerInner();
        System.out.println(innerInner);

    }

    public int outer = 100;

    private static class StaticNested {
        @Override
        public String toString() {
            return "StaticNested";
        }
    }

    public class Inner {

        public class InnerInner{

            @Override
            public String toString() {
                return "InnerInner"  + outer;
            }
        }

        @Override
        public String toString() {
            return "Inner" + outer;
        }
    }
}
