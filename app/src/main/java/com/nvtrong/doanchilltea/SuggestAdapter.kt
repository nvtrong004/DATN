package com.nvtrong.doanchilltea

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.nvtrong.doanchilltea.model.Product

class SuggestAdapter(
    private val itemList: List<Product>,
    private val onAddClick: (Product) -> Unit
) : RecyclerView.Adapter<SuggestAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgItem: ImageView = view.findViewById(R.id.imgSuggestItem)
        val tvName: TextView = view.findViewById(R.id.tvSuggestName)
        val tvPrice: TextView = view.findViewById(R.id.tvSuggestPrice)
        val btnAdd: MaterialButton = view.findViewById(R.id.btnSuggestAdd)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_suggest_today, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = itemList[position]
        holder.tvName.text = product.name
        holder.tvPrice.text = product.price
        ProductImageLoader.load(holder.imgItem, product.imageUrl)

        holder.btnAdd.setOnClickListener { onAddClick(product) }
    }

    override fun getItemCount(): Int = itemList.size
}
