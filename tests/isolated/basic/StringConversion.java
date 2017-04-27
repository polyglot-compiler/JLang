package basic;

public class StringConversion {
    public static void main(String[] args) {
        System.out.println("Creating...\n" + new StringConversion() +"\nDone!");
        System.out.println("NullToString: " + new NullToString());
        System.out.println("byte: " + (byte) 0);
        System.out.println("short: " + (short) 0);
        System.out.println("char: " + 'a');
        System.out.println("int: " + 0);
        System.out.println("long: " + 0L);
//        System.out.println("float: " + 0.0f); Not implemented yet
//        System.out.println("double: " + 0.0); Not implemented yet
        System.out.println("bool: " + true);
        System.out.println("bool: " + false);
    }

    public String toString() {
        return "StringConversion";
    }

    public static class NullToString {
        public String toString(){
            return null;
        }
    }
}
