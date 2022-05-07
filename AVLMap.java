import java.util.ArrayList;

public class AVLMap<K extends Comparable<? super K>,V> { // K:キーの型, V:値の型

    private class Node { // ノードの型
        int height;      // そのノードを根とする部分木の高さ
        K key;           // そのノードのキー
        V value;         // そのノードの値
        Node lst = null; // 左部分木
        Node rst = null; // 右部分木

        Node(int height, K key, V x) {
            this.height = height;
            this.key = key;
            this.value = x;
        }
    }
    private Node root = null; // AVL木の根
    private boolean active;   // 修正中かを示すフラグ(true:修正中, false:安定)
    private Node submax;      // deleteMax 用の補助変数

    // 部分木 t の高さを返す
    private int height(Node t) { return t == null ? 0 : t.height; }

    // 左右の部分木の高さの差を返す。左が高いと正、右が高いと負
    private int bias(Node t) { return height(t.lst) - height(t.rst); }

    // 左右の部分木の高さから、その木の高さを計算して修正する
    private void modHeight(Node t) {
        t.height = 1 + Math.max(height(t.lst), height(t.rst));
    }

    // ２分探索木 v の左回転。回転した木を返す
    private Node rotateL(Node v) {
        Node u = v.rst, t2 = u.lst;
        u.lst = v; v.rst = t2;
        modHeight(u.lst);
        modHeight(u);
        return u;
    }

    // ２分探索木 u の右回転。回転した木を返す
    private Node rotateR(Node u) {
        Node v = u.lst, t2 = v.rst;
        v.rst = u; u.lst = t2;
        modHeight(v.rst);
        modHeight(v);
        return v;
    }

    // ２分探索木 t の二重回転(左回転 -> 右回転)。回転した木を返す
    private Node rotateLR(Node t) {
        t.lst = rotateL(t.lst);
        return rotateR(t);
    }

    // ２分探索木 t の二重回転(右回転 -> 左回転)。回転した木を返す
    private Node rotateRL(Node t) {
        t.rst = rotateR(t.rst);
        return rotateL(t);
    }

    ///////////////////////////////////////////////////////////////////////////
    // バランス回復
    ///////////////////////////////////////////////////////////////////////////

    // 挿入時の修正(balanceLi:左部分木への挿入, balanceRi:右部分木への挿入)
    private Node balanceLi(Node t) { return balanceL(t); }
    private Node balanceRi(Node t) { return balanceR(t); }

    // 削除時の修正(balanceLd:左部分木での削除, balanceRd:右部分木での削除)
    private Node balanceLd(Node t) { return balanceR(t); }
    private Node balanceRd(Node t) { return balanceL(t); }

    // 部分木 t のバランスを回復して戻り値で返す
    // 左部分木への挿入に伴うAVL木の修正
    // 右部分木での削除に伴うAVL木の修正
    private Node balanceL(Node t) {
        if (!active) return t;
        int h = height(t);
        if (bias(t) == 2) {
            if (bias(t.lst) >= 0)
                t = rotateR(t);
            else
                t = rotateLR(t);
        }
        else modHeight(t);
        active = (h != height(t));
        return t;
    }

    // 部分木 t のバランスを回復して戻り値で返す
    // 右部分木への挿入に伴うAVL木の修正
    // 左部分木での削除に伴うAVL木の修正
    private Node balanceR(Node t) {
        if (!active) return t;
        int h = height(t);
        if (bias(t) == -2) {
            if (bias(t.rst) <= 0)
                t = rotateL(t);
            else
                t = rotateRL(t);
        }
        else modHeight(t);
        active = (h != height(t));
        return t;
    }

    ///////////////////////////////////////////////////////////////////////////
    // insert(挿入)
    ///////////////////////////////////////////////////////////////////////////

    // エントリー(key, x のペア)を挿入する
    public void insert(K key, V x) {
        active = false;
        root = insert(root, key, x);
    }

