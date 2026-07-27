package org.weekendware.basil.presentation.auth

import io.github.jan.supabase.compose.auth.composable.NativeSignInResult

/**
 * What a compose-auth [NativeSignInResult] actually means, decoupled from
 * any particular ViewModel's state shape. [AuthViewModel] and
 * [org.weekendware.basil.presentation.onboarding.SaveProgressViewModel] both
 * handle Google/Apple sign-in results and previously classified them with
 * identical, independently-copy-pasted `when` blocks — a real drift risk,
 * since a future change to how one variant is handled (e.g. distinguishing
 * network errors from generic ones) could easily update one call site and
 * not the other. This is the one place that classification lives now; each
 * ViewModel still owns its own state update, since their state shapes
 * genuinely differ (and one needs an extra action on success that the other
 * doesn't).
 */
sealed interface SocialSignInOutcome {
    /** @property email The signed-in user's email, if available, for [org.weekendware.basil.data.repository.AuthRepository.recordLastUsedEmail]. */
    data class Success(val email: String?) : SocialSignInOutcome
    data object Dismissed : SocialSignInOutcome
    data object Failed : SocialSignInOutcome
}

/** Classifies a [NativeSignInResult] using [currentUserEmail] to resolve the signed-in email on success. */
fun NativeSignInResult.toSocialSignInOutcome(currentUserEmail: () -> String?): SocialSignInOutcome =
    when (this) {
        is NativeSignInResult.Success -> SocialSignInOutcome.Success(currentUserEmail())
        is NativeSignInResult.ClosedByUser -> SocialSignInOutcome.Dismissed
        is NativeSignInResult.NetworkError, is NativeSignInResult.Error -> SocialSignInOutcome.Failed
    }
