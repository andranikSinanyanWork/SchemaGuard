package com.example.schemaguard.domain.model

data class ProjectConfig(
    val projectPath: String = "",
    val githubRepoUrl: String = "",
    val branchName: String = "main",
    val autoDetectChanges: Boolean = true
)
