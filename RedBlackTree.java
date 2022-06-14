import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.io.BufferedReader;

public class RedBlackTree {

    private class Node{
        boolean color; // true:black, false:red
        String key;
        String value;
        Node lst = null; // 左の子
        Node rst = null; // 右の子

        Node(boolean color, String key, String value) {
            this.color = color;
            this.key = key;
            this.value = value;
        }
    }

    private Node root = null; // 根
    private boolean active;   // 修正中かを示すフラグ(true:修正中, false:安定)
    private Node submax;

    // 赤かチェック
    private boolean isR(Node n) { return n != null && !n.color; }

    // 黒かチェック
    private boolean isB(Node n) { return n != null && n.color; }

    // 左回転
    private Node rotateL(Node v) {
        Node u = v.rst, t2 = u.lst;
        u.lst = v; v.rst = t2;
        return u;
    }

    // 右回転
    private Node rotateR(Node u) {
        Node v = u.lst, t2 = v.rst;
        v.rst = u; u.lst = t2;
        return v;
    }

    // 左回転→右回転
    private Node rotateLR(Node t) {
        t.lst = rotateL(t.lst);
        return rotateR(t);
    }

    // 右回転→左回転
    private Node rotateRL(Node t) {
        t.rst = rotateR(t.rst);
        return rotateL(t);
    }

    // 挿入に伴う赤黒木の修正(4パターン)
    private Node balance(Node t) {
        if (!active) return t;
        else if (!isB(t)) return t; // 根が赤なら何もしない
        else if (isR(t.lst) && isR(t.lst.lst)) {
            // パターン LL
            t = rotateR(t);
            t.lst.color = true;
        }
        else if (isR(t.lst) && isR(t.lst.rst)) {
            // パターン LR
            t = rotateLR(t);
            t.lst.color = true;
        }
        else if (isR(t.rst) && isR(t.rst.lst)) {
            // パターン RL
            t = rotateRL(t);
            t.rst.color = true;
        }
        else if (isR(t.rst) && isR(t.rst.rst)) {
            // パターン RR
            t = rotateL(t);
            t.rst.color = true;
        }
        else {
            // 最下層のノードが赤でその親が黒の場合は修正の必要なし
            active = false;
        }
        return t;
    }

// ノードへの挿入
    private Node insert(Node t, String key, String x) {
        if (t == null) { 
            active = true; 
            return new Node(false, key, x); 
        }
        int cmp = key.compareTo(t.key);
        if (cmp < 0) {
            t.lst = insert(t.lst, key, x);
            return balance(t);
        }
        else if (cmp > 0) {
            t.rst = insert(t.rst, key, x);
            return balance(t);
        }
        else {
            t.value = x;
            return t;
        }
    }

    // 挿入
    public void put(String key, String x) {
        active = false;
        root = insert(root, key, x);
        root.color = true;
    }

    // 検索
    public String get(String key) {
        Node t = root;
        while (t != null) {
            int cmp = key.compareTo(t.key);
            if      (cmp < 0) t = t.lst;
            else if (cmp > 0) t = t.rst;
            else return t.value;
        }
        return null;
    }

    // 削除

    private Node delete(Node t, String key) {
        if (t == null) return null;
        int cmp = key.compareTo(t.key);
        if (cmp < 0) {
            t.lst = delete(t.lst, key);
            return balanceL(t);
        }
        else if (cmp > 0) {
            t.rst = delete(t.rst, key);
            return balanceR(t);
        }
        else if (t.lst != null) {// 内部ノードの削除
            t.lst = deleteMax(t.lst); // 左部分木の最大値のノードを削除
            t.key = submax.key; // 削除した最大値で置き換える
            t.value = submax.value;
            submax = null; // コピーしたら捨てる
            return balanceL(t);
        }
        else {
            // 最下層のノードの削除
            active = (t.color == true);
            return t.rst; // 右部分木を昇格させる
        }
    }

