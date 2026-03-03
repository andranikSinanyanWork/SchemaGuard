package com.example.schemaguard.domain.usecase

import com.example.schemaguard.domain.repository.FileTreeRepository

class GetChangedFilesUseCase(private val repository: FileTreeRepository) {
    suspend operator fun invoke(rootPath: String, branch: String): List<String> {
        return repository.getChangedFiles(rootPath, branch)
    }
}
