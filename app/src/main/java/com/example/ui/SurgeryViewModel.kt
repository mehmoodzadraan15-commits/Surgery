package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SurgeryRepository
import com.example.model.SurgeryQuestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SurgeryUiState(
    val allQuestions: List<SurgeryQuestion> = emptyList(),
    val filteredQuestions: List<SurgeryQuestion> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val currentIndex: Int = 0,
    val userAnswers: Map<Int, String> = emptyMap(),
    val bookmarkedIds: Set<Int> = emptySet(),
    val isJumpDialogOpen: Boolean = false,
    val isResetDialogOpen: Boolean = false,
    val showExplanationManually: Boolean = false
) {
    val currentQuestion: SurgeryQuestion?
        get() = filteredQuestions.getOrNull(currentIndex)

    val totalQuestionsCount: Int
        get() = allQuestions.size

    val answeredCount: Int
        get() = userAnswers.size

    val correctCount: Int
        get() = allQuestions.count { q ->
            userAnswers[q.id]?.equals(q.correctAnswer, ignoreCase = true) == true
        }

    val incorrectCount: Int
        get() = allQuestions.count { q ->
            val ans = userAnswers[q.id]
            ans != null && !ans.equals(q.correctAnswer, ignoreCase = true)
        }

    val bookmarkedCount: Int
        get() = bookmarkedIds.size

    val progressFraction: Float
        get() = if (totalQuestionsCount > 0) answeredCount.toFloat() / totalQuestionsCount.toFloat() else 0f
}

class SurgeryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SurgeryRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(SurgeryUiState())
    val uiState: StateFlow<SurgeryUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val questions = repository.getQuestions()
            val categories = repository.getCategories()
            val userAnswers = repository.loadUserAnswers()
            val bookmarkedIds = repository.loadBookmarkedIds()
            val lastQId = repository.getLastQuestionId()

            val initialIndex = questions.indexOfFirst { it.id == lastQId }.coerceAtLeast(0)

            _uiState.update {
                it.copy(
                    allQuestions = questions,
                    filteredQuestions = questions,
                    categories = categories,
                    userAnswers = userAnswers,
                    bookmarkedIds = bookmarkedIds,
                    currentIndex = initialIndex
                )
            }
        }
    }

    fun selectCategory(category: String) {
        _uiState.update { state ->
            val filtered = filterQuestions(state.allQuestions, category, state.searchQuery, state.bookmarkedIds)
            state.copy(
                selectedCategory = category,
                filteredQuestions = filtered,
                currentIndex = 0,
                showExplanationManually = false
            )
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { state ->
            val filtered = filterQuestions(state.allQuestions, state.selectedCategory, query, state.bookmarkedIds)
            state.copy(
                searchQuery = query,
                filteredQuestions = filtered,
                currentIndex = 0,
                showExplanationManually = false
            )
        }
    }

    private fun filterQuestions(
        questions: List<SurgeryQuestion>,
        category: String,
        search: String,
        bookmarks: Set<Int>
    ): List<SurgeryQuestion> {
        return questions.filter { q ->
            val matchesCategory = when (category) {
                "All" -> true
                "Bookmarked" -> bookmarks.contains(q.id)
                else -> q.category == category
            }
            val matchesSearch = if (search.isBlank()) true else {
                q.question.contains(search, ignoreCase = true) ||
                    q.options.any { it.contains(search, ignoreCase = true) } ||
                    q.explanation.contains(search, ignoreCase = true) ||
                    q.id.toString() == search.trim()
            }
            matchesCategory && matchesSearch
        }
    }

    fun selectOption(optionText: String) {
        val currentQ = _uiState.value.currentQuestion ?: return
        // Only allow selecting once per question or allow re-selecting
        viewModelScope.launch {
            repository.saveUserAnswer(currentQ.id, optionText)
            _uiState.update { state ->
                val newAnswers = state.userAnswers.toMutableMap()
                newAnswers[currentQ.id] = optionText
                state.copy(userAnswers = newAnswers)
            }
        }
    }

    fun nextQuestion() {
        _uiState.update { state ->
            if (state.currentIndex < state.filteredQuestions.size - 1) {
                val nextIdx = state.currentIndex + 1
                state.filteredQuestions.getOrNull(nextIdx)?.let {
                    repository.saveLastQuestionId(it.id)
                }
                state.copy(
                    currentIndex = nextIdx,
                    showExplanationManually = false
                )
            } else {
                state
            }
        }
    }

    fun previousQuestion() {
        _uiState.update { state ->
            if (state.currentIndex > 0) {
                val prevIdx = state.currentIndex - 1
                state.filteredQuestions.getOrNull(prevIdx)?.let {
                    repository.saveLastQuestionId(it.id)
                }
                state.copy(
                    currentIndex = prevIdx,
                    showExplanationManually = false
                )
            } else {
                state
            }
        }
    }

    fun jumpToQuestion(questionIdOrIndex: Int) {
        _uiState.update { state ->
            // Try matching by question id first, then by index
            val idxById = state.filteredQuestions.indexOfFirst { it.id == questionIdOrIndex }
            val targetIdx = if (idxById != -1) {
                idxById
            } else {
                (questionIdOrIndex - 1).coerceIn(0, (state.filteredQuestions.size - 1).coerceAtLeast(0))
            }
            state.filteredQuestions.getOrNull(targetIdx)?.let {
                repository.saveLastQuestionId(it.id)
            }
            state.copy(
                currentIndex = targetIdx,
                isJumpDialogOpen = false,
                showExplanationManually = false
            )
        }
    }

    fun toggleBookmark(questionId: Int) {
        viewModelScope.launch {
            val updated = repository.toggleBookmark(questionId)
            _uiState.update { state ->
                val newFiltered = if (state.selectedCategory == "Bookmarked") {
                    filterQuestions(state.allQuestions, state.selectedCategory, state.searchQuery, updated)
                } else {
                    state.filteredQuestions
                }
                val safeIndex = state.currentIndex.coerceIn(0, (newFiltered.size - 1).coerceAtLeast(0))
                state.copy(
                    bookmarkedIds = updated,
                    filteredQuestions = newFiltered,
                    currentIndex = safeIndex
                )
            }
        }
    }

    fun toggleExplanation() {
        _uiState.update { it.copy(showExplanationManually = !it.showExplanationManually) }
    }

    fun setJumpDialogOpen(open: Boolean) {
        _uiState.update { it.copy(isJumpDialogOpen = open) }
    }

    fun setResetDialogOpen(open: Boolean) {
        _uiState.update { it.copy(isResetDialogOpen = open) }
    }

    fun resetAllAnswers() {
        viewModelScope.launch {
            repository.resetAllAnswers()
            _uiState.update { state ->
                state.copy(
                    userAnswers = emptyMap(),
                    isResetDialogOpen = false,
                    showExplanationManually = false
                )
            }
        }
    }
}
