public class SingleTest {
    
    void eval(int mode, int len, String[] Keys, int numKeys, int[] IndexArray, int t){
        if(mode == 0){
            MassTree tree = new MassTree();
        }
        else if(mode == 1){
            Bplustree tree = new Bplustree();
        }
        else if(mode == 2){
            RedBlackTree tree = new RedBlackTree();
        } else {System.out.println("Error: mode is invalid"); return;}
        // initialize
        for(int i = 0; i < 100000; i++){
            tree.put(Keys[i], " ");
        }
        long sumins =0;
        long sumget =0;
        long sumgetr =0;
        long sumdel =0;
        long startTime = System.currentTimeMillis();
        for(int i = 100000; i < numKeys + 100000; i++){
            tree.put(Keys[i], " ");
        }
        long Time = System.currentTimeMillis() - startTime;
        if(t > 0){sumins += Time;}
        System.out.println("Masstree #" + t + ": " + numKeys + "keys inserted/" + Time + " ms");

        startTime = System.currentTimeMillis();
        for(int i = 0; i < numKeys/2; i++){
            tree.get(Keys[IndexArray[i]]);
        }
        Time = System.currentTimeMillis() - startTime;
        if(t > 0){sumget += Time;}
        System.out.println("Masstree #" + t + ": " + numKeys/2 + "keys searched/" + Time + " ms");

        startTime = System.currentTimeMillis();
        for(int i = 0; i < numKeys/10; i++){
            tree.getrange(Keys[IndexArray[i]], 1000);
        }
        Time = System.currentTimeMillis() - startTime;
        if(t > 0){sumgetr += Time;}
        System.out.println("Masstree #" + t + ": " + numKeys/10 + "times rangesearched/" + Time + " ms");

        startTime = System.currentTimeMillis();
        for(int i = 0; i < numKeys/2; i++){
            tree.delete(Keys[IndexArray[i]]);
        }
        Time = System.currentTimeMillis() - startTime;
        if(t > 0){sumdel += Time;}
        System.out.println("Masstree #" + t + ": " + numKeys/2 + "keys deleted/" + Time + " ms");
    }
}