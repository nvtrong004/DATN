package com.nvtrong.doanchilltea

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.nvtrong.doanchilltea.model.AdminProduct
import com.nvtrong.doanchilltea.network.AddCategoryRequest
import com.nvtrong.doanchilltea.network.AddProductRequest
import com.nvtrong.doanchilltea.network.ApiResponse
import com.nvtrong.doanchilltea.network.CategoriesResponse
import com.nvtrong.doanchilltea.network.CategoryApiItem
import com.nvtrong.doanchilltea.network.DeleteCategoryRequest
import com.nvtrong.doanchilltea.network.DeleteProductRequest
import com.nvtrong.doanchilltea.network.EditProductRequest
import com.nvtrong.doanchilltea.network.ProductsResponse
import com.nvtrong.doanchilltea.network.RetrofitClient
import com.nvtrong.doanchilltea.network.ToggleProductRequest
import com.nvtrong.doanchilltea.network.UploadImageResponse
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class ManageProductsActivity : AppCompatActivity() {

    private lateinit var adapter   : AdminProductAdapter
    private val allProducts        = mutableListOf<AdminProduct>()
    private val categories         = mutableListOf<CategoryApiItem>()
    private var currentImageInput: TextInputEditText? = null

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) uploadSelectedImage(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manage_products)
        ViewCompat.setOnApplyWindowInsetsListener(this.window.decorView) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val btnBack    = findViewById<ImageView>(R.id.btnBackAdminProducts)
        val edtSearch  = findViewById<TextInputEditText>(R.id.edtSearchProduct)
        val btnAddCategory = findViewById<MaterialButton>(R.id.btnAddCategory)
        val btnDeleteCategory = findViewById<MaterialButton>(R.id.btnDeleteCategory)
        val btnAddNew  = findViewById<MaterialButton>(R.id.btnAddNewProduct)
        val rvProducts = findViewById<RecyclerView>(R.id.rvAdminProducts)
        val progress   = findViewById<ProgressBar>(R.id.progressProducts)

        btnBack.setOnClickListener { finish() }

        adapter = AdminProductAdapter(
            allProducts,
            onToggle = { product -> toggleProduct(product) },
            onEdit   = { product -> showEditDialog(product) },
            onDelete = { product -> confirmDelete(product) }
        )
        rvProducts.adapter = adapter
        rvProducts.layoutManager = LinearLayoutManager(this)

        loadCategories { loadProducts(progress) }

        edtSearch.addTextChangedListener { text ->
            val q = text.toString().trim().lowercase()
            adapter.updateData(if (q.isEmpty()) allProducts else allProducts.filter { it.name.lowercase().contains(q) })
        }

        btnAddCategory.setOnClickListener { showAddCategoryDialog() }
        btnDeleteCategory.setOnClickListener { showDeleteCategoryDialog() }
        btnAddNew.setOnClickListener { showAddDialog() }
    }

    private fun loadCategories(onDone: () -> Unit) {
        RetrofitClient.instance.getCategories().enqueue(object : Callback<CategoriesResponse> {
            override fun onResponse(call: Call<CategoriesResponse>, response: Response<CategoriesResponse>) {
                categories.clear()
                categories.addAll(response.body()?.categories ?: emptyList())
                onDone()
            }
            override fun onFailure(call: Call<CategoriesResponse>, t: Throwable) { onDone() }
        })
    }

    private fun loadProducts(progress: ProgressBar) {
        progress.visibility = View.VISIBLE
        RetrofitClient.instance.getProducts(includeInactive = 1).enqueue(object : Callback<ProductsResponse> {
            override fun onResponse(call: Call<ProductsResponse>, response: Response<ProductsResponse>) {
                progress.visibility = View.GONE
                if (response.isSuccessful && response.body()?.success == true) {
                    allProducts.clear()
                    allProducts.addAll(response.body()!!.products.map { p ->
                        val catName = categories.find { it.id == p.categoryId }?.name ?: "—"
                        AdminProduct(p.id, p.name, p.price, catName, p.isActive, p.imageUrl)
                    })
                    adapter.updateData(allProducts)
                }
            }
            override fun onFailure(call: Call<ProductsResponse>, t: Throwable) {
                progress.visibility = View.GONE
            }
        })
    }

    private fun showAddCategoryDialog(onAdded: (() -> Unit)? = null) {
        val input = EditText(this).apply {
            hint = "Tên danh mục"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
            setSingleLine(true)
            setPadding(48, 24, 48, 24)
        }

        AlertDialog.Builder(this)
            .setTitle("Thêm danh mục")
            .setView(input)
            .setPositiveButton("Thêm") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(this, "Nhập tên danh mục!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                RetrofitClient.instance.addCategory(AddCategoryRequest(name))
                    .enqueue(object : Callback<ApiResponse> {
                        override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                            val body = response.body()
                            if (!response.isSuccessful || body?.success != true) {
                                Toast.makeText(
                                    this@ManageProductsActivity,
                                    body?.message ?: "Them danh muc that bai! Ma loi: ${response.code()}",
                                    Toast.LENGTH_LONG
                                ).show()
                                return
                            }
                            if (response.body()?.success == true) {
                                Toast.makeText(this@ManageProductsActivity, "Đã thêm danh mục!", Toast.LENGTH_SHORT).show()
                                loadCategories {
                                    loadProducts(findViewById(R.id.progressProducts))
                                    onAdded?.invoke()
                                }
                            } else {
                                Toast.makeText(
                                    this@ManageProductsActivity,
                                    response.body()?.message ?: "Thêm danh mục thất bại!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                            Toast.makeText(this@ManageProductsActivity, "Lỗi: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun toggleProduct(product: AdminProduct) {
        RetrofitClient.instance.toggleProduct(ToggleProductRequest(product.id, product.isActive))
            .enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    val msg = if (product.isActive) "Đang bán" else "Đã ẩn"
                    Toast.makeText(this@ManageProductsActivity, "${product.name}: $msg", Toast.LENGTH_SHORT).show()
                }
                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    product.isActive = !product.isActive // Rollback
                    adapter.notifyDataSetChanged()
                }
            })
    }

    private fun showDeleteCategoryDialog() {
        if (categories.isEmpty()) {
            Toast.makeText(this, "Chua co danh muc de xoa!", Toast.LENGTH_SHORT).show()
            return
        }

        val categoryNames = categories.map { it.name }.toTypedArray()
        var selectedIndex = 0

        AlertDialog.Builder(this)
            .setTitle("Xoa danh muc")
            .setSingleChoiceItems(categoryNames, selectedIndex) { _, which ->
                selectedIndex = which
            }
            .setPositiveButton("Xoa") { _, _ ->
                val category = categories.getOrNull(selectedIndex) ?: return@setPositiveButton
                confirmDeleteCategory(category)
            }
            .setNegativeButton("Huy", null)
            .show()
    }

    private fun confirmDeleteCategory(category: CategoryApiItem) {
        val productCount = allProducts.count { it.category == category.name }
        val message = if (productCount > 0) {
            "Danh mục \"${category.name}\" đang có $productCount món. Bạn có chắc chắn muốn xóa?"
        } else {
            "Bạn có chắc muốn xóa danh mục \"${category.name}\"?"
        }

        AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage(message)
            .setPositiveButton("Xóa") { _, _ ->
                RetrofitClient.instance.deleteCategory(DeleteCategoryRequest(category.id))
                    .enqueue(object : Callback<ApiResponse> {
                        override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                            val body = response.body()
                            if (response.isSuccessful && body?.success == true) {
                                Toast.makeText(this@ManageProductsActivity, "Đã xóa danh mục!", Toast.LENGTH_SHORT).show()
                                loadCategories { loadProducts(findViewById(R.id.progressProducts)) }
                            } else {
                                Toast.makeText(
                                    this@ManageProductsActivity,
                                    body?.message ?: "Xóa danh mục thất bại: ${response.code()}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                        override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                            Toast.makeText(this@ManageProductsActivity, "Lỗi xóa danh mục: ${t.message}", Toast.LENGTH_LONG).show()
                        }
                    })
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun chooseProductImage(target: TextInputEditText) {
        currentImageInput = target
        imagePicker.launch("image/*")
    }

    private fun uploadSelectedImage(uri: Uri) {
        val input = currentImageInput ?: return
        val tempFile = File.createTempFile("product_", ".jpg", cacheDir)
        contentResolver.openInputStream(uri)?.use { source ->
            tempFile.outputStream().use { target -> source.copyTo(target) }
        } ?: run {
            Toast.makeText(this, "Không đọc được ảnh!", Toast.LENGTH_SHORT).show()
            return
        }

        val requestBody = RequestBody.create(MediaType.parse("image/*"), tempFile)
        val part = MultipartBody.Part.createFormData("image", tempFile.name, requestBody)

        Toast.makeText(this, "Đang tải ảnh...", Toast.LENGTH_SHORT).show()
        RetrofitClient.instance.uploadImage(part).enqueue(object : Callback<UploadImageResponse> {
            override fun onResponse(call: Call<UploadImageResponse>, response: Response<UploadImageResponse>) {
                val body = response.body()
                if (response.isSuccessful && body?.success == true && !body.image_url.isNullOrBlank()) {
                    input.setText(body.image_url)
                    Toast.makeText(this@ManageProductsActivity, "Đã chọn ảnh!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        this@ManageProductsActivity,
                        body?.message ?: "Tải ảnh thất bại!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<UploadImageResponse>, t: Throwable) {
                Toast.makeText(this@ManageProductsActivity, "Lỗi tải ảnh: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showAddDialog() {
        if (categories.isEmpty()) {
            Toast.makeText(this, "Hãy thêm danh mục trước!", Toast.LENGTH_SHORT).show()
            showAddCategoryDialog { showAddDialog() }
            return
        }

        val view       = LayoutInflater.from(this).inflate(R.layout.dialog_product_form, null)
        val edtName    = view.findViewById<TextInputEditText>(R.id.edtDialogProductName)
        val edtPrice   = view.findViewById<TextInputEditText>(R.id.edtDialogProductPrice)
        val edtImage   = view.findViewById<TextInputEditText>(R.id.edtDialogImageUrl)
        val btnChooseImage = view.findViewById<MaterialButton>(R.id.btnChooseProductImage)
        val spinner    = view.findViewById<Spinner>(R.id.spinnerCategory)
        btnChooseImage.setOnClickListener { chooseProductImage(edtImage) }

        val catNames = categories.map { it.name }
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, catNames)

        AlertDialog.Builder(this)
            .setTitle("Thêm món mới")
            .setView(view)
            .setPositiveButton("Thêm") { _, _ ->
                val name  = edtName.text.toString().trim()
                val price = edtPrice.text.toString().toIntOrNull() ?: 0
                val image = edtImage.text.toString().trim().ifEmpty { null }
                val catId = categories.getOrNull(spinner.selectedItemPosition)?.id
                if (catId == null) {
                    Toast.makeText(this, "Chưa tải được danh mục!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (name.isEmpty() || price <= 0) {
                    Toast.makeText(this, "Nhập đủ tên và giá!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                RetrofitClient.instance.addProduct(AddProductRequest(catId, name, price, image))
                    .enqueue(object : Callback<ApiResponse> {
                        override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                            if (response.body()?.success == true) {
                                Toast.makeText(this@ManageProductsActivity, "Đã thêm món!", Toast.LENGTH_SHORT).show()
                                loadCategories { loadProducts(findViewById(R.id.progressProducts)) }
                            } else {
                                Toast.makeText(this@ManageProductsActivity,
                                    response.body()?.message ?: "Thất bại!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                            Toast.makeText(this@ManageProductsActivity, "Lỗi: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showEditDialog(product: AdminProduct) {
        val view       = LayoutInflater.from(this).inflate(R.layout.dialog_product_form, null)
        val edtName    = view.findViewById<TextInputEditText>(R.id.edtDialogProductName)
        val edtPrice   = view.findViewById<TextInputEditText>(R.id.edtDialogProductPrice)
        val edtImage   = view.findViewById<TextInputEditText>(R.id.edtDialogImageUrl)
        val btnChooseImage = view.findViewById<MaterialButton>(R.id.btnChooseProductImage)
        val spinner    = view.findViewById<Spinner>(R.id.spinnerCategory)
        btnChooseImage.setOnClickListener { chooseProductImage(edtImage) }

        edtName.setText(product.name)
        edtPrice.setText(product.price.toString())
        edtImage.setText(product.imageUrl ?: "")

        val catNames = categories.map { it.name }
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, catNames)
        val currentCatIndex = categories.indexOfFirst { it.name == product.category }
        if (currentCatIndex >= 0) spinner.setSelection(currentCatIndex)

        AlertDialog.Builder(this)
            .setTitle("Chỉnh sửa: ${product.name}")
            .setView(view)
            .setPositiveButton("Lưu") { _, _ ->
                val name  = edtName.text.toString().trim()
                val price = edtPrice.text.toString().toIntOrNull() ?: 0
                val image = edtImage.text.toString().trim().ifEmpty { null }
                val catId = categories.getOrNull(spinner.selectedItemPosition)?.id
                if (catId == null) {
                    Toast.makeText(this, "Chưa tải được danh mục!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (name.isEmpty() || price <= 0) {
                    Toast.makeText(this, "Nhập đủ tên và giá!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                RetrofitClient.instance.editProduct(EditProductRequest(product.id, catId, name, price, image, product.isActive))
                    .enqueue(object : Callback<ApiResponse> {
                        override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                            if (response.body()?.success == true) {
                                Toast.makeText(this@ManageProductsActivity, "Đã cập nhật!", Toast.LENGTH_SHORT).show()
                                loadCategories { loadProducts(findViewById(R.id.progressProducts)) }
                            }
                        }
                        override fun onFailure(call: Call<ApiResponse>, t: Throwable) {}
                    })
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun confirmDelete(product: AdminProduct) {
        AlertDialog.Builder(this)
            .setTitle("Xóa món ăn")
            .setMessage("Bạn có chắc muốn xóa \"${product.name}\"?")
            .setPositiveButton("Xóa") { _, _ ->
                RetrofitClient.instance.deleteProduct(DeleteProductRequest(product.id))
                    .enqueue(object : Callback<ApiResponse> {
                        override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                            val body = response.body()
                            if (!response.isSuccessful || body?.success != true) {
                                Toast.makeText(
                                    this@ManageProductsActivity,
                                    body?.message ?: "Xoa san pham that bai! Ma loi: ${response.code()}",
                                    Toast.LENGTH_LONG
                                ).show()
                                return
                            }
                            if (response.body()?.success == true) {
                                allProducts.removeAll { it.id == product.id }
                                adapter.updateData(allProducts)
                                loadCategories { loadProducts(findViewById(R.id.progressProducts)) }
                                Toast.makeText(this@ManageProductsActivity, "Đã xóa!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                            Toast.makeText(this@ManageProductsActivity, "Loi xoa san pham: ${t.message}", Toast.LENGTH_LONG).show()
                        }
                    })
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}
