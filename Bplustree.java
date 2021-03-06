import java.util.ArrayList;
import java.util.List;

public class Bplustree {

    final static int MAX_CHILD = 12;
    final private static int MAX_KEYS = MAX_CHILD - 1;
    final private static int HALF_MAX_CHILD = ((MAX_CHILD + 1) / 2);

    // Node
    public static abstract class Node {
        int serial;
        int nkeys;
        String[] keys;

        int  isKeyExist(String k){ //　ノードにキーkがあるときはそのインデックス、ないときは-1を返す
            for(int i = 0; i < this.nkeys; i++){
                if(this.keys[i].equals(k)){
                    return i;
                }
                else if(this.keys[i].compareTo(k) > 0){
                    return -1;
                }
            }
            return -1;
        }

        int keyIndex(String k){ //　キーkが入るべきindexを返す
            int i;
            for(i = 0; i < this.nkeys; i++){
                int cmp = k.compareTo(this.keys[i]); 
                if(cmp < 0){
                    break;
                } // k < keys[i]
            }
            return i;
        }

        abstract public SplitRequest insert(String k, String v);
        abstract public String get(String k);
        abstract public int getrange(String startKey, String[]vals, int startIndex, int n);
        abstract public boolean deleteWithNoRebalance(String k);
        abstract public boolean delete(String k);
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
        Node[] child;

        // コンストラクタ
        private InteriorNode() {
            this.serial = serialNumber++;
            this.nkeys = 0;
            this.keys = new String[MAX_KEYS + 1];
            this.child = new Node[MAX_CHILD + 1];
        }

        // internalノードへのキーkの挿入
        public SplitRequest insert(String k, String v) {
            int ki = this.keyIndex(k);
            SplitRequest req = this.child[ki].insert(k,v); // 再帰
            if(req == null){ // 何もしない
                return null;
            } else { // 子が分割→SplitRequest
                int i;
                String insertedKey = req.borderKey;
                Node lchild = req.left;
                Node rchild = req.right;
                for(i = nkeys; i > 0; i--){ // iはnkeysから1まで

                    int cmp = insertedKey.compareTo(this.keys[i-1]);
                    if(cmp < 0){ // k < keys[i-1]
                        this.keys[i] = this.keys[i-1];
                        this.child[i+1] = this.child[i];
                    } else { // k > keys[i]
                        this.keys[i] = insertedKey;
                        this.child[i+1] = rchild;
                        this.child[i] = lchild;
                        this.nkeys++;
                        break;
                    }
                }
                if(i == 0){
                    this.keys[0] = insertedKey; // 左端に追加
                    this.child[1] = rchild;
                    this.child[0] = lchild;
                    this.nkeys++;
                }
            }
            if(this.nkeys > MAX_KEYS){ // ノードが満杯のとき、分割が発生
                    return this.split(); // 分割
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
                r.child[i - borderIndex - 1] = l.child[i];
                l.child[i] = null;
            }
            r.child[MAX_CHILD - borderIndex - 1] = l.child[MAX_CHILD];
            l.child[MAX_CHILD] = null;
            String borderKey = l.keys[borderIndex];
            l.keys[borderIndex] = null;
            l.nkeys = borderIndex;
            r.nkeys = MAX_KEYS - borderIndex;
            return new SplitRequest(borderKey, l, r);
        }

        public boolean delete(String k){
            int ki = this.keyIndex(k);
            boolean req = this.child[ki].delete(k); // 再帰
            if (req == false){ // 子どもが十分な数のキーを持つ
                return false;
            } else { // 子どもが十分な数のキーを持たない
                if(ki == nkeys) {return rebalance(ki-1);}
                else {return rebalance(ki);}
            }
        }

