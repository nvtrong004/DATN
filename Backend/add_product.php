<?php
include 'db.php';
$data = json_decode(file_get_contents("php://input"));

if (!isset($data->category_id) || !isset($data->product_name) || !isset($data->price)) {
    echo json_encode(["success" => false, "message" => "Thiếu thông tin!"], JSON_UNESCAPED_UNICODE);
    exit;
}

$imageUrl = isset($data->image_url) ? $data->image_url : null;

$stmt = $conn->prepare(
    "INSERT INTO product (CategoryID, ProductName, Price, ImageURL, IsActive) VALUES (?, ?, ?, ?, 1)"
);
$stmt->bind_param("isis", $data->category_id, $data->product_name, $data->price, $imageUrl);

if ($stmt->execute()) {
    echo json_encode(["success" => true, "message" => "Thêm món thành công!"], JSON_UNESCAPED_UNICODE);
} else {
    echo json_encode(["success" => false, "message" => "Thêm thất bại!"], JSON_UNESCAPED_UNICODE);
}
$stmt->close(); $conn->close();
?>
