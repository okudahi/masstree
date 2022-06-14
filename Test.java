// import java.util.List;
// import java.util.ArrayList;
public class Test {
    public static void main(String[] args){
        
        String name[] = {"Suzuki", "Katou", "Yamada"};
        int num[] = {1, 2, 3};

        for (var str: name){
            for(var n: num){
                System.out.println(n +str);
            }
        }
        return;
    }
}
