package basic;

public class IfTest {
    public static void main(String[] args) {
        int x = 3;

        if(x>=3){
            System.out.print(1);
        }


        if(x>=4){
            System.out.print(-1);
        } else {
            System.out.print(2);
        }

        if(x==3 && x-32 > 6){
            System.out.print(-1);
        } else {
            if(x<7){
                System.out.print(3);
            } else {
                System.out.print(-1);
            }
        }

        if(x-32>6 && t()){
            System.out.println(-1);
        } else {
            System.out.println(4);
        }
    }

    public static boolean t(){
        System.out.println(-1);
        return true;
    }
}