    private Node insert(Node t, K key, V x) {
        if (t == null) { active = true; return new Node(1, key, x); }
        int cmp = key.compareTo(t.key);
        if (cmp < 0) {
            t.lst = insert(t.lst, key, x);
            return balanceLi(t);
        }
        else if (cmp > 0) {
            t.rst = insert(t.rst, key, x);
            return balanceRi(t);
        }
        else {
            t.value = x;
            return t;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // delete(削除)
    ///////////////////////////////////////////////////////////////////////////

    // key で指すエントリー(ノード)を削除する
    public void delete(K key) {
        active = false;
        root = delete(root, key);
    }

    private Node delete(Node t, K key) {
        if (t == null) return null;
        int cmp = key.compareTo(t.key);
        if (cmp < 0) {
            t.lst = delete(t.lst, key);
            return balanceLd(t);
        }
        else if (cmp > 0) {
            t.rst = delete(t.rst, key);
            return balanceRd(t);
        }
        else if (t.lst != null) {
            // 内部ノードの削除
            t.lst = deleteMax(t.lst); // 左部分木の最大値のノードを削除する
            t.key = submax.key; // 削除した最大値で置き換える
            t.value = submax.value;
            submax = null; // コピーしたら捨てる
            return balanceLd(t);
        }
        else {
            // 最下層のノードの削除
            active = true;
            return t.rst; // 右部分木を昇格させる
        }
    }

    // 部分木 t の最大値のノードを削除する
    // 戻り値は削除により修正された部分木
    // 削除した最大値のノードを submax に保存する
    private Node deleteMax(Node t) {
        if (t.rst != null) {
            t.rst = deleteMax(t.rst);
            return balanceRd(t);
        }
        else {
            active = true;
            submax = t;   // 最大値のノードを保存
            return t.lst; // 左部分木を昇格させる
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // 検索等
    ///////////////////////////////////////////////////////////////////////////

    // キーの検索。ヒットすれば true、しなければ false
    public boolean member(K key) {
        Node t = root;
        while (t != null) {
            int cmp = key.compareTo(t.key);
            if      (cmp < 0) t = t.lst;
            else if (cmp > 0) t = t.rst;
            else return true;
        }
        return false;
    }

    // キーから値を得る。キーがヒットしない場合は null を返す
    public V lookup(K key) {
        Node t = root;
        while (t != null) {
            int cmp = key.compareTo(t.key);
            if      (cmp < 0) t = t.lst;
            else if (cmp > 0) t = t.rst;
            else return t.value;
        }
        return null;
    }

    // マップが空なら true、空でないなら false
    public boolean isEmpty() { return root == null; }

    // マップを空にする
    public void clear() { root = null; }

    // キーのリスト
    public ArrayList<K> keys() {
        var al = new ArrayList<K>();
        keys(root, al);
        return al;
    }

    // 値のリスト
    public ArrayList<V> values() {
        var al = new ArrayList<V>();
        values(root, al);
        return al;
    }

    // マップのサイズ
    public int size() { return keys().size(); }

    // キーの最小値
    public K min() {
        Node t = root;
        if (t == null) return null;
        while (t.lst != null) t = t.lst;
        return t.key;
    }

    // キーの最大値
    public K max() {
        Node t = root;
        if (t == null) return null;
        while (t.rst != null) t = t.rst;
        return t.key;
    }

    private void keys(Node t, ArrayList<K> al) {
        if (t != null) {
            keys(t.lst, al);
            al.add(t.key);
            keys(t.rst, al);
        }
    }

    private void values(Node t, ArrayList<V> al) {
        if (t != null) {
            values(t.lst, al);
            al.add(t.value);
            values(t.rst, al);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // debug 用ルーチン
    ///////////////////////////////////////////////////////////////////////////

    // AVL木をグラフ文字列に変換する
    public String toString() {
        return toGraph("", "", root).replaceAll("\\s+$", "");
    }

    // AVL木のバランスが取れているか確認する
    public boolean balanced() { return balanced(root); }

    // ２分探索木になっているか確認する
    public boolean bstValid() { return bstValid(root); }

    private String toGraph(String head, String bar, Node t) {
        String graph = "";
        if (t != null) {
            graph += toGraph(head + "　　", "／", t.rst);
            String node = "" + t.height;
            node += ":" + t.key;
            node += ":" + t.value;
            graph += String.format("%s%s%s%n", head, bar, node);
            graph += toGraph(head + "　　", "＼", t.lst);
        }
        return graph;
    }

    private boolean balanced(Node t) {
        if (t == null) return true;
        return Math.abs(bias(t)) <= 1 && balanced(t.lst) && balanced(t.rst);
    }

    private boolean bstValid(Node t) {
        if (t == null) return true;
        boolean flag = small(t.key, t.lst) && large(t.key, t.rst);
        return flag && bstValid(t.lst) && bstValid(t.rst);
    }

    private boolean small(K key, Node t) {
        if (t == null) return true;
        boolean flag = key.compareTo(t.key) > 0;
        return flag && small(key, t.lst) && small(key, t.rst);
    }

    private boolean large(K key, Node t) {
        if (t == null) return true;
        boolean flag = key.compareTo(t.key) < 0;
        return flag && large(key, t.lst) && large(key, t.rst);
    }

    ///////////////////////////////////////////////////////////////////////////
    // メインルーチン
    ///////////////////////////////////////////////////////////////////////////

    public static void main(String[] args) {
        final int n = 30;
        var m = new AVLMap<Integer,Integer>();
        for (int i = 0; i < n; i++) m.insert(i, i);
        /*
        {
            final int nn = 40;
            m.clear();
            var keys = new ArrayList<Integer>();
            for (int i = 0; i < nn; i++) keys.add(i);
            java.util.Collections.shuffle(keys);
            for (int i = 0; i < nn; i++) m.insert(keys.get(i), i);
            var deleteKeys = keys.subList(0, 10);
            for (int key: deleteKeys) m.delete(key);
        }
        */
        System.out.println(m);
        System.out.println();
        m.keys().stream().limit(5).forEach(key -> {
            int val = m.lookup(key);
            System.out.printf("m.lookup(%2d) == %2d%n", key, val);
        });
        System.out.println();
        System.out.println("size: " + m.size());
        System.out.println("keys: " + m.keys());

        final int N = 1000000;
        var rng = new java.util.Random();
        var answer = new java.util.TreeMap<Integer,Integer>();
        int insertErrors = 0;
        int deleteErrors = 0;
        m.clear();
        for (int i = 0; i < N; i++) {
            int key = rng.nextInt(N);
            m.insert(key, i);
            answer.put(key, i);
        }
        for (int key: answer.keySet()) {
            if (m.member(key)) {
                int x = m.lookup(key);
                int y = answer.get(key);
                if (x != y) insertErrors++;
            }
            else insertErrors++;
        }
        final int half = answer.size() / 2;
        var keys = new ArrayList<Integer>(answer.keySet());
        java.util.Collections.shuffle(keys);
        var deleteKeys = keys.subList(0, half);
        for (int key: deleteKeys) m.delete(key);
        for (int key: deleteKeys) {
            if (m.member(key)) deleteErrors++;
        }
        System.out.println();
        System.out.println("バランス:   " + (m.balanced()      ? "OK" : "NG"));
        System.out.println("２分探索木: " + (m.bstValid()      ? "OK" : "NG"));
        System.out.println("挿入:       " + (insertErrors == 0 ? "OK" : "NG"));
        System.out.println("削除:       " + (deleteErrors == 0 ? "OK" : "NG"));
        for (int key: m.keys()) m.delete(key);
        System.out.println("全削除:     " + (m.isEmpty()       ? "OK" : "NG"));
    }
}