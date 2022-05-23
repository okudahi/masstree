public class Bplustree {

    final private static int MAX_CHILD = 5;
    final private static int MAX_KEYS = MAX_CHILD - 1;
    final private static int HALF_MAX_CHILD = ((MAX_CHILD + 1) / 2);

    // Node
    public static abstract class Node {
        int nkeys;
        String[] keys;
        abstract public SplitRequest insert(String k, Object v);
        int  Iskeyexist(String k){ //　ノードにキーkがあるときはそのインデックス、ないときは-1を返す
            for(int i = 0; i < this.nkeys; i++){
                if(this.keys[i].equals(k)){
                    return i;
                }
            }
            return -1;
        }
        int keyindex(String k){ //　キーkが入るべきindexを返す
            int i;
            for(i = 0; i < this.nkeys; i++){
                int cmp = k.compareTo(this.keys[i]); 
                if(cmp < 0){
                    break;
                } // k < keys[i]
            }
            return i;
        }
        abstract public void show(); {
        }
    }

    public static class SplitRequest{ // 分割の際に親にリクエストを送る
        String borderKey; // 真ん中の値
        Node left; // 真ん中より左
        Node right; // 真ん中より右

        // コンストラクタ
        SplitRequest(String b, Node l, Node r){ 
            this.borderKey = b;
            this.left = l;
            this.right = r;
        }
    }

    //interior node
    public class InteriorNode extends Node {
        // 部分木
        Node[] childs;

        // コンストラクタ
        private InteriorNode() {
            this.nkeys = 0;
            this.keys = new String[MAX_KEYS + 1];
            this.childs = new Node[MAX_CHILD + 1];
        }

        // internalノードへのキーkの挿入
        public SplitRequest insert(String k, Object v) {
            int ki = this.keyindex(k);
            SplitRequest req = this.childs[ki].insert(k,v);
            if(req == null){ // 何もしない
                return null;
            }
            else{ // 子が分割→SplitRequest
                int i;
                String insertedKey = req.borderKey;
                Node lchild = req.left;
                Node rchild = req.right;
                for(i = nkeys; i > 0; i--){ // iはnkeysから1まで

                    int cmp = insertedKey.compareTo(this.keys[i-1]);
                    if(cmp < 0){ // k < keys[i-1]
                        this.keys[i] = this.keys[i-1];
                        this.childs[i+1] = this.childs[i];
                    } 
                    else{ // k > keys[i]
                        this.keys[i] = insertedKey;
                        this.childs[i+1] = rchild;
                        this.childs[i] = lchild;
                        this.nkeys++;
                        break;
                    }
                }
                if(i == 0){
                    this.keys[0] = insertedKey; // 左端に追加
                    this.childs[1] = rchild;
                    this.childs[0] = lchild;
                    this.nkeys++;
                }
            }
            if(this.nkeys > MAX_KEYS){ // ノードが満杯のとき、分割が発生
                    this.split(); // 分割
            }
            return null; // 満杯でないならnullを返す(これより上はreq = nullなので何もしない)

        }

        // internalノードでの分割
        SplitRequest split() {
                int borderIndex = HALF_MAX_CHILD - 1; // key[borderIndex]を親ノードに挿入、他を分割
                InteriorNode l = this;
                InteriorNode r = new InteriorNode();
                for(int i = borderIndex + 1; i < MAX_KEYS + 1; i++ ){ // rとlに分配
                    r.keys[i - borderIndex - 1] = l.keys[i];
                    l.keys[i] = null;
                    r.childs[i - borderIndex - 1] = l.childs[i];
                    l.childs[i] = null;
                }
                r.childs[MAX_CHILD - borderIndex - 1] = l.childs[MAX_CHILD];
                l.childs[MAX_CHILD] = null;
                String borderKey = l.keys[borderIndex];
                l.keys[borderIndex] = null;
                l.nkeys = borderIndex;
                r.nkeys = MAX_KEYS - borderIndex;
                if(this != root){ // rootでないなら親にSplitRequest送る
                    return new SplitRequest(borderKey, l, r);
                }
                else{ // rootのとき、新たなrootを作る
                    InteriorNode newRoot = new InteriorNode();
                    newRoot.keys[0] = borderKey;
                    newRoot.childs[0] = l;
                    newRoot.childs[1] = r;
                    newRoot.nkeys = 1;
                    root = newRoot;
                    return null;
                }

        }

        public void show(){
            for(int i = 0; i < this.nkeys; i++){
                this.childs[i].show();
            }
            for(int i = 0; i < this.nkeys; i++){
                System.out.println(this.keys[i]);
                System.out.println("nkeys = " + this.nkeys);
            }

        }
    }

    

    // leaf node
    public static class LeafNode extends Node {

        // データ
        Object[] data;
        // 左隣ノード
        LeafNode prev;
        // 右隣ノード
        LeafNode next;

        // コンストラクタ(空のLeafNode)
        public LeafNode() {
            nkeys = 0;
            this.keys = new String[MAX_KEYS + 1];
            this.data = new Object[MAX_KEYS + 1];
        }
        // コンストラクタ(要素が一つ入ったLeafNode)
        public LeafNode(String key, Object x) {
            this.keys = new String[MAX_KEYS + 1];
            this.data = new Object[MAX_KEYS + 1];
            this.keys[0] = key; 
            this.data[0] = x;
            this.nkeys = 1; 
        }

        // leafノードへのキーk、データxの挿入
        public SplitRequest insert(String k, Object v) {
            int ki = this.Iskeyexist(k);
            if (ki >= 0){ // key(k)がもうある(ki番目に一致)
                this.data[ki] = v; // データの置き換え
                return null;
            }
            else{ // key(k)がまだない場合
                int i;
                for(i = nkeys; i > 0; i--){ // iはnkeysから1まで
                    int cmp = k.compareTo(this.keys[i-1]);
                    if(cmp < 0){ // k < keys[i-1]
                        this.keys[i] = this.keys[i-1]; // 右にずらす
                        this.data[i] = this.data[i-1];
                    } 
                    else{ // k > keys[i-1]
                        this.keys[i] = k;
                        this.data[i] = v;
                        this.nkeys++; // 空いたところに挿入
                        break;
                    }
                }
                if(i == 0){ // k < keys[0]
                    this.keys[0] = k; // 左端に挿入
                    this.data[0] = v;
                    this.nkeys++;
                }
                if(this.nkeys > MAX_KEYS){
                    return this.split();
                }
                return null;
            }
        }

        // Leafノードでの分割
        SplitRequest split() {
            int borderIndex = HALF_MAX_CHILD - 1; // key[j]を親ノードに挿入、他を分割
            LeafNode l = this;
            LeafNode r = new LeafNode();
            for(int i = borderIndex; i < MAX_KEYS + 1; i++ ){ // rとlに分配
                r.keys[i-borderIndex] = l.keys[i];
                l.keys[i] = null;
                r.data[i-borderIndex] = l.data[i];
                l.data[i] = null;
            }
            l.nkeys = borderIndex;
            r.nkeys = MAX_KEYS - borderIndex + 1;
            String borderKey = r.keys[0];
            return new SplitRequest(borderKey, l, r);
        }

        public void show(){
            for(int i = 0; i < this.nkeys; i++){
                System.out.println(this.keys[i]);
                System.out.println("this is LeafNode");
                System.out.println("nkeys = " + this.nkeys);
            }

        }
    }


    // 挿入
    void insert(String k, Object v){ // tree
        Node t = this.root;
        t.insert(k, v);
    }


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

    // 挿入
    public void put(String k, Object x){
        if (root == null) {
            root = new LeafNode(k,x);
        }
        else {
            SplitRequest req = root.insert(k,x);
            if (req == null) {
                // do nothing
            } else {
                // split occurs
                InteriorNode newRoot = new InteriorNode();
                newRoot.keys[0] = req.borderKey;
                newRoot.childs[0] = req.left;
                newRoot.childs[1] = req.right;
                newRoot.nkeys = 1;
                root = newRoot;
            }
        }
    }

    // 削除
    public boolean remove(String key){
        if (root == null) {
            return false;
        }
        else{
            return true;
        }
    }

    public void show(){
        this.root.show();
    }

    //範囲検索
    // public Object getrange(String key, Integer n){

    // }


    public static void main(String[] args){
    }
} 