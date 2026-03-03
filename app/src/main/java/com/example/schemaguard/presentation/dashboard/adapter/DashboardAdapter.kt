package com.example.schemaguard.presentation.dashboard.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.schemaguard.databinding.ItemDashboardBinding
import com.example.schemaguard.domain.model.DashboardItem

class DashboardAdapter : ListAdapter<DashboardItem, DashboardAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(
        private val binding: ItemDashboardBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DashboardItem) {
            binding.tvTitle.text = item.title
            binding.tvSubtitle.text = item.subtitle
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDashboardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class DiffCallback : DiffUtil.ItemCallback<DashboardItem>() {
        override fun areItemsTheSame(oldItem: DashboardItem, newItem: DashboardItem) =
            oldItem.type == newItem.type

        override fun areContentsTheSame(oldItem: DashboardItem, newItem: DashboardItem) =
            oldItem == newItem
    }
}
