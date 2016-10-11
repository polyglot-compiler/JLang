
public class SubSubClass extends UseSimpleClass {

//    public static void main(String[] args) {
//        main();
//    }

    public static void main() {
        UseSimpleClass s = new SubSubClass();
        s.field = 'A';//12;
        print(s.method() + s.method2());
        SimpleClass x = s;
        print(x.method());

        print(s.hashCode());
        print(s.hashCode());
    }


    public int method2() {
        return super.method2() - 10;
    }

//    public int method(){
//        return super.method() - 10;
//    }


      public int hashCode(){
          return super.hashCode() + 4;
      }

}