        private boolean rebalance(int x) {
            if(child[x] instanceof LeafNode){
                LeafNode a = (LeafNode)child[x];
                LeafNode b = (LeafNode)child[x+1];
                int an = a.nkeys;
                int bn = b.nkeys;
                if (an + bn <= MAX_KEYS) { // 部分木aとbを併合
                    for (int i = 0; i < bn; i++) {
                        a.keys[i+an] = b.keys[i];
                        b.keys[i] = null;
                        a.data[i+an] = b.data[i];
                        b.data[i] = null;
                    }
                    a.nkeys += bn;
                    if(b.next != null){
                        a.next = b.next;
                        b.next.prev = a; // リーフノード同士のポインタを修正
                    }
                    b = null;
                    for(int i = x+1; i < this.nkeys; i++){ // bが無くなったので左詰め
                        this.keys[i-1] = this.keys[i];
                        this.child[i] = this.child[i+1];
                    }
                    this.keys[this.nkeys-1] = null; // 右端のキーと子削除
                    this.child[this.nkeys] = null;
                    this.nkeys--;
                    if(this.nkeys < HALF_MAX_CHILD - 1){return true;} // 下限を下回ったら親に知らせる
                } else { // 多い方から少ない方に1個分ける
                    if (an >= HALF_MAX_CHILD) { // bn = HALF_MAX_CHILD-2 部分木aから部分木bへ1つと移動する
                        for(int i = bn-1; i >= 0; i--){ // iはbn-1から0まで
                            b.keys[i+1] = b.keys[i];
                            b.data[i+1] = b.data[i];
                        }
                        b.keys[0] = a.keys[an-1];
                        b.data[0] = a.data[an-1];
                        this.keys[x] = b.keys[0];
                        a.keys[an-1] = null;
                        a.data[an-1] = null;
                        a.nkeys--;
                        b.nkeys++;
                    } else { // an = HALF_MAX_CHILD-2 部分木bから部分木aへと移動する
                        a.keys[an] = b.keys[0];
                        a.data[an] = b.data[0];
                        for(int i = 0; i < bn-1; i++){ // iはbnから0まで
                            b.keys[i] = b.keys[i+1];
                            b.data[i] = b.data[i+1];
                        }
                        this.keys[x] = b.keys[0];
                        b.keys[bn-1] = null;
                        b.data[bn-1] = null;
                        a.nkeys++;
                        b.nkeys--;
                    }
                }
                return false;
            } else {
                InteriorNode a = (InteriorNode)child[x];
                InteriorNode b = (InteriorNode)child[x+1];
                int an = a.nkeys;
                int bn = b.nkeys;
                if (an + bn <= MAX_KEYS) { // 部分木aとbを併合
                    a.keys[an] = this.keys[x];
                    a.child[1+an] = b.child[0];
                    for (int i = 0; i < bn; i++) {
                        a.keys[1+an+i] = b.keys[i];
                        b.keys[i] = null;
                        a.child[2+an+i] = b.child[i+1];
                        b.child[i+1] = null;
                    }
                    a.nkeys += bn+1;
                    b = null;
                    for(int i = x+1; i < this.nkeys; i++){ // bが無くなったので左詰め
                        this.keys[i-1] = this.keys[i];
                        this.child[i] = this.child[i+1];
                    }
                    this.keys[this.nkeys-1] = null; // 右端のキーと子削除
                    this.child[this.nkeys] = null;
                    this.nkeys--;
                    if(this.nkeys < HALF_MAX_CHILD - 1){return true;} // 下限を下回ったら親に知らせる
                } else { // 多い方から少ない方に1個分ける
                    if (an >= HALF_MAX_CHILD) { // bn = HALF_MAX_CHILD-2 部分木aから部分木bへと1つ移動する
                        for(int i = bn-1; i >= 0; i--){ // iはbn-1から0まで
                            b.keys[i+1] = b.keys[i];
                            b.child[i+2] = b.child[i+1];
                        }
                        b.child[1] = b.child[0];
                        b.keys[0] = a.keys[an-1];
                        b.child[0] = a.child[an];
                        this.keys[x] = a.keys[an-1];
                        a.keys[an-1] = null;
                        a.child[an] = null;
                        a.nkeys--;
                        b.nkeys++;
                    } else { // an = HALF_MAX_CHILD-2 部分木bから部分木aへと移動する
                        a.keys[an] = this.keys[x];
                        a.child[an+1] = b.child[0];
                        this.keys[x] = b.keys[0];
                        for(int i = 0; i < bn-1; i++){ // iはbnから0まで
                            b.keys[i] = b.keys[i+1];
                            b.child[i] = b.child[i+1];
                        }
                        b.child[bn] = b.child[bn+1];
                        this.keys[x] = b.keys[0];
                        b.keys[bn-1] = null;
                        b.child[bn] = null;
                        a.nkeys++;
                        b.nkeys--;
                    }
                }
                return false;
            }
        }

        // 検索:適切な位置の子をたどる
        public String get(String k){
            int ki = this.keyIndex(k);
            return this.child[ki].get(k);
        }

        // 範囲検索:適切な位置の子をたどる
        public int getrange(String startKey, String[] vals, int startIndex, int n){
            int ki = this.keyIndex(startKey);
            return this.child[ki].getrange(startKey, vals, startIndex, n);
        }

