package org.weekendware.basil.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage

/**
 * [AvatarRepository] backed by Supabase Storage.
 *
 * Avatars are stored in the `avatars` bucket under the path `{userId}/avatar`.
 * The bucket must be configured as **public** in the Supabase dashboard so
 * [publicUrl] returns a usable image URL without signed-URL expiry.
 *
 * @param client The shared [SupabaseClient] with the Storage plugin installed.
 */
class SupabaseAvatarRepository(private val client: SupabaseClient) : AvatarRepository {

    private val bucket = "avatars"

    /**
     * Uploads [imageBytes] to `avatars/{userId}/avatar`, replacing any
     * existing file. Returns the public URL of the uploaded image.
     */
    override suspend fun uploadAvatar(userId: String, imageBytes: ByteArray): Result<String> =
        runCatching {
            val path = avatarPath(userId)
            client.storage.from(bucket).upload(path, imageBytes) { upsert = true }
            client.storage.from(bucket).publicUrl(path)
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
