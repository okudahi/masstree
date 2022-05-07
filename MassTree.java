public class MassTree {
    final private static int MAX_CHILD = 16;
    final private static int HALF_CHILD = ((MAX_CHILD + 1) / 2);

    // Node
    private abstract class Node {

        int serial;

    }

    //internal node
    private class InternalNode extends Node {

        // キーの数
        int nkeys;

        //キースライス
        Integer[] keyslice;

        // 部分木
        Node[] child;

        // 各部分木の最小の要素
        Integer[] low;

        // コンストラクタ
        private InternalNode() {
            serial = serialNumber++;
            nkeys = 0;
            keyslice = new Integer[MAX_CHILD];
            child = new Node[MAX_CHILD];
            low = new Integer[MAX_CHILD];
        }

        
        // キーkeyをもつデータは何番目の部分木に入るか
        private int locateSubtree(Integer key) {
            for (int i = nkeys; i > 0; i--) {
                if (key.compareTo(low[i]) >= 0) {
                    return i;
                }
            }
            return 0;
        }
    }

    private class border_node extends Node {

        Integer key;    // 葉が持っているキーの値
        Object data;    // 葉に格納するデータ

        // コンストラクタ
        private border_node(Integer key, Object data) {
            serial = serialNumber++;
            this.key = key;
            this.data = data;
        }

    }

    // B木の根
    private Node root;
    // Nodeに附番するシリアル番号
    private int serialNumber = 0;

    private Leaf currentLeaf;

    // コンストラクタ
    public MassTree() {
        root = null;
    }

}