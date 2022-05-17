import java.util.List;
import java.util.ArrayList;

public class Bplustree {

    final private static int MAX_CHILD = 5;
    final private static int HALF_MAX_CHILD = ((MAX_CHILD + 1) / 2);

    // Node
    private abstract class Node {
        List<Node> child;
        int nkeys;
        List<String> keys;
        Node parent;
        int  keyindex(String k){
            for(int i = 0; i < this.nkeys; i++){
                if(this.keys.get(i) == k){
                    return i;
                }
            }
            return -1;
        }
    }

    //interior node
    private class InteriorNode extends Node {

        // 子
        List<Node> child;
        // 親ノード
        Node parent;

        // コンストラクタ
        private InteriorNode() {
            nkeys = 0;
            List<String> keys = new ArrayList<String>();
            List<Node> child = new ArrayList<Node>(1);
        }

        // keyが何番目にあるか(無かったら-1)
        int  keyindex(String k){
            for(int i = 0; i < this.nkeys; i++){
                if(this.keys.get(i) == k){
                    return i;
                }
            }
            return -1;
        }

        // internalノードへの挿入
        void insert(String k) {
                int i;
                for(i = 0; i < this.nkeys; i++){
                    int cmp = k.compareTo(keys.get(i));
                    if(cmp < 0){
                        this.keys.add(i,k);
                        this.nkeys++;
                        break;
                    } 
                }
                if(i == this.nkeys){
                    this.keys.add(k); // 右端に追加
                    this.nkeys++;
                }
            if(this.nkeys == MAX_CHILD){ // ノードが溢れた
                this.split(); // 分割
            }
        }

        void split(){
            int j = HALF_MAX_CHILD - 1; // key[j]を親ノードに挿入、他を分割
            InteriorNode l = this;
            InteriorNode r = new InteriorNode();
            r.keys.addAll(l.keys.subList(j+1, MAX_CHILD));
            r.child.addAll(l.child.subList(j+1, MAX_CHILD+1));
            
            if(l.parent != null){
                ((Bplustree.InteriorNode) l.parent).insert(l.keys.get(j));
                l.keys.subList(j, MAX_CHILD).clear();
                l.child.subList(j+1, MAX_CHILD+1).clear();
            }
            else{ // l = this = rootのとき
                InteriorNode l2 = new InteriorNode(); 
                l2.keys.addAll(l.keys);
                l2.child.addAll(l.child);
                root = new InteriorNode();
                root.keys.add(l2.keys.get(j));
                root.child.add(l2);
                root.child.add(r);
                l2.parent = root;
                r.parent = root;
            }

        }
    }

    // leaf node
    private class LeafNode extends Node {

        // データ
        ArrayList<Object> data;
        // 左隣ノード
        LeafNode prev;
        // 右隣ノード
        LeafNode next;

        // コンストラクタ
        private LeafNode() {
            nkeys = 0;
            ArrayList<String> keys = new ArrayList<String>();
            ArrayList<Object> data = new ArrayList<Object>();
        }
        // コンストラクタ
        private LeafNode(String key, Object x) {
            ArrayList<String> keys = new ArrayList<String>();
            ArrayList<Object> data = new ArrayList<Object>();
            keys.add(key); 
            data.add(x);
            nkeys++; 
        }
        

        // keyが何番目にあるか(無かったら-1)
        int  keyindex(String k){
            for(int i = 0; i < this.nkeys; i++){
                if(this.keys.get(i) == k){
                    return i;
                }
            }
            return -1;
        }

        // borderノードへの挿入
        void insert(String k, Object x) {
            int ki = this.keyindex(k);
            if (ki >= 0){ // key(k)がもうある(ki番目に一致)
                this.data.set(ki,x);
            }
            else{ // key(k)がまだない
                int i;
                for(i = 0; i < this.nkeys; i++){
                    int cmp = k.compareTo(keys.get(i));
                    if(cmp < 0){
                        this.keys.add(i,k);
                        this.data.add(i,x);
                        this.nkeys++;
                        break;
                    } 
                }
                if(i == this.nkeys){
                    this.keys.add(k); // 右端に追加
                    this.data.add(x);
                    this.nkeys++;
                }
            }
            if(this.nkeys == MAX_CHILD){ // ノードが溢れた
                this.split(); // 分割
            }
        }

        // 分割
        void split(){
            int j = HALF_MAX_CHILD - 1; // key[j]を親ノードに挿入、他を分割
            LeafNode l = this;
            LeafNode r = new LeafNode();
            r.keys.addAll(l.keys.subList(j, MAX_CHILD));
            r.data.addAll(l.data.subList(j, MAX_CHILD));
            l.keys.subList(j, MAX_CHILD).clear();
            l.data.subList(j, MAX_CHILD).clear();
            if(l.parent != null){ // 親がある(rootじゃない)
                ((Bplustree.InteriorNode) l.parent).insert(l.keys.get(j));
                r.parent = l.parent;
                r.next = l.next;
                l.next = r;
            }
            else{ // l = this = rootのとき
                LeafNode l2 = new LeafNode(); 
                l2.keys.addAll(l.keys);
                l2.data.addAll(l.data);
                root = new InteriorNode();
                root.keys.add(r.keys.get(0));
                l2.parent = root;
                r.parent = root;
                root.child.add(r);
                root.child.add(l2);
                r.next = l2.next;
                l2.next = r;
            }

        }

    }
    // LeafNodeかどうか
    private boolean isleaf(Node t) { return t instanceof LeafNode; }

    // Bplustreeの根
    private Node root;

    // コンストラクタ
    public Bplustree() {
        this.root = new LeafNode();
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
                int cmp = key.compareTo(t.keys.get(i));
                if      (cmp <  0) break;
                else if (cmp == 0) return true;
            }
            t = t.child.get(i);
        }
        LeafNode u = (LeafNode) t;
        for (int i = 0; i < u.nkeys; i++)
            if (key.compareTo(u.keys.get(i)) == 0) return true;
        return false;
    }    

    // 挿入
    public void put(String k, Object x){
        if (root == null) {
            root = new LeafNode(k,x);
        }
        else {
        }
    }

    // Node insert(String key, Object x) {
    //     if (this == null) return new BorderNode(key, x);
    //     int i;
    //     for (i = 0; i < nkeys; i++) {
    //         int cmp = key.compareTo(keys.get(i));
    //         if (cmp < 0) return balance(i, key, x);
    //         else if (cmp == 0) { data.set(i, x); return this; }
    //     }
    //     return balance(i, key, x);
    // }

    // 削除
    public boolean remove(String key){
        if (root == null) {
            root = new LeafNode();
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
        Bplustree tree = new Bplustree();
        tree.put("ABC",2);
        tree.root.keys.add("ABE");
        tree.root.nkeys++;
        tree.root.keys.add("ABF");
        tree.root.nkeys++;
        String a = tree.root.keys.get(0);
        System.out.println("keys:" + tree.root.keys);
        System.out.println("ABE index is:" + tree.root.keyindex("ABF"));
    }
} 