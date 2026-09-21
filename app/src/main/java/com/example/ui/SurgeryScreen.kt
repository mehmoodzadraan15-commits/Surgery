package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SurgeryQuestion
import com.example.ui.theme.AnswerCorrect
import com.example.ui.theme.AnswerCorrectContainer
import com.example.ui.theme.AnswerWrong
import com.example.ui.theme.AnswerWrongContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurgeryScreen(
    viewModel: SurgeryViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var isSearchExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Medical 'S' emblem badge
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "S",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp
                            )
                        }
                        Column {
                            Text(
                                text = "Surgery",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "500 High-Yield MCQs",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { isSearchExpanded = !isSearchExpanded },
                        modifier = Modifier.testTag("search_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isSearchExpanded) Icons.Default.Clear else Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                    IconButton(
                        onClick = { viewModel.setJumpDialogOpen(true) },
                        modifier = Modifier.testTag("jump_to_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatListNumbered,
                            contentDescription = "Jump to Question"
                        )
                    }
                    IconButton(
                        onClick = { viewModel.setResetDialogOpen(true) },
                        modifier = Modifier.testTag("reset_progress_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Progress"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            SurgeryBottomBar(
                currentIndex = uiState.currentIndex,
                totalInFilter = uiState.filteredQuestions.size,
                onPrevious = { viewModel.previousQuestion() },
                onNext = { viewModel.nextQuestion() },
                onJumpClick = { viewModel.setJumpDialogOpen(true) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Optional Search Bar
            AnimatedVisibility(visible = isSearchExpanded) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search questions, topics, keywords...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("search_text_input"),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Stats / Progress Summary Bar
            StatsSummaryBar(
                answered = uiState.answeredCount,
                total = uiState.totalQuestionsCount,
                correct = uiState.correctCount,
                incorrect = uiState.incorrectCount,
                progressFraction = uiState.progressFraction
            )

            // Category Filter Chips
            CategoryChipsRow(
                categories = uiState.categories,
                selectedCategory = uiState.selectedCategory,
                bookmarkedCount = uiState.bookmarkedCount,
                onCategorySelected = { viewModel.selectCategory(it) }
            )

            // Main Question Content
            val currentQ = uiState.currentQuestion
            if (currentQ != null) {
                val userAnswer = uiState.userAnswers[currentQ.id]
                val isBookmarked = uiState.bookmarkedIds.contains(currentQ.id)

                QuestionContentView(
                    question = currentQ,
                    indexInFilter = uiState.currentIndex,
                    totalInFilter = uiState.filteredQuestions.size,
                    userAnswer = userAnswer,
                    isBookmarked = isBookmarked,
                    showExplanationManually = uiState.showExplanationManually,
                    onOptionSelect = { viewModel.selectOption(it) },
                    onBookmarkToggle = { viewModel.toggleBookmark(currentQ.id) },
                    onToggleExplanation = { viewModel.toggleExplanation() },
                    modifier = Modifier.weight(1f)
                )
            } else {
                EmptyStateView(
                    isSearch = uiState.searchQuery.isNotEmpty(),
                    isBookmark = uiState.selectedCategory == "Bookmarked",
                    onResetFilter = {
                        viewModel.setSearchQuery("")
                        viewModel.selectCategory("All")
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // Jump to Question Dialog
    if (uiState.isJumpDialogOpen) {
        JumpToQuestionDialog(
            maxQuestions = uiState.totalQuestionsCount,
            currentQuestionId = uiState.currentQuestion?.id ?: 1,
            onDismiss = { viewModel.setJumpDialogOpen(false) },
            onConfirm = { number -> viewModel.jumpToQuestion(number) }
        )
    }

    // Reset Progress Confirmation Dialog
    if (uiState.isResetDialogOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.setResetDialogOpen(false) },
            title = { Text("Reset Progress?") },
            text = { Text("This will clear all your answers and reset your practice statistics. Your bookmarked questions will be kept.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.resetAllAnswers() },
                    modifier = Modifier.testTag("confirm_reset_button")
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setResetDialogOpen(false) }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StatsSummaryBar(
    answered: Int,
    total: Int,
    correct: Int,
    incorrect: Int,
    progressFraction: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Solved: $answered / $total",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "✓ $correct",
                        color = AnswerCorrect,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "✗ $incorrect",
                        color = AnswerWrong,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

@Composable
fun CategoryChipsRow(
    categories: List<String>,
    selectedCategory: String,
    bookmarkedCount: Int,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "All" chip
        FilterChip(
            selected = selectedCategory == "All",
            onClick = { onCategorySelected("All") },
            label = { Text("All (500)") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.testTag("category_chip_all")
        )

        // "Bookmarked" chip
        FilterChip(
            selected = selectedCategory == "Bookmarked",
            onClick = { onCategorySelected("Bookmarked") },
            label = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text("Saved ($bookmarkedCount)")
                }
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.testTag("category_chip_bookmarked")
        )

        // Dynamic Categories
        categories.filter { it != "All" }.forEach { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(category) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
fun QuestionContentView(
    question: SurgeryQuestion,
    indexInFilter: Int,
    totalInFilter: Int,
    userAnswer: String?,
    isBookmarked: Boolean,
    showExplanationManually: Boolean,
    onOptionSelect: (String) -> Unit,
    onBookmarkToggle: () -> Unit,
    onToggleExplanation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isAnswered = userAnswer != null

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Question Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("question_card_${question.id}"),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category & ID tag
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Q${question.id} • ${question.category}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = onBookmarkToggle,
                        modifier = Modifier.testTag("bookmark_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (isBookmarked) "Remove bookmark" else "Bookmark question",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Question Text
                Text(
                    text = question.question,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Options
        val letters = listOf("A", "B", "C", "D")
        question.displayOptions.forEachIndexed { idx, optionText ->
            val letter = letters.getOrElse(idx) { "${idx + 1}" }
            val isSelected = userAnswer?.equals(optionText, ignoreCase = true) == true
            val isCorrect = question.correctAnswer.equals(optionText, ignoreCase = true)

            OptionItemCard(
                letter = letter,
                optionText = optionText,
                isSelected = isSelected,
                isCorrect = isCorrect,
                isAnswered = isAnswered,
                onClick = {
                    if (!isAnswered) {
                        onOptionSelect(optionText)
                    }
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Explanation Section
        AnimatedVisibility(
            visible = isAnswered || showExplanationManually,
            enter = fadeIn(tween(250)),
            exit = fadeOut(tween(200))
        ) {
            ExplanationCard(
                explanation = question.explanation,
                correctAnswer = question.correctAnswer
            )
        }

        // If unanswered, user can toggle explanation flashcard style
        if (!isAnswered) {
            TextButton(
                onClick = onToggleExplanation,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (showExplanationManually) "Hide Explanation" else "Reveal Answer & Explanation")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun OptionItemCard(
    letter: String,
    optionText: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    isAnswered: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val targetContainerColor = when {
        isAnswered && isSelected && isCorrect -> AnswerCorrectContainer
        isAnswered && isSelected && !isCorrect -> AnswerWrongContainer
        isAnswered && !isSelected && isCorrect -> AnswerCorrectContainer.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surface
    }

    val targetBorderColor = when {
        isAnswered && isCorrect -> AnswerCorrect
        isAnswered && isSelected && !isCorrect -> AnswerWrong
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    val animatedContainerColor by animateColorAsState(targetValue = targetContainerColor, label = "optionContainer")
    val animatedBorderColor by animateColorAsState(targetValue = targetBorderColor, label = "optionBorder")

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isAnswered && (isCorrect || isSelected)) 2.dp else 1.dp,
                color = animatedBorderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = !isAnswered, onClick = onClick)
            .testTag("option_${letter}"),
        color = animatedContainerColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Letter circle
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isAnswered && isCorrect -> AnswerCorrect
                            isAnswered && isSelected && !isCorrect -> AnswerWrong
                            isSelected -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter,
                    color = if (isAnswered && (isCorrect || isSelected)) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Option Text
            Text(
                text = optionText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isAnswered && (isCorrect || isSelected)) FontWeight.Bold else FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            // Status Icon
            if (isAnswered) {
                Spacer(modifier = Modifier.width(8.dp))
                if (isCorrect) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Correct",
                        tint = AnswerCorrect,
                        modifier = Modifier.size(24.dp)
                    )
                } else if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Incorrect",
                        tint = AnswerWrong,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ExplanationCard(
    explanation: String,
    correctAnswer: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .testTag("explanation_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Key Surgical Point & Explanation",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Correct Answer: $correctAnswer",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AnswerCorrect
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = explanation,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SurgeryBottomBar(
    currentIndex: Int,
    totalInFilter: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onJumpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onPrevious,
                enabled = currentIndex > 0,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("previous_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Previous")
            }

            // Quick Question Indicator & Jump trigger
            TextButton(
                onClick = onJumpClick,
                modifier = Modifier.testTag("jump_indicator_button")
            ) {
                Text(
                    text = if (totalInFilter > 0) "${currentIndex + 1} / $totalInFilter" else "0 / 0",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Button(
                onClick = onNext,
                enabled = currentIndex < totalInFilter - 1,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.testTag("next_button")
            ) {
                Text("Next")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyStateView(
    isSearch: Boolean,
    isBookmark: Boolean,
    onResetFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isBookmark) Icons.Default.BookmarkBorder else Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isBookmark) "No bookmarked questions yet" else "No matching questions found",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isBookmark)
                    "Tap the star bookmark icon on any question to save it here for quick revision."
                else
                    "Try adjusting your search terms or view all questions.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onResetFilter) {
                Text("View All 500 Questions")
            }
        }
    }
}

@Composable
fun JumpToQuestionDialog(
    maxQuestions: Int,
    currentQuestionId: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var textValue by remember { mutableStateOf("$currentQuestionId") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Jump to Question") },
        text = {
            Column {
                Text(
                    text = "Enter question number (1 to $maxQuestions):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = textValue,
                    onValueChange = {
                        textValue = it.filter { char -> char.isDigit() }
                        isError = false
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val num = textValue.toIntOrNull()
                            if (num != null && num in 1..maxQuestions) {
                                onConfirm(num)
                            } else {
                                isError = true
                            }
                        }
                    ),
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text("Please enter a number between 1 and $maxQuestions")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("jump_number_input")
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val num = textValue.toIntOrNull()
                    if (num != null && num in 1..maxQuestions) {
                        onConfirm(num)
                    } else {
                        isError = true
                    }
                },
                modifier = Modifier.testTag("jump_confirm_button")
            ) {
                Text("Go")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
