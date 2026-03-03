package com.example.schemaguard.presentation.generator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.schemaguard.databinding.FragmentGeneratorBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class GeneratorFragment : Fragment() {

    private var _binding: FragmentGeneratorBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GeneratorViewModel by viewModel()

    // Selected file paths passed from FileTreeFragment via shared state or arguments
    private var selectedFilePaths: List<String> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGeneratorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnGenerate.setOnClickListener {
            viewModel.generateSchemas(selectedFilePaths)
        }

        binding.btnCopy.setOnClickListener {
            val text = viewModel.getSchemaText()
            if (text.isNotEmpty()) {
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE)
                        as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Schema", text))
                Toast.makeText(requireContext(), "Schema copied!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.schemaResults.observe(viewLifecycleOwner) { results ->
            val text = results.joinToString("\n\n") { result ->
                "// ${result.className}\n${result.schemaJson}"
            }
            binding.tvSchemaOutput.text = text
        }

        viewModel.status.observe(viewLifecycleOwner) { status ->
            binding.tvStatus.text = when (status) {
                GeneratorStatus.IDLE -> "Ready to generate"
                GeneratorStatus.GENERATING -> "Generating schemas..."
                GeneratorStatus.DONE -> "Generation complete"
                GeneratorStatus.ERROR -> "Error occurred"
                null -> "Ready to generate"
            }
            binding.progressBar.visibility = if (status == GeneratorStatus.GENERATING)
                View.VISIBLE else View.GONE
            binding.btnCopy.visibility = if (status == GeneratorStatus.DONE)
                View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            binding.tvError.visibility = if (error != null) View.VISIBLE else View.GONE
            binding.tvError.text = error
        }
    }

    fun setSelectedFiles(paths: List<String>) {
        selectedFilePaths = paths
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
