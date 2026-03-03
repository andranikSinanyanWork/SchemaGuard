package com.example.schemaguard.domain.model.entity

import com.example.schemaguard.domain.model.annotation.GenerateJsonSchema
import com.google.gson.annotations.SerializedName

/**
 * Schema validation report model.
 * Tests: lists of complex objects, nullable nested objects, mixed primitive types,
 *        enum in nested contexts, self-referencing-like structures via file diffs
 */
@GenerateJsonSchema
data class SchemaValidationReport(
    @SerializedName("report_id") val reportId: String,
    @SerializedName("run_timestamp") val runTimestamp: Long,
    @SerializedName("check") val check: Long,
    @SerializedName("commit_sha") val commitSha: String,
    @SerializedName("branch") val branch: String,
    @SerializedName("repository_url") val repositoryUrl: String,
    @SerializedName("overall_status") val overallStatus: ValidationStatus,
    @SerializedName("summary") val summary: ReportSummary,
    @SerializedName("file_results") val fileResults: List<FileValidationResult>,
    @SerializedName("changed_files") val changedFiles: List<ChangedFileInfo>,
    @SerializedName("schema_diffs") val schemaDiffs: List<SchemaDiff>?,
    @SerializedName("execution_time_ms") val executionTimeMs: Long,
    @SerializedName("triggered_by") val triggeredBy: String,
    @SerializedName("ci_run_url") val ciRunUrl: String?
)

enum class ValidationStatus {
    PASSED, FAILED, WARNING, SKIPPED
}

data class ReportSummary(
    @SerializedName("total_files_scanned") val totalFilesScanned: Int,
    @SerializedName("schemas_generated") val schemasGenerated: Int,
    @SerializedName("schemas_failed") val schemasFailed: Int,
    @SerializedName("warnings_count") val warningsCount: Int,
    @SerializedName("new_schemas") val newSchemas: Int,
    @SerializedName("modified_schemas") val modifiedSchemas: Int,
    @SerializedName("deleted_schemas") val deletedSchemas: Int
)

data class FileValidationResult(
    @SerializedName("file_path") val filePath: String,
    @SerializedName("class_name") val className: String,
    @SerializedName("status") val status: ValidationStatus,
    @SerializedName("schema_json") val schemaJson: String?,
    @SerializedName("errors") val errors: List<ValidationError>,
    @SerializedName("warnings") val warnings: List<ValidationWarning>,
    @SerializedName("field_count") val fieldCount: Int,
    @SerializedName("has_nested_objects") val hasNestedObjects: Boolean,
    @SerializedName("generation_time_ms") val generationTimeMs: Long
)

data class ValidationError(
    @SerializedName("error_code") val errorCode: String,
    @SerializedName("message") val message: String,
    @SerializedName("field_path") val fieldPath: String?,
    @SerializedName("severity") val severity: ErrorSeverity,
    @SerializedName("line_number") val lineNumber: Int?  // NEW FIELD - someone added this
)

enum class ErrorSeverity {
    CRITICAL, HIGH, MEDIUM, LOW
}

data class ValidationWarning(
    @SerializedName("warning_code") val warningCode: String,
    @SerializedName("message") val message: String,
    @SerializedName("suggestion") val suggestion: String?
)

data class ChangedFileInfo(
    @SerializedName("path") val path: String,
    @SerializedName("change_type") val changeType: FileChangeType,
    @SerializedName("additions") val additions: Int,
    @SerializedName("deletions") val deletions: Int,
    @SerializedName("is_annotated") val isAnnotated: Boolean
)

enum class FileChangeType {
    ADDED, MODIFIED, DELETED, RENAMED
}

data class SchemaDiff(
    @SerializedName("class_name") val className: Int,
    @SerializedName("diff_type") val diffType: SchemaDiffType,
    @SerializedName("added_fields") val addedFields: List<FieldChange>,
    @SerializedName("removed_fields") val removedFields: List<FieldChange>,
    @SerializedName("modified_fields") val modifiedFields: List<FieldModification>,
    @SerializedName("is_breaking_change") val isBreakingChange: Boolean
)

enum class SchemaDiffType {
    NEW_SCHEMA, SCHEMA_UPDATED, SCHEMA_REMOVED, NO_CHANGE
}

data class FieldChange(
    @SerializedName("field_name") val fieldName: String,
    @SerializedName("field_type") val fieldType: String,
    @SerializedName("is_nullable") val isNullable: Boolean
)

data class FieldModification(
    @SerializedName("field_name") val fieldName: String,
    @SerializedName("old_type") val oldType: String,
    @SerializedName("new_type") val newType: String,
    @SerializedName("nullability_changed") val nullabilityChanged: Boolean,
    @SerializedName("is_breaking") val isBreaking: Boolean
)
