package com.example.schemaguard.domain.repository

import com.example.schemaguard.domain.model.SchemaResult

interface SchemaRepository {
    suspend fun generateSchema(filePaths: List<String>): List<SchemaResult>
    suspend fun getRecentSchemas(): List<SchemaResult>
    suspend fun saveSchema(result: SchemaResult)
}
