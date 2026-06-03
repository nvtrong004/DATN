package com.nvtrong.doanchilltea

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.nvtrong.doanchilltea.model.OrderItem

class AdminOrderAdapter(
    private val items: MutableList<OrderItem>,
    private val onAction: (OrderItem, String) -> Unit
) : RecyclerView.Adapter<AdminOrderAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvId: TextView = view.findViewById(R.id.tvAdminOrderID)
        val tvStatus: TextView = view.findViewById(R.id.tvAdminOrderStatus)
        val tvCustomer: TextView = view.findViewById(R.id.tvAdminCustomerName)
        val tvDate: TextView = view.findViewById(R.id.tvAdminOrderDate)
        val tvTotal: TextView = view.findViewById(R.id.tvAdminOrderTotal)
        val btnConfirm: MaterialButton = view.findViewById(R.id.btnAdminConfirmOrder)
        val btnCancel: MaterialButton = view.findViewById(R.id.btnAdminCancelOrder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_admin, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = items[position]
        holder.tvId.text = order.id
        holder.tvStatus.text = order.status
        holder.tvCustomer.text = "Khách hàng: ${order.customerName}"
        holder.tvDate.text = "Ngày đặt: ${order.date}"
        holder.tvTotal.text = order.displayTotal

        val isDelivering = order.status == "Đang giao"
        val isDone = order.status == "Đã hoàn thành" || order.status == "Đã hủy"

        holder.btnConfirm.visibility = if (isDone) View.GONE else View.VISIBLE
        holder.btnCancel.visibility = if (isDone || isDelivering) View.GONE else View.VISIBLE
        holder.btnConfirm.text = if (isDelivering) "Hoàn thành" else "Duyệt đơn"

        holder.btnConfirm.setOnClickListener {
            onAction(order, if (isDelivering) "complete" else "confirm")
        }
        holder.btnCancel.setOnClickListener { onAction(order, "cancel") }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<OrderItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
