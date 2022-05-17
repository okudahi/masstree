import java.util.List;
import java.util.ArrayList;

public class MassTree {
    final private static int MAX_CHILD = 16;
    final private static int HALF_MAX_CHILD = ((MAX_CHILD + 1) / 2);
    final private static int LEN_KEYSLICE = 8;

    // Node
    private abstract class Node {

        int nkeys;
        List<String> keyslices;
        InteriorNode parent;
    }

    //interior node
    private class InteriorNode extends Node {

        // 子
        List<Node> child;
        // 親ノード
        InteriorNode parent;

        // コンストラクタ
        private InteriorNode() {
            nkeys = 0;
            List<String> keyslices = new ArrayList<String>();
            List<Node> child = new ArrayList<Node>(1);
        }
        
        // internalノードへの挿入
        Node insert(String k, Object x) {

        }
    }

    // border node
    private class BorderNode extends Node {

        // 次の層のrootnode
        List<Node> next_layer;
        // データ
        List<Object> data;
        // 接尾辞
        List<String> keysuffixes;
        // 左隣ノード
        BorderNode prev;
        // 右隣ノード
        BorderNode next;
        // permutation
        long permutation;

        // コンストラクタ
        private BorderNode() {
            nkeys = 0;
            List<String> keyslices = new ArrayList<String>();
        }
        // コンストラクタ
        BorderNode(String key, Object x) {
            keyslices.add(key); 
            data.add(x);
            nkeys++; 
        }

        // keyが何番目にあるか(無かったら-1)
        int  keyindex(String k){
            for(int i = 0; i < this.nkeys; i++){
                if(this.keyslices.get(i) == k){
                    return i;
                }
            }
            return -1;
        }

        // borderノードへの挿入
        Node insert(String k, String suffix, Object x) {
            int ki = this.keyindex(k);
            if (ki >= 0){ // key(k)がもうある(ki番目に一致)
                if(suffix == this.keysuffixes.get(ki)){ // suffixも一致(完全に同じキー)
                    this.data.set(ki,x); // 値の置き換え
                    return this;
                } 
                else if(!(suffix == this.keysuffixes.get(ki))){ // suffixが違う
                    this.makenextlayer(ki, suffix, keysuffixes.get(ki)); // 次の層のrootnode作成
                    this.keysuffixes.set(ki,null); //次の層へ行くのでsuffixは無くなる
                    return this;
                }
            }
            else if(this.nkeys < MAX_CHILD - 1){ // ノードが満杯ではない
                int i;
                for(i = 0; i < this.nkeys; i++){
                    int cmp = k.compareTo(keyslices.get(i));
                    if(cmp < 0){
                        this.keyslices.add(i,k);
                        this.nkeys++;
                        return this;
                    } 
                }
                this.keyslices.add(i,k); // 右端
                this.nkeys++;
                return this;
            }
            else{ // ノードが満杯
                this.split(); // 分割
            }
        }

        // 分割
        Node split(){
            int j = HALF_MAX_CHILD - 1;
            BorderNode l = this;
            BorderNode r = new BorderNode();
            r.keyslices.addAll(l.keyslices.subList(j, MAX_CHILD));
            r.data.addAll(l.data.subList(j, MAX_CHILD));
            r.next_layer.addAll(l.next_layer.subList(j, MAX_CHILD));
            l.keyslices.subList(j, MAX_CHILD).clear();
            l.data.subList(j, MAX_CHILD).clear();
            l.next_layer.subList(j, MAX_CHILD).clear();
            r.next = l.next; l.next = r;
        }

        // keysliceが同じだがkeysuffixが違うとき、
        private void makenextlayer(int i,String a, String b){
            this.next_layer.set(i,new BorderNode());
            int cmp = a.compareTo(b);
            if(cmp < 0){
                this.next_layer.get(i).keyslices.add(a);
                this.next_layer.get(i).keyslices.add(b);
            }
            else{
                this.next_layer.get(i).keyslices.add(b);
                this.next_layer.get(i).keyslices.add(a);
            }
        }
    }
    // BorderNodeかどうか
    private boolean isborder(Node t) { return t instanceof BorderNode; }

    // MassTreeの根
    private BorderNode root;

    // コンストラクタ
    public MassTree() {
        root = null;
    }

    // get
    public Object get(String key){
        return 0;
    }

    // キーがあるかどうか
    public boolean member(String key) {
        if (root == null) return false;
        Node t = root;
        while (t instanceof InteriorNode) {
            int i;
            for (i = 0; i < t.nkeys; i++) {
                int cmp = key.compareTo(t.keyslices.get(i));
                if      (cmp <  0) break;
                else if (cmp == 0) return true;
            }
            t = t.child.get(i);
        }
        BorderNode u = (BorderNode) t;
        for (int i = 0; i < u.nkeys; i++)
            if (key.compareTo(u.keyslices.get(i)) == 0) return true;
        return false;
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
    public void put(String k, Object x){
        String suffix = keysuffix(k);
        k = keyintoslice(k);
        if (root == null) {
            root = new BorderNode(k,x);
            root.keysuffixes.add(suffix);
        }
        else {
        }
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
    public boolean remove(String key){
        if (root == null) {
            root = new BorderNode();
            return false;
        }
        else{
            return true;
        }
    }

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