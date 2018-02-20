class InstanceOf {
	public static void main(String[] args) {
		f(new C2());
		f(new C3());
	}
    public static void f(Object o) {
        System.out.println(o instanceof I1);
        System.out.println(o instanceof I2);
        System.out.println(o instanceof C1);
        System.out.println(o instanceof C2);
        System.out.println(o instanceof C3);
        System.out.println(o instanceof InstanceOf);
    }
	public static class C1 implements I1<I2> { }
	public static class C2<T> extends C1 implements I2 { }
	public static class C3 extends C2<C3> { }
	public interface I1<T> {}
	public interface I2 extends I1<I2> {}
}
