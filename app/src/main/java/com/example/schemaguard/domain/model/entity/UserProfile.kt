package com.example.schemaguard.domain.model.entity

import com.example.schemaguard.domain.model.annotation.GenerateJsonSchema
import com.google.gson.annotations.SerializedName

/**
 * Complex user profile model.
 * Tests: nested objects, enums, lists, nullable fields, @SerializedName mapping
 */
//todo add the @GenerateJsonSchema
data class UserProfile(
    @SerializedName("user_id") val userId: Long,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("display_name") val displayName: String?,
    @SerializedName("avatar_url") val avatarUrl: String?,
    @SerializedName("role") val role: UserRole,
    @SerializedName("account_status") val accountStatus: AccountStatus,
    @SerializedName("settings") val settings: UserSettings,
    @SerializedName("repositories") val repositories: List<RepositorySummary>,
    @SerializedName("active_tokens") val activeTokens: List<AccessToken>,
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("last_login") val lastLogin: Long?,
    @SerializedName("is_verified") val isVerified: Boolean,
    @SerializedName("two_factor_enabled") val twoFactorEnabled: Boolean,
    @SerializedName("login_count") val loginCount: Int
)

enum class UserRole {
    ADMIN, DEVELOPER, VIEWER, GUEST
}

enum class AccountStatus {
    ACTIVE, SUSPENDED, PENDING_VERIFICATION, DEACTIVATED
}

data class UserSettings(
    @SerializedName("theme") val theme: ThemePreference,
    @SerializedName("notifications_enabled") val notificationsEnabled: Boolean,
    @SerializedName("auto_sync") val autoSync: Boolean,
    @SerializedName("sync_interval_minutes") val syncIntervalMinutes: Int,
    @SerializedName("default_branch") val defaultBranch: String,
    @SerializedName("language") val language: String
)

enum class ThemePreference {
    LIGHT, DARK, SYSTEM
}

data class RepositorySummary(
    @SerializedName("repo_id") val repoId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("is_private") val isPrivate: Boolean,
    @SerializedName("schema_count") val schemaCount: Int,
    @SerializedName("last_scan") val lastScan: Long?
)

data class AccessToken(
    @SerializedName("token_id") val tokenId: String,
    @SerializedName("label") val label: String,
    @SerializedName("scope") val scope: TokenScope,
    @SerializedName("expires_at") val expiresAt: Long?,
    @SerializedName("is_active") val isActive: Boolean
)

enum class TokenScope {
    READ_ONLY, READ_WRITE, ADMIN
}
