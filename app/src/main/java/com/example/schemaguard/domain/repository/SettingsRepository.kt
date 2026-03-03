package com.example.schemaguard.domain.repository

import com.example.schemaguard.domain.model.ProjectConfig

interface SettingsRepository {
    suspend fun getConfig(): ProjectConfig
    suspend fun saveConfig(config: ProjectConfig)
}
