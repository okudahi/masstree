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


    public static void main(String[] args){
        Bplustree tree = new Bplustree();
        tree.put("C", "C");
        tree.put("D", "D");
        tree.put("CD", "CD");
        // int[] intArray0 = new Random().ints(100, 1000, 10000).toArray();
        // int[] intArray1 = new Random().ints(90, 0, 99).toArray();
        // for(int i = 0; i < intArray0.length; i++){
        //     tree.put(String.valueOf(intArray0[i]), String.valueOf(intArray0[i]));
        // }
        // for (int i = 99; i > 74; i--){
        //     tree.put(String.valueOf(i),String.valueOf(i));
        // }
        // for (int i = 74; i > 0; i--){
        //     tree.put(String.valueOf(i),String.valueOf(i));
        // }
        // for (int i = 1; i < 70; i++){
        //     tree.delete(String.valueOf(i));
        // }
        tree.makeDotFile();
        // assertEqual(tree.root.nkeys, 0);
        List<String> getr = tree.getrange("10",50);
        System.out.println(getr);
        return;
    }
}
