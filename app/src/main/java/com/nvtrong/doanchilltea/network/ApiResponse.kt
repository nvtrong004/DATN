package com.nvtrong.doanchilltea.network

import com.google.gson.annotations.SerializedName


data class UserData(val id: Int, val fullname: String, val role: Int)
data class ApiResponse(val success: Boolean, val message: String, val user: UserData? = null)
data class LoginRequest(val phone: String, val password: String)
data class RegisterRequest(val phone: String, val password: String, val fullname: String)

data class ProductApiItem(
    val id: Int,
    @SerializedName(value = "categoryId", alternate = ["category_id"]) val categoryId: Int,
    @SerializedName(value = "categoryName", alternate = ["category_name"]) val categoryName: String,
    val name: String,
    val price: Int,
    @SerializedName(value = "imageUrl", alternate = ["image_url"]) val imageUrl: String?,
    @SerializedName(value = "isActive", alternate = ["is_active"]) val isActive: Boolean
)
data class ProductsResponse(val success: Boolean, val products: List<ProductApiItem>)
data class AddProductRequest(val category_id: Int, val product_name: String, val price: Int, val image_url: String?)
data class EditProductRequest(val product_id: Int, val category_id: Int, val product_name: String, val price: Int, val image_url: String?, val is_active: Boolean)
data class DeleteProductRequest(val product_id: Int, val id: Int = product_id)
data class ToggleProductRequest(val product_id: Int, val is_active: Boolean)
data class UploadImageResponse(val success: Boolean, val message: String, val image_url: String?)
data class ToggleAccountRequest(val user_id: Int, val is_active: Boolean)

data class CategoryApiItem(val id: Int, val name: String)
data class CategoriesResponse(val success: Boolean, val categories: List<CategoryApiItem>)
data class AddCategoryRequest(val name: String)
data class DeleteCategoryRequest(val category_id: Int, val id: Int = category_id)

data class PlaceOrderItem(val product_id: Int, val quantity: Int, val unit_price: Int)
data class PlaceOrderRequest(
    val user_id: Int,
    val delivery_address: String,
    val items: List<PlaceOrderItem>,
    val shipping_fee: Int = 0,
    val discount_amount: Int = 0,
    val temperature: Int? = null,
    val weather_condition: String? = null
)
data class PlaceOrderResponse(val success: Boolean, val message: String, val order_id: Int?)
data class CreateVnPayRequest(val amount: Int)
data class CreateVnPayResponse(val success: Boolean, val message: String, val payment_url: String?)

data class OrderDetailApiItem(
    @SerializedName(value = "productId", alternate = ["product_id"]) val productId: Int,
    @SerializedName(value = "productName", alternate = ["product_name"]) val productName: String,
    @SerializedName(value = "imageUrl", alternate = ["image_url"]) val imageUrl: String?,
    val quantity: Int,
    @SerializedName(value = "unitPrice", alternate = ["unit_price"]) val unitPrice: Int
)
data class OrderApiItem(
    val id: Int, val orderDate: String, val deliveryAddress: String,
    val totalPrice: Int, val status: String, val items: List<OrderDetailApiItem>
)
data class MyOrdersResponse(val success: Boolean, val orders: List<OrderApiItem>)


data class UserProfileData(val id: Int, val fullname: String, val phone: String, val address: String?)
data class UserProfileResponse(val success: Boolean, val user: UserProfileData?)
data class UpdateProfileRequest(val user_id: Int, val fullname: String, val phone: String, val address: String?)

data class AdminOrderApiItem(
    val id: Int, val customerName: String,
    val date: String, val total: Int, val status: String
)
data class AdminOrdersResponse(val success: Boolean, val orders: List<AdminOrderApiItem>)
data class UpdateOrderStatusRequest(val order_id: Int, val status: String)

data class AccountApiItem(
    val id: Int,
    val name: String,
    val phone: String,
    val role: Int,
    @SerializedName(value = "isActive", alternate = ["is_active"]) val isActive: Boolean? = true
)
data class AccountsResponse(val success: Boolean, val accounts: List<AccountApiItem>)

data class TopProductApiItem(
    val rank: Int,
    val name: String,
    @SerializedName(value = "imageUrl", alternate = ["image_url"]) val imageUrl: String?,
    @SerializedName(value = "totalSold", alternate = ["total_sold"]) val totalSold: Int
)
data class StatsResponse(
    val success: Boolean, val revenue: Int,
    val successOrders: Int, val canceledOrders: Int,
    val topProducts: List<TopProductApiItem>
)
