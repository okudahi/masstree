import java.util.ArrayList;
import java.util.List;
import java.lang.Math;

public class MassTree {
    MassTreeNode rootTree;

    // ノード番号
    public static int serialNumber = 0;

    // コンストラクタ
    public MassTree() {
        this.rootTree = new MassTreeNode();
    }

    // 検索
    public String get(String key){
        MassTreeNode.MassTreeVal val = this.rootTree.get(key);
        if(val == null) {return null;}
        else {return ((MassTreeNode.SingleMassTreeVal)val).getData();} 
    }

    // 挿入
    public void put(String k, String x){
        this.rootTree.insert(k, x);
    }

    // 削除
    public void delete(String k){
        this.rootTree.delete(k);
    }

    // 範囲検索
    public Object getrange(String k, Integer n){
        return this.rootTree.getrange(k, n);
    }

    public static class MassTreeNode {

        final static int MAX_CHILD = 15;
        final static int MAX_KEYS = MAX_CHILD - 1;
        final private static int HALF_MAX_CHILD = ((MAX_CHILD + 1) / 2);
        final private static int LEN_KEYSLICE = 8;
    
        
        // Node
        public static abstract class Node {
            int serial;
            int nkeys;
            String[] keys;
    
            //　ノードにキーkがあるときはそのインデックス、ないときは-1を返す
            int  isKeyExist(String k){ 
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
            //　キーkが入るべきindexを返す
            int keyIndex(String k){ 
                int i;
                for(i = 0; i < this.nkeys; i++){
                    int cmp = k.compareTo(this.keys[i]); 
                    if(cmp < 0){
                        break;
                    } // k < keys[i]
                }
                return i;
            }
    
            abstract public SplitRequest insert(String k, String v, String suf);
            abstract public MassTreeVal get(String k);
            abstract public int getrange(String startKey, String[] vals, int startIndex, int n);
            abstract public boolean delete(String k, String suf);
        }
        
        // 分割の際に親にリクエストを送る
        public static class SplitRequest{ 
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
    
        public interface MassTreeVal {
            String getSuffix();
        }
    
        // nextLayer
        public static class NextLayerMassTreeVal implements MassTreeVal {
            MassTreeNode nextLayer;
            public String getSuffix() { return ""; }
            public MassTreeNode getNextLayer() { return nextLayer; }
    
            NextLayerMassTreeVal(){
                this.nextLayer = new MassTreeNode();
            }
        }
    
        // {data, suffix}
        public static class SingleMassTreeVal implements MassTreeVal {
            String suffix;
            String data;
            public String getSuffix() { return suffix; }
            public String getData() { return data; }
    
            SingleMassTreeVal(String val, String suf){
                this.suffix = suf;
                this.data = val;
            }
        }
    
        // interior node
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
            public SplitRequest insert(String k, String v, String suf) {
                int ki = this.keyIndex(k);
                SplitRequest req = this.child[ki].insert(k,v,suf); // 再帰
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
                        if(cmp < 0){ // k < keys[i-1] 右にずらす
                            this.keys[i] = this.keys[i-1];
                            this.child[i+1] = this.child[i];
                        } 
                        else{ // k > keys[i] 空いたところに挿入
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
    
            // 検索:適切な位置の子をたどる
            public MassTreeVal get(String k){
                int ki = this.keyIndex(k);
                return this.child[ki].get(k);
            }

    
            // 範囲検索:適切な位置の子をたどる
            public int getrange(String startKey, String[] vals, int startIndex, int n){
                int ki = this.keyIndex(startKey);
                return this.child[ki].getrange(startKey, vals, startIndex, n);
            }
    
            // 削除:適切な位置の子をたどる
            public boolean delete(String k, String suf){
                int ki = this.keyIndex(k);
                boolean req = this.child[ki].delete(k, suf); // 再帰
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
                        }
                        else{ // 消えたのが左端
                            for(int i = ki; i < this.nkeys - 1; i++){ // 左詰め
                                this.keys[i] = this.keys[i+1];
                                this.child[i] = this.child[i+1];
                            }
                            this.child[this.nkeys-1] = this.child[this.nkeys];
                            this.keys[this.nkeys-1] = null; // 右端のキーと子削除
                            this.child[this.nkeys] = null;
                            this.nkeys--;
                        }
                    }
                    else{ // キーが存在しない 
                        return true; // 子が一つもなくなったらDeleteRequestを返す
                    }
                }
                return false; // 子どもが消えてないとき、または削除後のnkeysが1以上のとき、そのまま終了
            }
    
        }
    
        
    
