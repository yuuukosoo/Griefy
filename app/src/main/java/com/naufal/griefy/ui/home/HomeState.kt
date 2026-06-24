package com.naufal.griefy.ui.home

import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.domain.model.UserProfile

data class HomeState(
    val searchQuery: String = "",
    val userProfile: UserProfile? = null,
    val memories: List<Memory> = emptyList()
)
