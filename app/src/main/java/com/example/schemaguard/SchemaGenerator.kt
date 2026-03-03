package com.example.schemaguard

import com.example.schemaguard.domain.model.annotation.GenerateJsonSchema
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import java.io.File
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.util.jar.JarFile
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

object SchemaGenerator {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    @JvmStatic
    fun main(args: Array<String>) {
        println("=== SchemaGuard - JSON Schema Generator ===\n")

        val filesToProcess: List<String>

        if (args.isNotEmpty()) {
            println("Received changed files from arguments:")
            args.forEach { println("  - $it") }
            filesToProcess = args.toList()
        } else {
            println("No arguments received. Scanning all annotated classes...")
            filesToProcess = emptyList()
        }

        val classesToProcess = if (filesToProcess.isNotEmpty()) {
            getClassesFromChangedFiles(filesToProcess)
        } else {
            findAllAnnotatedClasses("com.example.schemaguard")
        }

        processAnnotatedClasses(classesToProcess.toList())
    }

    // ──────────────────────────────────────────────
    // Class Discovery
    // ──────────────────────────────────────────────

    fun findAllAnnotatedClasses(packageName: String): Set<Class<*>> {
        val classes = mutableSetOf<Class<*>>()
        val path = packageName.replace('.', '/')
        val resources = Thread.currentThread().contextClassLoader?.getResources(path)
        resources ?: return emptySet()

        while (resources.hasMoreElements()) {
            val resource = resources.nextElement()
            val file = File(resource.file)
            if (file.isDirectory) {
                findClassesInDirectory(file, packageName, classes)
            } else if (resource.protocol == "jar") {
                val jarPath = resource.path.substring(5, resource.path.indexOf("!"))
                findClassesInJar(jarPath, packageName, classes)
            }
        }
        return classes
    }

    private fun findClassesInDirectory(
        directory: File,
        packageName: String,
        classes: MutableSet<Class<*>>
    ) {
        val files = directory.listFiles() ?: return
        for (file in files) {
            if (file.isDirectory) {
                findClassesInDirectory(file, "$packageName.${file.name}", classes)
            } else if (file.name.endsWith(".class")) {
                val className = "$packageName.${file.name.substring(0, file.name.length - 6)}"
                try {
                    val clazz = Class.forName(className, false, Thread.currentThread().contextClassLoader)
                    if (clazz.isAnnotationPresent(GenerateJsonSchema::class.java)) {
                        classes.add(clazz)
                    }
                } catch (_: Throwable) {
                }
            }
        }
    }

