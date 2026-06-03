package com.nvtrong.doanchilltea

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView
import com.nvtrong.doanchilltea.network.AdminOrdersResponse
import com.nvtrong.doanchilltea.network.RetrofitClient
import com.nvtrong.doanchilltea.network.StatsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminActivity : AppCompatActivity() {

    private lateinit var tvTodayOrders: TextView
    private lateinit var tvTodayRevenue: TextView
    private lateinit var tvOrderBadge: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.admin_home)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val adminName = intent.getStringExtra("USER_NAME") ?: "Quản lý"
        findViewById<TextView>(R.id.tvAdminName).text = adminName
        tvTodayOrders = findViewById(R.id.tvTodayOrders)
        tvTodayRevenue = findViewById(R.id.tvTodayRevenue)
        tvOrderBadge = findViewById(R.id.tvOrderBadge)

        findViewById<ImageView>(R.id.btnAdminLogout).setOnClickListener {
            SessionManager.clear(this)
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        findViewById<MaterialCardView>(R.id.btnManageOrders).setOnClickListener {
            startActivity(Intent(this, ManageOrdersActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.btnManageProducts).setOnClickListener {
            startActivity(Intent(this, ManageProductsActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.btnManageAccounts).setOnClickListener {
            startActivity(Intent(this, ManageAccountsActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.btnManageStats).setOnClickListener {
            startActivity(Intent(this, ManageStatsActivity::class.java))
        }

        loadTodayStats()
        loadPendingOrderBadge()
    }

    override fun onResume() {
        super.onResume()
        loadTodayStats()
        loadPendingOrderBadge()
    }

    private fun loadTodayStats() {
        RetrofitClient.instance.getStats("today").enqueue(object : Callback<StatsResponse> {
            override fun onResponse(call: Call<StatsResponse>, response: Response<StatsResponse>) {
                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    val todayOrders = body.successOrders + body.canceledOrders
                    tvTodayOrders.text = todayOrders.toString()
                    tvTodayRevenue.text = formatCompactMoney(body.revenue)
                } else {
                    tvTodayOrders.text = "0"
                    tvTodayRevenue.text = "0 đ"
                }
            }

            override fun onFailure(call: Call<StatsResponse>, t: Throwable) {
                tvTodayOrders.text = "0"
                tvTodayRevenue.text = "0 đ"
                Toast.makeText(this@AdminActivity, "Lỗi tải thống kê: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun formatCompactMoney(value: Int): String {
        return when {
            value >= 1_000_000 -> {
                val millions = value / 1_000_000.0
                "%.1fM".format(millions).replace(".0M", "M")
            }
            value >= 1_000 -> "${value / 1_000}K"
            else -> "%,d đ".format(value)
        }
    }

    private fun loadPendingOrderBadge() {
        RetrofitClient.instance.getAdminOrders().enqueue(object : Callback<AdminOrdersResponse> {
            override fun onResponse(call: Call<AdminOrdersResponse>, response: Response<AdminOrdersResponse>) {
                val pendingCount = if (response.isSuccessful && response.body()?.success == true) {
                    response.body()!!.orders.count { isPendingOrder(it.status) }
                } else {
                    0
                }
                updateOrderBadge(pendingCount)
            }

            override fun onFailure(call: Call<AdminOrdersResponse>, t: Throwable) {
                updateOrderBadge(0)
            }
        })
    }

    private fun updateOrderBadge(count: Int) {
        tvOrderBadge.visibility = if (count > 0) View.VISIBLE else View.GONE
        tvOrderBadge.text = if (count > 99) "99+" else count.toString()
    }

    private fun isPendingOrder(status: String): Boolean {
        return status == "Chờ xác nhận"
    }
}
