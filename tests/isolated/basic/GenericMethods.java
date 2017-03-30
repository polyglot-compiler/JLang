package basic;

import java.util.HashMap;

public class GenericMethods {

    public static void main(String[] args) {
        new GenericMethods().print("hi", "world");
    }

    public <T> T print(T obj1, T obj2){
        System.out.println(obj1 + " -- " + obj2);
        return obj1;
    }

    public <T> void print(HashMap<T,T> obj1){
        System.out.println(obj1 + " -- " );
    }
}
