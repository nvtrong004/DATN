<?php
include 'db.php';
$data = json_decode(file_get_contents("php://input"));

if (!isset($data->order_id) || !isset($data->status)) {
    echo json_encode(["success" => false, "message" => "Thiếu thông tin!"], JSON_UNESCAPED_UNICODE);
    exit;
}

$allowed = ['Chờ xác nhận', 'Đang giao', 'Đã hoàn thành', 'Đã hủy'];
if (!in_array($data->status, $allowed)) {
    echo json_encode(["success" => false, "message" => "Trạng thái không hợp lệ!"], JSON_UNESCAPED_UNICODE);
    exit;
}

$stmt = $conn->prepare("UPDATE orders SET Status = ? WHERE OrderID = ?");
$stmt->bind_param("si", $data->status, $data->order_id);

if ($stmt->execute()) {
    echo json_encode(["success" => true, "message" => "Cập nhật trạng thái thành công!"], JSON_UNESCAPED_UNICODE);
} else {
    echo json_encode(["success" => false, "message" => "Cập nhật thất bại!"], JSON_UNESCAPED_UNICODE);
}

$stmt->close();
$conn->close();
?>
