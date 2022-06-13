import java.util.List;
import java.util.ArrayList;

public class BPMap<K extends Comparable<? super K>,V> { // K:キーの型, V:値の型
    //=========================================================================
    // 共通定義
    //=========================================================================

    private int m;  // B+木のオーダー
    private int hm; // B+木の根を除くノードの最小分岐数[m/2] 

    public BPMap() { this(5); }
    public BPMap(int n) {
        m  = n;
        hm = (m + 1) / 2;
    }

    private abstract class Node { // ノードの型(抽象型)
        List<K>    ks() { return null; } // キーのリスト
        List<Node> ns() { return null; } // 枝のリスト
        Node       deactivate() { return this; }
        Node       trim() { return this; }
        Node       insert(K key, V x) { return this; }
        void       delete(K key) {}
        K          deleteMin() { return null; }
        void       balanceL(Inner t, int i) {}
        void       balanceR(Inner t, int j) {}
    }
    private final Bottom nil = new Bottom(); // null の代わり
    private       Node  root = nil;          // B+木の根(nil は空の木)

    // 挿入時のアクティブなノードなら true
    private boolean active(Node t) { return t instanceof Active; }

    // 最下層のノードなら true
    private boolean bottom(Node t) { return t instanceof Bottom; }

    //=========================================================================
    // 挿入時のアクティブなノードの型
    //=========================================================================

    private class Active extends Node {
        List<K>    ks = new ArrayList<K>(1);    // キーのリスト
        List<Node> ns = new ArrayList<Node>(2); // 枝のリスト

        Active(K key, Node l, Node r) { ks.add(key); ns.add(l); ns.add(r); }
        List<K>    ks() { return ks; }
        List<Node> ns() { return ns; }

        // アクティブなノードを内部ノードに変換する
        Node deactivate() { return new Inner(ks.get(0), ns.get(0), ns.get(1)); }
    }

    //=========================================================================
    // 内部ノードの型
    //=========================================================================

    private class Inner extends Node {
        List<K>    ks = new ArrayList<K>(m);        // キーのリスト
        List<Node> ns = new ArrayList<Node>(m + 1); // 枝のリスト

        Inner() {}
        Inner(K key, Node l, Node r) { ks.add(key); ns.add(l); ns.add(r); }
        List<K>    ks() { return ks; }
        List<Node> ns() { return ns; }

        // 枝が１本の余分なノードを切り詰める
        Node trim() { return ns.size() == 1 ? ns.get(0) : this; }

        //=====================================================================
        // 内部ノードでの挿入
        //=====================================================================

        // 木 this にキー key で値 x を挿入する。
        Node insert(K key, V x) {
            int i;
            for (i = 0; i < ks.size(); i++) {
                int cmp = key.compareTo(ks.get(i));
                if (cmp < 0) {
                    ns.set(i, ns.get(i).insert(key, x));
                    return balance(i);
                }
                else if (cmp == 0) {
                    ns.set(i+1, ns.get(i+1).insert(key, x));
                    return balance(i+1);
                }
            }
            ns.set(i, ns.get(i).insert(key, x));
            return balance(i);
        }

        // 挿入時のバランス調整
        Node balance(int i) {
            Node ni = ns.get(i);
            if (!active(ni)) return this;
            // 以下、ni はアクティブなノード(つまり２分岐)
            ks.add(i, ni.ks().get(0));
            ns.set(i, ni.ns().get(1));
            ns.add(i, ni.ns().get(0));
            return ks.size() < m ? this : split();
        }

        // 要素数が m のノードを分割してアクティブなノードに変換する
        Node split() {
            int j = hm, i = j - 1;
            Inner l = this;
            Inner r = new Inner();
            r.ks.addAll(l.ks.subList(j, m));
            r.ns.addAll(l.ns.subList(j, m + 1));
            l.ks.subList(j, m).clear();
            l.ns.subList(j, m + 1).clear();
            return new Active(l.ks.remove(i), l, r);
        }

        //=====================================================================
        // 内部ノードでの削除
        //=====================================================================

