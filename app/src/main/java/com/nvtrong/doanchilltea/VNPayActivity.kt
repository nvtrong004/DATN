package com.nvtrong.doanchilltea

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nvtrong.doanchilltea.model.CartManager
import com.nvtrong.doanchilltea.network.CreateVnPayRequest
import com.nvtrong.doanchilltea.network.CreateVnPayResponse
import com.nvtrong.doanchilltea.network.PlaceOrderItem
import com.nvtrong.doanchilltea.network.PlaceOrderRequest
import com.nvtrong.doanchilltea.network.PlaceOrderResponse
import com.nvtrong.doanchilltea.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VNPayActivity : AppCompatActivity() {

    companion object {
        private const val FIRST_ORDER_DISCOUNT_RATE = 0.2
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_vnpay)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.vnpay)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val amount = intent.getIntExtra("AMOUNT", 0)
        val webView = findViewById<WebView>(R.id.webViewVNPay)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                return when {
                    url.startsWith("chilltea://payment-success") -> {
                        placePaidOrder()
                        true
                    }
                    url.startsWith("chilltea://payment-failed") -> {
                        Toast.makeText(this@VNPayActivity, "Thanh toán VNPay thất bại", Toast.LENGTH_LONG).show()
                        finish()
                        true
                    }
                    else -> false
                }
            }
        }

        RetrofitClient.instance.createVnPayPayment(CreateVnPayRequest(amount))
            .enqueue(object : Callback<CreateVnPayResponse> {
                override fun onResponse(
                    call: Call<CreateVnPayResponse>,
                    response: Response<CreateVnPayResponse>
                ) {
                    val paymentUrl = response.body()?.payment_url
                    if (response.isSuccessful && response.body()?.success == true && !paymentUrl.isNullOrBlank()) {
                        webView.loadUrl(paymentUrl)
                    } else {
                        Toast.makeText(
                            this@VNPayActivity,
                            response.body()?.message ?: "Không tạo được liên kết VNPay",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                }

                override fun onFailure(call: Call<CreateVnPayResponse>, t: Throwable) {
                    Toast.makeText(this@VNPayActivity, "Lỗi kết nối VNPay: ${t.message}", Toast.LENGTH_LONG).show()
                    finish()
                }
            })
    }

    private fun placePaidOrder() {
        val userId = intent.getIntExtra("USER_ID", -1)
        val address = intent.getStringExtra("DELIVERY_ADDRESS").orEmpty()
        val shippingFee = intent.getIntExtra("SHIPPING_FEE", 0)
        val discountAmount = intent.getIntExtra("DISCOUNT_AMOUNT", 0)
        val weatherTemp = intent.getIntExtra("WEATHER_TEMP", Int.MIN_VALUE).takeIf { it != Int.MIN_VALUE }
        val weatherCondition = intent.getStringExtra("WEATHER_CONDITION")
        val cartItems = CartManager.getMutableItems()

        if (userId <= 0 || address.isBlank() || cartItems.isEmpty()) {
            Toast.makeText(this, "Không thể tạo đơn sau thanh toán", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val orderItems = cartItems.map {
            val unitPrice = if (discountAmount > 0) {
                (it.price * (1 - FIRST_ORDER_DISCOUNT_RATE)).toInt()
            } else {
                it.price
            }
            PlaceOrderItem(it.id, it.quantity, unitPrice)
        }
        val request = PlaceOrderRequest(
            user_id = userId,
            delivery_address = address,
            items = orderItems,
            shipping_fee = shippingFee,
            discount_amount = discountAmount,
            temperature = weatherTemp,
            weather_condition = weatherCondition
        )

        RetrofitClient.instance.placeOrder(request).enqueue(object : Callback<PlaceOrderResponse> {
            override fun onResponse(call: Call<PlaceOrderResponse>, response: Response<PlaceOrderResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    CartManager.clear()
                    Toast.makeText(this@VNPayActivity, "Thanh toán VNPay thành công!", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@VNPayActivity, MyOrdersActivity::class.java).apply {
                        putExtra("USER_ID", userId)
                        putExtra("USER_NAME", intent.getStringExtra("USER_NAME") ?: "")
                        putExtra("BACK_TO_HOME", true)
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    })
                    finish()
                } else {
                    Toast.makeText(
                        this@VNPayActivity,
                        response.body()?.message ?: "Thanh toán thành công nhưng tạo đơn thất bại",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<PlaceOrderResponse>, t: Throwable) {
                Toast.makeText(this@VNPayActivity, "Lỗi tạo đơn: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
