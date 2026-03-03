package com.example.schemaguard

import com.example.schemaguard.domain.model.annotation.GenerateJsonSchema
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.io.File
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType

/**
 * Builds a downstream dependency tree for each @GenerateJsonSchema class.
 *
 * For each annotated entry point, walks all fields recursively and collects
 * every class (data class, enum, nested object) that the schema depends on.
 * Maps each class back to its source file path.
 *
 * Output: a JSON tree + a visual ASCII tree printed to console.
 */
object DependencyTreeBuilder {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    @JvmStatic
    fun main(args: Array<String>) {
        println("=== SchemaGuard — Dependency Tree Builder ===\n")

        val annotatedClasses = SchemaGenerator.findAllAnnotatedClasses("com.example.schemaguard")

        if (annotatedClasses.isEmpty()) {
            println("No @GenerateJsonSchema classes found.")
            return
        }

        val allTrees = JsonObject()

        for (clazz in annotatedClasses) {
            println("━".repeat(60))
            println("  Entry Point: ${clazz.simpleName}")
            println("━".repeat(60))

            // Build the tree data structure
            val visited = mutableSetOf<Class<*>>()
            val treeNode = buildTree(clazz, visited)

            // Print ASCII tree to console
            printAsciiTree(treeNode, prefix = "", isLast = true, isRoot = true)
            println()

            // Build JSON representation
            val treeJson = buildTreeJson(clazz, treeNode)
            allTrees.add(clazz.simpleName, treeJson)
        }

        // Print the full JSON
        println("\n${"═".repeat(60)}")
        println("  schema_trees.json")
        println("═".repeat(60))
        println(gson.toJson(allTrees))

        // Save to file if --save flag is passed
        if (args.any { it == "--save" }) {
            val outputDir = args.firstOrNull { it.startsWith("--output=") }
                ?.substringAfter("=")
                ?: "."
            val outputFile = File(outputDir, "schema_trees.json")
            outputFile.parentFile?.mkdirs()
            outputFile.writeText(gson.toJson(allTrees))
            println("\n[SAVED] ${outputFile.absolutePath}")
        }
    }

    // ──────────────────────────────────────────────
    // Tree Data Structure
    // ──────────────────────────────────────────────

    data class TreeNode(
        val className: String,
        val qualifiedName: String,
        val kind: NodeKind,
        val sourceFile: String,
        val children: MutableList<TreeNode> = mutableListOf()
    )

    enum class NodeKind {
        DATA_CLASS, ENUM, PRIMITIVE, LIST, UNKNOWN
    }

    // ──────────────────────────────────────────────
    // Build tree via reflection (downstream)
    // ──────────────────────────────────────────────

    private fun buildTree(
        clazz: Class<*>,
        visited: MutableSet<Class<*>>
    ): TreeNode {
        val kind = when {
            clazz.isEnum -> NodeKind.ENUM
            clazz.declaredFields.isNotEmpty() -> NodeKind.DATA_CLASS
            else -> NodeKind.UNKNOWN
        }

        val node = TreeNode(
            className = clazz.simpleName,
            qualifiedName = clazz.name,
            kind = kind,
            sourceFile = resolveSourceFile(clazz)
        )

        if (!visited.add(clazz)) return node // prevent cycles

        if (clazz.isEnum) {
            // Enums are leaf nodes — list their values as info but no children
            return node
        }

        val fields = clazz.declaredFields.toList()

        for (field in fields) {
            val fieldType = field.type

            when {
                fieldType.isEnum -> {
                    node.children.add(
                        TreeNode(
                            className = "${field.name}: ${fieldType.simpleName}",
                            qualifiedName = fieldType.name,
                            kind = NodeKind.ENUM,
                            sourceFile = resolveSourceFile(fieldType)
                        )
                    )
                }

                List::class.java.isAssignableFrom(fieldType) -> {
                    val itemType = getListElementType(field)

                    if (isPrimitiveOrWrapper(itemType) || itemType == String::class.java) {
                        node.children.add(
                            TreeNode(
                                className = "${field.name}: List<${itemType.simpleName}>",
                                qualifiedName = itemType.name,
                                kind = NodeKind.PRIMITIVE,
                                sourceFile = "(primitive)"
                            )
                        )
                    } else {
                        val listNode = TreeNode(
                            className = "${field.name}: List<${itemType.simpleName}>",
                            qualifiedName = itemType.name,
                            kind = NodeKind.LIST,
                            sourceFile = resolveSourceFile(itemType)
                        )
                        // Recurse into the list item type
                        val childTree = buildTree(itemType, visited)
                        listNode.children.addAll(childTree.children)
                        node.children.add(listNode)
                    }
                }

                isPrimitiveOrWrapper(fieldType) || fieldType == String::class.java -> {
                    // Skip primitives — they don't create dependencies
                }

                else -> {
                    // Nested object — recurse
                    val childTree = buildTree(fieldType, visited)
                    val nestedNode = TreeNode(
                        className = "${field.name}: ${fieldType.simpleName}",
                        qualifiedName = fieldType.name,
                        kind = NodeKind.DATA_CLASS,
                        sourceFile = resolveSourceFile(fieldType),
                        children = childTree.children
                    )
                    node.children.add(nestedNode)
                }
            }
        }

        visited.remove(clazz) // allow same class in different branches
        return node
    }

