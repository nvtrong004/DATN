<?php
include 'db.php';
$data = json_decode(file_get_contents("php://input"));

if (!isset($data->user_id) || !isset($data->delivery_address) || !isset($data->items) || count($data->items) == 0) {
    echo json_encode(["success" => false, "message" => "Dữ liệu đơn hàng không hợp lệ!"], JSON_UNESCAPED_UNICODE);
    exit;
}

$userId = (int)$data->user_id;
$deliveryAddress = (string)$data->delivery_address;
$shippingFee = isset($data->shipping_fee) ? max(0, (int)$data->shipping_fee) : 15000;
$temperature = isset($data->temperature) && $data->temperature !== null ? (int)$data->temperature : null;
$weatherCondition = isset($data->weather_condition) ? trim((string)$data->weather_condition) : null;
if ($weatherCondition === '') {
    $weatherCondition = null;
}

$conn->begin_transaction();
try {
    $totalPrice = 0;
    foreach ($data->items as $item) {
        $totalPrice += (int)$item->unit_price * (int)$item->quantity;
    }
    $totalPrice += $shippingFee;

    $stmt = $conn->prepare(
        "INSERT INTO orders (UserID, OrderDate, DeliveryAddress, TotalPrice, Status)
         VALUES (?, NOW(), ?, ?, 'Chờ xác nhận')"
    );
    $stmt->bind_param("isi", $userId, $deliveryAddress, $totalPrice);
    $stmt->execute();
    $orderId = $conn->insert_id;
    $stmt->close();

    $detailStmt = $conn->prepare(
        "INSERT INTO orderdetail (OrderID, ProductID, Quantity, UnitPrice) VALUES (?, ?, ?, ?)"
    );
    foreach ($data->items as $item) {
        $productId = (int)$item->product_id;
        $quantity = (int)$item->quantity;
        $unitPrice = (int)$item->unit_price;
        $detailStmt->bind_param("iiii", $orderId, $productId, $quantity, $unitPrice);
        $detailStmt->execute();
    }
    $detailStmt->close();

    $historyStmt = $conn->prepare(
        "INSERT INTO ai_orderhistory (UserID, ProductID, PurchaseDate, Temperature, WeatherCondition)
         VALUES (?, ?, NOW(), ?, ?)"
    );
    if ($historyStmt) {
        foreach ($data->items as $item) {
            $productId = (int)$item->product_id;
            $historyStmt->bind_param("iiis", $userId, $productId, $temperature, $weatherCondition);
            $historyStmt->execute();
        }
        $historyStmt->close();
    }

    $conn->commit();
    echo json_encode([
        "success"  => true,
        "message"  => "Đặt hàng thành công!",
        "order_id" => $orderId
    ], JSON_UNESCAPED_UNICODE);
} catch (Exception $e) {
    $conn->rollback();
    echo json_encode(["success" => false, "message" => "Đặt hàng thất bại: " . $e->getMessage()], JSON_UNESCAPED_UNICODE);
}

$conn->close();
?>
