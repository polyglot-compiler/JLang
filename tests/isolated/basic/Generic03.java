package basic;

public class Generic03 {
	public static void main(String[] args) {
		C<D2> c = new C<D2>(new D2());
		m(c).n();
	}
	static <T extends D1> T m(C<T> c) {
		return c.getVal();
	}
	static class C<T> {
		private T val;
		public C(T t) { val = t; }
		public T getVal() { return val; }
	}
	static class D1 {
		public void n() { System.out.println("D1.n"); }
	}
	static class D2 extends D1 {
		public void n() { System.out.println("D2.n"); }
	}
}

