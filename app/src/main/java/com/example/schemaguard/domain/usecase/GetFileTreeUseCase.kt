package com.example.schemaguard.domain.usecase

import com.example.schemaguard.domain.model.FileNode
import com.example.schemaguard.domain.repository.FileTreeRepository

class GetFileTreeUseCase(private val repository: FileTreeRepository) {
    suspend operator fun invoke(rootPath: String): FileNode {
        return repository.getFileTree(rootPath)
    }
}
