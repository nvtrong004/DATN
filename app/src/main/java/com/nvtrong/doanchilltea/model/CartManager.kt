package com.nvtrong.doanchilltea.model

object CartManager {
    private val cartsByUser = mutableMapOf<Int, MutableList<CartItem>>()
    private var currentUserId: Int = -1

    private val currentItems: MutableList<CartItem>
        get() = cartsByUser.getOrPut(currentUserId) { mutableListOf() }

    fun setCurrentUser(userId: Int) {
        currentUserId = userId
        cartsByUser.getOrPut(currentUserId) { mutableListOf() }
    }

    fun getItems(): List<CartItem> = currentItems.toList()

    fun getMutableItems(): MutableList<CartItem> = currentItems

    fun addItem(product: Product) {
        val existing = currentItems.find { it.id == product.id }
        if (existing != null) {
            existing.quantity++
        } else {
            val priceInt = product.price
                .replace(".", "")
                .replace("đ", "")
                .replace("đ", "")
                .replace(",", "")
                .trim()
                .toIntOrNull() ?: 0
            currentItems.add(CartItem(product.id, product.name, priceInt, 1, product.imageUrl))
        }
    }

    fun increaseQty(itemId: Int) {
        currentItems.find { it.id == itemId }?.quantity++
    }

    fun decreaseQty(itemId: Int) {
        val item = currentItems.find { it.id == itemId } ?: return
        if (item.quantity > 1) item.quantity-- else currentItems.remove(item)
    }

    fun removeItem(itemId: Int) {
        currentItems.removeAll { it.id == itemId }
    }

    fun clear() = currentItems.clear()

    fun totalCount(): Int = currentItems.sumOf { it.quantity }
    fun subtotal(): Int = currentItems.sumOf { it.totalPrice }
}
