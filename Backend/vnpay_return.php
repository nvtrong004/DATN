<?php
include 'vnpay_config.php';

error_reporting(E_ALL & ~E_NOTICE & ~E_DEPRECATED);

$inputData = [];
foreach ($_GET as $key => $value) {
    if (substr($key, 0, 4) === 'vnp_' && $key !== 'vnp_SecureHash' && $key !== 'vnp_SecureHashType') {
        $inputData[$key] = $value;
    }
}

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

$secureHash = $_GET['vnp_SecureHash'] ?? '';
$calculatedHash = hash_hmac('sha512', $hashData, trim($vnp_HashSecret));
$isValidHash = $secureHash !== '' && hash_equals($calculatedHash, $secureHash);
$isSuccess = $isValidHash
    && ($_GET['vnp_ResponseCode'] ?? '') === '00'
    && ($_GET['vnp_TransactionStatus'] ?? '') === '00';

$target = $isSuccess ? 'chilltea://payment-success' : 'chilltea://payment-failed';
header('Location: ' . $target);
exit;
?>