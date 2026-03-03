package com.example.schemaguard.presentation.filetree

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schemaguard.domain.model.FileNode
import com.example.schemaguard.domain.repository.SettingsRepository
import com.example.schemaguard.domain.usecase.GetFileTreeUseCase
import kotlinx.coroutines.launch

class FileTreeViewModel(
    private val getFileTreeUseCase: GetFileTreeUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _fileTree = MutableLiveData<List<FileNode>>()
    val fileTree: LiveData<List<FileNode>> = _fileTree

    private val _selectedFiles = MutableLiveData<List<FileNode>>(emptyList())
    val selectedFiles: LiveData<List<FileNode>> = _selectedFiles

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadFileTree() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val config = settingsRepository.getConfig()
                if (config.projectPath.isEmpty()) {
                    _error.value = "Project path not configured. Go to Settings."
                    _isLoading.value = false
                    return@launch
                }
                val tree = getFileTreeUseCase(config.projectPath)
                _fileTree.value = flattenTree(tree)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load file tree"
            }
            _isLoading.value = false
        }
    }

    fun toggleSelection(node: FileNode) {
        val current = _selectedFiles.value.orEmpty().toMutableList()
        if (current.any { it.path == node.path }) {
            current.removeAll { it.path == node.path }
        } else {
            current.add(node)
        }
        _selectedFiles.value = current
    }

    fun getSelectedPaths(): List<String> {
        return _selectedFiles.value.orEmpty().map { it.path }
    }

    private fun flattenTree(node: FileNode): List<FileNode> {
        val result = mutableListOf<FileNode>()
        result.add(node)
        if (node.isDirectory && node.isExpanded) {
            node.children.forEach { child ->
                result.addAll(flattenTree(child))
            }
        }
        return result
    }

    fun toggleExpand(node: FileNode) {
        val currentTree = _fileTree.value ?: return
        val updatedTree = currentTree.map { n ->
            if (n.path == node.path && n.isDirectory) {
                n.copy(isExpanded = !n.isExpanded)
            } else n
        }
        // Re-flatten from root with updated expand states
        _fileTree.value = updatedTree
    }
}
