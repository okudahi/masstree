import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;
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
        MassTreeNode.LayerOrDatum val = this.rootTree.get(key, 0);
        if(val == null) {return null;}
        else {return ((MassTreeNode.Datum)val).getData();}
    }

    // 挿入
    public void put(String k, String x){
        this.rootTree.insert(k, x, 0);
    }

    // 削除
    public void delete(String k){
        this.rootTree.delete(k);
    }

    // 範囲検索
    public List<String> getrange(String k, Integer n){
        return this.rootTree.getrange(k, n);
    }

    public static class MassTreeNode {

        final static int MAX_CHILD = 12;
        final static int MAX_KEYS = MAX_CHILD - 1;
        // split するとき child の数は MAX_CHILD + 1。奇数だった時に右の子が多くなるように HALF_MAX_CHILD = (MAX_CHILD + 1) / 2
        final private static int HALF_MAX_CHILD = ((MAX_CHILD + 1) / 2);
        final static int LEN_KEYSLICE = 8;
    
        
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
            int keyIndex(String k, int sliceIndex){ 
                int i;
                for(i = 0; i < this.nkeys; i++){
                    int cmp = compKey(k, keys[i], sliceIndex);
                    if(cmp <= 0) return i;
                }
                return i;
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

            int compKey(String k, String keysi, int sliceIndex){
                for(int j = 0; j < Math.min(LEN_KEYSLICE, k.length()-sliceIndex); j++){
                    int cmp = k.charAt(j+sliceIndex) - keysi.charAt(j);
                    if (cmp != 0)
                        return cmp; // cmp > 0 => keyslice > keysi
                }
                return 0; // キーが一致
            }
    
            abstract public SplitRequest insert(String k, String v, int sliceIndex);
            abstract public LayerOrDatum get(String k, int sliceIndex);
            abstract public int getrange(String startKeySlice, String suffix, String[] vals, int startIndex, int n);
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
        // data[]に入る型
        public interface LayerOrDatum {
            String getSuffix();
        }
    
        // nextLayer
        public static class Layer implements LayerOrDatum {
            MassTreeNode nextLayer;
            public String getSuffix() { return ""; }
            public MassTreeNode getNextLayer() { return nextLayer; }
    
            Layer(){
                this.nextLayer = new MassTreeNode();
            }
        }
    
        // {data, suffix}
        public static class Datum implements LayerOrDatum {
            String suffix;
            String value;
            public String getSuffix() { return suffix; }
            public String getData() { return value; }
    
            Datum(String val, String suf){
                this.suffix = suf;
                this.value = val;
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
            public SplitRequest insert(String k, String v, int sliceIndex) {
                int ki = keyIndex(k,sliceIndex);
                SplitRequest req = this.child[ki].insert(k,v,sliceIndex); // 再帰
                if(req == null){ // 何もしない
                    return null;
                } else { // 子が分割→SplitRequest
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
            public LayerOrDatum get(String k, int sliceIndex){
                int ki = this.keyIndex(k, sliceIndex);
                return this.child[ki].get(k, sliceIndex);
            }

    
            // 範囲検索:適切な位置の子をたどる
            public int getrange(String startKeySlice, String suffix, String[] vals, int startIndex, int n){
                int ki = this.keyIndex(startKeySlice);
                return this.child[ki].getrange(startKeySlice, suffix, vals, startIndex, n);
            }
    
            // 削除:適切な位置の子をたどる
            public boolean delete(String k, String suf){
                int ki = this.keyIndex(k);
                boolean req = this.child[ki].delete(k, suf); // 再帰
                if (req == false){ // 子どもが消えない→そのまま
                    return false;
                } else { // 子どもが消える
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
    
        
    
        // Border node
        public static class BorderNode extends Node {
    
            // データ
            LayerOrDatum[] data;
            // 左隣ノード
            BorderNode prev;
            // 右隣ノード
            BorderNode next;
    
    
    
            // コンストラクタ(空のBorderNode)
            public BorderNode() {
                this.serial = serialNumber++;
                nkeys = 0;
                this.keys = new String[MAX_KEYS + 1];
                this.data = new LayerOrDatum[MAX_KEYS + 1];
            }
            // コンストラクタ(要素が一つ入ったBorderNode)
            public BorderNode(String key, String x) {
                this.serial = serialNumber++;
                this.keys = new String[MAX_KEYS + 1];
                this.data = new LayerOrDatum[MAX_KEYS + 1];
                this.keys[0] = key.substring(0, Math.min(key.length(), LEN_KEYSLICE)); 
                if(key.length() <= LEN_KEYSLICE) {this.data[0] = new Datum(x,"");}
                else{this.data[0] = new Datum(x, key.substring(LEN_KEYSLICE));}
                this.nkeys = 1;
            }
    
            // Borderノードへのキーk、データx、接尾辞sufの挿入
            public SplitRequest insert(String k, String v, int sliceIndex) {
                String keyslice = k.substring(sliceIndex, Math.min(k.length(), sliceIndex+LEN_KEYSLICE));
                int ki = isKeyExist(keyslice);
                if(ki >= 0){ // keysliceがある場合
                    LayerOrDatum val = data[ki];
                    if(val instanceof Layer){
                        ((Layer)val).getNextLayer().insert(k, v, sliceIndex+LEN_KEYSLICE);
                    } else { // val instanceof Datum
                        String suf = k.substring(Math.min(k.length(), sliceIndex+LEN_KEYSLICE));
                        if(((Datum)val).getSuffix().equals(suf)){ // keyが完全に一致→上書き
                            ((Datum)val).value = v;
                        }
                        else{ // suffixが違う→nextlayer作成
                            String suffix0 = ((Datum)val).getSuffix(); // 元々あったsuffix
                            String val0 = ((Datum)val).value; // 元々あったvalue
                            data[ki] = new Layer();
                            ((Layer)data[ki]).nextLayer.insert(suffix0, val0, 0);
                            ((Layer)data[ki]).nextLayer.insert(k, v, sliceIndex+LEN_KEYSLICE);
                        }
                    }
                    return null;
                }
                else{
                    // keysliceがまだない場合、このB+Treeに挿入
                    int i;
                    String suf = k.substring(Math.min(k.length(), sliceIndex+LEN_KEYSLICE));
                    Datum newChild = new Datum(v, suf);
                    for(i = nkeys; i > 0; i--){ // iはnkeysから1まで
                        int cmp = keyslice.compareTo(this.keys[i-1]);
                        if(cmp < 0){ // k < keys[i-1]
                            this.keys[i] = this.keys[i-1]; // 右にずらす
                            this.data[i] = this.data[i-1];
                        } else { // k > keys[i-1]
                            this.keys[i] = keyslice;
                            this.data[i] = newChild;
                            this.nkeys++; // 空いたところに挿入
                            break;
                        }
                    }
                    if(i == 0){ // k < keys[0]
                        this.keys[0] = keyslice; // 左端に挿入
                        this.data[0] = newChild;
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
                int borderIndex = HALF_MAX_CHILD; // key[j]を親ノードに挿入、他を分割
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
            public LayerOrDatum get(String k, int sliceIndex){
                String keyslice = k.substring(sliceIndex, Math.min(k.length(), sliceIndex + LEN_KEYSLICE));
                int ki = this.isKeyExist(keyslice);
                if (ki < 0){ // キーkが無い
                    return null;
                }
                return this.data[ki];
            }
    
        // 範囲検索:開始位置の検索
        public int getrange(String startKeySlice, String suffix, String[] vals, int startIndex, int n){
            int ki; // 開始インデックス
            for(ki = 0; ki < this.nkeys; ki++){
                int cmp = startKeySlice.compareTo(this.keys[ki]);
                if(cmp < 0){ // k < keys[i]
                    return getrangeContinue(ki, vals, startIndex, n);
                }
                else if(cmp == 0){
                    return getrangeContinueEqual(ki, suffix, vals, startIndex, n);
                }
            }
            return getrangeContinue(ki, vals, startIndex, n);
        }

        // 範囲検索:処理(キーが一致したとき、suffixの比較が必要)
        public int getrangeContinueEqual(int ki, String suffix, String[] vals, int startIndex, int n){
            int count = 0; // 読み取った値の数
            int i = ki;
            while(i < nkeys && count < n){
                if(data[i] instanceof Datum){
                    if(i == ki){ // 最初だけsuffixを比較
                        if(suffix.compareTo(data[i].getSuffix()) <= 0){ // suffix <= data[i].suffix
                            vals[startIndex+count] = ((Datum)data[i]).value; // data[ki+i]
                            count++;
                        }
                    } else {
                        vals[startIndex+count] = ((Datum)data[i]).value; // data[ki+i]
                        count++;
                    }
                }
                else{ // data[ki+i] instanceof Layer
                    if(i == ki){ // 最初だけsuffixを比較
                        count += ((Layer)data[i]).getNextLayer().root.getrange(suffix.substring(0, Math.min(suffix.length(), LEN_KEYSLICE)), suffix.substring(Math.min(suffix.length(), LEN_KEYSLICE)), vals, startIndex+count, n - count);
                    } else {
                        count += ((Layer)data[i]).getNextLayer().root.getrange("", "", vals, startIndex+count, n - count);
                    }
                }
                i++;
            }
            if(n > count && next != null){
                return next.getrangeContinue(0, vals, startIndex+count, n-count) + count;
            }
            return count;
        }

        // 範囲検索:処理(キーが一致しないとき、それより右側を無条件で追加)
        public int getrangeContinue(int ki, String[] vals, int startIndex, int n){
            int count = 0; // 読み取った値の数
            int i = ki;
            assert(nkeys > 0);
            while(i < nkeys && count < n){
                if(data[i] instanceof Datum){
                    vals[startIndex+count] = ((Datum)data[i]).value; // data[ki+i]
                    count++;
                } else { // data[ki+i] instanceof Layer
                    count += ((Layer)data[i]).getNextLayer().root.getrange("", "", vals, startIndex+count, n - count);
                }
                i++;
            }
            if(n > count && next != null){
                // System.out.println(count);
                return next.getrangeContinue(0, vals, startIndex+count, n-count) + count;
            }
            return count;
        }

    
            // 削除
            public boolean delete(String k, String suf){
                int ki = this.isKeyExist(k);
                if (ki >= 0){ // key(k)がもうある(ki番目に一致)とき、削除
                    LayerOrDatum val = this.data[ki];
                    if(val instanceof Layer){
                        boolean req = ((Layer)val).getNextLayer().delete(suf);
                        if(req == true){ // nextlayerが完全に空になった場合、このキーを削除
                            for(int i = ki; i < this.nkeys - 1; i++){ // 左詰め
                                this.keys[i] = this.keys[i+1];
                                this.data[i] = this.data[i+1];
                            }
                            this.keys[this.nkeys-1] = null; // 右端のキーと値削除
                            this.data[this.nkeys-1] = null;
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
                    else{ // val instanceof Datum
                        if(((Datum)val).getSuffix().equals(suf)){ // suffixが一致した場合、削除
                            for(int i = ki; i < this.nkeys - 1; i++){ // 左詰め
                                this.keys[i] = this.keys[i+1];
                                this.data[i] = this.data[i+1];
                            }
                            this.keys[this.nkeys-1] = null; // 右端のキーと値削除
                            this.data[this.nkeys-1] = null;
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
                        } else {return false;} // suffixが不一致のとき、何もしない
                    }
                } else {return false;} // key(k)がまだない場合、何もしない
            }
        }
    
    
        // MassTreeNodeへの挿入
        void insert(String key, String value, int sliceIndex){ // tree
            if(this.root == null){
                root = new BorderNode(key,value);
                return;
            }
            SplitRequest req = this.root.insert(key, value, sliceIndex);
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
        Node root;
    
        // コンストラクタ
        public MassTreeNode() {
            this.root = null;
        }
    
        // get(MassTreeValを返す)
        LayerOrDatum get(String key, int sliceIndex){
            if(this.root == null){return null;}
            LayerOrDatum val = this.root.get(key, sliceIndex);
            if(val == null){return null;}
            else if(val instanceof Layer){
                return ((Layer) val).getNextLayer().get(key, sliceIndex + LEN_KEYSLICE); // 次の8文字で検索
            } else { // val instanceof Datum
                if(val.getSuffix().equals(key.substring(Math.min(sliceIndex + LEN_KEYSLICE, key.length())))){ // キーが一致
                    return val;
                } else { // キーがない
                    return null;
                }
            }
        }
    
        // 削除・・・リバランスしない
        public boolean delete(String key){
            if (root == null) {
                return false;
            } else {
                boolean req = this.root.delete(key.substring(0, Math.min(key.length(), LEN_KEYSLICE)), key.substring(Math.min(key.length(), LEN_KEYSLICE)));
                if (req == false){ // rootからnullが返ってきたとき、何もしない
                    return false;
                } else { // rootがなくなったとき、木が空になる
                    root = null;
                    return true;
                }
            }
        }
    
        // 可視化用dotファイル用
        public static String makedot(Node t){ 
            String text = "";
            if(t != null){
                if(t instanceof BorderNode){
                    boolean[] nextLayerExist = new boolean[t.nkeys];
                    text += "node" + t.serial + "[label = \"";
                    for(int i = 0; i < t.nkeys - 1; i++){
                        if(((BorderNode)t).data[i] instanceof Layer){
                            text += "<f" + i + "> "+ t.keys[i] + "|";
                            if(((Layer)((BorderNode)t).data[i]).nextLayer.root != null){
                                nextLayerExist[i] = true;
                            }
                        } else {
                            text += "<f" + i + "> "+ t.keys[i] + ((Datum)((BorderNode)t).data[i]).suffix + "|";
                        }
                    }
                    if(((BorderNode)t).data[t.nkeys - 1] instanceof Layer){
                        text += "<f" + (t.nkeys - 1) + "> "+ t.keys[t.nkeys - 1] + "\"];\n";
                        if(((Layer)((BorderNode)t).data[t.nkeys - 1]).nextLayer.root != null){
                            nextLayerExist[t.nkeys - 1] = true;
                        }
                    } else {
                        text += "<f" + (t.nkeys - 1) + "> "+ t.keys[t.nkeys - 1] + ((Datum)((BorderNode)t).data[t.nkeys - 1]).suffix + "\"];\n";
                    }
                    for(int i = 0; i < t.nkeys; i++){
                        if(nextLayerExist[i] == true){
                            text += makedot(((Layer)((BorderNode)t).data[i]).nextLayer.root);
                            text += "\"node" + t.serial + "\":f" + i + " -> \"node" + ((Layer)((BorderNode)t).data[i]).nextLayer.root.serial + "\"[color = red];\n"; 
                        }
                    }
                }
                if(t instanceof InteriorNode){
                    text += "node" + t.serial + "[label = \"";
                    for(int i = 0; i < t.nkeys; i++){
                        text += "<f" + i + "> " + "|" + t.keys[i] + "|";
                    }
                    text += "<f" + t.nkeys + ">\"];\n";
                    for(int i = 0; i < t.nkeys + 1; i++){
                        text += makedot(((InteriorNode)t).child[i]);
                        text += "\"node" + t.serial + "\":f" + i + " -> \"node" + ((InteriorNode)t).child[i].serial + "\"\n"; 
                    }
                }
            }   
            return text;
        }

        // 可視化用dotファイル出力
        public void makeDotFile(){
            try{
                FileWriter fw = new FileWriter("MassTreeShow.dot");
                fw.write("digraph G {\n  node [shape = record,height=.1];\n");
                fw.write(makedot(this.root));
                fw.write("}");
                fw.close();
            } catch (IOException ex){
                ex.printStackTrace();
            }
        }
    
        //範囲検索
        public List<String> getrange(String startKey, int n){
            if (this.root == null){
                return null;
            }
            String[] vals = new String[n];
            int nfound = this.root.getrange(startKey.substring(0,Math.min(startKey.length(), LEN_KEYSLICE)), startKey.substring(Math.min(startKey.length(), LEN_KEYSLICE)), vals,0,n);
            ArrayList<String> l = new ArrayList<String>(nfound);
            for(int i = 0; i < nfound; i++){
                l.add(vals[i]);
            }
            return l;
        }
    
    

    }

    static void eval(int len, String[] Keys, int numKeys, int[] IndexArray, int t){
        // initialize
        MassTree tree = new MassTree();
        for(int i = 0; i < 100000; i++){
            tree.put(Keys[i], " ");
        }
        long sumins = 0;
        long sumget = 0;
        long sumgetr = 0;
        long sumdel = 0;
        long startTime = System.currentTimeMillis();
        for(int i = 100000; i < numKeys + 100000; i++){
            tree.put(Keys[i], " ");
        }
        long Time = System.currentTimeMillis() - startTime;
        if(t > 0){sumins += Time;}
        System.out.println("Masstree #" + t + ": " + numKeys + "keys inserted/" + Time + " ms");

        startTime = System.currentTimeMillis();
        for(int i = 0; i < numKeys/2; i++){
            tree.get(Keys[IndexArray[i]]);
        }
        Time = System.currentTimeMillis() - startTime;
        if(t > 0){sumget += Time;}
        System.out.println("Masstree #" + t + ": " + numKeys/2 + "keys searched/" + Time + " ms");

        startTime = System.currentTimeMillis();
        for(int i = 0; i < numKeys/10; i++){
            tree.getrange(Keys[IndexArray[i]], 1000);
        }
        Time = System.currentTimeMillis() - startTime;
        if(t > 0){sumgetr += Time;}
        System.out.println("Masstree #" + t + ": " + numKeys/10 + "times rangesearched/" + Time + " ms");

        startTime = System.currentTimeMillis();
        for(int i = 0; i < numKeys/2; i++){
            tree.delete(Keys[IndexArray[i]]);
        }
        Time = System.currentTimeMillis() - startTime;
        if(t > 0){sumdel += Time;}
        System.out.println("Masstree #" + t + ": " + numKeys/2 + "keys deleted/" + Time + " ms");
    }

    public static void main(String[] args){

    }

}