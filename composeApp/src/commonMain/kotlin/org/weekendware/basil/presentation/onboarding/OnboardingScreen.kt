package org.weekendware.basil.presentation.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import basil.composeapp.generated.resources.Res
import basil.composeapp.generated.resources.onboarding_chip_closed_loop
import basil.composeapp.generated.resources.onboarding_chip_five_to_ten
import basil.composeapp.generated.resources.onboarding_chip_injections
import basil.composeapp.generated.resources.onboarding_chip_less_than_year
import basil.composeapp.generated.resources.onboarding_chip_not_sure
import basil.composeapp.generated.resources.onboarding_chip_one_to_five
import basil.composeapp.generated.resources.onboarding_chip_patterns
import basil.composeapp.generated.resources.onboarding_chip_pump
import basil.composeapp.generated.resources.onboarding_chip_someone_gets_it
import basil.composeapp.generated.resources.onboarding_chip_ten_plus
import basil.composeapp.generated.resources.onboarding_chip_vent
import basil.composeapp.generated.resources.onboarding_complete
import basil.composeapp.generated.resources.onboarding_opening
import basil.composeapp.generated.resources.onboarding_resume_with_name
import basil.composeapp.generated.resources.onboarding_resume_without_name
import basil.composeapp.generated.resources.onboarding_retry
import basil.composeapp.generated.resources.onboarding_step2_question
import basil.composeapp.generated.resources.onboarding_step2_subtext
import basil.composeapp.generated.resources.onboarding_step3_question
import basil.composeapp.generated.resources.onboarding_step3_subtext
import basil.composeapp.generated.resources.onboarding_step4_question
import basil.composeapp.generated.resources.onboarding_step4_subtext
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.weekendware.basil.domain.model.DiagnosisDuration
import org.weekendware.basil.domain.model.Goal
import org.weekendware.basil.domain.model.ManagementType
import org.weekendware.basil.presentation.theme.BasilTheme
import org.weekendware.basil.presentation.theme.basilSpacing

private sealed interface OnboardingMessage {
    data class Basil(val text: String, val subtext: String? = null) : OnboardingMessage
    data class User(val text: String) : OnboardingMessage
}

@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    OnboardingScreenContent(
        state = state,
        onNameSubmitted = viewModel::onNameSubmitted,
        onManagementTypeSelected = viewModel::onManagementTypeSelected,
        onDiagnosisDurationSelected = viewModel::onDiagnosisDurationSelected,
        onGoalSelected = viewModel::onGoalSelected,
        onClearError = viewModel::clearError
    )
}

