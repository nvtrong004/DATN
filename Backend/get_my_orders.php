<?php
include 'db.php';

$userId = isset($_GET['user_id']) ? (int)$_GET['user_id'] : 0;
if (!$userId) {
    echo json_encode(["success" => false, "message" => "Thiếu user_id"], JSON_UNESCAPED_UNICODE);
    exit;
}

$sql  = "SELECT o.OrderID, o.OrderDate, o.DeliveryAddress, o.TotalPrice, o.Status,
                od.ProductID, od.Quantity, od.UnitPrice, p.ProductName, p.ImageURL
         FROM orders o
         JOIN orderdetail od ON o.OrderID = od.OrderID
         JOIN product p      ON od.ProductID = p.ProductID
         WHERE o.UserID = ?
         ORDER BY o.OrderDate DESC";

$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $userId);
$stmt->execute();
$result = $stmt->get_result();

// Gom nhóm theo đơn hàng
$orders = [];
while ($row = $result->fetch_assoc()) {
    $oid = $row['OrderID'];
    if (!isset($orders[$oid])) {
        $orders[$oid] = [
            "id"              => $oid,
            "orderDate"       => $row['OrderDate'],
            "deliveryAddress" => $row['DeliveryAddress'],
            "totalPrice"      => (int) $row['TotalPrice'],
            "status"          => $row['Status'],
            "items"           => []
        ];
    }
    $orders[$oid]["items"][] = [
        "productId"   => (int) $row['ProductID'],
        "productName" =>       $row['ProductName'],
        "imageUrl"    =>       $row['ImageURL'],
        "quantity"    => (int) $row['Quantity'],
        "unitPrice"   => (int) $row['UnitPrice'],
    ];
}

echo json_encode([
    "success" => true,
    "orders"  => array_values($orders)
], JSON_UNESCAPED_UNICODE);

$stmt->close();
$conn->close();
?>
