import placeholder.Print;

/**
 * Created by Daniel on 2/11/17.
 */
public class IfTest {
    public static void main(String[] args) {
        int x = 3;

        if(x>=3){
            Print.print(1);
        }


        if(x>=4){
            Print.print(-1);
        } else {
            Print.print(2);
        }

        if(x==3 && x-32 > 6){
            Print.print(-1);
        } else {
            if(x<7){
                Print.print(3);
            } else {
                Print.print(-1);
            }
        }

        if(x-32>6 && t()){
            Print.println(-1);
        } else {
            Print.println(4);
        }
    }

    public static boolean t(){
        Print.println(-1);
        return true;
    }
}
