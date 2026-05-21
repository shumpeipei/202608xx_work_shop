// ============================================================
// 注文管理システム（リファクタリング後 — 模範解答）
// ============================================================
// 【全体共有セッション終了後に配布してください】
//
// 改善ポイント一覧:
//   1. 顧客データを Customer クラス + CUSTOMERS マップに集約 → DRY原則
//   2. 割引率を DISCOUNT_RATES マップに → if/else 爆発を解消、OCP準拠
//   3. 送料ルールを SHIPPING_RULES リストに → マジックナンバーを排除
//   4. getCustomer() を共通メソッドとして抽出 → DRY原則
//   5. calculateSubtotal() を分離 → SRP、テスト可能な単位に
//   6. calculateDiscount() を分離 → SRP
//   7. calculateShipping() を分離 → SRP
//   8. sendEmail() を分離 → DRY原則（メール送信が3箇所にあった）
//   9. 変数名を意図が伝わる名前に変更 (n→name, e→email, t→subtotal 等)
//  10. 型を明確にした Item クラス / ShippingRule クラスを導入
//  11. エラー処理（存在しない顧客IDに対する例外スロー）
// ============================================================

import java.util.*;

class OrderManagerRefactored {

    // ---- データクラス ----------------------------------------

    record Customer(String name, String email, String rank) {}

    record Item(String name, String type, int price, int quantity) {}

    record ShippingRule(int minAmount, int fee) {}

    // ---- 定数 -----------------------------------------------

    private static final Map<Integer, Customer> CUSTOMERS = Map.of(
        1, new Customer("田中太郎", "tanaka@example.com", "gold"),
        2, new Customer("鈴木花子", "suzuki@example.com", "silver"),
        3, new Customer("佐藤次郎", "sato@example.com",   "bronze")
    );

    // 割引コード → 割引率（新コード追加はここだけ変えればよい）
    private static final Map<String, Double> DISCOUNT_RATES = Map.of(
        "GOLD10",   0.10,
        "SILVER5",  0.05,
        "SUMMER20", 0.20,
        "WINTER15", 0.15
    );

    // 送料ルール（降順で定義、最初に一致したものを適用）
    private static final List<ShippingRule> SHIPPING_RULES = List.of(
        new ShippingRule(5000, 0),
        new ShippingRule(3000, 300),
        new ShippingRule(0,    500)
    );

    // ---- ヘルパーメソッド ------------------------------------

    private static Customer getCustomer(int customerId) {
        Customer customer = CUSTOMERS.get(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("顧客ID " + customerId + " が見つかりません");
        }
        return customer;
    }

    private static int calculateSubtotal(List<Item> items) {
        return items.stream()
                    .mapToInt(item -> item.price() * item.quantity())
                    .sum();
    }

    private static int calculateDiscount(int subtotal, String discountCode) {
        double rate = DISCOUNT_RATES.getOrDefault(discountCode, 0.0);
        return (int) (subtotal * rate);
    }

    private static int calculateShipping(int subtotal) {
        return SHIPPING_RULES.stream()
                             .filter(rule -> subtotal >= rule.minAmount())
                             .mapToInt(ShippingRule::fee)
                             .findFirst()
                             .orElse(500); // フォールバック
    }

    private static void sendEmail(Customer customer, String subject, String body) {
        System.out.println("メール送信先: " + customer.email());
        System.out.println("件名: " + subject);
        System.out.println("本文: " + body);
    }

    // ---- メイン処理 ------------------------------------------

    public static Map<String, Object> processOrder(
            int customerId, List<Item> items, String discountCode) {

        Customer customer = getCustomer(customerId);

        int subtotal  = calculateSubtotal(items);
        int discount  = calculateDiscount(subtotal, discountCode);
        int discounted = subtotal - discount;
        int shipping  = calculateShipping(discounted);
        int total     = discounted + shipping;

        sendEmail(
            customer,
            "ご注文確認 - 合計金額: " + total + "円",
            customer.name() + "様、ご注文ありがとうございます。合計金額は" + total + "円です。"
        );

        return Map.of(
            "customerId", customerId,
            "subtotal",   subtotal,
            "discount",   discount,
            "shipping",   shipping,
            "total",      total,
            "status",     "pending"
        );
    }

    public static Map<String, Object> processReturn(
            int customerId, List<Item> items, String reason) {

        Customer customer = getCustomer(customerId);
        int refundAmount  = calculateSubtotal(items);

        sendEmail(
            customer,
            "返金処理完了 - 返金金額: " + refundAmount + "円",
            customer.name() + "様、返品を受け付けました。返金金額は" + refundAmount + "円です。"
        );

        return Map.of(
            "customerId",   customerId,
            "refundAmount", refundAmount,
            "reason",       reason,
            "status",       "refunded"
        );
    }

    public static Map<String, Object> checkOrderStatus(int customerId, int orderId) {
        Customer customer = getCustomer(customerId);
        String status = "shipped"; // 実際はDB・外部APIから取得

        sendEmail(
            customer,
            "注文状況のお知らせ",
            customer.name() + "様、ご注文(ID:" + orderId + ")の現在のステータス: " + status
        );

        return Map.of("orderId", orderId, "customerId", customerId, "status", status);
    }

    // ---- 動作確認 --------------------------------------------

    public static void main(String[] args) {
        List<Item> sampleItems = List.of(
            new Item("りんご",       "food",        200,  3),
            new Item("スマホケース", "electronics", 1500, 1),
            new Item("Tシャツ",      "clothing",    2000, 2)
        );

        System.out.println("=== 注文処理 ===");
        System.out.println(processOrder(1, sampleItems, "GOLD10"));

        System.out.println("\n=== 返品処理 ===");
        System.out.println(processReturn(2, sampleItems.subList(0, 1), "破損"));

        System.out.println("\n=== 注文状況確認 ===");
        System.out.println(checkOrderStatus(1, 12345));

        System.out.println("\n=== エラーケース（存在しない顧客ID）===");
        try {
            processOrder(999, sampleItems, "");
        } catch (IllegalArgumentException e) {
            System.out.println("エラーを正常に検出: " + e.getMessage());
        }
    }
}
