package com.example.schemaguard.domain.usecase

import com.example.schemaguard.domain.model.SchemaResult
import com.example.schemaguard.domain.repository.SchemaRepository

class GenerateSchemaUseCase(private val repository: SchemaRepository) {
    suspend operator fun invoke(filePaths: List<String>): List<SchemaResult> {
        return repository.generateSchema(filePaths)
    }
}
