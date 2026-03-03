package com.example.schemaguard.domain.usecase

import com.example.schemaguard.domain.model.ProjectConfig
import com.example.schemaguard.domain.repository.SettingsRepository

class SaveSettingsUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(config: ProjectConfig) {
        repository.saveConfig(config)
    }
}
