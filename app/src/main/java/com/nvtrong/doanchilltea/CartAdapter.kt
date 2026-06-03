package com.nvtrong.doanchilltea

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.nvtrong.doanchilltea.model.CartItem

class CartAdapter(
    private val items: MutableList<CartItem>,
    private val editable: Boolean = true,
    private val onQtyChanged: () -> Unit
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgItem: ImageView = view.findViewById(R.id.itemCartImage)
        val tvName: TextView = view.findViewById(R.id.itemCartName)
        val tvPrice: TextView = view.findViewById(R.id.itemCartPrice)
        val tvQuantity: TextView = view.findViewById(R.id.itemCartQuantity)
        val btnPlus: MaterialButton = view.findViewById(R.id.itemCartBtnPlus)
        val btnMinus: MaterialButton = view.findViewById(R.id.itemCartBtnMinus)
        val btnDelete: ImageView = view.findViewById(R.id.itemCartBtnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.name
        holder.tvPrice.text = item.displayPrice
        holder.tvQuantity.text = item.quantity.toString()
        ProductImageLoader.load(holder.imgItem, item.imageUrl)

        val controlsVisibility = if (editable) View.VISIBLE else View.GONE
        holder.btnPlus.visibility = controlsVisibility
        holder.btnMinus.visibility = controlsVisibility
        holder.btnDelete.visibility = controlsVisibility

        if (!editable) {
            holder.btnPlus.setOnClickListener(null)
            holder.btnMinus.setOnClickListener(null)
            holder.btnDelete.setOnClickListener(null)
            return
        }

        holder.btnPlus.setOnClickListener {
            item.quantity++
            notifyItemChanged(holder.adapterPosition)
            onQtyChanged()
        }

        holder.btnMinus.setOnClickListener {
            if (item.quantity > 1) {
                item.quantity--
                notifyItemChanged(holder.adapterPosition)
            } else {
                val pos = holder.adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    items.removeAt(pos)
                    notifyItemRemoved(pos)
                }
            }
            onQtyChanged()
        }

        holder.btnDelete.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                items.removeAt(pos)
                notifyItemRemoved(pos)
            }
            onQtyChanged()
        }
    }

    override fun getItemCount(): Int = items.size
}
