<?php
include 'db.php';

$data = json_decode(file_get_contents("php://input"));

if (!isset($data->phone) || !isset($data->password) || !isset($data->fullname)) {
    echo json_encode(
    ["success" => false, "message" => "Vui lòng nhập đủ thông tin!"], 
    JSON_UNESCAPED_UNICODE
);
    exit;
}

$phone = $data->phone;
$fullname = $data->fullname;

// MÃ HÓA MẬT KHẨU TẠI ĐÂY (Dùng chuẩn mã hóa Bcrypt an toàn nhất hiện nay)
$hashed_password = password_hash($data->password, PASSWORD_DEFAULT);

// 1. Kiểm tra xem số điện thoại đã tồn tại chưa
$checkSql = "SELECT UserID FROM user WHERE Phone = ?";
$stmtCheck = $conn->prepare($checkSql);
$stmtCheck->bind_param("s", $phone);
$stmtCheck->execute();
$stmtCheck->store_result();

if ($stmtCheck->num_rows > 0) {
    echo json_encode(["success" => false, "message" => "Số điện thoại này đã được đăng ký!"]);
    $stmtCheck->close();
    exit;
}
$stmtCheck->close();

// 2. Tiến hành tạo tài khoản với mật khẩu đã mã hóa
$insertSql = "INSERT INTO user (Phone, PasswordHash, FullName, Role) VALUES (?, ?, ?, 0)";
$stmtInsert = $conn->prepare($insertSql);

// Truyền biến $hashed_password vào thay vì pass gốc
$stmtInsert->bind_param("sss", $phone, $hashed_password, $fullname);

if ($stmtInsert->execute()) {
    echo json_encode(["success" => true, "message" => "Đăng ký thành công!"]);
} else {
    echo json_encode(["success" => false, "message" => "Lỗi hệ thống, không thể đăng ký."]);
}

$stmtInsert->close();
$conn->close();
?>