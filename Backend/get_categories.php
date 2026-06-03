<?php
include 'db.php';

$result     = $conn->query("SELECT CategoryID, CategoryName FROM category ORDER BY CategoryName");
$categories = [];

while ($row = $result->fetch_assoc()) {
    $categories[] = [
        "id"   => (int) $row['CategoryID'],
        "name" =>       $row['CategoryName'],
    ];
}

echo json_encode(["success" => true, "categories" => $categories], JSON_UNESCAPED_UNICODE);
$conn->close();
?>
