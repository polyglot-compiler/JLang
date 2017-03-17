package basic;

public class ForLoop {
    public static void main(String[] args){
        for (int i=0; i<10; i++){
            System.out.println(i);
        }
        for (int i=10; i>0; i--){
            System.out.println(i);
        }
        for (int i=0; i<10; i+=2){
            System.out.println(i);
        }
        for (int i=0; i!=0; System.out.print(1)){
            System.out.println(i);
        }
    }
}
