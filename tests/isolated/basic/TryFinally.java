package basic;

class TryFinally {

    public static void main(String[] args) {
        empty();
        simpleReturn();
        simpleBreak();
        simpleContinue();
        nestedReturn();
        crazy();
    }

    static void empty() {
        try {}
        finally {}
    }

    static void simpleReturn() {
        try {
            System.out.println("simple return try");
            if (true) return;
        } finally {
            System.out.println("simple return finally");
        }
        System.out.println("simple return end");
    }

    static void simpleBreak() {
        label: try {
            System.out.println("simple break try");
            break label;
        } finally {
            System.out.println("simple break finally");
        }

        while (true) {
            try {
                inner: if (true) {
                    break inner;
                }
                System.out.println("simple break loop try");
                break;
            } finally {
                System.out.println("simple break loop finally");
            }
        }

        System.out.println("simple break end");
    }

    static void simpleContinue() {
        for (int i = 0; i < 2; ++i) {
            try {
                for (int j = 0; j < 1; ++j)
                    if (j == 0)
                        continue;
                System.out.println("simple continue try");
                continue;
            } finally {
                System.out.println("simple continue finally");
            }
        }

        System.out.println("simple continue end");
    }

    static void nestedReturn() {
        try {
            try {
                System.out.println("nested return try");
                if (true) return;
            } finally {
                System.out.println("nested return inner");
            }
        } finally {
            System.out.println("nested return outer");
        }
        System.out.println("nested return end");
    }

    static void crazy() {
        while (true) {
            try {
                try {
                    System.out.println("crazy inner try");
                    if (true) break;
                } finally {
                    System.out.println("crazy inner finally");
                }
                System.out.println("crazy outer try end");
            } finally {
                System.out.println("crazy outer finally");
                try {
                    try {
                        System.out.println("crazy second inner try");
                        if (true) {
                            while (true) {
                                return;
                            }
                        }
                    } finally {
                        System.out.println("crazy second inner finally");
                        if (true) break;
                    }
                    System.out.println("crazy second inner try end");
                } finally {
                    System.out.println("crazy second outer try finally");
                }
                System.out.println("crazy second outer finally end");
            }
            System.out.println("crazy loop end");
        }
        System.out.println("crazy end");
    }
}
