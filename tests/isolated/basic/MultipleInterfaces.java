public class MultipleInterfaces {
    public static void main(String[] args){
        MIC obj = new MIC();
        System.out.println(obj.method1());
        System.out.println(obj.method2());
        System.out.println(obj.method3());

        MI1 mi1 = obj;
        System.out.println(mi1.method1());

        MI2 mi2 = obj;
        System.out.println(mi2.method2());

        MI3 mi3 = obj;
        System.out.println(mi3.method3());

    }


}

interface MI1 {
    int method1();
}

interface MI2 {
    int method2();
}

interface MI3 {
    int method3();
}

class MIC implements MI1, MI2, MI3{

    public int method1() {
        return 1;
    }

    public int method2() {
        return 2;
    }

    public int method3() {
        return 3;
    }
}
