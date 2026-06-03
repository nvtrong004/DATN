package com.nvtrong.doanchilltea

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.nvtrong.doanchilltea.model.AccountItem
import com.nvtrong.doanchilltea.network.AccountsResponse
import com.nvtrong.doanchilltea.network.ApiResponse
import com.nvtrong.doanchilltea.network.RetrofitClient
import com.nvtrong.doanchilltea.network.ToggleAccountRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ManageAccountsActivity : AppCompatActivity() {

    private lateinit var adapter: AdminAccountAdapter
    private val allAccounts = mutableListOf<AccountItem>()
    private var currentQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manage_accounts)
        ViewCompat.setOnApplyWindowInsetsListener(this.window.decorView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnBack = findViewById<ImageView>(R.id.btnBackAdmin)
        val edtSearch = findViewById<TextInputEditText>(R.id.edtSearchAccount)
        val rvAccounts = findViewById<RecyclerView>(R.id.rvAccounts)

        btnBack.setOnClickListener { finish() }

        adapter = AdminAccountAdapter(mutableListOf()) { account ->
            toggleAccount(account)
        }

        rvAccounts.adapter = adapter
        rvAccounts.layoutManager = LinearLayoutManager(this)

        edtSearch.addTextChangedListener { text ->
            currentQuery = text.toString().trim().lowercase()
            adapter.updateData(filteredAccounts())
        }

        loadAccounts()
    }

    private fun loadAccounts() {
        RetrofitClient.instance.getAccounts().enqueue(object : Callback<AccountsResponse> {
            override fun onResponse(call: Call<AccountsResponse>, response: Response<AccountsResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    allAccounts.clear()
                    allAccounts.addAll(response.body()!!.accounts.map {
                        AccountItem(
                            id = it.id,
                            name = it.name,
                            phone = it.phone,
                            role = it.role,
                            isActive = it.isActive ?: true
                        )
                    })
                    adapter.updateData(filteredAccounts())
                } else {
                    Toast.makeText(this@ManageAccountsActivity, "Không tải được danh sách tài khoản", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AccountsResponse>, t: Throwable) {
                Toast.makeText(this@ManageAccountsActivity, "Lỗi tải tài khoản: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun toggleAccount(account: AccountItem) {
        RetrofitClient.instance.toggleAccount(ToggleAccountRequest(account.id, account.isActive))
            .enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(
                            this@ManageAccountsActivity,
                            response.body()?.message ?: "Đã cập nhật tài khoản",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        rollback(account)
                        Toast.makeText(
                            this@ManageAccountsActivity,
                            response.body()?.message ?: "Cập nhật tài khoản thất bại",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    rollback(account)
                    Toast.makeText(this@ManageAccountsActivity, "Lỗi cập nhật: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun rollback(account: AccountItem) {
        account.isActive = !account.isActive
        allAccounts.find { it.id == account.id }?.isActive = account.isActive
        adapter.updateData(filteredAccounts())
    }

    private fun filteredAccounts(): List<AccountItem> {
        if (currentQuery.isEmpty()) return allAccounts
        return allAccounts.filter {
            it.name.lowercase().contains(currentQuery) || it.phone.contains(currentQuery)
        }
    }
}
