package com.example.schemaguard.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.schemaguard.domain.model.ProjectConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedPrefsDataSource(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveConfig(config: ProjectConfig) {
        prefs.edit().putString(KEY_CONFIG, gson.toJson(config)).apply()
    }

    fun getConfig(): ProjectConfig {
        val json = prefs.getString(KEY_CONFIG, null) ?: return ProjectConfig()
        return gson.fromJson(json, ProjectConfig::class.java)
    }

    fun saveRecentSchemas(schemas: List<String>) {
        prefs.edit().putString(KEY_RECENT_SCHEMAS, gson.toJson(schemas)).apply()
    }

    fun getRecentSchemas(): List<String> {
        val json = prefs.getString(KEY_RECENT_SCHEMAS, null) ?: return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type)
    }

    companion object {
        private const val PREFS_NAME = "schema_guard_prefs"
        private const val KEY_CONFIG = "project_config"
        private const val KEY_RECENT_SCHEMAS = "recent_schemas"
    }
}
