package com.nvtrong.doanchilltea

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nvtrong.doanchilltea.network.OrderApiItem

class MyOrderAdapter(
    private val orders: List<OrderApiItem>
) : RecyclerView.Adapter<MyOrderAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvOrderId: TextView = view.findViewById(R.id.tvMyOrderId)
        val tvStatus: TextView = view.findViewById(R.id.tvMyOrderStatus)
        val tvDate: TextView = view.findViewById(R.id.tvMyOrderDate)
        val tvAddress: TextView = view.findViewById(R.id.tvMyOrderAddress)
        val tvTotal: TextView = view.findViewById(R.id.tvMyOrderTotal)
        val tvItems: TextView = view.findViewById(R.id.tvMyOrderItems)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_my_order, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orders[position]
        val itemLines = order.items.joinToString("\n") {
            "• ${it.productName} x${it.quantity} - %,d đ".format(it.unitPrice * it.quantity)
        }

        holder.tvOrderId.text = "Đơn #${order.id}"
        holder.tvStatus.text = order.status
        holder.tvDate.text = "Ngày đặt: ${order.orderDate}"
        holder.tvAddress.text = "Địa chỉ: ${order.deliveryAddress}"
        holder.tvTotal.text = "Tổng: %,d đ".format(order.totalPrice)
        holder.tvItems.text = itemLines

        holder.itemView.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Chi tiết đơn #${order.id}")
                .setMessage(
                    "Trạng thái: ${order.status}\n" +
                        "Ngày đặt: ${order.orderDate}\n" +
                        "Địa chỉ: ${order.deliveryAddress}\n\n" +
                        "$itemLines\n\n" +
                        "Tổng: %,d đ".format(order.totalPrice)
                )
                .setPositiveButton("Đóng", null)
                .show()
        }

        holder.tvStatus.setTextColor(
            holder.itemView.context.getColor(
                when (order.status) {
                    "Đã hoàn thành" -> android.R.color.holo_green_dark
                    "Đã hủy" -> android.R.color.holo_red_dark
                    "Đang giao" -> android.R.color.holo_blue_dark
                    else -> android.R.color.darker_gray
                }
            )
        )
    }

    override fun getItemCount() = orders.size
}
