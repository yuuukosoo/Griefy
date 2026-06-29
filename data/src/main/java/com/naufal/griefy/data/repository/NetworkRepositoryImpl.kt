package com.naufal.griefy.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.naufal.griefy.domain.repository.NetworkRepository
import javax.inject.Inject

import dagger.hilt.android.qualifiers.ApplicationContext

class NetworkRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : NetworkRepository {

    override fun isConnected(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
