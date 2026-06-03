package com.nvtrong.doanchilltea

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nvtrong.doanchilltea.network.MyOrdersResponse
import com.nvtrong.doanchilltea.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyOrdersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_my_orders)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.myOrders)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val userId = intent.getIntExtra("USER_ID", -1)
        val userName = intent.getStringExtra("USER_NAME") ?: ""
        val backToHome = intent.getBooleanExtra("BACK_TO_HOME", false)

        val btnBack     = findViewById<ImageView>(R.id.btnBackOrders)
        val rvOrders    = findViewById<RecyclerView>(R.id.rvMyOrders)
        val progressBar = findViewById<ProgressBar>(R.id.progressOrders)
        val tvEmpty     = findViewById<TextView>(R.id.tvEmptyOrders)

        btnBack.setOnClickListener { handleBack(backToHome, userId, userName) }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBack(backToHome, userId, userName)
            }
        })

        progressBar.visibility = View.VISIBLE
        tvEmpty.visibility     = View.GONE

        RetrofitClient.instance.getMyOrders(userId).enqueue(object : Callback<MyOrdersResponse> {
            override fun onResponse(call: Call<MyOrdersResponse>, response: Response<MyOrdersResponse>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful && response.body()?.success == true) {
                    val orders = response.body()!!.orders
                    if (orders.isEmpty()) {
                        tvEmpty.visibility = View.VISIBLE
                    } else {
                        rvOrders.adapter = MyOrderAdapter(orders)
                        rvOrders.layoutManager = LinearLayoutManager(this@MyOrdersActivity)
                    }
                } else {
                    tvEmpty.visibility = View.VISIBLE
                }
            }
            override fun onFailure(call: Call<MyOrdersResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                tvEmpty.visibility = View.VISIBLE
                Toast.makeText(this@MyOrdersActivity, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun handleBack(backToHome: Boolean, userId: Int, userName: String) {
        if (!backToHome) {
            finish()
            return
        }

        startActivity(Intent(this, HomeActivity::class.java).apply {
            putExtra("USER_ID", userId)
            putExtra("USER_NAME", userName)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        })
        finish()
    }
}
