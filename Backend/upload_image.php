<?php
include 'db.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(["success" => false, "message" => "Phương thức không hợp lệ!"], JSON_UNESCAPED_UNICODE);
    exit;
}

if (!isset($_FILES['image']) || $_FILES['image']['error'] !== UPLOAD_ERR_OK) {
    echo json_encode(["success" => false, "message" => "Chưa nhận được ảnh!"], JSON_UNESCAPED_UNICODE);
    exit;
}

$allowedExtensions = ['jpg', 'jpeg', 'png', 'webp'];
$originalName = $_FILES['image']['name'];
$extension = strtolower(pathinfo($originalName, PATHINFO_EXTENSION));

if (!in_array($extension, $allowedExtensions)) {
    echo json_encode(["success" => false, "message" => "Chỉ hỗ trợ ảnh JPG, PNG, WEBP!"], JSON_UNESCAPED_UNICODE);
    exit;
}

$uploadDir = __DIR__ . '/uploads/';
if (!is_dir($uploadDir)) {
    mkdir($uploadDir, 0777, true);
}

$fileName = 'product_' . time() . '_' . bin2hex(random_bytes(4)) . '.' . $extension;
$targetPath = $uploadDir . $fileName;

if (!move_uploaded_file($_FILES['image']['tmp_name'], $targetPath)) {
    echo json_encode(["success" => false, "message" => "Lưu ảnh thất bại!"], JSON_UNESCAPED_UNICODE);
    exit;
}

$scheme = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') ? 'https' : 'http';
$host = $_SERVER['HTTP_HOST'];
$basePath = rtrim(dirname($_SERVER['SCRIPT_NAME']), '/\\');
$imageUrl = $scheme . '://' . $host . $basePath . '/uploads/' . $fileName;

echo json_encode([
    "success" => true,
    "message" => "Tải ảnh thành công!",
    "image_url" => $imageUrl
], JSON_UNESCAPED_UNICODE);

$conn->close();
?>