    private fun findClassesInJar(
        jarPath: String,
        packageName: String,
        classes: MutableSet<Class<*>>
    ) {
        try {
            val jarFile = JarFile(jarPath)
            val entries = jarFile.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                val name = entry.name
                if (name.endsWith(".class")) {
                    val className = name.replace('/', '.').substring(0, name.length - 6)
                    if (className.startsWith(packageName)) {
                        try {
                            val clazz = Class.forName(className)
                            if (clazz.isAnnotationPresent(GenerateJsonSchema::class.java)) {
                                classes.add(clazz)
                            }
                        } catch (_: Throwable) {
                        }
                    }
                }
            }
        } catch (_: Exception) {
        }
    }

    fun getClassesFromChangedFiles(files: List<String>): Set<Class<*>> {
        val classes = mutableSetOf<Class<*>>()
        for (file in files) {
            if (file.endsWith(".kt")) {
                val javaPathStart = file.indexOf("src/main/java/")
                if (javaPathStart != -1) {
                    val relativePath = file.substring(javaPathStart + "src/main/java/".length)
                    val className = relativePath.replace('/', '.').replace(".kt", "")
                    try {
                        val clazz = Class.forName(className)
                        if (clazz.isAnnotationPresent(GenerateJsonSchema::class.java)) {
                            classes.add(clazz)
                        }
                    } catch (e: ClassNotFoundException) {
                        println("[WARN] Could not load class for file: $file")
                    }
                }
            }
        }
        return classes
    }

    // ──────────────────────────────────────────────
    // Schema Processing
    // ──────────────────────────────────────────────

    fun processAnnotatedClasses(classesToProcess: List<Class<*>>) {
        println("\n--- Starting JSON Schema Generation ---")
        println("Found ${classesToProcess.size} annotated class(es)\n")

        for (clazz in classesToProcess) {
            println("[INFO] Generating schema for: ${clazz.simpleName}")
            try {
                val schema = generateSchema(clazz)
                println(gson.toJson(schema))
                println("\n[SUCCESS] Schema generated for ${clazz.simpleName}\n")
            } catch (e: Exception) {
                System.err.println("[ERROR] Failed to generate schema for ${clazz.simpleName}: ${e.message}")
            }
        }
        println("--- Generation Complete ---")
    }

    fun generateSchema(rootClass: Class<*>): JsonObject {
        val jsonProperties = getJsonFromClass(rootClass).first
        return makeSchemeStructured(rootClass.simpleName, jsonProperties)
    }

    // ──────────────────────────────────────────────
    // Core Recursive Schema Builder
    // ──────────────────────────────────────────────

    private fun getJsonFromClass(
        clazz: Class<*>,
        visitedClasses: MutableSet<Class<*>> = mutableSetOf()
    ): Pair<JsonObject, MutableSet<String>> {
        if (!visitedClasses.add(clazz)) return Pair(JsonObject(), mutableSetOf())

        val jsonObject = JsonObject()
        val requiredFields = mutableSetOf<String>()

        val fields = if (clazz.isAnnotationPresent(Metadata::class.java)) {
            clazz.kotlin.memberProperties.mapNotNull { it.javaField }
        } else {
            clazz.declaredFields.toList()
        }

        for (field in fields) {
            val key = getSerializedName(field) ?: field.name
            val fieldType = field.type
            val fieldJson = JsonObject()

            when {
                fieldType.isEnum -> {
                    fieldJson.addProperty("type", "string")
                    val enumValues = JsonArray().apply {
                        getEnumValues(fieldType).forEach { add(it) }
                    }
                    fieldJson.add("enum", enumValues)
                }

                List::class.java.isAssignableFrom(fieldType) -> {
                    val itemType = getListElementType(field)
                    val itemJson = if (isPrimitiveOrWrapper(itemType) || itemType == String::class.java) {
                        JsonObject().apply { addProperty("type", mapTypeName(itemType)) }
                    } else if (itemType.isEnum) {
                        JsonObject().apply {
                            addProperty("type", "string")
                            add("enum", JsonArray().apply {
                                getEnumValues(itemType).forEach { add(it) }
                            })
                        }
                    } else {
                        createNestedTypeJson(itemType, visitedClasses)
                    }
                    fieldJson.addProperty("type", "array")
                    fieldJson.add("items", itemJson)
                }

                isPrimitiveOrWrapper(fieldType) || fieldType == String::class.java -> {
                    fieldJson.addProperty("type", mapTypeName(fieldType))
                }

                else -> {
                    val nested = getJsonFromClass(fieldType, visitedClasses)
                    fieldJson.addProperty("type", "object")
                    fieldJson.add("properties", nested.first)
                    fieldJson.addProperty("additionalProperties", false)
                }
            }

            fieldJson.addProperty("description", "")
            jsonObject.add(key, fieldJson)
        }

        visitedClasses.remove(clazz)
        return Pair(jsonObject, requiredFields)
    }

    // ──────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────

    private fun getListElementType(field: Field): Class<*> {
        val genericType = field.genericType
        if (genericType is ParameterizedType) {
            val actualType = genericType.actualTypeArguments.firstOrNull()
            if (actualType is Class<*>) {
                return actualType
            }
        }
        return String::class.java
    }

    private fun getEnumValues(enumClass: Class<*>): List<String> {
        return enumClass.enumConstants?.map { (it as Enum<*>).name } ?: emptyList()
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

    private fun mapTypeName(type: Class<*>): String {
        return when (type) {
            java.lang.Integer::class.java, Int::class.java -> "integer"
            java.lang.Long::class.java, Long::class.java -> "integer"
            java.lang.Float::class.java, Float::class.java -> "number"
            java.lang.Double::class.java, Double::class.java -> "number"
            java.lang.Boolean::class.java, Boolean::class.java -> "boolean"
            else -> "string"
        }
    }

    private fun getSerializedName(field: Field): String? {
        return field.getAnnotation(SerializedName::class.java)?.value
    }

    private fun createNestedTypeJson(
        clazz: Class<*>,
        visitedClasses: MutableSet<Class<*>>
    ): JsonObject {
        val data = getJsonFromClass(clazz, visitedClasses)
        return JsonObject().apply {
            addProperty("type", "object")
            add("properties", data.first)
            addProperty("additionalProperties", false)
        }
    }

    private fun makeSchemeStructured(className: String, properties: JsonObject): JsonObject {
        return JsonObject().apply {
            addProperty("\$schema", "https://plugins.jetbrains.com/plugin/25654-jsonscheme-generator")
            addProperty("title", className)
            addProperty("type", "object")
            add("properties", properties)
            addProperty("additionalProperties", false)
        }
    }
}
