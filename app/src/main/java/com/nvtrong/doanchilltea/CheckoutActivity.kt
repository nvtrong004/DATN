package com.nvtrong.doanchilltea

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.nvtrong.doanchilltea.model.CartManager
import com.nvtrong.doanchilltea.network.MyOrdersResponse
import com.nvtrong.doanchilltea.network.PlaceOrderItem
import com.nvtrong.doanchilltea.network.PlaceOrderRequest
import com.nvtrong.doanchilltea.network.PlaceOrderResponse
import com.nvtrong.doanchilltea.network.RetrofitClient
import com.nvtrong.doanchilltea.network.UserProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CheckoutActivity : AppCompatActivity() {

    private val SHIPPING_FEE = 15_000
    private val FREE_SHIPPING_MIN_SUBTOTAL = 50_000
    private val FIRST_ORDER_DISCOUNT_RATE = 0.2
    private var selectedPayment = "Tiền mặt"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_checkout)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_checkout)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val userId   = intent.getIntExtra("USER_ID", -1)
        val subtotal = intent.getIntExtra("SUBTOTAL", 0)
        val weatherTemp = intent.getIntExtra("WEATHER_TEMP", Int.MIN_VALUE)
            .takeIf { it != Int.MIN_VALUE }
        val weatherCondition = intent.getStringExtra("WEATHER_CONDITION")
        var shippingFee = SHIPPING_FEE
        var discountAmount = 0
        var total = subtotal + shippingFee - discountAmount

        val btnBack           = findViewById<ImageView>(R.id.btnBack)
        val rvItems           = findViewById<RecyclerView>(R.id.rvCheckoutItems)
        val edtAddress        = findViewById<TextInputEditText>(R.id.edtDeliveryAddress)
        val layoutPayment     = findViewById<LinearLayout>(R.id.layoutPaymentMethod)
        val tvSelectedPayment = findViewById<TextView>(R.id.tvSelectedPayment)
        val tvSubtotal        = findViewById<TextView>(R.id.tvSubtotal)
        val tvShippingFee     = findViewById<TextView>(R.id.tvShippingFee)
        val discountLayout    = findViewById<RelativeLayout>(R.id.layoutFirstOrderDiscount)
        val tvDiscount        = findViewById<TextView>(R.id.tvFirstOrderDiscount)
        val tvTotalDetail     = findViewById<TextView>(R.id.tvCheckoutTotalDetail)
        val tvTotal           = findViewById<TextView>(R.id.tvCheckoutTotal)
        val btnPlaceOrder     = findViewById<MaterialButton>(R.id.btnPlaceOrder)
        val progressBar       = findViewById<View>(R.id.progressCheckout)

        btnBack.setOnClickListener { finish() }
        tvSubtotal.text = "%,d đ".format(subtotal)
        updateTotals(tvShippingFee, discountLayout, tvDiscount, tvTotalDetail, tvTotal, shippingFee, discountAmount, total)

        if (userId > 0) {
            RetrofitClient.instance.getUserProfile(userId).enqueue(object : Callback<UserProfileResponse> {
                override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                    val address = response.body()?.user?.address.orEmpty()
                    if (address.isNotBlank()) edtAddress.setText(address)
                }

                override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {}
            })

            RetrofitClient.instance.getMyOrders(userId).enqueue(object : Callback<MyOrdersResponse> {
                override fun onResponse(call: Call<MyOrdersResponse>, response: Response<MyOrdersResponse>) {
                    val isFirstOrder = response.isSuccessful &&
                        response.body()?.success == true &&
                        response.body()?.orders.orEmpty().isEmpty()

                    shippingFee = if (isFirstOrder || subtotal > FREE_SHIPPING_MIN_SUBTOTAL) 0 else SHIPPING_FEE
                    discountAmount = if (isFirstOrder) (subtotal * FIRST_ORDER_DISCOUNT_RATE).toInt() else 0
                    total = subtotal + shippingFee - discountAmount
                    updateTotals(tvShippingFee, discountLayout, tvDiscount, tvTotalDetail, tvTotal, shippingFee, discountAmount, total)
                }

                override fun onFailure(call: Call<MyOrdersResponse>, t: Throwable) {
                    shippingFee = SHIPPING_FEE
                    discountAmount = 0
                    total = subtotal + shippingFee
                    updateTotals(tvShippingFee, discountLayout, tvDiscount, tvTotalDetail, tvTotal, shippingFee, discountAmount, total)
                }
            })
        }

        val cartItems = CartManager.getMutableItems()
        rvItems.adapter = CartAdapter(cartItems, editable = false) {}
        rvItems.layoutManager = LinearLayoutManager(this)
        rvItems.isNestedScrollingEnabled = false


        layoutPayment.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menu.add("Tiền mặt")
            popup.menu.add("VN Pay")
            popup.menu.add("MoMo (Đang phát triển)")
            popup.setOnMenuItemClickListener {
                selectedPayment = it.title.toString()
                tvSelectedPayment.text = selectedPayment
                true
            }
            popup.show()
        }


        btnPlaceOrder.setOnClickListener {
            val address = edtAddress?.text.toString().trim()
            if (address.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập địa chỉ giao hàng!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng trống!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedPayment == "VN Pay") {
                val intent = Intent(this, VNPayActivity::class.java)
                intent.putExtra("AMOUNT", total)
                intent.putExtra("USER_ID", userId)
                intent.putExtra("USER_NAME", this.intent.getStringExtra("USER_NAME") ?: "")
                intent.putExtra("DELIVERY_ADDRESS", address)
                intent.putExtra("SHIPPING_FEE", shippingFee)
                intent.putExtra("DISCOUNT_AMOUNT", discountAmount)
                weatherTemp?.let { intent.putExtra("WEATHER_TEMP", it) }
                intent.putExtra("WEATHER_CONDITION", weatherCondition)
                startActivity(intent)
                return@setOnClickListener
            }

            if (selectedPayment.startsWith("MoMo")) {
                Toast.makeText(this, "MoMo đang phát triển", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            progressBar?.visibility = View.VISIBLE
            btnPlaceOrder.isEnabled = false

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
                    progressBar?.visibility = View.GONE
                    btnPlaceOrder.isEnabled = true
                    if (response.isSuccessful && response.body()?.success == true) {
                        CartManager.clear()
                        Toast.makeText(this@CheckoutActivity, "Đặt hàng thành công!", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this@CheckoutActivity, MyOrdersActivity::class.java).apply {
                            putExtra("USER_ID", userId)
                            putExtra("USER_NAME", intent.getStringExtra("USER_NAME") ?: "")
                            putExtra("BACK_TO_HOME", true)
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        })
                        finish()
                    } else {
                        Toast.makeText(this@CheckoutActivity,
                            response.body()?.message ?: "Đặt hàng thất bại!", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<PlaceOrderResponse>, t: Throwable) {
                    progressBar?.visibility = View.GONE
                    btnPlaceOrder.isEnabled = true
                    Toast.makeText(this@CheckoutActivity, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun updateTotals(
        tvShippingFee: TextView,
        discountLayout: View,
        tvDiscount: TextView,
        tvTotalDetail: TextView,
        tvTotal: TextView,
        shippingFee: Int,
        discountAmount: Int,
        total: Int
    ) {
        tvShippingFee.text = if (shippingFee == 0) "0 đ" else "%,d đ".format(shippingFee)
        discountLayout.visibility = if (discountAmount > 0) View.VISIBLE else View.GONE
        tvDiscount.text = "-%,d đ".format(discountAmount)
        val displayTotal = total.coerceAtLeast(0)
        tvTotalDetail.text = "%,d đ".format(displayTotal)
        tvTotal.text = "%,d đ".format(displayTotal)
    }
}
