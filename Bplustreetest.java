import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Bplustreetest {
    public static <T extends Comparable<T>> void assertEqual(T obj, T expected) {
        if (!obj.equals(expected)) {
            StringBuilder builder = new StringBuilder();
            builder.append("Assertion failed: ");
            builder.append(obj);
            builder.append(" is not equals to ");
            builder.append(expected);
            throw new AssertionError(builder.toString());
        }
    }
    public static void makeDotFile(Bplustree tree){ // 可視化用dotファイル出力
        try{
            FileWriter fw = new FileWriter("BPTshow.dot");
            fw.write("digraph G {\n  node [shape = record,height=.1];\n");
            fw.write(tree.makedot());
            fw.write("}");
            fw.close();
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }

    public static void main(String[] args){
        Bplustree tree = new Bplustree();
        int[] intArray = new Random().ints(1000, 1000, 10000).toArray();
        for(int i = 0; i < intArray.length; i++){
            tree.put(String.valueOf(intArray[i]), String.valueOf(intArray[i]));
        }
        // for (int i = 99; i > 74; i--){
        //     tree.put(String.valueOf(i),String.valueOf(i));
        // }
        // for (int i = 74; i > 0; i--){
        //     tree.put(String.valueOf(i),String.valueOf(i));
        // }
        // for (int i = 1; i < 70; i++){
        //     tree.delete(String.valueOf(i));
        // }
        for(int i = 1000; i < 9000; i++){
            tree.delete(String.valueOf(i));
        }
        makeDotFile(tree);
        tree.delete("50");
        tree.delete("51");
        makeDotFile(tree);
        // assertEqual(tree.root.nkeys, 0);
        List<String> getr = tree.getrange("10",50);
        System.out.println(getr);
        return;
    }
}
