package basic;

public class InterfaceHierarchy {
    public static void main(String[] args){
        IHC obj = new IHC();
        System.out.println(obj.method1());
        System.out.println(obj.method2());

        IH1 ih1 = obj;
        System.out.println(ih1.method1());

        IH2 ih2 = obj;
        System.out.println(ih2.method1());
        System.out.println(ih2.method2());
    }


}

interface IH1 {
    int method1();
}

interface IH2 extends IH1 {
    int method2();
}


class IHC implements IH2{

    public int method1() {
        return 1;
    }

    public int method2() {
        return 2;
    }

}
