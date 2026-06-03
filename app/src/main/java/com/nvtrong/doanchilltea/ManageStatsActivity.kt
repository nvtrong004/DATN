package com.nvtrong.doanchilltea

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.nvtrong.doanchilltea.model.TopProduct
import com.nvtrong.doanchilltea.network.RetrofitClient
import com.nvtrong.doanchilltea.network.StatsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ManageStatsActivity : AppCompatActivity() {

    private lateinit var tvRevenue  : TextView
    private lateinit var tvSuccess  : TextView
    private lateinit var tvCanceled : TextView
    private lateinit var rvTop      : RecyclerView
    private lateinit var progress   : ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manage_stats)
        ViewCompat.setOnApplyWindowInsetsListener(this.window.decorView) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        tvRevenue  = findViewById(R.id.tvTotalRevenue)
        tvSuccess  = findViewById(R.id.tvSuccessOrders)
        tvCanceled = findViewById(R.id.tvCanceledOrders)
        rvTop      = findViewById(R.id.rvTopProducts)
        progress   = findViewById(R.id.progressStats)

        val btnBack   = findViewById<ImageView>(R.id.btnBackAdminStats)
        val chipGroup = findViewById<ChipGroup>(R.id.chipGroupTime)

        btnBack.setOnClickListener { finish() }

        rvTop.layoutManager = LinearLayoutManager(this)
        rvTop.isNestedScrollingEnabled = false

        loadStats("today")

        chipGroup.setOnCheckedStateChangeListener { group, _ ->
            val chip   = group.findViewById<Chip>(group.checkedChipId)
            val period = when (chip?.text?.toString()) {
                "Tuần này"  -> "week"
                "Tháng này" -> "month"
                else        -> "today"
            }
            loadStats(period)
        }
    }

    private fun loadStats(period: String) {
        progress.visibility = View.VISIBLE
        RetrofitClient.instance.getStats(period).enqueue(object : Callback<StatsResponse> {
            override fun onResponse(call: Call<StatsResponse>, response: Response<StatsResponse>) {
                progress.visibility = View.GONE
                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    tvRevenue.text  = "%,d đ".format(body.revenue)
                    tvSuccess.text  = body.successOrders.toString()
                    tvCanceled.text = body.canceledOrders.toString()

                    val topList = body.topProducts.map {
                        TopProduct(it.rank, it.name, it.totalSold, it.imageUrl)
                    }
                    rvTop.adapter = TopProductAdapter(topList)
                } else {
                    showEmpty()
                }
            }
            override fun onFailure(call: Call<StatsResponse>, t: Throwable) {
                progress.visibility = View.GONE
                Toast.makeText(this@ManageStatsActivity, "Lỗi: ${t.message}", Toast.LENGTH_SHORT).show()
                showEmpty()
            }
        })
    }

    private fun showEmpty() {
        tvRevenue.text  = "0 đ"
        tvSuccess.text  = "0"
        tvCanceled.text = "0"
        rvTop.adapter   = TopProductAdapter(emptyList())
    }
}
