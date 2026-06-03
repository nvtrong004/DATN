package com.nvtrong.doanchilltea

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.nvtrong.doanchilltea.network.ApiResponse
import com.nvtrong.doanchilltea.network.LoginRequest
import com.nvtrong.doanchilltea.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SessionManager.getUser(this)?.let { session ->
            startActivity(SessionManager.createHomeIntent(this, session))
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etUsername = findViewById<TextInputEditText>(R.id.etUsername)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btnLogin.setOnClickListener {
            val phone = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số điện thoại và mật khẩu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!phone.matches(Regex("^0\\d{9}$"))) {
                etUsername.error = "Số điện thoại phải bắt đầu bằng 0 và gồm 10 số"
                return@setOnClickListener
            }

            RetrofitClient.instance.loginUser(LoginRequest(phone, password))
                .enqueue(object : Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            val user = response.body()!!.user
                            val name = user?.fullname ?: "Người dùng"
                            val role = user?.role ?: 0
                            val id = user?.id ?: -1
                            SessionManager.saveUser(this@MainActivity, id, name, role)

                            Toast.makeText(this@MainActivity, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()

                            val intent = SessionManager.createHomeIntent(
                                this@MainActivity,
                                SessionManager.UserSession(id, name, role)
                            )
                            startActivity(intent)
                            finish()
                        } else {
                            val msg = response.body()?.message ?: "Lỗi từ máy chủ: ${response.code()}"
                            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        Toast.makeText(this@MainActivity, "Lỗi kết nối: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
        }
    }
}
