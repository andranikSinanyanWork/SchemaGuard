package com.example.schemaguard.data.local

import com.example.schemaguard.domain.model.FileNode
import java.io.File

class FileSystemDataSource {

    fun buildFileTree(rootPath: String, depth: Int = 0): FileNode {
        val file = File(rootPath)
        if (!file.exists()) {
            return FileNode(
                name = file.name,
                path = rootPath,
                isDirectory = false,
                depth = depth
            )
        }

        if (!file.isDirectory) {
            return FileNode(
                name = file.name,
                path = file.absolutePath,
                isDirectory = false,
                hasSchemaAnnotation = checkForSchemaAnnotation(file),
                depth = depth
            )
        }

        val children = file.listFiles()
            ?.filter { !it.name.startsWith(".") && it.name != "build" }
            ?.sortedWith(compareBy({ !it.isDirectory }, { it.name }))
            ?.map { buildFileTree(it.absolutePath, depth + 1) }
            ?: emptyList()

        return FileNode(
            name = file.name,
            path = file.absolutePath,
            isDirectory = true,
            children = children,
            depth = depth
        )
    }

    fun getKotlinFiles(rootPath: String): List<FileNode> {
        val result = mutableListOf<FileNode>()
        collectKotlinFiles(File(rootPath), result, 0)
        return result
    }

    private fun collectKotlinFiles(dir: File, result: MutableList<FileNode>, depth: Int) {
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory && !file.name.startsWith(".") && file.name != "build") {
                collectKotlinFiles(file, result, depth + 1)
            } else if (file.extension == "kt") {
                result.add(
                    FileNode(
                        name = file.name,
                        path = file.absolutePath,
                        isDirectory = false,
                        hasSchemaAnnotation = checkForSchemaAnnotation(file),
                        depth = depth
                    )
                )
            }
        }
    }

    private fun checkForSchemaAnnotation(file: File): Boolean {
        if (file.extension != "kt") return false
        return try {
            file.readText().contains("@GenerateJsonSchema")
        } catch (e: Exception) {
            false
        }
    }

    fun getChangedFiles(rootPath: String, branch: String): List<String> {
        return try {
            val process = ProcessBuilder("git", "diff", "--name-only", "origin/$branch...HEAD")
                .directory(File(rootPath))
                .redirectErrorStream(true)
                .start()
            process.inputStream.bufferedReader().readLines()
                .filter { it.endsWith(".kt") }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
