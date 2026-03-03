package com.example.schemaguard.presentation.filetree

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.schemaguard.databinding.FragmentFileTreeBinding
import com.example.schemaguard.presentation.filetree.adapter.FileTreeAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class FileTreeFragment : Fragment() {

    private var _binding: FragmentFileTreeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FileTreeViewModel by viewModel()
    private lateinit var adapter: FileTreeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFileTreeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        viewModel.loadFileTree()
    }

    private fun setupRecyclerView() {
        adapter = FileTreeAdapter(
            onItemClick = { node ->
                if (node.isDirectory) {
                    viewModel.toggleExpand(node)
                }
            },
            onCheckChanged = { node ->
                viewModel.toggleSelection(node)
            }
        )
        binding.rvFileTree.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFileTree.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.fileTree.observe(viewLifecycleOwner) { nodes ->
            adapter.submitList(nodes)
        }

        viewModel.selectedFiles.observe(viewLifecycleOwner) { selected ->
            adapter.updateSelectedPaths(selected.map { it.path })
            binding.tvSelectedCount.text = "${selected.size} files selected"
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            binding.tvError.visibility = if (error != null) View.VISIBLE else View.GONE
            binding.tvError.text = error
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
