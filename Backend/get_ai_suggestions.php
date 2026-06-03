<?php
include 'db.php';

$userId = isset($_GET['user_id']) ? (int)$_GET['user_id'] : 0;
$temperature = isset($_GET['temperature']) && $_GET['temperature'] !== '' ? (int)$_GET['temperature'] : 999;
$weatherCondition = isset($_GET['weather_condition']) ? trim((string)$_GET['weather_condition']) : '';

if (!$userId) {
    echo json_encode(["success" => false, "products" => [], "message" => "Thiếu user_id"], JSON_UNESCAPED_UNICODE);
    exit;
}

$sql = "SELECT
            p.ProductID,
            p.CategoryID,
            c.CategoryName,
            p.ProductName,
            p.Price,
            p.ImageURL,
            p.IsActive,
            COUNT(*) AS purchaseCount,
            SUM(CASE WHEN h.WeatherCondition = ? THEN 1 ELSE 0 END) AS weatherScore,
            SUM(CASE WHEN h.Temperature IS NOT NULL AND ABS(h.Temperature - ?) <= 3 THEN 1 ELSE 0 END) AS tempScore,
            MAX(h.PurchaseDate) AS lastPurchase
        FROM ai_orderhistory h
        JOIN product p ON h.ProductID = p.ProductID
        JOIN category c ON p.CategoryID = c.CategoryID
        WHERE h.UserID = ? AND p.IsActive = 1
        GROUP BY p.ProductID, p.CategoryID, c.CategoryName, p.ProductName, p.Price, p.ImageURL, p.IsActive
        ORDER BY weatherScore DESC, tempScore DESC, purchaseCount DESC, lastPurchase DESC
        LIMIT 10";

$stmt = $conn->prepare($sql);
$stmt->bind_param("sii", $weatherCondition, $temperature, $userId);
$stmt->execute();
$result = $stmt->get_result();
$products = [];

while ($row = $result->fetch_assoc()) {
    $products[] = [
        "id" => (int)$row['ProductID'],
        "categoryId" => (int)$row['CategoryID'],
        "categoryName" => $row['CategoryName'],
        "name" => $row['ProductName'],
        "price" => (int)$row['Price'],
        "imageUrl" => $row['ImageURL'],
        "isActive" => (bool)$row['IsActive'],
    ];
}

$stmt->close();
$conn->close();

echo json_encode(["success" => true, "products" => $products], JSON_UNESCAPED_UNICODE);
?>
