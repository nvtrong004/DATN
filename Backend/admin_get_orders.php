<?php
include 'db.php';

$status = isset($_GET['status']) ? $_GET['status'] : null;

if ($status && $status !== 'Tất cả') {
    $sql  = "SELECT o.OrderID, o.UserID, u.FullName, o.OrderDate, o.TotalPrice, o.Status
             FROM orders o JOIN user u ON o.UserID = u.UserID
             WHERE o.Status = ?
             ORDER BY o.OrderDate DESC";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("s", $status);
} else {
    $sql  = "SELECT o.OrderID, o.UserID, u.FullName, o.OrderDate, o.TotalPrice, o.Status
             FROM orders o JOIN user u ON o.UserID = u.UserID
             ORDER BY o.OrderDate DESC";
    $stmt = $conn->prepare($sql);
}

$stmt->execute();
$result = $stmt->get_result();
$orders = [];

while ($row = $result->fetch_assoc()) {
    $orders[] = [
        "id"           => (int) $row['OrderID'],
        "customerName" =>       $row['FullName'],
        "date"         =>       $row['OrderDate'],
        "total"        => (int) $row['TotalPrice'],
        "status"       =>       $row['Status'],
    ];
}

echo json_encode(["success" => true, "orders" => $orders], JSON_UNESCAPED_UNICODE);
$stmt->close();
$conn->close();
?>