        // 削除:適切な位置の子をたどる
        public boolean deleteWithNoRebalance(String k){
            int ki = this.keyIndex(k);
            boolean req = this.child[ki].deleteWithNoRebalance(k); // 再帰
            if (req == false){ // 子どもが消えない→そのまま
                return false;
            }
            else{ // 子どもが消える
                if(this.nkeys > 0){ // キーが存在
                    if(ki > 0){ // 消えたのが左端じゃない
                        for(int i = ki; i < this.nkeys; i++){ // 左詰め
                            this.keys[i-1] = this.keys[i];
                            this.child[i] = this.child[i+1];
                        }
                        this.keys[this.nkeys-1] = null; // 右端のキーと子削除
                        this.child[this.nkeys] = null;
                        this.nkeys--;
                    } else { // 消えたのが左端
                        for(int i = ki; i < this.nkeys - 1; i++){ // 左詰め
                            this.keys[i] = this.keys[i+1];
                            this.child[i] = this.child[i+1];
                        }
                        this.child[this.nkeys-1] = this.child[this.nkeys];
                        this.keys[this.nkeys-1] = null; // 右端のキーと子削除
                        this.child[this.nkeys] = null;
                        this.nkeys--;
                    }
                } else { // キーが存在しない 
                    return true; // 子が一つもなくなったらDeleteRequestを返す
                }
            }
            return false; // 子どもが消えてないとき、または削除後のnkeysが1以上のとき、そのまま終了
        }

    }

    

    // leaf node
    public static class LeafNode extends Node {

        // データ
        String[] data;
        // 左隣ノード
        LeafNode prev;
        // 右隣ノード
        LeafNode next;

        // コンストラクタ(空のLeafNode)
        public LeafNode() {
            this.serial = serialNumber++;
            nkeys = 0;
            this.keys = new String[MAX_KEYS + 1];
            this.data = new String[MAX_KEYS + 1];
        }
        // コンストラクタ(要素が一つ入ったLeafNode)
        public LeafNode(String key, String x) {
            this.serial = serialNumber++;
            this.keys = new String[MAX_KEYS + 1];
            this.data = new String[MAX_KEYS + 1];
            this.keys[0] = key; 
            this.data[0] = x;
            this.nkeys = 1; 
        }

        // leafノードへのキーk、データxの挿入
        public SplitRequest insert(String k, String v) {
            int ki = this.isKeyExist(k);
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
                    }  else { // k > keys[i-1]
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
            r.next = l.next;
            if(r.next != null){
                r.next.prev = r;
            }
            l.next = r;
            r.prev = l;

            return new SplitRequest(borderKey, l, r);
        }

        // 削除
        public boolean delete(String k){
            int ki = this.isKeyExist(k);
            if (ki >= 0){ // key(k)がもうある(ki番目に一致)とき、削除
                for(int i = ki; i < nkeys - 1; i++){ // 左詰め
                    keys[i] = keys[i+1];
                    data[i] = data[i+1];
                }
                keys[nkeys-1] = null; // 右端のキーと値削除
                data[nkeys-1] = null;
                nkeys--;
                // System.out.println("the key " + k + " is deleted");
                if(nkeys <= HALF_MAX_CHILD - 2) {return true;} // キーが足りないとき、親にリバランスを頼む
            } else { // key(k)がまだない場合、何もしない
                // System.out.println("The key " + k + " is already deleted");
            }
            return false;
        }

        // 検索
        public String get(String k){
            int ki = this.isKeyExist(k);
            if (ki < 0){ // キーkが無い
                return null;
            }
            return this.data[ki];
        }

        // 範囲検索:開始
        public int getrange(String startKey, String[] vals, int startIndex, int n){
            int ki; // 開始インデックス
            for(ki = 0; ki < this.nkeys; ki++){
                int cmp = startKey.compareTo(this.keys[ki]); 
                if(cmp <= 0){
                    break;
                } // k < keys[i]
            }
            return getrangeContinue(ki, vals, startIndex, n);
        }

        // 範囲検索:処理(this.data[ki]からmin(nkeys-ki,n)個をvals[startIndex]~に格納)
        public int getrangeContinue(int ki, String[] vals, int startIndex, int n){
            int c = Math.min(nkeys-ki, n); // 読み取る値の数
            for(int i =  0; i < c; i++){
                    vals[startIndex+i] = data[ki+i]; 
            }
            if(n > c && next != null){
                return next.getrangeContinue(0, vals, startIndex+c, n-c) + c;
            }
            return c;
        }

