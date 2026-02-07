package com.jwmaila.appticketera.ui.screens.profile

import androidx.lifecycle.ViewModel
import com.jwmaila.appticketera.data.local.UserPreferences
import com.jwmaila.appticketera.data.repository.TicketeraRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    val repository: TicketeraRepository,
    val userPreferences: UserPreferences
) : ViewModel()
