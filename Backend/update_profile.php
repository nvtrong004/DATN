<?php
include 'db.php';
$data = json_decode(file_get_contents("php://input"));

if (!isset($data->user_id) || !isset($data->fullname)) {
    echo json_encode(["success" => false, "message" => "Thiếu thông tin!"], JSON_UNESCAPED_UNICODE);
    exit;
}

$address = isset($data->address) ? $data->address : null;
$stmt = $conn->prepare("UPDATE user SET FullName = ?, Address = ? WHERE UserID = ?");
$stmt->bind_param("ssi", $data->fullname, $address, $data->user_id);

if ($stmt->execute()) {
    echo json_encode(["success" => true, "message" => "Cập nhật thành công!"], JSON_UNESCAPED_UNICODE);
} else {
    echo json_encode(["success" => false, "message" => "Cập nhật thất bại!"], JSON_UNESCAPED_UNICODE);
}

$stmt->close();
$conn->close();
?>
