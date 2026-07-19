package org.weekendware.basil.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.weekendware.basil.domain.model.DiagnosisDuration
import org.weekendware.basil.domain.model.Goal
import org.weekendware.basil.domain.model.ManagementType
import org.weekendware.basil.domain.model.OnboardingPersistedState

class SupabaseProfileRepository(
    private val client: SupabaseClient
) : ProfileRepository {

    override suspend fun fetchProfile(userId: String): Result<OnboardingPersistedState?> =
        runCatching {
            client.from("profiles")
                .select {
                    filter { eq("user_id", userId) }
                    limit(1)
                }
                .decodeSingleOrNull<UserProfileDTO>()
                ?.toDomain()
        }

    override suspend fun upsertStep(userId: String, state: OnboardingPersistedState): Result<Unit> =
        runCatching {
            client.from("profiles").upsert(state.toDTO(userId)) {
                onConflict = "user_id"
            }
        }

    @Serializable
    private data class UserProfileDTO(
        @SerialName("user_id") val userId: String,
        val name: String? = null,
        @SerialName("management_type") val managementType: String? = null,
        @SerialName("diagnosis_duration") val diagnosisDuration: String? = null,
        val goal: String? = null,
        @SerialName("is_complete") val isComplete: Boolean = false
    )

    private fun UserProfileDTO.toDomain() = OnboardingPersistedState(
        name = name,
        managementType = managementType?.let { runCatching { ManagementType.valueOf(it) }.getOrNull() },
        diagnosisDuration = diagnosisDuration?.let { runCatching { DiagnosisDuration.valueOf(it) }.getOrNull() },
        goal = goal?.let { runCatching { Goal.valueOf(it) }.getOrNull() },
        isComplete = isComplete
    )

    private fun OnboardingPersistedState.toDTO(userId: String) = UserProfileDTO(
        userId = userId,
        name = name,
        managementType = managementType?.name,
        diagnosisDuration = diagnosisDuration?.name,
        goal = goal?.name,
        isComplete = isComplete
    )
}
