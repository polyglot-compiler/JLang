package tests;

import java.util.LinkedList;

public class BreakTest {
    public static void main(String[] args) {
        main();
    }

    public static void main() {
        LinkedList<String> m = new LinkedList<>();
        m.push("k1");
        m.push("k2");
        m.push("k3");
        m.push("k4");

        int x = 1;
        Loop: while (true) {
            x++;
            break Loop;
        }

        Ret: return;

    }

}
