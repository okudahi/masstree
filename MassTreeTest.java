import java.util.Random;

public class MassTreeTest {
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
        MassTree tree = new MassTree();
        int[] intArray0 = new Random().ints(80, 10000, 19999).toArray();
        // int[] intArray1 = new Random().ints(70, 0, 70).toArray();
        for(int i = 0; i < intArray0.length; i++){
            tree.put(String.valueOf(intArray0[i]), String.valueOf(intArray0[i]));
            System.out.println(intArray0[i] + " inserted");
        }
        for(int i = 0; i < intArray0.length; i++){ 
            System.out.println("key "+ tree.get(String.valueOf(intArray0[i])));
        }
        tree.makeDotFile();
        for(int i = 0; i < intArray0.length - 2; i++){ 
            tree.delete(String.valueOf(intArray0[i]));
            System.out.println(intArray0[i] + " deleted");
        }
        for(int i = 0; i < intArray0.length; i++){ 
            System.out.println("key " + tree.get(String.valueOf(intArray0[i])));
        }
        // for(int i = 0; i < intArray0.length; i++){
        //     System.out.println("key:" + String.valueOf(intArray0[i]) + " value:" + tree.get(String.valueOf(intArray0[i])));
        // }
        tree.makeDotFile();
        return;
    }
}