package com.nvtrong.doanchilltea

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.nvtrong.doanchilltea.model.AdminProduct

class AdminProductAdapter(
    initialItems: List<AdminProduct>,
    private val onToggle: (AdminProduct) -> Unit,
    private val onEdit: (AdminProduct) -> Unit,
    private val onDelete: (AdminProduct) -> Unit
) : RecyclerView.Adapter<AdminProductAdapter.ViewHolder>() {

    private val items = initialItems.toMutableList()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgProduct: ImageView = view.findViewById(R.id.imgAdminProduct)
        val tvName: TextView = view.findViewById(R.id.tvAdminProductName)
        val tvPrice: TextView = view.findViewById(R.id.tvAdminProductPrice)
        val tvCategory: TextView = view.findViewById(R.id.tvAdminProductCategory)
        val btnEdit: ImageView = view.findViewById(R.id.btnEditProduct)
        val toggle: SwitchMaterial = view.findViewById(R.id.switchProductActive)
        val tvStatus: TextView = view.findViewById(R.id.tvActiveStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_product_admin, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = items[position]
        holder.tvName.text = product.name
        holder.tvPrice.text = product.displayPrice
        holder.tvCategory.text = "Danh mục: ${product.category}"
        holder.tvStatus.text = if (product.isActive) "Đang bán" else "Đã ẩn"
        ProductImageLoader.load(holder.imgProduct, product.imageUrl)

        holder.toggle.setOnCheckedChangeListener(null)
        holder.toggle.isChecked = product.isActive
        holder.toggle.setOnCheckedChangeListener { _, isChecked ->
            product.isActive = isChecked
            holder.tvStatus.text = if (isChecked) "Đang bán" else "Đã ẩn"
            onToggle(product)
        }

        holder.btnEdit.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menu.add("Chỉnh sửa")
            popup.menu.add("Xóa món này")
            popup.setOnMenuItemClickListener { item ->
                when {
                    item.title.toString().contains("Chỉnh") -> onEdit(product)
                    item.title.toString().contains("Xóa") -> onDelete(product)
                }
                true
            }
            popup.show()
        }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<AdminProduct>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
