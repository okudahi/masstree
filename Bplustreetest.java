import java.io.FileWriter;
import java.io.IOException;

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
        // assertEqual(tree.root.nkeys, 0);
        tree.getPrint("AEE");
        tree.put("ABC", "0");
        tree.put("ABC", "1");
        tree.put("AAA", "2");
        tree.put("ABB", "3");
        tree.put("ACC", "4");
        tree.put("ADD", "5");
        tree.put("AEE", "6");
        tree.put("AFF", "7");
        tree.put("AGG", "8");
        tree.put("AHH", "9");
        tree.put("AII", "10");
        tree.put("AJJ", "11");
        tree.put("AKK", "12");
        tree.put("ALL", "13");
        tree.put("AMM", "14");
        tree.put("ANN", "15");
        tree.put("AOO", "16");
        tree.put("APP", "17");
        tree.put("AQQ", "18");
        tree.delete("ALL");
        tree.delete("AMM");
        tree.delete("AAA");
        tree.delete("ABB");
        tree.delete("ABC");
        // makeDotFile(tree);
        tree.delete("ACC");
        // makeDotFile(tree);
        tree.delete("ADD");
        // makeDotFile(tree);
        tree.delete("AEE");
        tree.getPrint("AEE");
        String aee = tree.get("AFF"); assertEqual(aee, "7");
        tree.getPrint("ZZZ");
        tree.getrange("ABC",14);
        makeDotFile(tree);
        return;
    }
}
