import placeholder.Print;

public class ForLoop {
    public static void main(String[] args){
        for (int i=0; i<10; i++){
            Print.println(i);
        }
        for (int i=10; i>0; i--){
            Print.println(i);
        }
        for (int i=0; i<10; i+=2){
            Print.println(i);
        }
        for (int i=0; i!=0; Print.print(1)){
            Print.println(i);
        }
    }
}
