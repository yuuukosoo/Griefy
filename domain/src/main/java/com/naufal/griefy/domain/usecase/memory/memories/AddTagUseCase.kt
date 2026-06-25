package com.naufal.griefy.domain.usecase.memory.memories

import javax.inject.Inject

class AddTagUseCase @Inject constructor() {
    operator fun invoke(currentTags: List<String>, newTag: String): List<String> {
        val clean = newTag.trim()
        if (clean.isEmpty() || currentTags.contains(clean)) return currentTags
        return currentTags + clean
    }
}
