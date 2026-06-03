<?php
include 'db.php';

$period = isset($_GET['period']) ? $_GET['period'] : 'today';

// Xác định khoảng thời gian
switch ($period) {
    case 'week':
        $dateFilter = "o.OrderDate >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
        break;
    case 'month':
        $dateFilter = "MONTH(o.OrderDate) = MONTH(CURDATE()) AND YEAR(o.OrderDate) = YEAR(CURDATE())";
        break;
    default: // today
        $dateFilter = "DATE(o.OrderDate) = CURDATE()";
        break;
}

// Tổng doanh thu + đơn hoàn thành + đơn hủy
$stats = $conn->query(
    "SELECT
        COALESCE(SUM(CASE WHEN Status = 'Đã hoàn thành' THEN TotalPrice ELSE 0 END), 0) AS revenue,
        COUNT(CASE WHEN Status = 'Đã hoàn thành' THEN 1 END)                            AS successOrders,
        COUNT(CASE WHEN Status = 'Đã hủy'         THEN 1 END)                            AS canceledOrders
     FROM orders o WHERE $dateFilter"
)->fetch_assoc();

// Top 5 sản phẩm bán chạy
$topStmt = $conn->prepare(
    "SELECT p.ProductID, p.ProductName, p.ImageURL, SUM(od.Quantity) AS totalSold
     FROM orderdetail od
     JOIN orders o  ON od.OrderID  = o.OrderID
     JOIN product p ON od.ProductID = p.ProductID
     WHERE $dateFilter AND o.Status = 'Đã hoàn thành'
     GROUP BY od.ProductID
     ORDER BY totalSold DESC
     LIMIT 5"
);
$topStmt->execute();
$topResult = $topStmt->get_result();
$topProducts = [];
$rank = 1;
while ($row = $topResult->fetch_assoc()) {
    $topProducts[] = [
        "rank"      => $rank++,
        "name"      => $row['ProductName'],
        "imageUrl"  => $row['ImageURL'],
        "totalSold" => (int) $row['totalSold'],
    ];
}

echo json_encode([
    "success"      => true,
    "revenue"      => (int) $stats['revenue'],
    "successOrders"=> (int) $stats['successOrders'],
    "canceledOrders"=> (int) $stats['canceledOrders'],
    "topProducts"  => $topProducts
], JSON_UNESCAPED_UNICODE);

$topStmt->close();
$conn->close();
?>
