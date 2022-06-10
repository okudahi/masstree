public class Yamazaki {
    private Bplustree root;
    private Object value;
    public Yamazaki() {
        this.root = new Bplustree();
    }
    public insert(String str, Object value) {
        Yamazaki tree = this;
        List<String> words = str.splitEach(8);
        for (String word : words) {
            tree = tree.root.get(word);
        }
        tree.value = value;
    }
}
