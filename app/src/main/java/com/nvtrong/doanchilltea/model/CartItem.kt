package com.nvtrong.doanchilltea.model

data class CartItem(
    val id: Int,
    val name: String,
    val price: Int,
    var quantity: Int = 1,
    val imageUrl: String? = null
) {
    val displayPrice: String get() = "%,d đ".format(price)
    val totalPrice: Int get() = price * quantity
}