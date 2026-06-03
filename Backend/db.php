<?php

header("Content-Type: application/json; charset=UTF-8");

$host = "localhost";
$username = "root";       
$password = "";           
$dbname = "chill_tea";    

$conn = new mysqli($host, $username, $password, $dbname);

if ($conn->connect_error) {
    die(json_encode(["success" => false, "message" => "Lỗi kết nối cơ sở dữ liệu!"]));
}

$conn->set_charset("utf8mb4");
?>
