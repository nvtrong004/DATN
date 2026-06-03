<?php
include 'db.php';

$userId = isset($_GET['user_id']) ? (int)$_GET['user_id'] : 0;
if (!$userId) {
    echo json_encode(["success" => false, "message" => "Thiếu user_id"], JSON_UNESCAPED_UNICODE);
    exit;
}

$stmt = $conn->prepare("SELECT UserID, FullName, Phone, Address FROM user WHERE UserID = ?");
$stmt->bind_param("i", $userId);
$stmt->execute();
$user = $stmt->get_result()->fetch_assoc();

if ($user) {
    echo json_encode([
        "success" => true,
        "user" => [
            "id"       => (int) $user['UserID'],
            "fullname" => $user['FullName'],
            "phone"    => $user['Phone'],
            "address"  => $user['Address'],
        ]
    ], JSON_UNESCAPED_UNICODE);
} else {
    echo json_encode(["success" => false, "message" => "Không tìm thấy người dùng!"], JSON_UNESCAPED_UNICODE);
}

$stmt->close(); $conn->close();
?>
