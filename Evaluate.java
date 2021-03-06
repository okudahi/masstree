import java.util.Random;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class Evaluate {


    public static class RandStr {

        private static String target = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random rnd = new Random(0);
        // rand string create
        public String randStrFunc(int count) {
                StringBuffer sb = new StringBuffer(count);
                String res;
                int randPos;
                
                for (int i = 0; i < count; i++) {
                    //ランダムインデックス取得
                    randPos = (int) (rnd.nextFloat() * target.length());
                    sb.append(target.charAt(randPos));
                }
                res = sb.toString();
                return res;
        }

    }

    public static void main(String[] args){
        String name = args[0] + "prefix_20random(substring_removed)";
        System.out.println("Masstree:MAX_CHILD = " + MassTree.MassTreeNode.MAX_CHILD);
        System.out.println("Masstree:LEN_KEYSLICE = " + MassTree.MassTreeNode.LEN_KEYSLICE);
        System.out.println("B+tree:MAX_CHILD = " + Bplustree.MAX_CHILD);
        final int numInitialKeys = 100000;
        final int numKeys = 10000000;
        final int numTests = 5;
        final int dontUse = 1;
        final int len_prefix = Integer.parseInt(args[0]);
        final int len_random = 20;
        // int[] intKeyArray = new Random().ints(numKeys + numInitialKeys, 100000000, 999999999).toArray();
        int[] IndexArray = new Random().ints(numKeys/2,0, numKeys + numInitialKeys - 1).toArray();
        String[] Keys = new String[numKeys + numInitialKeys];
        String prefix = "";
        for(int i = 0; i < len_prefix; i++){
            prefix += "a";
        }
        // for(int i = 0; i < numKeys + numInitialKeys; i++){
        //     Keys[i] = String.valueOf(intKeyArray[i]);
        // }
        long sumins = 0;
        long sumget = 0;
        long sumdel = 0;
        long sumgetr = 0;
        RandStr rnd = new RandStr();
        for(int i = 0; i < numKeys + numInitialKeys; i++){
            Keys[i] = prefix + rnd.randStrFunc(len_random);
        }
        for(int t = 0; t < numTests; t++){
            MassTree tree = new MassTree();
            for(int i = 0; i < numInitialKeys; i++){
                tree.put(Keys[i], " ");
            }
            long startTime = System.currentTimeMillis();
            for(int i = numInitialKeys; i < numKeys + numInitialKeys; i++){
                tree.put(Keys[i], " ");
            }
            // tree.validate();
            // tree.rootTree.makeDotFile();
            long Time = System.currentTimeMillis() - startTime;
            if(t > dontUse){sumins += Time;}
            System.out.println("Masstree #" + t + ": " + numKeys + "keys inserted/" + Time + " ms");
            startTime = System.currentTimeMillis();
            for(int i = 0; i < numKeys/2; i++){
                tree.get(Keys[IndexArray[i]]);
            }
            Time = System.currentTimeMillis() - startTime;
            if(t > dontUse){sumget += Time;}
            System.out.println("Masstree #" + t + ": " + numKeys/2 + "keys searched/" + Time + " ms");

            startTime = System.currentTimeMillis();
            for(int i = 0; i < numKeys/10; i++){
                tree.getrange(Keys[IndexArray[i]], 1000);
            }
            Time = System.currentTimeMillis() - startTime;
            if(t > dontUse){sumgetr += Time;}
            System.out.println("Masstree #" + t + ": " + numKeys/10 + "times rangesearched/" + Time + " ms");

            startTime = System.currentTimeMillis();
            for(int i = 0; i < numKeys/2; i++){
                tree.delete(Keys[IndexArray[i]]);
            }
            Time = System.currentTimeMillis() - startTime;
            if(t > dontUse){sumdel += Time;}
            System.out.println("Masstree #" + t + ": " + numKeys/2 + "keys deleted/" + Time + " ms");
            tree = null;
        }
        long masstreeins = (numKeys) / (sumins/(numTests - dontUse - 1))*1000;
        long masstreeget = (numKeys/2) / (sumget/(numTests - dontUse - 1))*1000;
        long masstreegetr = (numKeys/10) / (sumgetr/(numTests - dontUse - 1))*1000;
        long masstreedel = (numKeys/2) / (sumdel/(numTests - dontUse - 1))*1000;
        System.out.println("Masstree: "+ numKeys + "keys inserted/" + (sumins/(numTests - dontUse - 1)) + "ms (mean of " + (numTests - dontUse - 1) + " times)");
        System.out.println("Masstree: "+ numKeys/2 + "keys searched/" + (sumget/(numTests - dontUse - 1)) + "ms (mean of " + (numTests - dontUse - 1) + " times)");
        System.out.println("Masstree: "+ numKeys/10 + "times rangesearched/" + (sumgetr/(numTests - dontUse - 1)) + "ms (mean of " + (numTests - dontUse - 1) + " times)");
        System.out.println("Masstree: "+ numKeys/2 + "keys deleted/" + (sumdel/(numTests - dontUse - 1)) + "ms (mean of " + (numTests - dontUse - 1) + " times)");
        System.out.println("Masstree.put: "+ masstreeins + " [times/sec]");
        System.out.println("Masstree.get: "+ masstreeget + " [times/sec]");
        System.out.println("Masstree.getrange: "+ masstreegetr + " [times/sec]");
        System.out.println("Masstree.delete: "+ masstreedel + " [times/sec]");
        System.out.println("=============================================================");
        sumins = 0;
        sumget = 0;
        sumdel = 0;
        sumgetr = 0;

        for(int t = 0; t < numTests; t++){
            Bplustree tree = new Bplustree();
            for(int i = 0; i < numInitialKeys; i++){
                tree.put(Keys[i], " ");
            }

            long startTime = System.currentTimeMillis();
            for(int i = numInitialKeys; i < numKeys + numInitialKeys; i++){
                tree.put(Keys[i], " ");
            }
            long Time = System.currentTimeMillis() - startTime;
            if(t > dontUse){sumins += Time;}
            System.out.println("B+tree #" + t + ": " + numKeys + "keys inserted/" + Time + " ms");

            startTime = System.currentTimeMillis();
            for(int i = 0; i < numKeys/2; i++){
                tree.get(Keys[IndexArray[i]]);
            }
            Time = System.currentTimeMillis() - startTime;
            if(t > dontUse){sumget += Time;}
            System.out.println("B+tree #" + t + ": " + numKeys/2 + "keys searched/" + Time + " ms");
            
            startTime = System.currentTimeMillis();
            for(int i = 0; i < numKeys/10; i++){
                tree.getrange(Keys[IndexArray[i]], 1000);
            }
            Time = System.currentTimeMillis() - startTime;
            if(t > dontUse){sumgetr += Time;}
            System.out.println("B+tree #" + t + ": " + numKeys/10 + "times rangesearched/" + Time + " ms");

            startTime = System.currentTimeMillis();
            for(int i = 0; i < numKeys/2; i++){
                tree.deleteWithNoRebalance(Keys[IndexArray[i]]);
            }
            Time = System.currentTimeMillis() - startTime;
            if(t > dontUse){sumdel += Time;}
            System.out.println("B+tree #" + t + ": " + numKeys/2 + "keys deleted/" + Time + " ms");
            tree = null;
            System.gc();
        }
        long bplusins = (numKeys) / (sumins/(numTests - dontUse - 1))*1000;
        long bplusget = (numKeys/2) / (sumget/(numTests - dontUse - 1))*1000;
        long bplusgetr = (numKeys/10) / (sumgetr/(numTests - dontUse - 1))*1000;
        long bplusdel = (numKeys/2) / (sumdel/(numTests - dontUse - 1))*1000;
        System.out.println("B+tree: "+ numKeys + "keys inserted/" + (sumins/(numTests - dontUse - 1)) + "ms (mean of " + (numTests - dontUse - 1) + " times)");
        System.out.println("B+tree: "+ numKeys/2 + "keys searched/" + (sumget/(numTests - dontUse - 1)) + "ms (mean of " + (numTests - dontUse - 1) + " times)");
        System.out.println("B+tree: "+ numKeys/10 + "times rangesearched/" + (sumgetr/(numTests - dontUse - 1)) + "ms (mean of " + (numTests - dontUse - 1) + " times)");
        System.out.println("B+tree: "+ numKeys/2 + "keys deleted/" + (sumdel/(numTests - dontUse - 1)) + "ms (mean of " + (numTests - dontUse - 1) + " times)");
        System.out.println("B+tree.put: "+ bplusins + " [times/sec]");
        System.out.println("B+tree.get: "+ bplusget + " [times/sec]");
        System.out.println("B+tree.getrange: "+ bplusgetr + " [times/sec]");
        System.out.println("B+tree.delete: "+ bplusdel + " [times/sec]");
        System.out.println("=============================================================");
        sumins = 0;
        sumget = 0;
        sumdel = 0;
        sumgetr = 0;

        for(int t = 0; t < numTests; t++){
            RedBlackTree tree = new RedBlackTree();
            for(int i = 0; i < numInitialKeys; i++){
                tree.put(Keys[i], " ");
            }

            long startTime = System.currentTimeMillis();
            for(int i = numInitialKeys; i < numKeys + numInitialKeys; i++){
                tree.put(Keys[i], " ");
            }
            long Time = System.currentTimeMillis() - startTime;
            if(t > dontUse){sumins += Time;}
            System.out.println("RedBlacktree #" + t + ": " + numKeys + "keys inserted/" + Time + " ms");

            startTime = System.currentTimeMillis();
            for(int i = 0; i < numKeys/2; i++){
                tree.get(Keys[IndexArray[i]]);
            }
            Time = System.currentTimeMillis() - startTime;
            if(t > dontUse){sumget += Time;}
            System.out.println("RedBlacktree #" + t + ": " + numKeys/2 + "keys searched/" + Time + " ms");

            startTime = System.currentTimeMillis();
            for(int i = 0; i < numKeys/2; i++){
                tree.delete(Keys[IndexArray[i]]);
            }
            Time = System.currentTimeMillis() - startTime;
            if(t > dontUse){sumdel += Time;}
            System.out.println("RedBlacktree #" + t + ": " + numKeys/2 + "keys deleted/" + Time + " ms");
            tree = null;
            System.gc();
        }
        long rbins = (numKeys) / (sumins/(numTests - dontUse - 1))*1000;
        long rbget = (numKeys/2) / (sumget/(numTests - dontUse - 1))*1000;
        long rbdel = (numKeys/2) / (sumdel/(numTests - dontUse - 1))*1000;
        System.out.println("RedBlacktree: "+ numKeys + "keys inserted/" + (sumins/(numTests - dontUse - 1)) + "ms (mean of " + (numTests - dontUse - 1) + " times)");
        System.out.println("RedBlacktree: "+ numKeys/2 + "keys searched/" + (sumget/(numTests - dontUse - 1)) + "ms (mean of " + (numTests - dontUse - 1) + " times)");
        System.out.println("RedBlacktree: "+ numKeys/2 + "keys deleted/" + (sumdel/(numTests - dontUse - 1)) + "ms (mean of " + (numTests - dontUse - 1) + " times)");
        System.out.println("RedBlacktree.put: "+ rbins + " [times/sec]");
        System.out.println("RedBlacktree.get: "+ rbget + " [times/sec]");
        System.out.println("RedBlacktree.delete: "+ rbdel + " [times/sec]");
        System.out.println("=============================================================");
        sumins = 0;
        sumget = 0;
        sumdel = 0;

        for(int t = 0; t < numTests; t++){
            HashMap<String, String> map = new HashMap<String, String>();
            for(int i = 0; i < numInitialKeys; i++){
                map.put(Keys[i], " ");
            }

            long startTime = System.currentTimeMillis();
            for(int i = numInitialKeys; i < numKeys + numInitialKeys; i++){
                map.put(Keys[i], " ");
            }
            long Time = System.currentTimeMillis() - startTime;
            if(t > dontUse){sumins += Time;}
            System.out.println("HashMap #" + t + ": " + numKeys + "keys inserted/" + Time + " ms");

            startTime = System.currentTimeMillis();
            for(int i = 0; i < numKeys/2; i++){
                map.get(Keys[IndexArray[i]]);
            }
            Time = System.currentTimeMillis() - startTime;
            if(t > dontUse){sumget += Time;}
            System.out.println("HashMap #" + t + ": " + numKeys/2 + "keys searched/" + Time + " ms");

            startTime = System.currentTimeMillis();
            for(int i = 0; i < numKeys/2; i++){
                map.remove(Keys[IndexArray[i]]);
            }
            Time = System.currentTimeMillis() - startTime;
            if(t > dontUse){sumdel += Time;}
            System.out.println("HashMap #" + t + ": " + numKeys/2 + "keys deleted/" + Time + " ms");
            map = null;
            System.gc();
        }
        long hashins = (numKeys) / (sumins/(numTests - dontUse - 1))*1000;
        long hashget = (numKeys/2) / (sumget/(numTests - dontUse - 1))*1000;
        long hashdel = (numKeys/2) / (sumdel/(numTests - dontUse - 1))*1000;
        System.out.println("HashMap: "+ numKeys + "keys inserted/" + (sumins/(numTests - dontUse - 1)) + "ms (mean of " + (numTests - dontUse - 1) + " times)");
        System.out.println("HashMap: "+ numKeys/2 + "keys searched/" + (sumget/(numTests - dontUse - 1)) + "ms (mean of " + (numTests - dontUse - 1) + " times)");
        System.out.println("HashMap: "+ numKeys/2 + "keys deleted/" + (sumdel/(numTests - dontUse - 1)) + "ms (mean of " + (numTests - dontUse - 1) + " times)");
        System.out.println("HashMap.put: "+ hashins + " [times/sec]");
        System.out.println("HashMap.get: "+ hashget + " [times/sec]");
        System.out.println("HashMap.delete: "+ hashdel + " [times/sec]");
        System.out.println("=============================================================");
        sumins = 0;
        sumget = 0;
        sumdel = 0;
        long[] data = {masstreeins, bplusins, rbins, hashins, masstreeget, bplusget, rbget, hashget, masstreegetr, bplusgetr, masstreedel, bplusdel, rbdel, hashdel};
        exportCsv(data, name, len_prefix, len_random);
        return;
    }

    public static void exportCsv(long[] data, String name, int lenpre, int lenran){
        try {
            File file = new File("./data/" + name + ".csv");
            file.createNewFile();
            FileWriter fw = new FileWriter(file, false);
            PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
            
            pw.print("Operation,Masstree,B+tree,RedBlackTree,HashMap");
            pw.println();
            pw.print("put");
            pw.print(",");
            pw.print(data[0]);
            pw.print(",");
            pw.print(data[1]);
            pw.print(",");
            pw.print(data[2]);
            pw.print(",");
            pw.print(data[3]);
            pw.println();
            pw.print("get");
            pw.print(",");
            pw.print(data[4]);
            pw.print(",");
            pw.print(data[5]);
            pw.print(",");
            pw.print(data[6]);
            pw.print(",");
            pw.print(data[7]);
            pw.println();
            pw.print("getrange");
            pw.print(",");
            pw.print(data[8]);
            pw.print(",");
            pw.print(data[9]);
            pw.println();
            pw.print("delete");
            pw.print(",");
            pw.print(data[10]);
            pw.print(",");
            pw.print(data[11]);
            pw.print(",");
            pw.print(data[12]);
            pw.print(",");
            pw.print(data[13]);
            pw.println();

            pw.close();
 
            System.out.println(lenpre + "prefix + " + lenran + "random: Output at "+ "data/" + name + ".csv");
 
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