        // 木 this からキー key のノードを削除する
        void delete(K key) {
            int i;
            for (i = 0; i < ks.size(); i++) {
                int cmp = key.compareTo(ks.get(i));
                if (cmp < 0) {
                    ns.get(i).delete(key);
                    ns.get(i).balanceL(this, i);
                    return;
                }
                else if (cmp == 0) {
                    ks.set(i, ns.get(i+1).deleteMin());
                    ns.get(i+1).balanceR(this, i+1);
                    return;
                }
            }
            ns.get(i).delete(key);
            ns.get(i).balanceR(this, i);
        }

        // 部分木 this の最小値キーを削除する
        // 部分木 this の新たな最小値のキーを返す
        K deleteMin() {
            K nmin  = ns.get(0).deleteMin();
            K spare = ks.get(0);
            ns.get(0).balanceL(this, 0);
            return nmin != null ? nmin : spare;
        }

        // 左部分木での削除時のバランス調整
        void balanceL(Inner t, int i) {
            Inner ni = this;
            if (ni.ns.size() >= hm) return;
            // 以下、ni がアクティブな場合
            int j = i + 1;
            K key = t.ks.get(i);
            Inner nj = (Inner) t.ns.get(j);
            if (nj.ns.size() == hm) { // nj に余裕がない場合(融合)
                ni.ks.add(key);
                ni.ks.addAll(nj.ks);
                ni.ns.addAll(nj.ns);
                t.ks.remove(i);
                t.ns.remove(j);
            }
            else t.ks.set(i, moveRL(key, ni, nj)); // nj に余裕がある場合
        }

        // 右部分木での削除時のバランス調整
        void balanceR(Inner t, int j) {
            Inner nj = this;
            if (nj.ns.size() >= hm) return;
            // 以下、nj がアクティブな場合
            int i = j - 1;
            K key  = t.ks.get(i);
            Inner ni = (Inner) t.ns.get(i);
            if (ni.ns.size() == hm) { // ni に余裕がない場合(融合)
                ni.ks.add(key);
                ni.ks.addAll(nj.ks);
                ni.ns.addAll(nj.ns);
                t.ks.remove(i);
                t.ns.remove(j);
            }
            else t.ks.set(i, moveLR(key, ni, nj)); // ni に余裕がある場合
        }

        // 余裕のある右のノードから枝を１本分けてもらう
        K moveRL(K key, Inner l, Inner r) {
            l.ks.add(key);
            l.ns.add(r.ns.remove(0));
            return r.ks.remove(0);
        }

        // 余裕のある左のノードから枝を１本分けてもらう
        K moveLR(K key, Inner l, Inner r) {
            int j = l.ks.size(), i = j - 1;
            r.ks.add(0, key);
            r.ns.add(0, l.ns.remove(j));
            return l.ks.remove(i);
        }
    }

    //=========================================================================
    // 最下層のノードの型
    //=========================================================================

    private class Bottom extends Node {
        List<K> ks = new ArrayList<K>(m); // キーのリスト
        List<V> vs = new ArrayList<V>(m); // 値のリスト
        Bottom  next = nil;               // 右隣の最下層のノード

        Bottom() {}
        Bottom(K key, V x) { ks.add(key); vs.add(x); }
        List<K> ks() { return ks; }

        // キーも枝もないノードを空の木(nil)に変換する
        Node trim() { return ks.size() == 0 ? nil : this; }

        //=====================================================================
        // 最下層のノードでの挿入
        //=====================================================================

        // 木 this にキー key で値 x を挿入する。
        Node insert(K key, V x) {
            if (this == nil) return new Bottom(key, x);
            int i;
            for (i = 0; i < ks.size(); i++) {
                int cmp = key.compareTo(ks.get(i));
                if (cmp < 0) return balance(i, key, x);
                else if (cmp == 0) { vs.set(i, x); return this; }
            }
            return balance(i, key, x);
        }

        // 挿入時のバランス調整
        Node balance(int i, K key, V x) {
            ks.add(i, key);
            vs.add(i, x);
            return ks.size() < m ? this : split();
        }

        // 要素数が m のノードを分割してアクティブなノードに変換する
        Node split() {
            int j = hm - 1;
            Bottom l = this;
            Bottom r = new Bottom();
            r.ks.addAll(l.ks.subList(j, m));
            r.vs.addAll(l.vs.subList(j, m));
            l.ks.subList(j, m).clear();
            l.vs.subList(j, m).clear();
            r.next = l.next; l.next = r;
            return new Active(r.ks.get(0), l, r);
        }

