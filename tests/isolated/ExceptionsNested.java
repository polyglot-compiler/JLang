class ExceptionsNested {
    public static void main(String[] args) {
        try {
            catchesRuntimeExn();
            System.out.println("Should not print");
        } catch (Exception e) {
            System.out.println("Caught exception");
        }
    }

    public static void catchesRuntimeExn() throws Exception {
        try {
            throwsExn();
        } catch (RuntimeException e) {
            System.out.println("Caught runtime exception");
        }
    }

    public static void throwsExn() throws Exception {
        throw new Exception();
    }
}