        // Border node
        public static class BorderNode extends Node {
    
            // データ
            MassTreeVal[] data;
            // 左隣ノード
            BorderNode prev;
            // 右隣ノード
            BorderNode next;
    
    
    
            // コンストラクタ(空のBorderNode)
            public BorderNode() {
                this.serial = serialNumber++;
                nkeys = 0;
                this.keys = new String[MAX_KEYS + 1];
                this.data = new MassTreeVal[MAX_KEYS + 1];
            }
            // コンストラクタ(要素が一つ入ったBorderNode)
            public BorderNode(String key, String x) {
                this.serial = serialNumber++;
                this.keys = new String[MAX_KEYS + 1];
                this.data = new MassTreeVal[MAX_KEYS + 1];
                this.keys[0] = key.substring(0, Math.min(key.length(), LEN_KEYSLICE)); 
                if(key.length() <= LEN_KEYSLICE) {this.data[0] = new SingleMassTreeVal(x,"");}
                else{this.data[0] = new SingleMassTreeVal(x, key.substring(LEN_KEYSLICE));}
                this.nkeys = 1;
            }
    
            // Borderノードへのキーk、データx、接尾辞sufの挿入
            public SplitRequest insert(String k, String v, String suf) {
                int ki = isKeyExist(k);
                if(ki >= 0){ // keysliceがある場合
                    MassTreeVal val = data[ki];
                    if(val instanceof NextLayerMassTreeVal){
                        ((NextLayerMassTreeVal)val).getNextLayer().insert(suf, v);
                    }
                    else{ // val instanceof SingleMassTreeVal
                        if(((SingleMassTreeVal)val).getSuffix().equals(suf)){ // keyが完全に一致→上書き
                            ((SingleMassTreeVal)val).data = v;
                        }
                        else{ // suffixが違う→nextlayer作成
                            String suffix0 = ((SingleMassTreeVal)val).getSuffix(); // 元々あったsuffix
                            String val0 = ((SingleMassTreeVal)val).data; // 元々あったvalue
                            data[ki] = new NextLayerMassTreeVal();
                            ((NextLayerMassTreeVal)data[ki]).nextLayer.insert(suffix0, val0);
                            ((NextLayerMassTreeVal)data[ki]).nextLayer.insert(suf, v);
                        }
                    }
                    return null;
                }
                else{
                    // keysliceがまだない場合、このB+Treeに挿入
                    int i;
                    for(i = nkeys; i > 0; i--){ // iはnkeysから1まで
                        int cmp = k.compareTo(this.keys[i-1]);
                        if(cmp < 0){ // k < keys[i-1]
                            this.keys[i] = this.keys[i-1]; // 右にずらす
                            this.data[i] = this.data[i-1];
                        } 
                        else{ // k > keys[i-1]
                            this.keys[i] = k;
                            this.data[i] = new SingleMassTreeVal(v, suf);
                            this.nkeys++; // 空いたところに挿入
                            break;
                        }
                    }
                    if(i == 0){ // k < keys[0]
                        this.keys[0] = k; // 左端に挿入
                        this.data[0] = new SingleMassTreeVal(v, suf);;
                        this.nkeys++;
                    }
                    if(this.nkeys > MAX_KEYS){
                        return this.split();
                    }
                    return null;
                }
                
            }
    
