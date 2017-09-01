package basic;

public class Generic04 {
	public static void main(String[] args) {
		A a1 = new A(new C());
		A a2 = new A(new B());
		a1.n('a');
	}
	static class A {
		public <X extends B>A(X x) {
		}
		public void n(long c) {
			System.out.println(c);
		}
	}
	static class B {
	}
	static class C extends B {
	}
}
