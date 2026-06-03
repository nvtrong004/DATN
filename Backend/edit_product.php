<?php
include 'db.php';
$data = json_decode(file_get_contents("php://input"));

if (!isset($data->product_id) || !isset($data->product_name) || !isset($data->price) || !isset($data->category_id)) {
    echo json_encode(["success" => false, "message" => "Thiếu thông tin!"], JSON_UNESCAPED_UNICODE);
    exit;
}

$imageUrl = isset($data->image_url) ? $data->image_url : null;
$isActive = isset($data->is_active) ? (int)$data->is_active : 1;

$stmt = $conn->prepare(
    "UPDATE product SET CategoryID=?, ProductName=?, Price=?, ImageURL=?, IsActive=? WHERE ProductID=?"
);
$stmt->bind_param("isisii",
    $data->category_id, $data->product_name,
    $data->price, $imageUrl, $isActive, $data->product_id
);

if ($stmt->execute()) {
    echo json_encode(["success" => true, "message" => "Cập nhật thành công!"], JSON_UNESCAPED_UNICODE);
} else {
    echo json_encode(["success" => false, "message" => "Cập nhật thất bại!"], JSON_UNESCAPED_UNICODE);
}
$stmt->close(); $conn->close();
?>
