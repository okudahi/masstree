

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
        tree.put("ABC", 1);
        tree.put("AAA", 2);
        tree.put("ABB", 3);
        tree.put("ACC", 4);
        tree.put("ADD", 5);
        tree.put("AEE", 6);
        tree.put("AFF", 7);
        tree.put("AGG", 7);
        tree.put("AHH", 7);
        tree.put("AII", 7);
        tree.put("AJJ", 7);
        tree.put("AKK", 7);
        tree.put("ALL", 7);
        tree.put("AMM", 7);
        tree.put("ANN", 7);
        tree.put("AOO", 7);
        tree.put("APP", 7);
        tree.put("AQQ", 7);
        tree.show();
        return;
    }
}
