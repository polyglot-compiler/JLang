public class Interface {
    public static void main(String[] args){
        I obj;
        obj = new I1();
        System.out.println(obj.method());
        obj = new I2();
        System.out.println(obj.method());
	obj = new I3();
	System.out.println(obj.method());
	J jbo = (J) obj;
	System.out.println(jbo.method2());
    }


}

interface I {
    int method();
}

interface J {
    int method2();
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

class I3 implements I,J {
    public int method() {
	return 3;
    }
    public int method2() {
	return 0;
    }
}