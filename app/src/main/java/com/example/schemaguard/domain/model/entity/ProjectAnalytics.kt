package com.example.schemaguard.domain.model.entity

import com.example.schemaguard.domain.model.annotation.GenerateJsonSchema
import com.google.gson.annotations.SerializedName

/**
 * Project analytics and AI file tree analysis model.
 * Tests: Double type, Boolean combinations, deeply nested lists,
 *        nullable lists, complex enum usage, mixed nesting depths
 */
//todo add the @GenerateJsonSchema
data class ProjectAnalytics(
    @SerializedName("project_id") val projectId: String,
    @SerializedName("project_name") val projectName: String,
    @SerializedName("analysis_timestamp") val analysisTimestamp: Long,
    @SerializedName("file_tree_snapshot") val fileTreeSnapshot: FileTreeNode,
    @SerializedName("dependency_graph") val dependencyGraph: List<DependencyNode>,
    @SerializedName("schema_coverage") val schemaCoverage: CoverageMetrics,
    @SerializedName("ai_analysis") val aiAnalysis: AiAnalysisResult?,
    @SerializedName("historical_runs") val historicalRuns: List<HistoricalRun>,
    @SerializedName("total_kotlin_files") val totalKotlinFiles: Int,
    @SerializedName("total_annotated_classes") val totalAnnotatedClasses: Int,
    @SerializedName("annotation_coverage_percent") val annotationCoveragePercent: Double
)

data class FileTreeNode(
    @SerializedName("name") val name: String,
    @SerializedName("path") val path: String,
    @SerializedName("type") val type: NodeType,
    @SerializedName("children") val children: List<FileTreeNode>,
    @SerializedName("metadata") val metadata: FileMetadata?,
    @SerializedName("is_in_schema_tree") val isInSchemaTree: Boolean,
    @SerializedName("depth") val depth: Int
)

enum class NodeType {
    DIRECTORY, KOTLIN_FILE, JAVA_FILE, RESOURCE_FILE, CONFIG_FILE, OTHER
}

data class FileMetadata(
    @SerializedName("size_bytes") val sizeBytes: Long,
    @SerializedName("last_modified") val lastModified: Long,
    @SerializedName("line_count") val lineCount: Int?,
    @SerializedName("has_schema_annotation") val hasSchemaAnnotation: Boolean,
    @SerializedName("class_names") val classNames: List<String>,
    @SerializedName("import_count") val importCount: Int
)

data class DependencyNode(
    @SerializedName("class_name") val className: String,
    @SerializedName("package_name") val packageName: String,
    @SerializedName("depends_on") val dependsOn: List<String>,
    @SerializedName("depended_by") val dependedBy: List<String>,
    @SerializedName("is_entity") val isEntity: Boolean,
    @SerializedName("complexity_score") val complexityScore: Double
)

data class CoverageMetrics(
    @SerializedName("total_data_classes") val totalDataClasses: Int,
    @SerializedName("annotated_data_classes") val annotatedDataClasses: Int,
    @SerializedName("coverage_percent") val coveragePercent: Double,
    @SerializedName("uncovered_classes") val uncoveredClasses: List<String>,
    @SerializedName("by_package") val byPackage: List<PackageCoverage>
)

data class PackageCoverage(
    @SerializedName("package_name") val packageName: String,
    @SerializedName("total") val total: Int,
    @SerializedName("covered") val covered: Int,
    @SerializedName("percent") val percent: Double
)

data class AiAnalysisResult(
    @SerializedName("model_used") val modelUsed: String,
    @SerializedName("confidence_score") val confidenceScore: Double,
    @SerializedName("suggested_annotations") val suggestedAnnotations: List<AiSuggestion>,
    @SerializedName("detected_patterns") val detectedPatterns: List<DetectedPattern>,
    @SerializedName("risk_assessment") val riskAssessment: RiskLevel,
    @SerializedName("analysis_notes") val analysisNotes: String?
)

data class AiSuggestion(
    @SerializedName("class_name") val className: String,
    @SerializedName("file_path") val filePath: String,
    @SerializedName("reason") val reason: String,
    @SerializedName("confidence") val confidence: Double,
    @SerializedName("priority") val priority: SuggestionPriority
)

enum class SuggestionPriority {
    HIGH, MEDIUM, LOW
}

data class DetectedPattern(
    @SerializedName("pattern_name") val patternName: String,
    @SerializedName("description") val description: String,
    @SerializedName("affected_files") val affectedFiles: List<String>,
    @SerializedName("pattern_type") val patternType: PatternType
)

enum class PatternType {
    DATA_MODEL, API_RESPONSE, CONFIG_OBJECT, EVENT_PAYLOAD, UI_STATE
}

enum class RiskLevel {
    NONE, LOW, MEDIUM, HIGH, CRITICAL
}

data class HistoricalRun(
    @SerializedName("run_id") val runId: String,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("status") val status: ValidationStatus,
    @SerializedName("schemas_generated") val schemasGenerated: Int,
    @SerializedName("duration_ms") val durationMs: Long,
    @SerializedName("commit_sha") val commitSha: String,
    @SerializedName("changes_count") val changesCount: Int
)
