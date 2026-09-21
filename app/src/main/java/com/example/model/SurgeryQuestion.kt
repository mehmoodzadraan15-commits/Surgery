package com.example.model

data class SurgeryQuestion(
    val id: Int,
    val category: String,
    val question: String,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String
) {
    // Generate a deterministically shuffled list of options based on the question ID
    // so option A isn't always the top choice, making practice realistic and engaging
    val displayOptions: List<String> by lazy {
        val seed = id * 31 + 17
        val indices = options.indices.toMutableList()
        // Simple deterministic pseudo-shuffle based on seed
        for (i in indices.size - 1 downTo 1) {
            val j = ((seed * (i + 1) + 11) % (i + 1)).let { if (it < 0) it + (i + 1) else it }
            val temp = indices[i]
            indices[i] = indices[j]
            indices[j] = temp
        }
        indices.map { options[it] }
    }
}
