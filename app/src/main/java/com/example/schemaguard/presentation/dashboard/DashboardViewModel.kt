package com.example.schemaguard.presentation.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schemaguard.domain.model.DashboardItem
import com.example.schemaguard.domain.model.DashboardItemType
import com.example.schemaguard.domain.model.SchemaResult
import com.example.schemaguard.domain.repository.SchemaRepository
import com.example.schemaguard.domain.repository.SettingsRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardViewModel(
    private val schemaRepository: SchemaRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _dashboardItems = MutableLiveData<List<DashboardItem>>()
    val dashboardItems: LiveData<List<DashboardItem>> = _dashboardItems

    private val _recentSchemas = MutableLiveData<List<SchemaResult>>()
    val recentSchemas: LiveData<List<SchemaResult>> = _recentSchemas

    fun loadDashboard() {
        viewModelScope.launch {
            val config = settingsRepository.getConfig()
            val recent = schemaRepository.getRecentSchemas()
            _recentSchemas.value = recent

            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            val lastRun = if (recent.isNotEmpty()) {
                dateFormat.format(Date(recent.last().generatedAt))
            } else {
                "Never"
            }

            _dashboardItems.value = listOf(
                DashboardItem(
                    title = "Schemas Generated",
                    subtitle = "${recent.size}",
                    type = DashboardItemType.SCHEMAS_COUNT
                ),
                DashboardItem(
                    title = "Last Run",
                    subtitle = lastRun,
                    type = DashboardItemType.LAST_RUN
                ),
                DashboardItem(
                    title = "Project Path",
                    subtitle = config.projectPath.ifEmpty { "Not configured" },
                    type = DashboardItemType.PROJECT_PATH
                )
            )
        }
    }
}
