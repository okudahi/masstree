import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class BTreeTest {


    /**
    * テスト用のメインルーチン
    *
    *      ">"というプロンプトが表示されるので、コマンドを入力すると実行結果が表示される。
    *
    *      コマンド一覧(nは整数)
    *          +n          : nを挿入する
    *          -n          : nを削除する
    *          /n          : nを探索する
    *          =String     : 直前に成功した/コマンドで見つけた要素に対する値をStringにする
    *          p           : B木の内容を表示する
    *          q           : 終了する
    * @throws IOException
    *
    * */
    public static void main(String[] args) throws IOException {

        BTree tree = new BTree();

        // B木に初期データを挿入する
        int data[] = {1,100,27,45,3,135};


        for (int x : data) {
            tree.insert(x, "["+x+"]");
        }

        // コマンドを1行入力して、それを実行する
        // これをEOFになるまで繰り返す
        System.out.println(">");
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        String str = null;
        while ((str = input.readLine()) != null) {
            if (str.length() == 0) {
                // 空行は読み飛ばす
                System.out.print(">");
                continue;
            }

            // 先頭の1文字（コマンド）をcommandに入れる
            char command = str.charAt(0);

            // 引数部分をargに入れる。その際に先頭のスペースを削除する
            String arg = str.substring(1).trim();

            // コマンドによって分岐する
            if (command == 'q') {
                break;
            } else if (command == 'p') {
                System.out.println(tree);
            } else if (command == '=') {
                if (tree.setData(arg)) {
                    System.out.println("value:" + arg + " succeeded.");
                } else {
                    System.out.println("value:" + arg + " failed.");
                }
            } else if (command == '+' || command == '-' || command == '/') {
                // +, - コマンドならば、コマンドに続く数値をnumに得る
                int num = 0;
                try {
                    num = Integer.parseInt(arg);
                } catch (NumberFormatException e) {
                    System.err.println("input NOT integer. value:" + arg);
                    continue;
                }
                if (command == '+') {
                    if (tree.insert(num, "["+num+"]" )) {
                        System.out.println(num + " insert succeeded.");
                    } else {
                        System.out.println(num + " insert failed.");
                    }
                } else if (command == '-') {
                    if (tree.delete(num)) {
                        System.out.println(num + " delete succeeded.");
                    } else {
                        System.out.println(num + " delete failed.");
                    }
                } else if (command == '/') {
                    if (tree.search(num)) {
                        System.out.println(num + " found. value:" + tree.getData());
                    } else {
                        System.out.println(num + " NOT found.");
                    }
                }
            } else {
                System.out.println("command:" + command + " is NOT supported.");
            }
            System.out.print(">");
        }
    }

}