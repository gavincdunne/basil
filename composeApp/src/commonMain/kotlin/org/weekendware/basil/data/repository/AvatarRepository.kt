package org.weekendware.basil.data.repository

/**
 * Contract for profile picture storage.
 *
 * Uploads and removes avatar images for a given user. The storage
 * backend is abstracted so callers (ViewModels) are not coupled to
 * Supabase directly.
 */
interface AvatarRepository {

    /**
     * Uploads [imageBytes] as the avatar for [userId] and returns the
     * public URL of the stored image.
     *
     * Calling this when an avatar already exists replaces it (upsert).
     *
     * @param userId    The authenticated user's ID — used as the storage path.
     * @param imageBytes Raw image data (JPEG or PNG).
     * @return A [Result] containing the public URL on success.
     */
    suspend fun uploadAvatar(userId: String, imageBytes: ByteArray): Result<String>

    /**
     * Deletes the avatar for [userId] from remote storage.
     *
     * @return A [Result] indicating success or failure.
     */
    suspend fun deleteAvatar(userId: String): Result<Unit>
}
