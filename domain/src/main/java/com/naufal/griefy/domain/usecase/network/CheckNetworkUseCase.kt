package com.naufal.griefy.domain.usecase.network

import com.naufal.griefy.domain.repository.NetworkRepository
import javax.inject.Inject

class CheckNetworkUseCase @Inject constructor(
    private val networkRepository: NetworkRepository
) {
    operator fun invoke(): Boolean = networkRepository.isConnected()
}
