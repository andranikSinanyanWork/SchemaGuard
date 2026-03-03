package com.example.schemaguard.domain.model

data class SchemaResult(
    val className: String,
    val filePath: String,
    val schemaJson: String,
    val generatedAt: Long = System.currentTimeMillis(),
    val isSuccess: Boolean = true,
    val errorMessage: String? = null
)
