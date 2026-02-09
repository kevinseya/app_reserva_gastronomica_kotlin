package com.mespinoza.appgastronomia.ui.screens.profile

import androidx.lifecycle.ViewModel
import com.mespinoza.appgastronomia.data.local.UserPreferences
import com.mespinoza.appgastronomia.data.repository.GastronomiaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    val repository: GastronomiaRepository,
    val userPreferences: UserPreferences
) : ViewModel()