    // 部分木 t の最大値のノードを削除する
    private Node deleteMax(Node t) {
        if (t.rst != null) {
            t.rst = deleteMax(t.rst);
            return balanceR(t);
        }
        else {
            active = (t.color == true);
            submax = t;   // 最大値のノード
            return t.lst; // 左部分木を昇格させる
        }
    }

    // 赤黒木の修正(左の子ノードを削除したとき)
    private Node balanceL(Node t) {
        if (!active) return t;
        else if (isB(t.rst) && isR(t.rst.lst)) {
            // パターン1
            Boolean rb = t.color;
            t = rotateRL(t);
            t.color = rb;
            t.lst.color = true;
            active = false;
        }
        else if (isB(t.rst) && isR(t.rst.rst)) {
            // パターン2
            Boolean rb = t.color;
            t = rotateL(t);
            t.color = rb;
            t.lst.color = true;
            t.rst.color = true;
            active = false;
        }
        else if (isB(t.rst)) {
            // パターン3
            Boolean rb = t.color;
            t.color = true;
            t.rst.color = false;
            active = (rb == true);
        }
        else if (isR(t.rst)) {
            // パターン4
            t = rotateL(t);
            t.color = true;
            t.lst.color = false;
            t.lst = balanceL(t.lst);
        }
        return t;
    }

    // 赤黒木の修正(右の子ノードを削除したとき)
    private Node balanceR(Node t) {
        if (!active) return t;
        else if (isB(t.lst) && isR(t.lst.rst)) {
            // パターン1
            Boolean rb = t.color;
            t = rotateLR(t);
            t.color = rb;
            t.rst.color = true;
            active = false;
        }
        else if (isB(t.lst) && isR(t.lst.lst)) {
            // パターン2
            Boolean rb = t.color;
            t = rotateR(t);
            t.color = rb;
            t.lst.color = true;
            t.rst.color = true;
            active = false;
        }
        else if (isB(t.lst)) {
            // パターン3
            Boolean rb = t.color;
            t.color = true;
            t.lst.color = false;
            active = (rb == true);
        }
        else if (isR(t.lst)) {
            // パターン4
            t = rotateR(t);
            t.color = true;
            t.rst.color = false;
            t.rst = balanceR(t.rst);
        }
        return t;
    }

    // 指定したキーを持つノードの削除
    public void delete(String key) {
        active = false;
        root = delete(root, key);
    }
    
    // dotFile出力用
    private static String makedot(Node t){
        String text = "";
        if(t != null){
            if(t.lst != null){
                text += t.key + " -> " + t.lst.key + ";\n";
                if(t.lst.color == false){
                    text += t.lst.key + "[fillcolor = \"#FF0000\"]" + ";\n";
                }
            }
            if(t.rst != null){
                text += t.key + " -> " + t.rst.key + ";\n";
                if(t.rst.color == false){
                    text += t.rst.key + "[fillcolor = \"#FF0000\"]" + ";\n";
                }
            }
            text += makedot(t.lst);
            text += makedot(t.rst);
        }   
        return text;
    }

    // テスト
    public static void main(String[] args) {
        final int n = 40;
        RedBlackTree m = new RedBlackTree();
        var keys = new ArrayList<String>();
        for (int i = 0; i < n; i++) keys.add(Integer.toString(i));
        java.util.Collections.shuffle(keys);
        for (int i = 0; i < n; i++) m.put(keys.get(i), Integer.toString(i));
        var deleteKeys = keys.subList(0, 10);
        for (String key: deleteKeys) m.delete(key);

        try {
            File file = new File("RBTTemplate.dot");// 読み込み
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            FileWriter fw = new FileWriter("RBTshow.dot");// 書き込み
            int linenum = 8; 
            // linenum行目までコピー(テンプレート)
            for (int i = 0; i < linenum; i++){
                fw.write(br.readLine() + "\n");
            }
            br.close();
            String s = makedot(m.root);
            fw.write(s);
            fw.write("}");
            fw.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
