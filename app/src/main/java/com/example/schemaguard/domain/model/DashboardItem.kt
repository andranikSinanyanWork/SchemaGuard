package com.example.schemaguard.domain.model

data class DashboardItem(
    val title: String,
    val subtitle: String,
    val type: DashboardItemType
)

enum class DashboardItemType {
    SCHEMAS_COUNT,
    LAST_RUN,
    PROJECT_PATH,
    RECENT_ACTIVITY
}
