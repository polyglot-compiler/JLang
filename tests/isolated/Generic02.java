public class Generic02 {
	public static void main(String[] args) {
		D1 d1 = new D1();
		D2 d2 = new D2();
		C1 c1 = new C1();
		C2 c2 = new C2();
		c1.m(d1);
		c1.m(d2);
		c2.m(d2);
	}
	static class C1<T extends D1> {
		public void m(T x) { System.out.println("C1.m"); }
	}
	static class C2<T extends D2> extends C1<T> {
		public void m(T x) { System.out.println("C2.m"); }
	}
	static class D1 { }
	static class D2 extends D1 { }
}
