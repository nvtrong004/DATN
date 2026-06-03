<?php
include 'db.php';

$conn->query("ALTER TABLE user ADD COLUMN IF NOT EXISTS IsActive TINYINT(1) NOT NULL DEFAULT 1");

$search = isset($_GET['search']) ? '%' . $_GET['search'] . '%' : '%%';

$sql  = "SELECT UserID, FullName, Phone, Role, IsActive FROM user WHERE FullName LIKE ? OR Phone LIKE ? ORDER BY UserID";
$stmt = $conn->prepare($sql);
$stmt->bind_param("ss", $search, $search);
$stmt->execute();
$result   = $stmt->get_result();
$accounts = [];

while ($row = $result->fetch_assoc()) {
    $accounts[] = [
        "id"    => (int) $row['UserID'],
        "name"  =>       $row['FullName'],
        "phone" =>       $row['Phone'],
        "role"  => (int) $row['Role'],
        "isActive" => (bool) $row['IsActive'],
    ];
}

echo json_encode(["success" => true, "accounts" => $accounts], JSON_UNESCAPED_UNICODE);
$stmt->close();
$conn->close();
?>
