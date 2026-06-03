<?php
include 'vnpay_config.php';

header('Content-Type: application/json; charset=utf-8');
error_reporting(E_ALL & ~E_NOTICE & ~E_DEPRECATED);

function vnpay_ipn_response($code, $message) {
    echo json_encode([
        'RspCode' => $code,
        'Message' => $message,
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

$inputData = [];
foreach ($_GET as $key => $value) {
    if (substr($key, 0, 4) === 'vnp_') {
        $inputData[$key] = $value;
    }
}

if (empty($inputData)) {
    vnpay_ipn_response('00', 'IPN URL is reachable');
}

if (empty($inputData['vnp_SecureHash'])) {
    vnpay_ipn_response('99', 'Invalid request');
}

$vnpSecureHash = $inputData['vnp_SecureHash'];
unset($inputData['vnp_SecureHash']);
unset($inputData['vnp_SecureHashType']);

ksort($inputData);
$hashData = '';
$i = 0;
foreach ($inputData as $key => $value) {
    if ($i == 1) {
        $hashData .= '&' . urlencode($key) . '=' . urlencode((string)$value);
    } else {
        $hashData .= urlencode($key) . '=' . urlencode((string)$value);
        $i = 1;
    }
}

$secureHash = hash_hmac('sha512', $hashData, trim($vnp_HashSecret));
if (!hash_equals($secureHash, $vnpSecureHash)) {
    vnpay_ipn_response('97', 'Invalid signature');
}

vnpay_ipn_response('00', 'Confirm Success');
?>