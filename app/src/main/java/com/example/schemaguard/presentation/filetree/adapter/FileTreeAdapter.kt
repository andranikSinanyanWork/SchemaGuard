package com.example.schemaguard.presentation.filetree.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.schemaguard.R
import com.example.schemaguard.databinding.ItemFileNodeBinding
import com.example.schemaguard.domain.model.FileNode

class FileTreeAdapter(
    private val onItemClick: (FileNode) -> Unit,
    private val onCheckChanged: (FileNode) -> Unit
) : ListAdapter<FileNode, FileTreeAdapter.ViewHolder>(DiffCallback()) {

    private val selectedPaths = mutableSetOf<String>()

    fun updateSelectedPaths(paths: List<String>) {
        selectedPaths.clear()
        selectedPaths.addAll(paths)
        notifyDataSetChanged()
    }

    class ViewHolder(
        private val binding: ItemFileNodeBinding,
        private val onItemClick: (FileNode) -> Unit,
        private val onCheckChanged: (FileNode) -> Unit,
        private val selectedPaths: Set<String>
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(node: FileNode) {
            val indentPx = node.depth * 24 * binding.root.context.resources.displayMetrics.density
            binding.viewIndent.layoutParams.width = indentPx.toInt()

            binding.tvFileName.text = node.name

            if (node.isDirectory) {
                binding.ivIcon.setImageResource(
                    if (node.isExpanded) R.drawable.ic_folder_open else R.drawable.ic_folder
                )
                binding.cbSelect.visibility = View.GONE
            } else {
                binding.ivIcon.setImageResource(R.drawable.ic_file)
                binding.cbSelect.visibility = View.VISIBLE
                binding.cbSelect.isChecked = selectedPaths.contains(node.path)
            }

            if (node.hasSchemaAnnotation) {
                binding.tvFileName.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.schema_annotated)
                )
            } else {
                binding.tvFileName.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.text_primary)
                )
            }

            binding.root.setOnClickListener { onItemClick(node) }
            binding.cbSelect.setOnClickListener { onCheckChanged(node) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFileNodeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding, onItemClick, onCheckChanged, selectedPaths)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class DiffCallback : DiffUtil.ItemCallback<FileNode>() {
        override fun areItemsTheSame(oldItem: FileNode, newItem: FileNode) =
            oldItem.path == newItem.path

        override fun areContentsTheSame(oldItem: FileNode, newItem: FileNode) =
            oldItem == newItem
    }
}
