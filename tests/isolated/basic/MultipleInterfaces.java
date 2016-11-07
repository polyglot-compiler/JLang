import placeholder.Print;

public class MultipleInterfaces {
    public static void main(String[] args){
        MIC obj = new MIC();
        Print.println(obj.method1());
        Print.println(obj.method2());
        Print.println(obj.method3());

        MI1 mi1 = obj;
        Print.println(mi1.method1());

        MI2 mi2 = obj;
        Print.println(mi2.method2());

        MI3 mi3 = obj;
        Print.println(mi3.method3());

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