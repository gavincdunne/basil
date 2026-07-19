package org.weekendware.basil.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import kotlin.time.Duration.Companion.seconds

/**
 * [AvatarRepository] backed by Supabase Storage.
 *
 * Avatars are stored in the `avatars` bucket under the path `{userId}/avatar`.
 * The bucket is **private** — callers must use [getSignedUrl] to produce a
 * time-limited URL for display rather than relying on public access.
 *
 * @param client The shared [SupabaseClient] with the Storage plugin installed.
 */
class SupabaseAvatarRepository(private val client: SupabaseClient) : AvatarRepository {

    private val bucket = "avatars"

    /**
     * Uploads [imageBytes] to `avatars/{userId}/avatar`, replacing any
     * existing file. Returns the storage path (not a URL) — store this in
     * the user record and call [getSignedUrl] when a displayable URL is needed.
     */
    override suspend fun uploadAvatar(userId: String, imageBytes: ByteArray): Result<String> =
        runCatching {
            val path = avatarPath(userId)
            client.storage.from(bucket).upload(path, imageBytes) { upsert = true }
            path
        }

    /**
     * Returns a signed URL for [path] that expires in 1 hour. Call this each
     * time the avatar needs to be displayed — do not store the signed URL.
     */
    override suspend fun getSignedUrl(path: String): Result<String> =
        runCatching {
            client.storage.from(bucket).createSignedUrl(path, expiresIn = 3600.seconds)
        }

    /**
     * Removes the avatar at `avatars/{userId}/avatar` from remote storage.
     */
    override suspend fun deleteAvatar(userId: String): Result<Unit> =
        runCatching {
            client.storage.from(bucket).delete(avatarPath(userId))
        }

    private fun avatarPath(userId: String) = "$userId/avatar"
}
