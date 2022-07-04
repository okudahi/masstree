# Masstree疑似コード(挿入関連)
1. B+木への挿入
```
Layer.insert(String k, String v){ 
    if(this.root == null){
        root = new BorderNode(k, v);
        return;
    }
    keyslice = k[ : min(k.length(), LEN_KEYSLICE)];
    suf = k[min(k.length(), LEN_KEYSLICE) : ];
    SplitRequest req = this.root.insert(keyslice, v, suf);
    if (req == null) {} // 何もしない
    else { // rootを分割
        InteriorNode newRoot = new InteriorNode();
        newRoot.keys[0] = req.borderKey;
        newRoot.child[0] = req.left;
        newRoot.child[1] = req.right;
        newRoot.nkeys = 1;
        root = newRoot;
    }
}
```
2. 子が分割された際に親が受け取る返り値の型
```Java
class SplitRequest{ 
    String borderKey; // leftとrightを分ける値
    Node left; // 左のノード
    Node right; // 右のノード
    
    SplitRequest(String b, Node l, Node r){ 
        borderKey = b;
        left = l;
        right = r;
    }
}
```
3. keysliceが入るべきインデックス
```Java
int Node.indexOf(String keyslice){
    int ki;
    for(ki = 0; i < keys.length; i++) if(keyslice < keys[i]){break;}
    return ki;
}
```

4. 配列のki番目にobjectInsertedを挿入
```Java
var[].insertAt(int ki, var objectInserted){
    len = this.length;
    if(ki == len){this.pushback(objectInserted);)
    else{
        this.pushback(this[len - 1]);
        for(i = len - 1; i > ki; i--){
            this[i] = this[i - 1];
        }
        this[ki] = objectInserted;
    }
}
```
5. InteriorNode
```Java
class InteriorNode{
    String keys[]; // keyslice
    Node child[]; // InteriorNode or BorderNode
}
```
5.1 InteriorNodeへの挿入
```Java
SplitRequest InteriorNode.insert(String keyslice, String v, String suf) {
    ki = keys.indexOf(keyslice);
    SplitRequest req = child[ki].insert(keyslice,v,suf);
    if(req == null){} // 何もしない
    else{ // 子が分割→SplitRequest
        String insertedKey = req.borderKey;
        Node lchild = req.left;
        Node rchild = req.right;
        keys.insertAt(ki, keyslice);
        child[ki] = lchild;
        child.insertAt(ki+1, rchild);
    }
    if(keys.length > MAX_KEYS){ // ノードが満杯のとき、分割が発生
        return split(); // 分割
    }
    return null; // 満杯でないならnullを返す
}
```
5.2 InteriorNodeの分割
```Java
SplitRequest InteriorNode.split() {
    borderIndex = ((MAX_CHILD + 1) / 2) - 1; 
    InteriorNode l = this;
    InteriorNode r = new InteriorNode;
    String borderKey = l.keys[borderIndex];
    // borderIndexより右と左でノードを分割
    r.keys[ : MAX_KEYS - borderIndex] = l.keys[borderIndex + 1 : MAX_KEYS + 1];
    l.keys[borderIndex : MAX_KEYS + 1] = null;
    r.child[ : MAX_KEYS - borderIndex + 1] = l.child[borderIndex + 1 : MAX_KEYS + 2];
    l.child[borderIndex + 1 : MAX_KEYS + 2] = null;
    return new SplitRequest(borderKey, l, r);
}
```
6. BorderNode
```Java
class BorderNode{
    String keys[]; // keyslice
    MassTreeVal data[]; // next_layer or datum{value, suffix}
    BorderNode prev; // 削除時のnext更新用
    BorderNode next; // 範囲検索用
}
```
6.1. BorderNodeへの挿入
```Java
SplitRequest BorderNode.insert(String keyslice, String v, String suf) {
    if(keyslice in keys){ 
        ki = keys.indexOf(keyslice);
        layerOrDatum = data[ki];
        if(isLayer(layerOrDatum)){ // Layerへのポインタ
            ((Layer)layerOrDatum).insert(suf, v);
        } else{ // Datum {value, suffix}の組
            if(layerOrDatum.suffix == suf){ // keyが完全に一致→上書き
                ((Datum)layerOrDatum).value = v;
            } else{ // suffixが違う
                l = new Layer;
                l.insert(((Datum)layerOrDatum).suffix, ((Datum)layerOrDatum).value);
                l.insert(suf, v);
                data[ki] = l;
            }
        }
    }
    else{// keysliceがまだない
        ki = keys.indexOf(keyslice);
        keys.insertAt(ki, keyslice);
        data.insertAt(ki, v);
        if(keys.length > MAX_KEYS){
            return split();
        }
    }
    return null;
}
```
6.2. BorderNodeの分割
```Java
SplitRequest BorderNode.split() {
    borderIndex = HALF_MAX_CHILD - 1;
    BorderNode l = this;
    BorderNode r = new BorderNode;
    // borderIndexより右と左でノードを分割
    r.keys[0 : MAX_KEYS - borderIndex + 1] = l.keys[borderIndex : MAX_KEYS + 1];
    l.keys[borderIndex : MAX_KEYS + 1] = null;
    r.data[0 : MAX_KEYS - borderIndex + 1] = l.data[borderIndex : MAX_KEYS + 1];
    l.data[borderIndex + 1 : MAX_KEYS + 1] = null;
    String borderKey = r.keys[0];
    r.next = l.next;
    r.prev = l;
    r.next.prev = r;
    l.next = r;
    return new SplitRequest(borderKey, l, r);
}
```
