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
        // int[] intArray0 = new Random().ints(100, 1000, 10000).toArray();
        // int[] intArray1 = new Random().ints(80, 0, intArray0.length).toArray();
        // for(int i = 0; i < intArray0.length; i++){
        //     tree.put(String.valueOf(intArray0[i]), String.valueOf(intArray0[i]));
        // }
        for (int i = 99; i > 9; i--){
            tree.put(String.valueOf(i),String.valueOf(i));
        }
        // tree.makeDotFile();
        for (int i = 99; i > 29; i--){
            tree.delete(String.valueOf(i));
        }
        // tree.makeDotFile();
        // assertEqual(tree.root.nkeys, 0);
        // List<String> getr = tree.getrange(String.valueOf(intArray0[0]),50);
        // for(int i = 0; i < getr.size(); i++){
        //     System.out.println(getr.get(i));
        // }
        // for(int i = 0; i < intArray1.length; i++){ 
        //     tree.delete(String.valueOf(intArray0[intArray1[i]]));
        //     tree.makeDotFile();
        // }
        return;
    }
}
