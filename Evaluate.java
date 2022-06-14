import java.util.Random;

public class Evaluate {


    public class RandStr {

        private static String target = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

        // rand string create
        public static String randStrFunc(int count) {
                StringBuffer sb = new StringBuffer(count);
                String res;
                int randPos;
                for (int i = 0; i < count; i++) {
                    //ランダムインデックス取得
                    randPos = (int) (Math.random() * target.length());
                    sb.append(target.charAt(randPos));
                }
                res = sb.toString();
                return res;
        }

    }

    public static void main(String[] args){
        System.out.println("Masstree:MAX_CHILD = " + MassTree.MassTreeNode.MAX_CHILD);
        System.out.println("Masstree:LEN_KEYSLICE = " + MassTree.MassTreeNode.LEN_KEYSLICE);
        System.out.println("B+tree:MAX_CHILD = " + Bplustree.MAX_CHILD);
        int numKeys = 1000000;
        int numTests = 11;
        long sumins = 0;
        long sumget = 0;
        long sumdel = 0;
        long sumgetr = 0;
        // int[] intKeyArray = new Random().ints(numKeys + 100000, 100000000, 999999999).toArray();
        int[] IndexArray = new Random().ints(numKeys/2,0, numKeys + 99999).toArray();
        String[] Keys = new String[numKeys + 100000];
        // for(int i = 0; i < numKeys + 100000; i++){
        //     Keys[i] = String.valueOf(intKeyArray[i]);
        // }
        // "aaaaaaaaaaaaaaaaaaaa"

        for(int i = 0; i < numKeys + 100000; i++){
            Keys[i] = RandStr.randStrFunc(60);
        }

        // for(int i = 0; i < numKeys + 100000; i++){
        //     Keys[i] = "aaaaaaaaaaaaaaaaaaaa" + RandStr.randStrFunc(40);
        // }

        // for(int i = 0; i < numKeys + 100000; i++){
        //     Keys[i] = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" + RandStr.randStrFunc(4);
        // }

        for(int t = 0; t < numTests; t++){
            MassTree tree = new MassTree();
            for(int i = 0; i < 100000; i++){
                tree.put(Keys[i], " ");
            }

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
        System.out.println("Masstree: "+ numKeys + "keys inserted/" + (sumins/(numTests-1)) + "ms (mean of " + (numTests - 1) + " times)");
        System.out.println("Masstree: "+ numKeys/2 + "keys searched/" + (sumget/(numTests-1)) + "ms (mean of " + (numTests - 1) + " times)");
        System.out.println("Masstree: "+ numKeys/10 + "times rangesearched/" + (sumgetr/(numTests-1)) + "ms (mean of " + (numTests - 1) + " times)");
        System.out.println("Masstree: "+ numKeys/2 + "keys deleted/" + (sumdel/(numTests-1)) + "ms (mean of " + (numTests - 1) + " times)");
        System.out.println("Masstree.put: "+ (numKeys) / (sumins/(numTests-1)) / 100.0 + " [100,000 times/sec]");
        System.out.println("Masstree.get: "+ (numKeys/2) / (sumget/(numTests-1)) / 100.0 + " [100,000 times/sec]");
        System.out.println("Masstree.getrange: "+ (numKeys/10) / (sumgetr/(numTests-1)) / 100.0 + " [100,000 times/sec]");
        System.out.println("Masstree.delete: "+ (numKeys/2) / (sumdel/(numTests-1)) / 100.0 + " [100,000 times/sec]");
        System.out.println("=============================================================");
        sumins = 0;
        sumget = 0;
        sumdel = 0;
        sumgetr = 0;

        for(int t = 0; t < numTests; t++){
            Bplustree tree = new Bplustree();
            for(int i = 0; i < 100000; i++){
                tree.put(Keys[i], " ");
            }

            long startTime = System.currentTimeMillis();
            for(int i = 100000; i < numKeys + 100000; i++){
                tree.put(Keys[i], " ");
            }
            long Time = System.currentTimeMillis() - startTime;
            if(t > 0){sumins += Time;}
            System.out.println("B+tree #" + t + ": " + numKeys + "keys inserted/" + Time + " ms");

            startTime = System.currentTimeMillis();
            for(int i = 0; i < numKeys/2; i++){
                tree.get(Keys[IndexArray[i]]);
            }
            Time = System.currentTimeMillis() - startTime;
            if(t > 0){sumget += Time;}
            System.out.println("B+tree #" + t + ": " + numKeys/2 + "keys searched/" + Time + " ms");
            
            startTime = System.currentTimeMillis();
            for(int i = 0; i < numKeys/10; i++){
                tree.getrange(Keys[IndexArray[i]], 1000);
            }
            Time = System.currentTimeMillis() - startTime;
            if(t > 0){sumgetr += Time;}
            System.out.println("B+tree #" + t + ": " + numKeys/10 + "times rangesearched/" + Time + " ms");

            startTime = System.currentTimeMillis();
            for(int i = 0; i < numKeys/2; i++){
                tree.deleteWithNoRebalance(Keys[IndexArray[i]]);
            }
            Time = System.currentTimeMillis() - startTime;
            if(t > 0){sumdel += Time;}
            System.out.println("B+tree #" + t + ": " + numKeys/2 + "keys deleted/" + Time + " ms");
        }
        System.out.println("B+tree: "+ numKeys + "keys inserted/" + (sumins/(numTests-1)) + "ms (mean of " + (numTests - 1) + " times)");
        System.out.println("B+tree: "+ numKeys/2 + "keys searched/" + (sumget/(numTests-1)) + "ms (mean of " + (numTests - 1) + " times)");
        System.out.println("B+tree: "+ numKeys/10 + "times rangesearched/" + (sumgetr/(numTests-1)) + "ms (mean of " + (numTests - 1) + " times)");
        System.out.println("B+tree: "+ numKeys/2 + "keys deleted/" + (sumdel/(numTests-1)) + "ms (mean of " + (numTests - 1) + " times)");
        System.out.println("B+tree.put: "+ (numKeys) / (sumins/(numTests-1)) / 100.0 + " [100,000 times/sec]");
        System.out.println("B+tree.get: "+ (numKeys/2) / (sumget/(numTests-1)) / 100.0 + " [100,000 times/sec]");
        System.out.println("B+tree.getrange: "+ (numKeys/10) / (sumgetr/(numTests-1)) / 100.0 + " [100,000 times/sec]");
        System.out.println("B+tree.delete: "+ (numKeys/2) / (sumdel/(numTests-1)) / 100.0 + " [100,000 times/sec]");
        System.out.println("=============================================================");
        sumins = 0;
        sumget = 0;
        sumdel = 0;
        sumgetr = 0;

        for(int t = 0; t < numTests; t++){
            RedBlackTree tree = new RedBlackTree();
            for(int i = 0; i < 100000; i++){
                tree.put(Keys[i], " ");
            }

            long startTime = System.currentTimeMillis();
            for(int i = 100000; i < numKeys + 100000; i++){
                tree.put(Keys[i], " ");
            }
            long Time = System.currentTimeMillis() - startTime;
            if(t > 0){sumins += Time;}
            System.out.println("RedBlacktree #" + t + ": " + numKeys + "keys inserted/" + Time + " ms");

            startTime = System.currentTimeMillis();
            for(int i = 0; i < numKeys/2; i++){
                tree.get(Keys[IndexArray[i]]);
            }
            Time = System.currentTimeMillis() - startTime;
            if(t > 0){sumget += Time;}
            System.out.println("RedBlacktree #" + t + ": " + numKeys/2 + "keys searched/" + Time + " ms");

            startTime = System.currentTimeMillis();
            for(int i = 0; i < numKeys/2; i++){
                tree.delete(Keys[IndexArray[i]]);
            }
            Time = System.currentTimeMillis() - startTime;
            if(t > 0){sumdel += Time;}
            System.out.println("RedBlacktree #" + t + ": " + numKeys/2 + "keys deleted/" + Time + " ms");
        }
        System.out.println("RedBlacktree: "+ numKeys + "keys inserted/" + (sumins/(numTests-1)) + "ms (mean of " + (numTests - 1) + " times)");
        System.out.println("RedBlacktree: "+ numKeys/2 + "keys searched/" + (sumget/(numTests-1)) + "ms (mean of " + (numTests - 1) + " times)");
        System.out.println("RedBlacktree: "+ numKeys/2 + "keys deleted/" + (sumdel/(numTests-1)) + "ms (mean of " + (numTests - 1) + " times)");
        System.out.println("RedBlacktree.put: "+ (numKeys) / (sumins/(numTests-1)) / 100.0 + " [100,000 times/sec]");
        System.out.println("RedBlacktree.get: "+ (numKeys/2) / (sumget/(numTests-1)) / 100.0 + " [100,000 times/sec]");
        System.out.println("RedBlacktree.delete: "+ (numKeys/2) / (sumdel/(numTests-1)) / 100.0 + " [100,000 times/sec]");
        System.out.println("=============================================================");

        sumins = 0;
        sumget = 0;
        sumdel = 0;

        return;
    }
}
