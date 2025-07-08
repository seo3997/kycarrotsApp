package com.whomade.kycarrots.ui.buy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.whomade.kycarrots.domain.service.AppServiceProvider

class AdListViewModelFactory(
    private val token: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdListViewModel(
                appService      = AppServiceProvider.getService(),
                initialToken      = token
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}