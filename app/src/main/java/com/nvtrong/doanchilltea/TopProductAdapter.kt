package com.nvtrong.doanchilltea

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nvtrong.doanchilltea.model.TopProduct

class TopProductAdapter(
    private val items: List<TopProduct>
) : RecyclerView.Adapter<TopProductAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRank: TextView    = view.findViewById(R.id.tvRank)
        val imgProduct: ImageView = view.findViewById(R.id.imgTopProduct)
        val tvName: TextView    = view.findViewById(R.id.tvTopProductName)
        val tvSold: TextView    = view.findViewById(R.id.tvTotalSold)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_top_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = items[position]
        holder.tvRank.text = "#${product.rank}"
        ProductImageLoader.load(holder.imgProduct, product.imageUrl)
        holder.tvName.text = product.name
        holder.tvSold.text = "${product.totalSold} ly"
    }

    override fun getItemCount(): Int = items.size
}
