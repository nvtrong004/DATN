<?php
include 'db.php';

$conn->query("ALTER TABLE user ADD COLUMN IF NOT EXISTS IsActive TINYINT(1) NOT NULL DEFAULT 1");

$data = json_decode(file_get_contents("php://input"));

if (!isset($data->phone) || !isset($data->password)) {
    echo json_encode(["success" => false, "message" => "Vui lòng nhập đủ thông tin!"], JSON_UNESCAPED_UNICODE);
    exit;
}

$phone = $data->phone;
$password = $data->password;

$sql = "SELECT UserID, FullName, PasswordHash, Role, IsActive FROM user WHERE Phone = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("s", $phone);
$stmt->execute();
$result = $stmt->get_result();

if ($user = $result->fetch_assoc()) {
    if ((int) $user['IsActive'] !== 1) {
        echo json_encode(["success" => false, "message" => "Tài khoản đã bị vô hiệu hóa!"], JSON_UNESCAPED_UNICODE);
        exit;
    }

    if (password_verify($password, $user['PasswordHash'])) {
        echo json_encode([
            "success" => true,
            "message" => "Đăng nhập thành công!",
            "user" => [
                "id" => $user['UserID'],
                "fullname" => $user['FullName'],
               "role"     => (int) $user['Role']
            ]
        ], JSON_UNESCAPED_UNICODE);
    } else {
        echo json_encode(["success" => false, "message" => "Sai mật khẩu!"], JSON_UNESCAPED_UNICODE);
    }
} else {
    echo json_encode(["success" => false, "message" => "Số điện thoại chưa được đăng ký!"], JSON_UNESCAPED_UNICODE);
}

$stmt->close();
$conn->close();
?>