@Composable
fun OnboardingScreenContent(
    state: OnboardingUiState,
    onNameSubmitted: (String) -> Unit,
    onManagementTypeSelected: (ManagementType) -> Unit,
    onDiagnosisDurationSelected: (DiagnosisDuration) -> Unit,
    onGoalSelected: (Goal) -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // ── String resolution ────────────────────────────────────────────────────
    val opening = when {
        state.isResuming && state.name != null ->
            stringResource(Res.string.onboarding_resume_with_name, state.name)
        state.isResuming ->
            stringResource(Res.string.onboarding_resume_without_name)
        else ->
            stringResource(Res.string.onboarding_opening)
    }
    val step2Q = if (state.name != null)
        stringResource(Res.string.onboarding_step2_question, state.name) else null
    val step2Sub = stringResource(Res.string.onboarding_step2_subtext)
    val step3Q = stringResource(Res.string.onboarding_step3_question)
    val step3Sub = stringResource(Res.string.onboarding_step3_subtext)
    val step4Q = stringResource(Res.string.onboarding_step4_question)
    val step4Sub = stringResource(Res.string.onboarding_step4_subtext)
    val completionMsg = stringResource(Res.string.onboarding_complete, state.name ?: "")

    // ── Chip option lists ────────────────────────────────────────────────────
    val managementOptions = persistentListOf(
        stringResource(Res.string.onboarding_chip_injections) to ManagementType.INJECTIONS,
        stringResource(Res.string.onboarding_chip_pump) to ManagementType.PUMP,
        stringResource(Res.string.onboarding_chip_closed_loop) to ManagementType.CLOSED_LOOP
    )
    val durationOptions = persistentListOf(
        stringResource(Res.string.onboarding_chip_less_than_year) to DiagnosisDuration.LESS_THAN_ONE,
        stringResource(Res.string.onboarding_chip_one_to_five) to DiagnosisDuration.ONE_TO_FIVE,
        stringResource(Res.string.onboarding_chip_five_to_ten) to DiagnosisDuration.FIVE_TO_TEN,
        stringResource(Res.string.onboarding_chip_ten_plus) to DiagnosisDuration.TEN_PLUS
    )
    val goalOptions = persistentListOf(
        stringResource(Res.string.onboarding_chip_someone_gets_it) to Goal.SOMEONE_WHO_GETS_IT,
        stringResource(Res.string.onboarding_chip_patterns) to Goal.PATTERNS,
        stringResource(Res.string.onboarding_chip_vent) to Goal.VENT,
        stringResource(Res.string.onboarding_chip_not_sure) to Goal.NOT_SURE
    )

    // ── Management type label for user bubble ────────────────────────────────
    val managementLabel = when (state.managementType) {
        ManagementType.INJECTIONS -> stringResource(Res.string.onboarding_chip_injections)
        ManagementType.PUMP -> stringResource(Res.string.onboarding_chip_pump)
        ManagementType.CLOSED_LOOP -> stringResource(Res.string.onboarding_chip_closed_loop)
        null -> ""
    }
    val durationLabel = when (state.diagnosisDuration) {
        DiagnosisDuration.LESS_THAN_ONE -> stringResource(Res.string.onboarding_chip_less_than_year)
        DiagnosisDuration.ONE_TO_FIVE -> stringResource(Res.string.onboarding_chip_one_to_five)
        DiagnosisDuration.FIVE_TO_TEN -> stringResource(Res.string.onboarding_chip_five_to_ten)
        DiagnosisDuration.TEN_PLUS -> stringResource(Res.string.onboarding_chip_ten_plus)
        null -> ""
    }
    val goalLabel = when (state.goal) {
        Goal.SOMEONE_WHO_GETS_IT -> stringResource(Res.string.onboarding_chip_someone_gets_it)
        Goal.PATTERNS -> stringResource(Res.string.onboarding_chip_patterns)
        Goal.VENT -> stringResource(Res.string.onboarding_chip_vent)
        Goal.NOT_SURE -> stringResource(Res.string.onboarding_chip_not_sure)
        null -> ""
    }

    // ── Build conversation history ───────────────────────────────────────────
    val messages = buildList {
        add(OnboardingMessage.Basil(opening))
        if (state.name != null && step2Q != null) {
            add(OnboardingMessage.User(state.name))
            add(OnboardingMessage.Basil(step2Q, step2Sub))
        }
        if (state.managementType != null) {
            add(OnboardingMessage.User(managementLabel))
            add(OnboardingMessage.Basil(step3Q, step3Sub))
        }
        if (state.diagnosisDuration != null) {
            add(OnboardingMessage.User(durationLabel))
            add(OnboardingMessage.Basil(step4Q, step4Sub))
        }
        if (state.goal != null) {
            add(OnboardingMessage.User(goalLabel))
            add(OnboardingMessage.Basil(completionMsg))
        }
    }

    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val error = state.error
    val errorMsg = error?.let { stringResource(it) }
    val retryLabel = stringResource(Res.string.onboarding_retry)
    LaunchedEffect(error) {
        if (error != null && errorMsg != null) {
            snackbarHostState.showSnackbar(message = errorMsg, actionLabel = retryLabel)
            onClearError()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .navigationBarsPadding()
        ) {
            // ── Chat history ─────────────────────────────────────────────────
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.basilSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.basilSpacing.md)
            ) {
                item { /* top breathing room */ }
                items(messages) { message ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = when (message) {
                            is OnboardingMessage.Basil -> Arrangement.Start
                            is OnboardingMessage.User  -> Arrangement.End
                        }
                    ) {
                        when (message) {
                            is OnboardingMessage.Basil ->
                                OnboardingChatBubble(text = message.text, subtext = message.subtext)
                            is OnboardingMessage.User  ->
                                OnboardingUserBubble(text = message.text)
                        }
                    }
                }
                item { /* bottom breathing room */ }
            }

            // ── Input area ───────────────────────────────────────────────────
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.basilSpacing.lg),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                when (state.currentStep) {
                    OnboardingStep.NAME ->
                        OnboardingNameInput(
                            onSubmit = onNameSubmitted,
                            isLoading = state.isLoading,
                            modifier = Modifier.padding(
                                horizontal = MaterialTheme.basilSpacing.lg,
                                vertical = MaterialTheme.basilSpacing.sm
                            )
                        )
                    OnboardingStep.MANAGEMENT_TYPE ->
                        OnboardingChipGroup(
                            options = managementOptions,
                            onSelect = onManagementTypeSelected,
                            isEnabled = !state.isLoading,
                            modifier = Modifier.padding(
                                horizontal = MaterialTheme.basilSpacing.lg,
                                vertical = MaterialTheme.basilSpacing.md
                            )
                        )
                    OnboardingStep.DIAGNOSIS_DURATION ->
                        OnboardingChipGroup(
                            options = durationOptions,
                            onSelect = onDiagnosisDurationSelected,
                            isEnabled = !state.isLoading,
                            modifier = Modifier.padding(
                                horizontal = MaterialTheme.basilSpacing.lg,
                                vertical = MaterialTheme.basilSpacing.md
                            )
                        )
                    OnboardingStep.GOAL ->
                        OnboardingChipGroup(
                            options = goalOptions,
                            onSelect = onGoalSelected,
                            isEnabled = !state.isLoading,
                            modifier = Modifier.padding(
                                horizontal = MaterialTheme.basilSpacing.lg,
                                vertical = MaterialTheme.basilSpacing.md
                            )
                        )
                    OnboardingStep.COMPLETE -> { /* no input — crossfade to MainApp */ }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun PreviewOnboardingFreshStart() {
    BasilTheme {
        OnboardingScreenContent(
            state = OnboardingUiState(),
            onNameSubmitted = {},
            onManagementTypeSelected = {},
            onDiagnosisDurationSelected = {},
            onGoalSelected = {},
            onClearError = {}
        )
    }
}

@Preview
@Composable
private fun PreviewOnboardingLoading() {
    BasilTheme {
        OnboardingScreenContent(
            state = OnboardingUiState(isLoading = true),
            onNameSubmitted = {},
            onManagementTypeSelected = {},
            onDiagnosisDurationSelected = {},
            onGoalSelected = {},
            onClearError = {}
        )
    }
}

@Preview
@Composable
private fun PreviewOnboardingManagementTypeStep() {
    BasilTheme {
        OnboardingScreenContent(
            state = OnboardingUiState(name = "Alice"),
            onNameSubmitted = {},
            onManagementTypeSelected = {},
            onDiagnosisDurationSelected = {},
            onGoalSelected = {},
            onClearError = {}
        )
    }
}

@Preview
@Composable
private fun PreviewOnboardingGoalStep() {
    BasilTheme {
        OnboardingScreenContent(
            state = OnboardingUiState(
                name = "Alice",
                managementType = ManagementType.PUMP,
                diagnosisDuration = DiagnosisDuration.ONE_TO_FIVE
            ),
            onNameSubmitted = {},
            onManagementTypeSelected = {},
            onDiagnosisDurationSelected = {},
            onGoalSelected = {},
            onClearError = {}
        )
    }
}

@Preview
@Composable
private fun PreviewOnboardingComplete() {
    BasilTheme {
        OnboardingScreenContent(
            state = OnboardingUiState(
                name = "Alice",
                managementType = ManagementType.CLOSED_LOOP,
                diagnosisDuration = DiagnosisDuration.TEN_PLUS,
                goal = Goal.SOMEONE_WHO_GETS_IT,
                isComplete = true
            ),
            onNameSubmitted = {},
            onManagementTypeSelected = {},
            onDiagnosisDurationSelected = {},
            onGoalSelected = {},
            onClearError = {}
        )
    }
}

@Preview
@Composable
private fun PreviewOnboardingResumingWithName() {
    BasilTheme {
        OnboardingScreenContent(
            state = OnboardingUiState(name = "Alice", isResuming = true),
            onNameSubmitted = {},
            onManagementTypeSelected = {},
            onDiagnosisDurationSelected = {},
            onGoalSelected = {},
            onClearError = {}
        )
    }
}

@Preview
@Composable
private fun PreviewOnboardingResumingWithoutName() {
    BasilTheme {
        OnboardingScreenContent(
            state = OnboardingUiState(isResuming = true),
            onNameSubmitted = {},
            onManagementTypeSelected = {},
            onDiagnosisDurationSelected = {},
            onGoalSelected = {},
            onClearError = {}
        )
    }
}
