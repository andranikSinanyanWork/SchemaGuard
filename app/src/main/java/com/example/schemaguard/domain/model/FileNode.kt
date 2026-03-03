package com.example.schemaguard.domain.model

data class FileNode(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val children: List<FileNode> = emptyList(),
    val isExpanded: Boolean = false,
    val isSelected: Boolean = false,
    val hasSchemaAnnotation: Boolean = false,
    val isChanged: Boolean = false,
    val depth: Int = 0
)
