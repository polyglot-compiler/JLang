/**
 * Created by Daniel on 10/30/16.
 */
public class Interface {
    public static void main(String[] args){
        I obj;
        obj = new I1();
        System.out.println(obj.method());
        obj = new I2();
        System.out.println(obj.method());
    }


}

interface I {
    int method();
}

class I1 implements I{
    public int a(){return -1;}
    public int method(){
        return 1;
    }
}

class I2 implements I{
    public int b(){return -2;}
    public int method(){
        return 2;
    }
}
