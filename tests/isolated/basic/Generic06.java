package basic;

public class Generic06 {
	public static void main(String[] args) {
	}
	public static class C1 implements I1 { }
	public static class C2<T> extends C1 implements I2 { }
	public static class C3 extends C2<C3> { }
	public interface I1<T> {}
	public interface I2 extends I1 {}
}
