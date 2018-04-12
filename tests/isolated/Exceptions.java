public class Exceptions {
    public static void main(String[] args) {
        simple();

        multi();

        try {
            tunnel();
        } catch (Throwable e) {
            System.out.println("catch");
        } finally {
            System.out.println("second finally");
        }

        nested();

        throwInFinally();

        returnFromCatch();

        returnFromFinally();

        try {
            rethrow();
        } catch (Error e) {
            System.out.println("second catch");
        }

        crazy();

        System.out.println(returnWithSideEffects());

        System.out.println("end");

        nestedLandingPad();
    }

    public static void simple() {
        System.out.println("begin simple");
        try {
            throw new Error();
        } catch (Throwable e) {
            System.out.println("catch");
        } finally {
            System.out.println("finally");
        }
    }

    public static void multi() {
        System.out.println("begin multi");
        try {
            throw new Exception();
        } catch (AssertionError e) {
            System.out.println("catch assertion error");
        } catch (Error | Exception e) {
            System.out.println("catch error or exception");
        } catch (Throwable e) {
            System.out.println("catch throwable");
        } finally {
            System.out.println("finally");
        }
    }

    public static void tunnel() {
        System.out.println("begin tunnel");
        try {
            Error err = new Error();
            Exception e = new Exception();
            throw err;
        } catch (Exception e) {
            System.out.println("bad");
        } finally {
            System.out.println("first finally");
        }
    }

    public static void nested() {
        System.out.println("begin nested");
        try {
            try {
                throw new Exception();
            } catch (Exception e) {
                System.out.println("catch inner");
                throw new Error();
            } finally {
                System.out.println("finally inner");
            }
        } catch (Error e) {
            System.out.println("catch outer");
        } finally {
            System.out.println("finally outer");
        }
    }

    public static void throwInFinally() {
        System.out.println("begin throw in finally");
        try {
            try {
                throw new Exception();
            } finally {
                throw new Error();
            }
        } catch (Exception e) {
            System.out.println("caught exception");
        } catch (Error e) {
            System.out.println("caught error");
        }
    }

    public static void returnFromCatch() {
        System.out.println("begin return from catch");
        try {
            try {
                throw new Error();
            } catch (Error e) {
                System.out.println("catch");
                return;
            } finally {
                System.out.println("inner finally");
            }
        } finally {
            System.out.println("outer finally");
        }
    }

    public static void returnFromFinally() {
        System.out.println("begin return from finally");
        try {
            try {
                throw new Error();
            } finally {
                System.out.println("inner finally");
                return;
            }
        } catch (Error e) {
            System.out.println("bad");
        } finally {
            System.out.println("outer finally");
        }
    }

    public static void rethrow() {
        System.out.println("begin rethrow");
        try {
            throw new Error();
        } catch (Error e) {
            System.out.println("first catch");
            throw e;
        } finally {
            System.out.println("finally");
        }
    }

    public static void crazy() {
        System.out.println("begin crazy");
        while (true) {
            try {
                try {
                    try {
                        throw new Exception();
                    } finally {
                        System.out.println("inner finally");
                        throw new Error();
                    }
                } catch (Exception e) {
                    System.out.println("catch exception");
                }
            } catch (Error e) {
                System.out.println("catch error");
                throw e;
            } finally {
                System.out.println("outer finally");
                break;
            }
        }
        System.out.println("good");
    }

    static int sideEffect() {
        System.out.println("g");
        return 42;
    }

    static int returnWithSideEffects() {
        try { return sideEffect(); }
        finally { System.out.println("finally"); }
    }

    static void nestedLandingPad() {
        try {
            class Inner {
                void f() {
                    System.out.println("inner f");
                    throw new RuntimeException();
                }

                void g() {
                    System.out.println("inner g");
                    f();
                }
            }
            new Inner().g();
        }
        finally {
            System.out.println("finally");
            return;
        }
    }
}
