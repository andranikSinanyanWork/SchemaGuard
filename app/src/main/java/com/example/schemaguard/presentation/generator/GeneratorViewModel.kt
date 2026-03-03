package com.example.schemaguard.presentation.generator

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schemaguard.domain.model.SchemaResult
import com.example.schemaguard.domain.usecase.GenerateSchemaUseCase
import kotlinx.coroutines.launch

class GeneratorViewModel(
    private val generateSchemaUseCase: GenerateSchemaUseCase
) : ViewModel() {

    private val _schemaResults = MutableLiveData<List<SchemaResult>>()
    val schemaResults: LiveData<List<SchemaResult>> = _schemaResults

    private val _status = MutableLiveData(GeneratorStatus.IDLE)
    val status: LiveData<GeneratorStatus> = _status

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun generateSchemas(filePaths: List<String>) {
        if (filePaths.isEmpty()) {
            _errorMessage.value = "No files selected. Select files in the File Tree tab."
            return
        }

        viewModelScope.launch {
            _status.value = GeneratorStatus.GENERATING
            _errorMessage.value = null
            try {
                val results = generateSchemaUseCase(filePaths)
                _schemaResults.value = results
                _status.value = GeneratorStatus.DONE
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Schema generation failed"
                _status.value = GeneratorStatus.ERROR
            }
        }
    }

    fun getSchemaText(): String {
        return _schemaResults.value?.joinToString("\n\n") { result ->
            "// ${result.className}\n${result.schemaJson}"
        } ?: ""
    }
}

enum class GeneratorStatus {
    IDLE, GENERATING, DONE, ERROR
}
