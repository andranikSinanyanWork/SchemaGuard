package com.example.schemaguard.data.repository

import com.example.schemaguard.data.local.SharedPrefsDataSource
import com.example.schemaguard.domain.model.ProjectConfig
import com.example.schemaguard.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val prefsDataSource: SharedPrefsDataSource
) : SettingsRepository {

    override suspend fun getConfig(): ProjectConfig {
        return prefsDataSource.getConfig()
    }

    override suspend fun saveConfig(config: ProjectConfig) {
        prefsDataSource.saveConfig(config)
    }
}
