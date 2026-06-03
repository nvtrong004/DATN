<?php
include 'db.php';

$data = json_decode(file_get_contents("php://input"));

if (!isset($data->name) || trim($data->name) === "") {
    echo json_encode(["success" => false, "message" => "Thiếu tên danh mục!"], JSON_UNESCAPED_UNICODE);
    exit;
}

$categoryName = trim($data->name);

$check = $conn->prepare("SELECT CategoryID FROM category WHERE CategoryName = ? LIMIT 1");
$check->bind_param("s", $categoryName);
$check->execute();
$check->store_result();

if ($check->num_rows > 0) {
    echo json_encode(["success" => false, "message" => "Danh mục đã tồn tại!"], JSON_UNESCAPED_UNICODE);
    $check->close();
    $conn->close();
    exit;
}
$check->close();

$stmt = $conn->prepare("INSERT INTO category (CategoryName) VALUES (?)");
$stmt->bind_param("s", $categoryName);

if ($stmt->execute()) {
    echo json_encode([
        "success" => true,
        "message" => "Thêm danh mục thành công!",
        "category" => [
            "id" => $conn->insert_id,
            "name" => $categoryName
        ]
    ], JSON_UNESCAPED_UNICODE);
} else {
    echo json_encode(["success" => false, "message" => "Thêm danh mục thất bại!"], JSON_UNESCAPED_UNICODE);
}

$stmt->close();
$conn->close();
?>
