package com.nvtrong.doanchilltea

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.nvtrong.doanchilltea.model.AccountItem

class AdminAccountAdapter(
    private val items: MutableList<AccountItem>,
    private val onToggle: (AccountItem) -> Unit
) : RecyclerView.Adapter<AdminAccountAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvAccountName)
        val tvPhone: TextView = view.findViewById(R.id.tvAccountPhone)
        val tvRole: TextView = view.findViewById(R.id.tvAccountRole)
        val toggle: SwitchMaterial = view.findViewById(R.id.switchAccountStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_account_admin, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val account = items[position]
        holder.tvName.text = account.name
        holder.tvPhone.text = account.phone
        holder.tvRole.text = account.roleLabel

        holder.toggle.setOnCheckedChangeListener(null)
        holder.toggle.isChecked = account.isActive
        holder.toggle.setOnCheckedChangeListener { _, isChecked ->
            account.isActive = isChecked
            onToggle(account)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<AccountItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
