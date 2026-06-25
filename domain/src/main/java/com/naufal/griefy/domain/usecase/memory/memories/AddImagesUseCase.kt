package com.naufal.griefy.domain.usecase.memory.memories

import com.naufal.griefy.domain.model.Memory
import javax.inject.Inject

class AddImagesUseCase @Inject constructor() {
    operator fun invoke(currentUris: List<String>, newUris: List<String>): List<String> {
        return (currentUris + newUris).distinct().take(Memory.MAX_IMAGES)
    }
}
