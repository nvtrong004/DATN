<?php
include 'db.php';

$conn->query("ALTER TABLE user ADD COLUMN IF NOT EXISTS IsActive TINYINT(1) NOT NULL DEFAULT 1");

$data = json_decode(file_get_contents("php://input"));

if (!isset($data->user_id) || !isset($data->is_active)) {
    echo json_encode(["success" => false, "message" => "Thiếu thông tin tài khoản!"], JSON_UNESCAPED_UNICODE);
    exit;
}

$userId = (int) $data->user_id;
$isActive = $data->is_active ? 1 : 0;

$stmt = $conn->prepare("UPDATE user SET IsActive = ? WHERE UserID = ?");
$stmt->bind_param("ii", $isActive, $userId);

if ($stmt->execute()) {
    $status = $isActive ? "Đã kích hoạt" : "Đã vô hiệu hóa";
    echo json_encode(["success" => true, "message" => $status], JSON_UNESCAPED_UNICODE);
} else {
    echo json_encode(["success" => false, "message" => "Cập nhật tài khoản thất bại!"], JSON_UNESCAPED_UNICODE);
}

$stmt->close();
$conn->close();
?>
