package com.naufal.griefy.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.naufal.griefy.R

object ProfileUtils {
    const val GENDER_MALE_KEY = "Male"
    const val GENDER_FEMALE_KEY = "Female"

    @Composable
    fun getLocalizedGender(gender: String?): String {
        return when (gender) {
            "Male", "Laki-laki" -> stringResource(R.string.profile_gender_male)
            "Female", "Perempuan" -> stringResource(R.string.profile_gender_female)
            else -> gender ?: stringResource(R.string.profile_gender_unspecified)
        }
    }
}
