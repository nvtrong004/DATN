<?php
include 'db.php';
$data = json_decode(file_get_contents("php://input"));

if (!isset($data->product_id) || !isset($data->is_active)) {
    echo json_encode(["success" => false, "message" => "Thiếu thông tin!"], JSON_UNESCAPED_UNICODE);
    exit;
}

$isActive = $data->is_active ? 1 : 0;
$stmt = $conn->prepare("UPDATE product SET IsActive = ? WHERE ProductID = ?");
$stmt->bind_param("ii", $isActive, $data->product_id);

if ($stmt->execute()) {
    $status = $isActive ? "Đang bán" : "Đã ẩn";
    echo json_encode(["success" => true, "message" => "Sản phẩm $status"], JSON_UNESCAPED_UNICODE);
} else {
    echo json_encode(["success" => false, "message" => "Cập nhật thất bại!"], JSON_UNESCAPED_UNICODE);
}

$stmt->close();
$conn->close();
?>
