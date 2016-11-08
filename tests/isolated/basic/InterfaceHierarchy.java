import placeholder.Print;

/**
 * Created by Daniel on 11/7/16.
 */
public class InterfaceHierarchy {
    public static void main(String[] args){
        IHC obj = new IHC();
        Print.println(obj.method1());
        Print.println(obj.method2());

        IH1 ih1 = obj;
        Print.println(ih1.method1());

        IH2 ih2 = obj;
        Print.println(ih2.method1());
        Print.println(ih2.method2());
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