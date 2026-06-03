package com.nvtrong.doanchilltea.model

data class OrderItem(
    val id: String,
    val customerName: String,
    val date: String,
    val total: Int,
    var status: String
) {
    val displayTotal: String get() = "%,d đ".format(total)
}

data class AccountItem(
    val id: Int,
    val name: String,
    val phone: String,
    val role: Int,
    var isActive: Boolean = true
) {
    val roleLabel: String get() = if (role == 1) "Quản lý" else "Khách hàng"
}

data class TopProduct(
    val rank: Int,
    val name: String,
    val totalSold: Int,
    val imageUrl: String? = null
)

data class AdminProduct(
    val id: Int,
    val name: String,
    val price: Int,
    val category: String,
    var isActive: Boolean = true,
    val imageUrl: String? = null
) {
    val displayPrice: String get() = "%,d đ".format(price)
}