<?php
include 'vnpay_config.php';

header('Content-Type: application/json; charset=utf-8');
error_reporting(E_ALL & ~E_NOTICE & ~E_DEPRECATED);
date_default_timezone_set('Asia/Ho_Chi_Minh');

function json_response($success, $message, $paymentUrl = null) {
    echo json_encode([
        'success' => $success,
        'message' => $message,
        'payment_url' => $paymentUrl,
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

function get_client_ip() {
    if (!empty($_SERVER['HTTP_CLIENT_IP'])) {
        return $_SERVER['HTTP_CLIENT_IP'];
    }
    if (!empty($_SERVER['HTTP_X_FORWARDED_FOR'])) {
        return trim(explode(',', $_SERVER['HTTP_X_FORWARDED_FOR'])[0]);
    }
    return $_SERVER['REMOTE_ADDR'] ?? '127.0.0.1';
}

$data = json_decode(file_get_contents('php://input'));
$amount = isset($data->amount) ? (int)$data->amount : 0;

if ($amount <= 0) {
    json_response(false, 'Số tiền thanh toán không hợp lệ!');
}

if (empty($vnp_TmnCode) || empty($vnp_HashSecret) || empty($vnp_Url)) {
    json_response(false, 'Thiếu cấu hình VNPay trên server!');
}

$scheme = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') ? 'https' : 'http';
$host = $_SERVER['HTTP_HOST'] ?? 'localhost';
$returnUrl = $scheme . '://' . $host . '/chilltea/vnpay_return.php';
$txnRef = 'CT' . date('YmdHis') . random_int(1000, 9999);
$createDate = date('YmdHis');
$expireDate = date('YmdHis', strtotime('+15 minutes'));

$inputData = [
    'vnp_Version' => '2.1.0',
    'vnp_TmnCode' => trim($vnp_TmnCode),
    'vnp_Amount' => $amount * 100,
    'vnp_Command' => 'pay',
    'vnp_CreateDate' => $createDate,
    'vnp_CurrCode' => 'VND',
    'vnp_IpAddr' => get_client_ip(),
    'vnp_Locale' => 'vn',
    'vnp_OrderInfo' => 'Thanh toán đơn hàng Chill Tea ' . $txnRef,
    'vnp_OrderType' => 'other',
    'vnp_ReturnUrl' => $returnUrl,
    'vnp_TxnRef' => $txnRef,
    'vnp_ExpireDate' => $expireDate,
];

ksort($inputData);
$query = '';
$hashData = '';
$i = 0;
foreach ($inputData as $key => $value) {
    if ($i == 1) {
        $hashData .= '&' . urlencode($key) . '=' . urlencode((string)$value);
    } else {
        $hashData .= urlencode($key) . '=' . urlencode((string)$value);
        $i = 1;
    }
    $query .= urlencode($key) . '=' . urlencode((string)$value) . '&';
}

$secureHash = hash_hmac('sha512', $hashData, trim($vnp_HashSecret));
$paymentUrl = $vnp_Url . '?' . $query . 'vnp_SecureHash=' . $secureHash;

json_response(true, 'Tạo liên kết VNPay thành công!', $paymentUrl);
?>