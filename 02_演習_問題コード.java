// ============================================================
// 注文管理システム（リファクタリング演習用）
// ============================================================
// このコードには複数の問題点があります。
// 以下の観点で問題点をすべてリストアップし、改善案を作成してください。
//
// チェック観点:
//   1. DRY原則 — 重複しているロジックはどこか？
//   2. SRP    — 1つのメソッドが複数の責務を持っていないか？
//   3. 命名   — 変数名・メソッド名は意図を正確に伝えているか？
//   4. マジックナンバー — 意味不明な数値が直書きされていないか？
//   5. エラー処理 — 存在しないIDが渡された場合どうなるか？
//   6. 変更容易性 — 新しい割引コードや送料ルールを追加するとき、
//                   何箇所を修正する必要があるか？
// ============================================================

class OrderManager {

    public static double processOrder(int cid, int[][] items, String dc) {
        String n = "";
        String e = "";
        if (cid == 1) {
            n = "田中太郎";
            e = "tanaka@example.com";
        } else if (cid == 2) {
            n = "鈴木花子";
            e = "suzuki@example.com";
        } else if (cid == 3) {
            n = "佐藤次郎";
            e = "sato@example.com";
        }

        double t = 0;
        for (int[] item : items) {
            t = t + item[0] * item[1];
        }

        if (dc.equals("GOLD10")) {
            t = t * 0.9;
        } else if (dc.equals("SILVER5")) {
            t = t * 0.95;
        } else if (dc.equals("SUMMER20")) {
            t = t * 0.8;
        } else if (dc.equals("WINTER15")) {
            t = t * 0.85;
        }

        double s = 0;
        if (t < 3000) {
            s = 500;
        } else if (t >= 3000 && t < 5000) {
            s = 300;
        } else if (t >= 5000) {
            s = 0;
        }

        double ft = t + s;

        System.out.println("メール送信先: " + e);
        System.out.println("件名: ご注文確認 - 合計金額: " + ft + "円");
        System.out.println("本文: " + n + "様、ご注文ありがとうございます。合計金額は" + ft + "円です。");

        return ft;
    }

    public static double processReturn(int cid, int[][] items, String reason) {
        String n = "";
        String e = "";
        if (cid == 1) {
            n = "田中太郎";
            e = "tanaka@example.com";
        } else if (cid == 2) {
            n = "鈴木花子";
            e = "suzuki@example.com";
        } else if (cid == 3) {
            n = "佐藤次郎";
            e = "sato@example.com";
        }

        double r = 0;
        for (int[] item : items) {
            r = r + item[0] * item[1];
        }

        System.out.println("メール送信先: " + e);
        System.out.println("件名: 返金処理完了 - 返金金額: " + r + "円");
        System.out.println("本文: " + n + "様、返品を受け付けました。返金金額は" + r + "円です。");

        return r;
    }

    public static String checkOrderStatus(int cid, int orderId) {
        String n = "";
        String e = "";
        if (cid == 1) {
            n = "田中太郎";
            e = "tanaka@example.com";
        } else if (cid == 2) {
            n = "鈴木花子";
            e = "suzuki@example.com";
        } else if (cid == 3) {
            n = "佐藤次郎";
            e = "sato@example.com";
        }

        String status = "shipped";

        System.out.println("メール送信先: " + e);
        System.out.println("件名: 注文状況のお知らせ");
        System.out.println("本文: " + n + "様、ご注文(ID:" + orderId + ")の現在のステータス: " + status);

        return status;
    }

    // ============================================================
    // 動作確認用サンプル
    // ============================================================
    public static void main(String[] args) {
        // items[i] = {price, quantity}
        int[][] sampleItems = {
            {200, 3},   // りんご
            {1500, 1},  // スマホケース
            {2000, 2},  // Tシャツ
        };

        System.out.println("=== 注文処理 ===");
        double total = processOrder(1, sampleItems, "GOLD10");
        System.out.println("合計: " + total + "円");

        System.out.println("\n=== 返品処理 ===");
        int[][] returnItems = {{200, 3}};
        double refund = processReturn(2, returnItems, "破損");
        System.out.println("返金額: " + refund + "円");

        System.out.println("\n=== 注文状況確認 ===");
        String status = checkOrderStatus(1, 12345);
        System.out.println("ステータス: " + status);
    }
}
