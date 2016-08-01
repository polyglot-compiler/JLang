public class UseSimpleClass extends SimpleClass {

//    public static void main(String[] args) {
//        main();
//    }

//    public static void main() {
//        SimpleClass s = new UseSimpleClass();
//        s.field = 'A';//12;
//        print(s.method());
//    }


    public int method() {
        return super.method() + 1;
    }

    public int method2() {
        return method() + 1;
    }
//
//    int method3() {
//        return 3;
//    }
//
//    protected int method4() {
//        return 4;
//    }
//
//    private int method5() {
//        return 5;
//    }
}
