package unit;

class InstanceOf implements IA {

    public static void main(String[] args) {
        f(new Subclass());
    }

    public static void f(Object o) {
        System.out.println(o instanceof Subclass);
        System.out.println(o instanceof InstanceOf);
        System.out.println(o instanceof Object);
        System.out.println(o instanceof String);
        System.out.println(o instanceof IA);
        System.out.println(o instanceof IB);
        System.out.println(o instanceof IC);
        System.out.println(o instanceof ID);
        System.out.println(o instanceof IE);
        System.out.println(o instanceof IF);
    }

    static class Subclass extends InstanceOf implements IE {}
}

interface IA {}
interface IB {}
interface IC {}
interface ID extends IA {}
interface IE extends IB {}
interface IF extends IC {}
