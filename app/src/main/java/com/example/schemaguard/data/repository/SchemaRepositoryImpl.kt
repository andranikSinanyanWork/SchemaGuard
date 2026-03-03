package com.example.schemaguard.data.repository

import com.example.schemaguard.data.local.SharedPrefsDataSource
import com.example.schemaguard.domain.model.SchemaResult
import com.example.schemaguard.domain.repository.SchemaRepository

class SchemaRepositoryImpl(
    private val prefsDataSource: SharedPrefsDataSource
) : SchemaRepository {

    private val cachedResults = mutableListOf<SchemaResult>()

    override suspend fun generateSchema(filePaths: List<String>): List<SchemaResult> {
        // Placeholder: will be replaced with actual schema generation logic
        // adapted from CiCdTest.kt in the Net project
        val results = filePaths.map { path ->
            SchemaResult(
                className = path.substringAfterLast("/").removeSuffix(".kt"),
                filePath = path,
                schemaJson = generatePlaceholderSchema(path)
            )
        }
        cachedResults.addAll(results)
        saveRecentToPrefs()
        return results
    }

    override suspend fun getRecentSchemas(): List<SchemaResult> {
        if (cachedResults.isEmpty()) {
            val saved = prefsDataSource.getRecentSchemas()
            // Return placeholder results from saved class names
            return saved.map { className ->
                SchemaResult(
                    className = className,
                    filePath = "",
                    schemaJson = "{}"
                )
            }
        }
        return cachedResults.takeLast(10)
    }

    override suspend fun saveSchema(result: SchemaResult) {
        cachedResults.add(result)
        saveRecentToPrefs()
    }

    private fun saveRecentToPrefs() {
        val classNames = cachedResults.takeLast(10).map { it.className }
        prefsDataSource.saveRecentSchemas(classNames)
    }

    private fun generatePlaceholderSchema(filePath: String): String {
        val className = filePath.substringAfterLast("/").removeSuffix(".kt")
        return """
        {
          "${'$'}schema": "http://json-schema.org/draft-07/schema#",
          "type": "object",
          "title": "$className",
          "description": "Auto-generated schema for $className",
          "properties": {},
          "additionalProperties": false
        }
        """.trimIndent()
    }
}
