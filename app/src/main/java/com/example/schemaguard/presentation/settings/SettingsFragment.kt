package com.example.schemaguard.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.schemaguard.databinding.FragmentSettingsBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeViewModel()
        viewModel.loadSettings()
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            viewModel.saveSettings(
                projectPath = binding.etProjectPath.text.toString(),
                githubUrl = binding.etGithubUrl.text.toString(),
                branch = binding.etBranch.text.toString(),
                autoDetect = binding.switchAutoDetect.isChecked
            )
        }

        binding.btnReset.setOnClickListener {
            viewModel.loadSettings()
        }
    }

    private fun observeViewModel() {
        viewModel.config.observe(viewLifecycleOwner) { config ->
            binding.etProjectPath.setText(config.projectPath)
            binding.etGithubUrl.setText(config.githubRepoUrl)
            binding.etBranch.setText(config.branchName)
            binding.switchAutoDetect.isChecked = config.autoDetectChanges
        }

        viewModel.saveSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Settings saved", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
