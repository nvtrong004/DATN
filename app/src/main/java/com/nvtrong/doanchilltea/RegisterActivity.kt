package com.nvtrong.doanchilltea

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
import com.nvtrong.doanchilltea.network.RegisterRequest
import com.nvtrong.doanchilltea.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val edtName = findViewById<TextInputEditText>(R.id.edtName)
        val edtPhoneReg = findViewById<TextInputEditText>(R.id.edtPhoneReg)
        val edtPassReg = findViewById<TextInputEditText>(R.id.edtPassReg)
        val edtConfirmPass = findViewById<TextInputEditText>(R.id.edtConfirmPass)
        val btnRegisterSubmit = findViewById<Button>(R.id.btnRegisterSubmit)
        val tvBackToLogin = findViewById<TextView>(R.id.tvBackToLogin)

        tvBackToLogin.setOnClickListener { finish() }

        btnRegisterSubmit.setOnClickListener {
            val name = edtName.text.toString().trim()
            val phone = edtPhoneReg.text.toString().trim()
            val pass = edtPassReg.text.toString().trim()
            val confirmPass = edtConfirmPass.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!phone.matches(Regex("^0\\d{9}$"))) {
                edtPhoneReg.error = "Số điện thoại phải bắt đầu bằng 0 và gồm 10 số"
                return@setOnClickListener
            }
            if (pass != confirmPass) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            RetrofitClient.instance.registerUser(RegisterRequest(phone, pass, name))
                .enqueue(object : Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        val apiResponse = response.body()
                        if (response.isSuccessful && apiResponse?.success == true) {
                            Toast.makeText(this@RegisterActivity, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(
                                this@RegisterActivity,
                                apiResponse?.message ?: "Đăng ký thất bại!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        Toast.makeText(this@RegisterActivity, "Lỗi kết nối: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
        }
    }
}
