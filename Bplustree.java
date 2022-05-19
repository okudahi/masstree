import java.util.List;
import java.util.ArrayList;

public class Bplustree {

    final private static int MAX_CHILD = 5;
    final private static int MAX_KEYS = MAX_CHILD - 1;
    final private static int HALF_MAX_CHILD = ((MAX_CHILD + 1) / 2);

    // Node
    private abstract class Node {
        int nkeys;
        String[] keys;
        Node parent;
        int  Iskeyexist(String k){ //　ノードにキーkがあるときはそのインデックス、ないときは-1を返す
            for(int i = 0; i < this.nkeys; i++){
                if(this.keys[i] == k){
                    return i;
                }
            }
            return -1;
        }
        int keyindex(String k){ //　ノードが入るべきindexを返す
            int i;
            for(i = 0; i < this.nkeys; i++){
                int cmp = k.compareTo(this.keys[i]); 
                if(cmp < 0){
                    break;
                } // k < keys[i]
            }
            return i;
        }
    }

    //interior node
    private class InteriorNode extends Node {
        // 部分木
        Node[] childs;

        // コンストラクタ
        private InteriorNode() {
            this.nkeys = 0;
            this.keys = new String[MAX_KEYS];
            this.childs = new Node[MAX_CHILD];
        }

        // internalノードへのキーkの挿入
        void insert(String k) {
            if(nkeys < MAX_KEYS){ // ノードが満杯でないとき、そのまま挿入
                int i;
                for(i = nkeys; i > 0; i--){ // iはnkeysから1まで
                    int cmp = k.compareTo(this.keys[i-1]);
                    if(cmp < 0){ // k < keys[i-1]
                        this.keys[i] = this.keys[i-1];
                    } 
                    else{ // k > keys[i]
                        this.keys[i] = k;
                        this.nkeys++;
                        break;
                    }
                }
                if(i == 0){
                    this.keys[0] = k; // 左端に追加
                    this.nkeys++;
                }
            }
            if(this.nkeys == MAX_CHILD){ // ノードが満杯のとき、分割が発生
                this.split(); // 分割
            }
        }

        // internalノードでの分割
        void split(){
            int j = HALF_MAX_CHILD - 1; // key[j]を親ノードに挿入、他を分割
            InteriorNode l = this;
            InteriorNode r = new InteriorNode();
            r.keys.addAll(l.keys.subList(j+1, MAX_CHILD));
            r.childs.addAll(l.childs.subList(j+1, MAX_CHILD+1));
            r.nkeys = r.keys.size();
            if(l.parent != null){ //親があるとき
                ((Bplustree.InteriorNode) l.parent).insert(l.keys.get(j));
                l.keys.subList(j, MAX_CHILD).clear();
                l.childs.subList(j+1, MAX_CHILD+1).clear();
                l.nkeys = l.keys.size();
                r.parent = l.parent;
                l.parent.chi
            }
            else{ // l = this = rootのとき
                InteriorNode l2 = new InteriorNode(); 
                l2.keys.addAll(l.keys);
                l2.childs.addAll(l.childs);
                root = new InteriorNode();
                root.keys.add(l2.keys.get(j));
                root.nkeys++;
                root.childs.add(l2);
                root.childs.add(r);
                l2.parent = root;
                r.parent = root;
            }

        }
    }

    

    // leaf node
    private class LeafNode extends Node {

        // データ
        Object[] data;
        // 左隣ノード
        LeafNode prev;
        // 右隣ノード
        LeafNode next;

        // コンストラクタ(空のLeafNode)
        private LeafNode() {
            nkeys = 0;
            this.keys = new String[MAX_KEYS];
            this.data = new Object[MAX_KEYS];
        }
        // コンストラクタ(要素が一つ入ったLeafNode)
        private LeafNode(String key, Object x) {
            this.keys = new String[MAX_KEYS];
            this.data = new Object[MAX_KEYS];
            this.keys[0] = key; 
            this.data[0] = x;
            this.nkeys = 1; 
        }

        // borderノードへのキーk、データxの挿入
        void insert(String k, Object x) {
            int ki = this.Iskeyexist(k);
            if (ki >= 0){ // key(k)がもうある(ki番目に一致)
                this.data[ki] = x; // データの置き換え
            }
            else if(this.nkeys < MAX_KEYS){ // key(k)がまだない、かつ余裕がある場合
                int i;
                for(i = nkeys; i > 0; i--){ // iはnkeysから1まで
                    int cmp = k.compareTo(this.keys[i-1]);
                    if(cmp < 0){ // k < keys[i-1]
                        this.keys[i] = this.keys[i-1]; // 右にずらす
                        this.data[i] = this.data[i-1];
                    } 
                    else{ // k > keys[i-1]
                        this.keys[i] = k;
                        this.data[i] = x;
                        this.nkeys++; // 空いたところに挿入
                        break;
                    }
                }
                if(i == 0){ // k < keys[0]
                    this.keys[0] = k; // 左端に挿入
                    this.data[0] = x;
                    this.nkeys++;
                }
            }
            else{ // ノードが溢れるとき
                this.split(k); // 分割
            }
        }

        // Leafノードでの分割
        void split(String k){
            // int index = this.keyindex(k); kも分配するように要修正
            // int j = HALF_MAX_CHILD - 1; // key[j]を親ノードに挿入、他を分割
            // LeafNode l = this;
            // LeafNode r = new LeafNode();
            // for(int i = j; i < MAX_KEYS; i++ ){ // rとlに分配
            //     r.keys[i-j] = l.keys[i];
            //     l.keys[i] = null;
            //     r.data[i-j] = l.data[i];
            //     l.data[i] = null;
            // }
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
                root.childs.add(r);
                root.childs.add(l2);
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
                int cmp = key.compareTo(t.keys[i]);
                if      (cmp <  0) break;
                else if (cmp == 0) return true;
            }
            t = t.childs[i];
        }
        LeafNode u = (LeafNode) t;
        for (int i = 0; i < u.nkeys; i++)
            if (key.compareTo(u.keys[i]) == 0) return true;
        return false;
    }    

    // 挿入
    public void put(String k, Object x){
        if (root == null) {
            root = new LeafNode(k,x);
        }
        else {
            ((LeafNode)root).insert(k,x);
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
        tree.put("ABD", 3);
        tree.put("ABF",4);
        tree.put("ABE",5);
        String a = tree.root.keys[0];
        System.out.println("keys:" + tree.root.keys);
        System.out.println("ABF index is:" + tree.root.keyindex("ABF"));
    }
} 