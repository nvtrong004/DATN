package com.nvtrong.doanchilltea

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.nvtrong.doanchilltea.network.ApiResponse
import com.nvtrong.doanchilltea.network.RetrofitClient
import com.nvtrong.doanchilltea.network.UpdateProfileRequest
import com.nvtrong.doanchilltea.network.UserProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {

    private var userId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profile)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val session = SessionManager.getUser(this)
        userId = intent.getIntExtra("USER_ID", session?.id ?: -1)

        val btnBack = findViewById<ImageView>(R.id.btnBackProfile)
        val edtName = findViewById<TextInputEditText>(R.id.edtProfileName)
        val edtPhone = findViewById<TextInputEditText>(R.id.edtProfilePhone)
        val edtAddress = findViewById<TextInputEditText>(R.id.edtProfileAddress)
        val btnSave = findViewById<MaterialButton>(R.id.btnSaveProfile)
        val btnLogout = findViewById<MaterialButton>(R.id.btnLogout)

        btnBack.setOnClickListener { finish() }

        if (userId != -1) {
            RetrofitClient.instance.getUserProfile(userId)
                .enqueue(object : Callback<UserProfileResponse> {
                    override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                        val user = response.body()?.user
                        if (user != null) {
                            edtName.setText(user.fullname)
                            edtPhone.setText(user.phone)
                            edtAddress.setText(user.address ?: "")
                        }
                    }

                    override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                        edtName.setText(intent.getStringExtra("USER_NAME") ?: "")
                    }
                })
        } else {
            edtName.setText(intent.getStringExtra("USER_NAME") ?: "")
        }

        btnSave.setOnClickListener {
            val newName = edtName.text.toString().trim()
            val newPhone = edtPhone.text.toString().trim()
            val newAddress = edtAddress.text.toString().trim()

            if (newName.isEmpty()) {
                Toast.makeText(this, "Tên không được để trống", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!newPhone.matches(Regex("^0\\d{9}$"))) {
                edtPhone.error = "So dien thoai phai bat dau bang 0 va gom 10 so"
                return@setOnClickListener
            }

            btnSave.isEnabled = false
            btnSave.text = "Đang lưu..."

            RetrofitClient.instance.updateProfile(
                UpdateProfileRequest(userId, newName, newPhone, newAddress.ifEmpty { null })
            ).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    btnSave.isEnabled = true
                    btnSave.text = "LƯU THAY ĐỔI"
                    if (response.isSuccessful && response.body()?.success == true) {
                        SessionManager.updateUserName(this@ProfileActivity, newName)
                        Toast.makeText(this@ProfileActivity, "Đã lưu thay đổi!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(
                            this@ProfileActivity,
                            response.body()?.message ?: "Lưu thất bại!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    btnSave.isEnabled = true
                    btnSave.text = "LƯU THAY ĐỔI"
                    Toast.makeText(this@ProfileActivity, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        btnLogout.setOnClickListener {
            SessionManager.clear(this)
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }
}
