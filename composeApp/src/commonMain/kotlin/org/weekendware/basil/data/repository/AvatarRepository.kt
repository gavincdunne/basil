package org.weekendware.basil.data.repository

/**
 * Contract for profile picture storage.
 *
 * Uploads and removes avatar images for a given user. The storage
 * backend is abstracted so callers (ViewModels) are not coupled to
 * Supabase directly.
 *
 * The avatars bucket is private. Callers must use [getSignedUrl] to
 * obtain a time-limited URL for display rather than constructing public URLs.
 */
interface AvatarRepository {

    /**
     * Uploads [imageBytes] as the avatar for [userId] and returns the
     * storage path (e.g. `userId/avatar`). Store this path in the user
     * record — call [getSignedUrl] to turn it into a displayable URL.
     *
     * @param userId     The authenticated user's ID — used as the storage path.
     * @param imageBytes Raw image data (JPEG or PNG).
     * @return A [Result] containing the storage path on success.
     */
    suspend fun uploadAvatar(userId: String, imageBytes: ByteArray): Result<String>

    /**
     * Generates a short-lived signed URL for [path] (1 hour expiry).
     * Call this each time the avatar needs to be displayed.
     *
     * @param path Storage path as returned by [uploadAvatar].
     * @return A [Result] containing the signed URL on success.
     */
    suspend fun getSignedUrl(path: String): Result<String>

    /**
     * Deletes the avatar for [userId] from remote storage.
     *
     * @return A [Result] indicating success or failure.
     */
    suspend fun deleteAvatar(userId: String): Result<Unit>
}
