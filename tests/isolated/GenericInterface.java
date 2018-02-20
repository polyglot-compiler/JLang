public class GenericInterface {

    public static void main(String[] args) {
        System.out.println(new A().getValue$$());
    }

    @Override
    public String toString() {
        return "GenericInterface";
    }

    private static class A implements I<GenericInterface> {

        @Override
        public GenericInterface getValue$$() {
            return new GenericInterface();
        }
    }

    private interface I<T> {
        T getValue$$();
    }
}
