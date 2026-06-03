package com.nvtrong.doanchilltea

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.nvtrong.doanchilltea.model.CartManager

class CartActivity : AppCompatActivity() {

    private val shippingFee = 15_000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cart)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cart)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val userName = intent.getStringExtra("USER_NAME") ?: ""
        val userId = intent.getIntExtra("USER_ID", -1)
        val weatherTemp = intent.getIntExtra("WEATHER_TEMP", Int.MIN_VALUE)
        val weatherCondition = intent.getStringExtra("WEATHER_CONDITION")

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val rvCart = findViewById<RecyclerView>(R.id.rcvCartList)
        val tvSubtotal = findViewById<TextView>(R.id.tvCartSubtotal)
        val tvShipping = findViewById<TextView>(R.id.tvCartShippingFee)
        val tvTotal = findViewById<TextView>(R.id.tvCartTotalAmount)
        val btnPay = findViewById<MaterialButton>(R.id.btnPay)

        btnBack.setOnClickListener { finish() }
        tvShipping.text = "%,d đ".format(shippingFee)

        val cartItems = CartManager.getMutableItems()
        val adapter = CartAdapter(cartItems) {
            updateTotals(tvSubtotal, tvTotal, cartItems.sumOf { it.totalPrice })
        }

        rvCart.adapter = adapter
        rvCart.layoutManager = LinearLayoutManager(this)
        updateTotals(tvSubtotal, tvTotal, cartItems.sumOf { it.totalPrice })

        btnPay.setOnClickListener {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng đang trống!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, CheckoutActivity::class.java)
            intent.putExtra("USER_NAME", userName)
            intent.putExtra("USER_ID", userId)
            intent.putExtra("SUBTOTAL", cartItems.sumOf { it.totalPrice })
            if (weatherTemp != Int.MIN_VALUE) intent.putExtra("WEATHER_TEMP", weatherTemp)
            intent.putExtra("WEATHER_CONDITION", weatherCondition)
            startActivity(intent)
        }
    }

    private fun updateTotals(tvSubtotal: TextView, tvTotal: TextView, subtotal: Int) {
        tvSubtotal.text = "%,d đ".format(subtotal)
        tvTotal.text = "%,d đ".format(subtotal + shippingFee)
    }
}
