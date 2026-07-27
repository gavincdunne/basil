package org.weekendware.basil.presentation.profile

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.weekendware.basil.data.repository.AvatarRepository
import org.weekendware.basil.data.repository.UserRepository
import org.weekendware.basil.domain.model.User
import org.weekendware.basil.domain.usecase.GetUserUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeAvatar: FakeAvatarRepository
    private lateinit var fakeUser: FakeUserRepository
    private lateinit var viewModel: ProfileViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
        fakeAvatar = FakeAvatarRepository()
        fakeUser = FakeUserRepository()
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(user: User? = null): ProfileViewModel {
        fakeUser.currentUser = user
        return ProfileViewModel(
            getUser          = GetUserUseCase(fakeUser),
            avatarRepository = fakeAvatar,
            userRepository   = fakeUser,
            scope            = null,
        )
    }

    @Test
    fun `init with no user — state is empty`() {
        viewModel = buildViewModel(user = null)
        val state = viewModel.state.value
        assertEquals("", state.name)
        assertEquals("", state.email)
        assertNull(state.avatarUrl)
    }

    @Test
    fun `init with user that has a storage path — signed URL is generated`() = runTest {
        fakeAvatar.signedUrlResult = Result.success("https://signed.example.com/avatar")
        viewModel = buildViewModel(user = User("uid", "Alice", "alice@example.com", avatarUrl = "uid/avatar"))
        assertEquals("https://signed.example.com/avatar", viewModel.state.value.avatarUrl)
    }

    @Test
    fun `init with legacy public URL — avatar is cleared`() = runTest {
        viewModel = buildViewModel(user = User("uid", "Alice", "alice@example.com", avatarUrl = "https://public.example.com/old"))
        assertNull(viewModel.state.value.avatarUrl)
        assertNull(fakeUser.storedAvatarUrl)
    }

    @Test
    fun `onAvatarPicked success — stores path and shows signed URL`() = runTest {
        fakeAvatar.uploadResult = Result.success("uid/avatar")
        fakeAvatar.signedUrlResult = Result.success("https://signed.example.com/avatar")
        viewModel = buildViewModel(user = User("uid", "Alice", "alice@example.com"))
        viewModel.onAvatarPicked(ByteArray(4))
        assertEquals("uid/avatar", fakeUser.storedAvatarUrl)
        assertEquals("https://signed.example.com/avatar", viewModel.state.value.avatarUrl)
    }

    @Test
    fun `onAvatarPicked upload failure — error state set`() = runTest {
        fakeAvatar.uploadResult = Result.failure(RuntimeException("network error"))
        viewModel = buildViewModel(user = User("uid", "Alice", "alice@example.com"))
        viewModel.onAvatarPicked(ByteArray(4))
        val state = viewModel.state.value
        assertNull(state.avatarUrl)
        assertTrue(state.error != null)
    }

    @Test
    fun `onRemoveAvatar success — avatar cleared from state and repository`() = runTest {
        fakeUser.storedAvatarUrl = "uid/avatar"
        viewModel = buildViewModel(user = User("uid", "Alice", "alice@example.com", avatarUrl = "uid/avatar"))
        viewModel.onRemoveAvatar()
        assertNull(viewModel.state.value.avatarUrl)
        assertNull(fakeUser.storedAvatarUrl)
    }
}

private class FakeAvatarRepository : AvatarRepository {
    var uploadResult: Result<String> = Result.success("uid/avatar")
    var signedUrlResult: Result<String> = Result.success("https://signed.example.com/avatar")
    var deleteResult: Result<Unit> = Result.success(Unit)

    override suspend fun uploadAvatar(userId: String, imageBytes: ByteArray): Result<String> = uploadResult
    override suspend fun getSignedUrl(path: String): Result<String> = signedUrlResult
    override suspend fun deleteAvatar(userId: String): Result<Unit> = deleteResult
}

private class FakeUserRepository : UserRepository {
    var currentUser: User? = null
    var storedAvatarUrl: String? = null

    override fun getAll(): List<User> = listOfNotNull(currentUser)
    override fun getAllAsFlow(): Flow<List<User>> = flowOf(getAll())
    override fun insert(id: String, name: String, email: String) { currentUser = User(id, name, email) }
    override fun updateAvatarUrl(userId: String, url: String?) { storedAvatarUrl = url }
    override fun deleteAll() { currentUser = null; storedAvatarUrl = null }
}
