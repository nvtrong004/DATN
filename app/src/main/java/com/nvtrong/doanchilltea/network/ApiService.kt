package com.nvtrong.doanchilltea.network

import retrofit2.Call
import retrofit2.http.*
import okhttp3.MultipartBody

interface ApiService {

    @POST("login.php")
    fun loginUser(@Body request: LoginRequest): Call<ApiResponse>

    @POST("register.php")
    fun registerUser(@Body request: RegisterRequest): Call<ApiResponse>

    @GET("get_products.php")
    fun getProducts(
        @Query("category_id") categoryId: Int? = null,
        @Query("include_inactive") includeInactive: Int = 0
    ): Call<ProductsResponse>

    @GET("get_ai_suggestions.php")
    fun getAiSuggestions(
        @Query("user_id") userId: Int,
        @Query("temperature") temperature: Int? = null,
        @Query("weather_condition") weatherCondition: String? = null
    ): Call<ProductsResponse>

    @GET("get_categories.php")
    fun getCategories(): Call<CategoriesResponse>

    @POST("add_category.php")
    fun addCategory(@Body request: AddCategoryRequest): Call<ApiResponse>

    @POST("delete_category.php")
    fun deleteCategory(@Body request: DeleteCategoryRequest): Call<ApiResponse>

    @POST("add_product.php")
    fun addProduct(@Body request: AddProductRequest): Call<ApiResponse>

    @POST("edit_product.php")
    fun editProduct(@Body request: EditProductRequest): Call<ApiResponse>

    @POST("delete_product.php")
    fun deleteProduct(@Body request: DeleteProductRequest): Call<ApiResponse>

    @POST("admin_toggle_product.php")
    fun toggleProduct(@Body request: ToggleProductRequest): Call<ApiResponse>

    @Multipart
    @POST("upload_image.php")
    fun uploadImage(@Part image: MultipartBody.Part): Call<UploadImageResponse>

    @POST("place_order.php")
    fun placeOrder(@Body request: PlaceOrderRequest): Call<PlaceOrderResponse>

    @POST("create_vnpay_payment.php")
    fun createVnPayPayment(@Body request: CreateVnPayRequest): Call<CreateVnPayResponse>

    @GET("get_my_orders.php")
    fun getMyOrders(@Query("user_id") userId: Int): Call<MyOrdersResponse>


    @GET("get_user_profile.php")
    fun getUserProfile(@Query("user_id") userId: Int): Call<UserProfileResponse>

    @POST("update_profile.php")
    fun updateProfile(@Body request: UpdateProfileRequest): Call<ApiResponse>


    @GET("admin_get_orders.php")
    fun getAdminOrders(@Query("status") status: String? = null): Call<AdminOrdersResponse>

    @POST("admin_update_order_status.php")
    fun updateOrderStatus(@Body request: UpdateOrderStatusRequest): Call<ApiResponse>


    @GET("admin_get_accounts.php")
    fun getAccounts(@Query("search") search: String? = null): Call<AccountsResponse>

    @POST("admin_toggle_account.php")
    fun toggleAccount(@Body request: ToggleAccountRequest): Call<ApiResponse>


    @GET("admin_get_stats.php")
    fun getStats(@Query("period") period: String): Call<StatsResponse>
}
