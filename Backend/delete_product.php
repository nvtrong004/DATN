<?php
include 'db.php';
$data = json_decode(file_get_contents("php://input"));

if (!isset($data->product_id)) {
    echo json_encode(["success" => false, "message" => "Thiếu product_id!"], JSON_UNESCAPED_UNICODE);
    exit;
}

// Kiểm tra xem sản phẩm có trong orderdetail không (nếu có thì chỉ ẩn, không xóa)
$check = $conn->prepare("SELECT COUNT(*) as cnt FROM orderdetail WHERE ProductID = ?");
$check->bind_param("i", $data->product_id);
$check->execute();
$row = $check->get_result()->fetch_assoc();
$check->close();

if ($row['cnt'] > 0) {
    // Có trong đơn hàng → chỉ ẩn đi (IsActive = 0)
    $stmt = $conn->prepare("UPDATE product SET IsActive = 0 WHERE ProductID = ?");
    $stmt->bind_param("i", $data->product_id);
    $stmt->execute();
    echo json_encode(["success" => true, "message" => "Món đã được ẩn (vì đã có trong đơn hàng)"], JSON_UNESCAPED_UNICODE);
} else {
    // Chưa có trong đơn → xóa hẳn
    $stmt = $conn->prepare("DELETE FROM product WHERE ProductID = ?");
    $stmt->bind_param("i", $data->product_id);
    if ($stmt->execute()) {
        echo json_encode(["success" => true, "message" => "Xóa món thành công!"], JSON_UNESCAPED_UNICODE);
    } else {
        echo json_encode(["success" => false, "message" => "Xóa thất bại!"], JSON_UNESCAPED_UNICODE);
    }
}

$stmt->close(); $conn->close();
?>
