<?php
include 'db.php';

$categoryId = isset($_GET['category_id']) ? (int) $_GET['category_id'] : null;
$includeInactive = isset($_GET['include_inactive']) && (int) $_GET['include_inactive'] === 1;
$activeClause = $includeInactive ? "1 = 1" : "p.IsActive = 1";

if ($categoryId) {
    $sql = "SELECT p.ProductID, p.CategoryID, c.CategoryName, p.ProductName, p.Price, p.ImageURL, p.IsActive
            FROM product p
            JOIN category c ON p.CategoryID = c.CategoryID
            WHERE $activeClause AND p.CategoryID = ?
            ORDER BY p.ProductName";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $categoryId);
} else {
    $sql = "SELECT p.ProductID, p.CategoryID, c.CategoryName, p.ProductName, p.Price, p.ImageURL, p.IsActive
            FROM product p
            JOIN category c ON p.CategoryID = c.CategoryID
            WHERE $activeClause
            ORDER BY p.CategoryID, p.ProductName";
    $stmt = $conn->prepare($sql);
}

$stmt->execute();
$result = $stmt->get_result();
$products = [];

while ($row = $result->fetch_assoc()) {
    $products[] = [
        "id" => (int) $row['ProductID'],
        "categoryId" => (int) $row['CategoryID'],
        "categoryName" => $row['CategoryName'],
        "name" => $row['ProductName'],
        "price" => (int) $row['Price'],
        "imageUrl" => $row['ImageURL'],
        "isActive" => (bool) $row['IsActive'],
    ];
}

echo json_encode(["success" => true, "products" => $products], JSON_UNESCAPED_UNICODE);
$stmt->close();
$conn->close();
?>