        //=====================================================================
        // 最下層のノードでの削除
        //=====================================================================

        // 木 this からキー key のノードを削除する
        void delete(K key) {
            for (int i = 0; i < ks.size(); i++) {
                int cmp = key.compareTo(ks.get(i));
                if (cmp < 0) return;
                else if (cmp == 0) { ks.remove(i); vs.remove(i); return; }
            }
        }

        // 部分木 this の最小値キーを削除する
        // 部分木 this の新たな最小値のキーを返す。空になったら null を返す
        K deleteMin() {
            ks.remove(0);
            vs.remove(0);
            return !ks.isEmpty() ? ks.get(0) : null;
        }

        // 左部分木での削除時のバランス調整
        void balanceL(Inner t, int i) {
            Bottom ni = this;
            if (ni.ks.size() >= hm-1) return;
            // 以下、ni がアクティブな場合
            int j = i + 1;
            Bottom nj = (Bottom) t.ns.get(j);
            if (nj.ks.size() == hm-1) { // nj に余裕がない場合(融合)
                ni.ks.addAll(nj.ks);
                ni.vs.addAll(nj.vs);
                t.ks.remove(i);
                t.ns.remove(j);
                ni.next = nj.next;
            }
            else t.ks.set(i, moveRL(ni, nj)); // nj に余裕がある場合
        }

        // 右部分木での削除時のバランス調整
        void balanceR(Inner t, int j) {
            Bottom nj = this;
            if (nj.ks.size() >= hm-1) return;
            // 以下、nj がアクティブな場合
            int i = j - 1;
            Bottom ni = (Bottom) t.ns.get(i);
            if (ni.ks.size() == hm-1) { // ni に余裕がない場合(融合)
                ni.ks.addAll(nj.ks);
                ni.vs.addAll(nj.vs);
                t.ks.remove(i);
                t.ns.remove(j);
                ni.next = nj.next;
            }
            else t.ks.set(i, moveLR(ni, nj)); // ni に余裕がある場合
        }

        // 余裕のある右のノードから枝を１本分けてもらう
        K moveRL(Bottom l, Bottom r) {
            l.ks.add(r.ks.remove(0));
            l.vs.add(r.vs.remove(0));
            return r.ks.get(0);
        }

        // 余裕のある左のノードから枝を１本分けてもらう
        K moveLR(Bottom l, Bottom r) {
            int i = l.ks.size() - 1;
            r.ks.add(0, l.ks.remove(i));
            r.vs.add(0, l.vs.remove(i));
            return r.ks.get(0);
        }
    }

    //=========================================================================
    // 挿入・削除・検索等
    //=========================================================================

    // 木 root にキー key で値 x を挿入する
    public void insert(K key, V x) { root = root.insert(key, x).deactivate(); }

    // 木 root からキー key のノードを削除する
    public void delete(K key) { root.delete(key); root = root.trim(); }

    // キーの検索。ヒットすれば true、しなければ false
    public boolean member(K key) {
        if (root == nil) return false;
        Node t = root;
        while (!bottom(t)) {
            int i;
            for (i = 0; i < t.ks().size(); i++) {
                final int cmp = key.compareTo(t.ks().get(i));
                if      (cmp <  0) break;
                else if (cmp == 0) return true;
            }
            t = t.ns().get(i);
        }
        Bottom u = (Bottom) t;
        for (int i = 0; i < u.ks.size(); i++)
            if (key.compareTo(u.ks.get(i)) == 0) return true;
        return false;
    }

    // キーから値を得る。キーがヒットしない場合は null を返す
    public V lookup(K key) {
        if (root == nil) return null;
        Node t = root;
        while (!bottom(t)) {
            int i;
            for (i = 0; i < t.ks().size(); i++) {
                final int cmp = key.compareTo(t.ks().get(i));
                if      (cmp <  0) break;
                else if (cmp == 0) { i++; break; }
            }
            t = t.ns().get(i);
        }
        Bottom u = (Bottom) t;
        for (int i = 0; i < u.ks.size(); i++)
            if (key.compareTo(u.ks.get(i)) == 0) return u.vs.get(i);
        return null;
    }

    // マップが空なら true、空でないなら false
    public boolean isEmpty() { return root == nil; }