            // Borderノードでの分割
            SplitRequest split() {
                int borderIndex = HALF_MAX_CHILD - 1; // key[j]を親ノードに挿入、他を分割
                BorderNode l = this;
                BorderNode r = new BorderNode();
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
    
            // 検索
            public MassTreeVal get(String k){
                int ki = this.isKeyExist(k);
                if (ki < 0){ // キーkが無い
                    return null;
                }
                return this.data[ki];
            }
    
        // 範囲検索:開始位置の検索
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
                    vals[startIndex+i] = null; // data[ki+i]
            }
            if(n > c && next != null){
                return next.getrangeContinue(0, vals, startIndex+c, n-c) + c;
            }
            return c;
        }

    
            // 削除
            public boolean delete(String k, String suf){
                int ki = this.isKeyExist(k);
                if (ki >= 0){ // key(k)がもうある(ki番目に一致)とき、削除
                    MassTreeVal val = this.data[ki];
                    if(val instanceof NextLayerMassTreeVal){
                        boolean req = ((NextLayerMassTreeVal)val).getNextLayer().delete(suf);
                        if(req == true){ // nextlayerが完全に空になった場合、このキーを削除
                            for(int i = ki; i < this.nkeys - 1; i++){ // 左詰め
                                this.keys[i] = this.keys[i+1];
                                this.data[i] = this.data[i+1];
                            }
                            this.keys[this.nkeys] = null; // 右端のキーと値削除
                            this.data[this.nkeys] = null;
                            this.nkeys--;
                            if(nkeys == 0){ // キーが一つもなくなったらノードを削除
                                if(this.next != null && this.prev != null){ // リーフノード同士のポインタを修正
                                    this.prev.next = this.next;
                                    this.next.prev = this.prev;
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
                        return false; // nextlayerが空にならなかったとき、終了
                    }
                    else{ // val instanceof SingleMassTreeVal
                        if(((SingleMassTreeVal)val).getSuffix().equals(suf)){ // suffixが一致した場合、削除
                            for(int i = ki; i < this.nkeys - 1; i++){ // 左詰め
                                this.keys[i] = this.keys[i+1];
                                this.data[i] = this.data[i+1];
                            }
                            this.keys[this.nkeys] = null; // 右端のキーと値削除
                            this.data[this.nkeys] = null;
                            this.nkeys--;
                            if(nkeys == 0){ // キーが一つもなくなったらノードを削除
                                if(this.next != null && this.prev != null){ // リーフノード同士のポインタを修正
                                    this.prev.next = this.next;
                                    this.next.prev = this.prev;
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
                        else {return false;} // suffixが不一致のとき、何もしない
                    }
                }
                else {return false;} // key(k)がまだない場合、何もしない
            }
        }
    
    
        // MassTreeNodeへの挿入
        void insert(String key, String value){ // tree
            if(this.root == null){
                root = new BorderNode(key,value);
                return;
            }
            SplitRequest req = this.root.insert(key.substring(0, Math.min(key.length(), LEN_KEYSLICE)), value, key.substring(Math.min(key.length(), LEN_KEYSLICE)));
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
    
        // MassTreeNodeの根
        private Node root;
    
        // コンストラクタ
        public MassTreeNode() {
            this.root = null;
        }
    
        // get(MassTreeValを返す)
        MassTreeVal get(String key){
            if(this.root == null){return null;}
            String keyslice = key.substring(0, Math.min(LEN_KEYSLICE, key.length())); // 8文字で切る
            MassTreeVal val = this.root.get(keyslice);
            if(val == null){return null;}
            else if(val instanceof NextLayerMassTreeVal){
                return ((NextLayerMassTreeVal) val).getNextLayer().get(key.substring(LEN_KEYSLICE)); // 次の8文字で検索
            }
            else{ // val instanceof SingleMassTreeVal
                if(val.getSuffix().equals(key.substring(Math.min(LEN_KEYSLICE, key.length())))){ // キーが一致
                    return val;
                }
                else{ // キーがない
                    return null;
                }
            }
        }
    
        // 削除・・・リバランスしない
        public boolean delete(String key){
            if (root == null) {
                return false;
            }
            else{
                boolean req = this.root.delete(key.substring(0, Math.min(key.length(), LEN_KEYSLICE)), key.substring(Math.min(key.length(), LEN_KEYSLICE)));
                if (req == false){ // rootからnullが返ってきたとき、何もしない
                    return false;
                }
                else{ // rootがなくなったとき、木が空になる
                    root = null;
                    return true;
                }
            }
        }
    
        //範囲検索
        public List<String> getrange(String startKey, int n){
            if (this.root == null){
                return null;
            }
            String[] vals = new String[n];
            int nfound = this.root.getrange(startKey,vals,0,n);
            ArrayList<String> l = new ArrayList<String>(nfound);
            for(int i = 0; i < nfound; i++){
                l.add(vals[i]);
            }
            return l;
        }
    
    
        public static void main(String[] args){
        }
    } 

}