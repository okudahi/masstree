public class MassTree {
    private MassTreeNode rootTree;
    private String value;
    final private static int LEN_KEYSLICE = 8;
    
    public MassTree() {
        this.rootTree = new MassTreeNode();
    }

    public void insert(String key, String value) {
        this.rootTree.insert(key, value);
    }

    public String get(String key){
        this.rootTree.get(key);
    }
}
    

    // keyからLEN_KEYSLICE文字切り取る
    private static String keyintoslice(String key){
        return key.substring(0,LEN_KEYSLICE);
    }

    // 切り取った後に残った文字
    private static String keysuffix(String key){
        return key.substring(LEN_KEYSLICE);
    }

    // 挿入
    public void put(String k, String x){
        this.rootTree.insert(k, x);
    }

    // Node insert(String key, Object x) {
    //     if (this == null) return new BorderNode(key, x);
    //     int i;
    //     for (i = 0; i < nkeys; i++) {
    //         int cmp = key.compareTo(keyslices.get(i));
    //         if (cmp < 0) return balance(i, key, x);
    //         else if (cmp == 0) { data.set(i, x); return this; }
    //     }
    //     return balance(i, key, x);
    // }

    // 削除

    //範囲検索
    // public Object getrange(String key, Integer n){

    // }


    public static void main(String[] args){
        MassTree tree = new MassTree();
        tree.root.keyslices.add("ABD");
        String a = tree.root.keyslices.get(0);
        // String key = "ABCDEFGHI";
        System.out.println(a);
    }
}