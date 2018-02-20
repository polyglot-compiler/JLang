public class Generic05 {
	public static void main(String[] args) {
		A a = new A();
		a.m(new B(), new C());
	}
	static class A<T extends B>  {
		public <S extends C> void m(T t, S s) {
			System.out.println("A.m");
		}
	}
	static class B {}
	static class C {}
}

