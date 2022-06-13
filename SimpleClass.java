import java.util.ArrayList;
import java.util.List;

public class SimpleClass {
    
        int number;
        String name;
        SimpleClass(){
            number = 2;
        }
        int getNumber(){return number;}
        String getName(){return name;}
    public static void main(String[] args) {
        SimpleClass sc = new SimpleClass();
        System.out.println(sc.getNumber());
        System.out.println(sc.getName());
    }
}