package com.nvtrong.doanchilltea

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.nvtrong.doanchilltea.model.OrderItem
import com.nvtrong.doanchilltea.network.AdminOrdersResponse
import com.nvtrong.doanchilltea.network.ApiResponse
import com.nvtrong.doanchilltea.network.RetrofitClient
import com.nvtrong.doanchilltea.network.UpdateOrderStatusRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ManageOrdersActivity : AppCompatActivity() {

    private lateinit var adapter: AdminOrderAdapter
    private val allOrders = mutableListOf<OrderItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manage_orders)
        ViewCompat.setOnApplyWindowInsetsListener(this.window.decorView) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val btnBack = findViewById<ImageView>(R.id.btnBackAdminOrders)
        val chipGroup = findViewById<ChipGroup>(R.id.chipGroupStatus)
        val rvOrders = findViewById<RecyclerView>(R.id.rvAdminOrders)
        val progress = findViewById<ProgressBar>(R.id.progressAdminOrders)

        btnBack.setOnClickListener { finish() }

        adapter = AdminOrderAdapter(mutableListOf()) { order, action ->
            val newStatus = when (action) {
                "confirm" -> "Đang giao"
                "complete" -> "Đã hoàn thành"
                else -> "Đã hủy"
            }
            updateOrderStatus(order, newStatus, chipGroup)
        }
        rvOrders.adapter = adapter
        rvOrders.layoutManager = LinearLayoutManager(this)

        chipGroup.setOnCheckedStateChangeListener { _, _ -> filterOrders(chipGroup) }

        loadOrders(progress)
    }

    private fun loadOrders(progress: ProgressBar) {
        progress.visibility = View.VISIBLE
        RetrofitClient.instance.getAdminOrders().enqueue(object : Callback<AdminOrdersResponse> {
            override fun onResponse(call: Call<AdminOrdersResponse>, response: Response<AdminOrdersResponse>) {
                progress.visibility = View.GONE
                if (response.isSuccessful && response.body()?.success == true) {
                    allOrders.clear()
                    allOrders.addAll(response.body()!!.orders.map {
                        OrderItem(it.id.toString(), it.customerName, it.date, it.total, it.status)
                    })
                    adapter.updateData(allOrders)
                }
            }

            override fun onFailure(call: Call<AdminOrdersResponse>, t: Throwable) {
                progress.visibility = View.GONE
                Toast.makeText(this@ManageOrdersActivity, "Lỗi: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateOrderStatus(order: OrderItem, newStatus: String, chipGroup: ChipGroup) {
        RetrofitClient.instance.updateOrderStatus(
            UpdateOrderStatusRequest(order.id.removePrefix("#ORD-").toIntOrNull() ?: 0, newStatus)
        ).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    order.status = newStatus
                    filterOrders(chipGroup)
                    Toast.makeText(this@ManageOrdersActivity, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        this@ManageOrdersActivity,
                        response.body()?.message ?: "Cập nhật thất bại!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@ManageOrdersActivity, "Lỗi: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterOrders(chipGroup: ChipGroup) {
        val chip = chipGroup.findViewById<Chip>(chipGroup.checkedChipId)
        val label = chip?.text?.toString() ?: "Tất cả"
        val filtered = if (label == "Tất cả") allOrders else allOrders.filter { it.status == label }
        adapter.updateData(filtered)
    }
}
