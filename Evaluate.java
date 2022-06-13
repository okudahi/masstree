import java.util.Random;

public class Evaluate {

    public static void main(String[] args){
        
        int numKeys = 10000000;
        int numTests = 11;

        for(int t = 0; t < numTests; t++){
            MassTree tree = new MassTree();
            int[] intArray0 = new Random().ints(numKeys + 100000, 100000000, 999999999).toArray();
            String[] keys = new String[numKeys + 100000];
            for(int i = 0; i < numKeys + 100000; i++){
                keys[i] = String.valueOf(intArray0);
            }
            for(int i = 0; i < 100000; i++){
                tree.put(keys[i], " ");
            }
            long startTime = System.currentTimeMillis();
            for(int i = 100000; i < numKeys + 100000; i++){
                tree.put(keys[i], " ");
            }
            long endTime = System.currentTimeMillis();
            System.out.println("Masstree: "+ numKeys + "keys inserted/" + (endTime - startTime) + " ms");
        }

        for(int t = 0; t < numTests; t++){
            Bplustree tree = new Bplustree();
            int[] intArray0 = new Random().ints(numKeys + 100000, 100000000, 999999999).toArray();
            String[] keys = new String[numKeys + 100000];
            for(int i = 0; i < numKeys + 100000; i++){
                keys[i] = String.valueOf(intArray0);
            }
            for(int i = 0; i < 100000; i++){
                tree.put(keys[i], " ");
            }
            long startTime = System.currentTimeMillis();
            for(int i = 100000; i < numKeys + 100000; i++){
                tree.put(keys[i], " ");
            }
            long endTime = System.currentTimeMillis();
            System.out.println("B+-tree: "+ numKeys + "keys inserted/" + (endTime - startTime) + " ms");
        }
        return;
    }
}
