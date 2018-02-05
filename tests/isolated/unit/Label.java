package unit;

public class Label {
    public static void main(String[] args) {
        stmtlabel:
        System.out.println("stmtlabel");

        {
            System.out.println("yes");
        }
        blocklabel: {
            if (true) break blocklabel;
            System.out.println("blocklabel");
        }

        iflabel: if (true) {
            if (true) break iflabel;
            System.out.println("iflabel");
        }

        trylabel: try {
            if (true) break trylabel;
            System.out.println("trylabel");
        } finally {}

        switchlabel: switch (5) {
            case 5: break switchlabel;
            default:
                System.out.println("switchlabel");
                break;
        }

        int i = 0;
        doloop: do {
            if (i > 0)
                System.out.print(' ');
            ++i;
            System.out.print(i);
            continue doloop;
        } while (i <= 5);
        System.out.println();

        int n = 10;

        boolean x = true;
        outer: while (x) {
            System.out.println("inside outer");
            inner: while (x) {
                System.out.println("inside inner");
                break outer;
            }
            System.out.println("no");
        }
        System.out.println("yes");

        outer2: for (int j = 0; j < n; ++j) {
            System.out.println("inside outer2");
            inner2: for (int k = j + 1; k < n; ++k) {
                System.out.println("inside inner2");
                if (k % 2 == 0) {
                    continue outer2;
                }
            }
            System.out.println("yes");
        }
        System.out.println("yes");
    }
}
