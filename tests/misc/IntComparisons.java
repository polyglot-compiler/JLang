public class IntComparisons {
	public static void main() {
		noelse();
	}
	
	public static void noelse(){
		if (1 < 2) {
			print(1);
		}
		if (1 < 1) {
			print(0);
		}
		if (1 < 0) {
			print(0);
		}
		if (4 > 3) {
			print(2);
		}
		if (3 > 3) {
			print(0);
		}
		if (3 > 4) {
			print(0);
		}
		if (2 == 2) {
			print(3);
		}
		if (2 == 1) {
			print(0);
		}
		if (1 <= 2) {
			print(4);
		}
		if (1 <= 1) {
			print(5);
		}
		if (1 <= 0) {
			print(0);
		}
		if (3 >= 4) {
			print(0);
		}
		if (3 >= 3) {
			print(6);
		}
		if (2 >= 3) {
			print(0);
		}
		if (2 != 1) {
			print(7);
		}
		if (2 != 2) {
			print(0);
		}
	}

	public static native void print(int i);

}
