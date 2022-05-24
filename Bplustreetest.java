

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
        // assertEqual(tree.root.nkeys, 0);
        tree.getPrint("AEE");
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
        tree.getPrint("AEE");
        String aee = tree.get("AEE"); assertEqual(aee, "6");
        tree.getPrint("ZZZ");
        tree.getrange("ABC",10);
        return;
    }
}
