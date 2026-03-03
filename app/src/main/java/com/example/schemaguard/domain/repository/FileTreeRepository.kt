package com.example.schemaguard.domain.repository

import com.example.schemaguard.domain.model.FileNode

interface FileTreeRepository {
    suspend fun getFileTree(rootPath: String): FileNode
    suspend fun getChangedFiles(rootPath: String, branch: String): List<String>
    suspend fun getKotlinFiles(rootPath: String): List<FileNode>
}
