package com.example.schemaguard.data.repository

import com.example.schemaguard.data.local.FileSystemDataSource
import com.example.schemaguard.domain.model.FileNode
import com.example.schemaguard.domain.repository.FileTreeRepository

class FileTreeRepositoryImpl(
    private val fileSystemDataSource: FileSystemDataSource
) : FileTreeRepository {

    override suspend fun getFileTree(rootPath: String): FileNode {
        return fileSystemDataSource.buildFileTree(rootPath)
    }

    override suspend fun getChangedFiles(rootPath: String, branch: String): List<String> {
        return fileSystemDataSource.getChangedFiles(rootPath, branch)
    }

    override suspend fun getKotlinFiles(rootPath: String): List<FileNode> {
        return fileSystemDataSource.getKotlinFiles(rootPath)
    }
}