    // ──────────────────────────────────────────────
    // ASCII Tree Printer
    // ──────────────────────────────────────────────

    private fun printAsciiTree(
        node: TreeNode,
        prefix: String,
        isLast: Boolean,
        isRoot: Boolean
    ) {
        val connector = when {
            isRoot -> ""
            isLast -> "└── "
            else -> "├── "
        }
        val continuationPrefix = when {
            isRoot -> ""
            isLast -> "    "
            else -> "│   "
        }

        val icon = when (node.kind) {
            NodeKind.DATA_CLASS -> "[D]"
            NodeKind.ENUM -> "[E]"
            NodeKind.LIST -> "[L]"
            NodeKind.PRIMITIVE -> "[P]"
            NodeKind.UNKNOWN -> "[?]"
        }

        val fileInfo = if (node.sourceFile != "(primitive)") {
            "  -> ${node.sourceFile}"
        } else ""

        println("$prefix$connector$icon ${node.className}$fileInfo")

        for ((index, child) in node.children.withIndex()) {
            val last = index == node.children.size - 1
            printAsciiTree(child, "$prefix$continuationPrefix", last, false)
        }
    }

    // ──────────────────────────────────────────────
    // JSON Builder
    // ──────────────────────────────────────────────

    private fun buildTreeJson(rootClass: Class<*>, rootNode: TreeNode): JsonObject {
        val json = JsonObject()
        json.addProperty("entry_class", rootClass.name)
        json.addProperty("entry_file", resolveSourceFile(rootClass))

        // Collect all unique dependency files
        val allFiles = mutableSetOf<String>()
        collectAllFiles(rootNode, allFiles)
        allFiles.remove("(primitive)")

        val filesArray = JsonArray()
        allFiles.sorted().forEach { filesArray.add(it) }
        json.add("downstream_files", filesArray)

        // Full tree structure
        json.add("tree", nodeToJson(rootNode))

        return json
    }

    private fun nodeToJson(node: TreeNode): JsonObject {
        val json = JsonObject()
        json.addProperty("class", node.className)
        json.addProperty("qualified_name", node.qualifiedName)
        json.addProperty("kind", node.kind.name)
        json.addProperty("source_file", node.sourceFile)

        if (node.children.isNotEmpty()) {
            val childrenArray = JsonArray()
            node.children.forEach { childrenArray.add(nodeToJson(it)) }
            json.add("children", childrenArray)
        }

        return json
    }

    private fun collectAllFiles(node: TreeNode, files: MutableSet<String>) {
        files.add(node.sourceFile)
        node.children.forEach { collectAllFiles(it, files) }
    }

    // ──────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────

    /**
     * Resolves a class back to its likely source file path.
     * Uses the class package + enclosing class or simple name to build the path.
     * For inner/nested classes, walks up to the top-level class.
     */
    private fun resolveSourceFile(clazz: Class<*>): String {
        // Get the top-level enclosing class (for inner classes, enums defined inside a file)
        var topLevel = clazz
        while (topLevel.enclosingClass != null) {
            topLevel = topLevel.enclosingClass
        }

        val packagePath = topLevel.`package`?.name?.replace('.', '/') ?: ""
        val fileName = topLevel.simpleName + ".kt"

        return "$packagePath/$fileName"
    }

    private fun getListElementType(field: Field): Class<*> {
        val genericType = field.genericType
        if (genericType is ParameterizedType) {
            val actualType = genericType.actualTypeArguments.firstOrNull()
            if (actualType is Class<*>) return actualType
        }
        return String::class.java
    }

    private fun isPrimitiveOrWrapper(type: Class<*>?): Boolean {
        if (type == null) return false
        return type.isPrimitive ||
                type == java.lang.Boolean::class.java ||
                type == java.lang.Byte::class.java ||
                type == java.lang.Short::class.java ||
                type == java.lang.Integer::class.java ||
                type == java.lang.Long::class.java ||
                type == java.lang.Float::class.java ||
                type == java.lang.Double::class.java ||
                type == java.lang.Character::class.java
    }
}