    // マップを空にする
    public void clear() { root = nil; }

    // B+木のキーのリストを返す
    ArrayList<K> keys() {
        if (root == nil) return null;
        Node t = root;
        while (!bottom(t)) t = t.ns().get(0);
        Bottom u = (Bottom) t;
        ArrayList<K> al = new ArrayList<K>();
        while (u != nil) { al.addAll(u.ks); u = u.next; }
        return al;
    }

    // B+木の値のリストを返す
    ArrayList<V> values() {
        if (root == nil) return null;
        Node t = root;
        while (!bottom(t)) t = t.ns().get(0);
        Bottom u = (Bottom) t;
        ArrayList<V> al = new ArrayList<V>();
        while (u != nil) { al.addAll(u.vs); u = u.next; }
        return al;
    }

    // マップのサイズ
    public int size() { return keys().size(); }

    // キーの最小値
    public K min() {
        if (root == nil) return null;
        Node t = root;
        while (!bottom(t)) t = t.ns().get(0);
        return t.ks().get(0);
    }

    // キーの最大値
    public K max() {
        if (root == nil) return null;
        Node t = root;
        while (!bottom(t)) t = t.ns().get(t.ns().size() - 1);
        return t.ks().get(t.ks().size() - 1);
    }

    //=========================================================================
    // debug 用ルーチン
    //=========================================================================

    // B+木をグラフ文字列に変換する
    public String toString() {
        return toGraph("", root).replaceAll("\\s+$", "");
    }

    // バランスが取れているか確認する
    public boolean balanced() { return balanced(root); }

    // 多分探索木になっているか確認する
    public boolean mstValid() { return mstValid(root); }

    // 根と最下層のノードを除くノードが hm 以上の枝を持っているか確認する
    public boolean dense() {
        if (root == nil) return true;
        int n = root.ns().size();
        if (bottom(root)) { if (n < 1 || m-1 < n) return false; }
        else {
            if (n < 2 || m < n) return false;
            for (int i = 0; i < n; i++)
                if (!denseHalf(root.ns().get(i))) return false;
        }
        return true;
    }

    private String toGraph(String head, Node t) {
        if (t == nil) return "";
        String graph = "";
        if (bottom(t))
            graph += String.format("%s%s%n", head, t.ks());
        else {
            int i = t.ns().size();
            graph += toGraph(head + "    ", t.ns().get(--i));
            graph += String.format("%s∧%n", head);
            do {
                graph += String.format("%s%s%n", head, t.ks().get(--i));
                if (i == 0) graph += String.format("%s∨%n", head);
                graph += toGraph(head + "    ", t.ns().get(i));
            } while (i > 0);
        }
        return graph;
    }

    // 部分木 t の高さを返す
    private int height(Node t) {
        if (t == nil) return 0;
        if (bottom(t)) return 1;
        int mx = height(t.ns().get(0));
        for (int i = 1; i < t.ns().size(); i++) {
            int h = height(t.ns().get(i));
            if (h > mx) mx = h;
        }
        return 1 + mx;
    }

    private boolean balanced(Node t) {
        if (t == nil) return true;
        if (bottom(t)) return true;
        if (!balanced(t.ns().get(0))) return false;
        int h = height(t.ns().get(0));
        for (int i = 1; i < t.ns().size(); i++) {
            if (!balanced(t.ns().get(i))) return false;
            if (h != height(t.ns().get(i))) return false;
        }
        return true;
    }

    private boolean mstValid(Node t) {
        if (t == nil) return true;
        if (bottom(t)) {
            for (int i = 1; i < t.ks().size(); i++) {
                K key1 = t.ks().get(i - 1);
                K key2 = t.ks().get(i);
                if (!(key1.compareTo(key2) < 0)) return false;
            }
            return true;
        }
        K key = t.ks().get(0);
        if (!small(key, t.ns().get(0))) return false;
        if (!mstValid(t.ns().get(0))) return false;
        int i;
        for (i = 1; i < t.ks().size(); i++) {
            K key1 = t.ks().get(i - 1);
            K key2 = t.ks().get(i);
            if (!(key1.compareTo(key2) < 0)) return false;
            if (!between(key1, key2, t.ns().get(i))) return false;
            if (!mstValid(t.ns().get(i))) return false;
        }
        key = t.ks().get(i - 1);
        if (!large(key, t.ns().get(i))) return false;
        if (!mstValid(t.ns().get(i))) return false;
        return true;
    }

