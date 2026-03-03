package com.example.schemaguard.presentation.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schemaguard.domain.model.ProjectConfig
import com.example.schemaguard.domain.repository.SettingsRepository
import com.example.schemaguard.domain.usecase.SaveSettingsUseCase
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val saveSettingsUseCase: SaveSettingsUseCase
) : ViewModel() {

    private val _config = MutableLiveData<ProjectConfig>()
    val config: LiveData<ProjectConfig> = _config

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    fun loadSettings() {
        viewModelScope.launch {
            _config.value = settingsRepository.getConfig()
        }
    }

    fun saveSettings(projectPath: String, githubUrl: String, branch: String, autoDetect: Boolean) {
        viewModelScope.launch {
            val config = ProjectConfig(
                projectPath = projectPath,
                githubRepoUrl = githubUrl,
                branchName = branch,
                autoDetectChanges = autoDetect
            )
            saveSettingsUseCase(config)
            _config.value = config
            _saveSuccess.value = true
        }
    }
}
