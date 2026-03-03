package com.example.schemaguard.domain.model.entity

import com.example.schemaguard.domain.model.annotation.GenerateJsonSchema
import com.google.gson.annotations.SerializedName

/**
 * CI/CD pipeline configuration model.
 * Tests: deeply nested objects, lists of enums, lists of nested objects,
 *        multiple levels of nesting, nullable primitives
 */
data class CiCdPipelineConfig(
    @SerializedName("pipeline_id") val pipelineId: String,
    @SerializedName("pipeline_name") val pipelineName: String,
    @SerializedName("description") val description: String?,
    @SerializedName("trigger") val trigger: PipelineTrigger,
    @SerializedName("environment") val environment: BuildEnvironment,
    @SerializedName("steps") val steps: List<PipelineStep>,
    @SerializedName("notifications") val notifications: NotificationConfig,
    @SerializedName("retry_policy") val retryPolicy: RetryPolicy?,
    @SerializedName("timeout_minutes") val timeoutMinutes: Int,
    @SerializedName("is_enabled") val isEnabled: Boolean,
    @SerializedName("created_by") val createdBy: String,
    @SerializedName("version") val version: Int
)

data class PipelineTrigger(
    @SerializedName("type") val type: TriggerType,
    @SerializedName("branches") val branches: List<String>,
    @SerializedName("file_patterns") val filePatterns: List<String>,
    @SerializedName("cron_expression") val cronExpression: String?,
    @SerializedName("on_tag") val onTag: Boolean
)

enum class TriggerType {
    PUSH, PULL_REQUEST, SCHEDULE, MANUAL, TAG
}

data class BuildEnvironment(
    @SerializedName("os") val os: OperatingSystem,
    @SerializedName("java_version") val javaVersion: Int,
    @SerializedName("gradle_version") val gradleVersion: String,
    @SerializedName("env_variables") val envVariables: List<EnvVariable>,
    @SerializedName("cache_enabled") val cacheEnabled: Boolean
)

enum class OperatingSystem {
    UBUNTU_LATEST, MACOS_LATEST, WINDOWS_LATEST
}

data class EnvVariable(
    @SerializedName("key") val key: String,
    @SerializedName("value") val value: String,
    @SerializedName("is_secret") val isSecret: Boolean
)

data class PipelineStep(
    @SerializedName("step_id") val stepId: String,
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: StepType,
    @SerializedName("command") val command: String?,
    @SerializedName("args") val args: List<String>,
    @SerializedName("depends_on") val dependsOn: List<String>,
    @SerializedName("condition") val condition: StepCondition?,
    @SerializedName("timeout_minutes") val timeoutMinutes: Int?,
    @SerializedName("continue_on_error") val continueOnError: Boolean
)

enum class StepType {
    CHECKOUT, SETUP_JDK, GRADLE_TASK, SHELL_COMMAND, UPLOAD_ARTIFACT, NOTIFY
}

data class StepCondition(
    @SerializedName("expression") val expression: String,
    @SerializedName("on_status") val onStatus: StepStatus
)

enum class StepStatus {
    SUCCESS, FAILURE, ALWAYS
}

data class NotificationConfig(
    @SerializedName("on_success") val onSuccess: Boolean,
    @SerializedName("on_failure") val onFailure: Boolean,
    @SerializedName("channels") val channels: List<NotificationChannel>
)

data class NotificationChannel(
    @SerializedName("type") val type: ChannelType,
    @SerializedName("target") val target: String,
    @SerializedName("is_enabled") val isEnabled: Boolean
)

enum class ChannelType {
    EMAIL, SLACK, WEBHOOK, TELEGRAM
}

data class RetryPolicy(
    @SerializedName("max_retries") val maxRetries: Int,
    @SerializedName("delay_seconds") val delaySeconds: Int,
    @SerializedName("backoff_multiplier") val backoffMultiplier: Double
)