    private boolean small(K key, Node t) {
        if (t == nil) return true;
        if (bottom(t)) {
            for (int i = 0; i < t.ks().size(); i++)
                if (!(key.compareTo(t.ks().get(i)) > 0)) return false;
            return true;
        }
        int i;
        for (i = 0; i < t.ks().size(); i++) {
            if (!small(key, t.ns().get(i))) return false;
            if (!(key.compareTo(t.ks().get(i)) > 0)) return false;
        }
        if (!small(key, t.ns().get(i))) return false;
        return true;
    }

    private boolean between(K key1, K key2, Node t) {
        if (t == nil) return true;
        if (bottom(t)) {
            for (int i = 0; i < t.ks().size(); i++) {
                if (!(key1.compareTo(t.ks().get(i)) <= 0)) return false;
                if (!(key2.compareTo(t.ks().get(i)) >  0)) return false;
            }
            return true;
        }
        int i;
        for (i = 0; i < t.ks().size(); i++) {
            if (!between(key1, key2, t.ns().get(i))) return false;
            if (!(key1.compareTo(t.ks().get(i)) <= 0)) return false;
            if (!(key2.compareTo(t.ks().get(i)) >  0)) return false;
        }
        if (!between(key1, key2, t.ns().get(i))) return false;
        return true;
    }

    private boolean large(K key, Node t) {
        if (t == nil) return true;
        if (bottom(t)) {
            for (int i = 0; i < t.ks().size(); i++)
                if (!(key.compareTo(t.ks().get(i)) <= 0)) return false;
            return true;
        }
        int i;
        for (i = 0; i < t.ks().size(); i++) {
            if (!large(key, t.ns().get(i))) return false;
            if (!(key.compareTo(t.ks().get(i)) <= 0)) return false;
        }
        if (!large(key, t.ns().get(i))) return false;
        return true;
    }

    private boolean denseHalf(Node t) {
        if (t == nil) return true;
        if (bottom(t)) {
            final int n = t.ks().size();
            if (n < hm-1 || m-1 < n) return false;
        }
        else {
            final int n = t.ns().size();
            if (n < hm || m < n) return false;
            for (int i = 0; i < n; i++)
                if (!denseHalf(t.ns().get(i))) return false;
        }
        return true;
    }

    //=========================================================================
    // メインルーチン
    //=========================================================================

    public static void main(String[] args) {
        final int n = 40;
        var m = new BPMap<Integer,Integer>();
        var keys = new ArrayList<Integer>();
        for (int i = 0; i < n; i++) keys.add(i);
        java.util.Collections.shuffle(keys);
        for (int i = 0; i < n; i++) m.insert(keys.get(i), i);
        var deleteKeys = keys.subList(0, 10);
        for (int key: deleteKeys) m.delete(key);
        System.out.println(m);
        System.out.println();
        System.out.println("size: " + m.size());
        System.out.println("keys: " + m.keys());

        final int N = 1000000;
        var rng = new java.util.Random();
        var answer = new java.util.TreeMap<Integer,Integer>();
        int insertErrors = 0;
        int deleteErrors = 0;
        m = new BPMap<Integer,Integer>(31);
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
        keys = new ArrayList<Integer>(answer.keySet());
        java.util.Collections.shuffle(keys);
        deleteKeys = keys.subList(0, half);
        for (int key: deleteKeys) m.delete(key);
        for (int key: deleteKeys) {
            if (m.member(key)) deleteErrors++;
        }
        System.out.println();
        System.out.println("バランス:   " + (m.balanced()      ? "OK" : "NG"));
        System.out.println("多分探索木: " + (m.mstValid()      ? "OK" : "NG"));
        System.out.println("密度:       " + (m.dense()         ? "OK" : "NG"));
        System.out.println("挿入:       " + (insertErrors == 0 ? "OK" : "NG"));
        System.out.println("削除:       " + (deleteErrors == 0 ? "OK" : "NG"));
        for (int key: m.keys()) m.delete(key);
        System.out.println("全削除:     " + (m.isEmpty()       ? "OK" : "NG"));
    }
}