        // 削除(リバランスしない)
        public boolean deleteWithNoRebalance(String k){
            int ki = this.isKeyExist(k);
            if (ki >= 0){ // key(k)がもうある(ki番目に一致)とき、削除
                for(int i = ki; i < this.nkeys - 1; i++){ // 左詰め
                    this.keys[i] = this.keys[i+1];
                    this.data[i] = this.data[i+1];
                }
                this.keys[this.nkeys-1] = null; // 右端のキーと値削除
                this.data[this.nkeys-1] = null;
                this.nkeys--;
                if(nkeys == 0){ // キーが一つもなくなったらノードを削除
                    if(this.next != null && this.prev != null){
                        this.prev.next = this.next;
                        this.next.prev = this.prev; // リーフノード同士のポインタを修正
                    }
                    else if(this.next != null){ // prev = null
                        this.next.prev = null;
                    }
                    else if(this.prev != null){ // next = null
                        this.prev.next = null;
                    }
                    return true; // 親に知らせる
                }
                return false; // nkeysが1以上のとき、そのまま終了
            }
            else{ // key(k)がまだない場合、何もしない
                return false;
            }
        }
    }


    // 挿入
    void insert(String k, String v){ // tree
        this.root.insert(k, v);
    }

    // ノード番号
    private static int serialNumber = 0;

    // Bplustreeの根
    private Node root;

    // コンストラクタ
    public Bplustree() {
        this.root = null;
    }

    // get(値を返す)
    public String get(String k){
        if (this.root == null){
            return null;
        }
        String val = this.root.get(k);
        return val;
    }

    // 挿入
    public void put(String k, String x){
        if (root == null) {
            root = new LeafNode(k,x);
        } else {
            SplitRequest req = root.insert(k,x);
            if (req == null) {
                // 何もしない
            } else {
                // 分割する
                InteriorNode newRoot = new InteriorNode();
                newRoot.keys[0] = req.borderKey;
                newRoot.child[0] = req.left;
                newRoot.child[1] = req.right;
                newRoot.nkeys = 1;
                root = newRoot;
            }
        }
    }

    // 削除・・・リバランスしない(Masstree用)
    public void deleteWithNoRebalance(String key){
        if (root == null) {
            // System.out.println("the tree is empty");
        }
        else{
            boolean req = this.root.deleteWithNoRebalance(key);
            if (req == false){ // rootからnullが返ってきたとき、何もしない

            } else { // rootがなくなったとき、木が空になる
                root = null;
            }
        }
    }

    // 削除
    public void delete(String key){
        if (root == null) {
        }
        else{
            boolean req = root.delete(key);
            if(req == true){
                if(root.nkeys >= 1){ // 何もしない
                } else {
                    if(root instanceof LeafNode){ // 削除したキーが最後の一つだったとき
                        root = null;
                    }
                    else{root = ((InteriorNode)root).child[0];} // 木の高さが一段減るs
                }
            }
        }
    }

    //範囲検索
    public List<String> getrange(String startKey, int n){
        if (this.root == null){
        }
        String[] vals = new String[n];
        int nfound = this.root.getrange(startKey,vals,0,n);
        ArrayList<String> l = new ArrayList<String>(nfound);
        for(int i = 0; i < nfound; i++){
            l.add(vals[i]);
        }
        return l;
    }

    // // 可視化用dotファイル用
    // private static String makedot(Node t){ 
    //     String text = "";
    //     if(t != null){
    //         if(t instanceof LeafNode){
    //             text += "node" + t.serial + "[label = \"";
    //             for(int i = 0; i < t.nkeys - 1; i++){
    //                 text += "<f" + i + "> "+ t.keys[i] + "|";
    //             }
    //             text += "<f" + t.nkeys + "> "+ t.keys[t.nkeys - 1] + "\"];\n";
    //         }
    //         if(t instanceof InteriorNode){
    //             text += "node" + t.serial + "[label = \"";
    //             for(int i = 0; i < t.nkeys; i++){
    //                 text += "<f" + i + "> " + "|" + t.keys[i] + "|";
    //             }
    //             text += "<f" + t.nkeys + ">\"];\n";
    //             for(int i = 0; i < t.nkeys + 1; i++){
    //                 text += makedot(((InteriorNode)t).child[i]);
    //                 text += "\"node" + t.serial + "\":f" + i + " -> \"node" + ((InteriorNode)t).child[i].serial + "\"\n"; 
    //             }
    //         }
    //     }   
    //     return text;
    // }

    // // 可視化用dotファイル出力
    // public void makeDotFile(){ 
    //     try{
    //         FileWriter fw = new FileWriter("BPTshow.dot");
    //         fw.write("digraph G {\n  node [shape = record,height=.1];\n");
    //         fw.write(makedot(this.root));
    //         fw.write("}");
    //         fw.close();
    //     } catch (IOException ex){
    //         ex.printStackTrace();
    //     }
    // }

    public static void main(String[] args){
    }
} 