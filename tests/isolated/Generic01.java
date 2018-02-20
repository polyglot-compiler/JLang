public class Generic01 {
	public static void main(String[] args) {
		D d = new D();
		C1 c1 = new C1<D>();
		c1.m(d);
		C2 c2 = new C2();
		c2.m(d);
	}
	static class C1<T> {
		public void m(T x) { System.out.println("C1.m"); }
	}
	static class C2 extends C1<D> {
		public void m(D x) { System.out.println("C2.m"); }
	}
	static class D { }
}
