package com.naufal.griefy.ui.otherprofile

import com.naufal.griefy.domain.model.UserProfile
import com.naufal.griefy.domain.util.Resource

data class OtherProfileState(
    val profileState: Resource<UserProfile> = Resource.Loading(),
    val memoryCount: Int = 0
)
