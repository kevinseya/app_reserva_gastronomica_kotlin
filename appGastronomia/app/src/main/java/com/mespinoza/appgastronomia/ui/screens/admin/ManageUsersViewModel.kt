package com.mespinoza.appgastronomia.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mespinoza.appgastronomia.data.model.User
import com.mespinoza.appgastronomia.data.repository.GastronomiaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageUsersViewModel @Inject constructor(
    private val repository: GastronomiaRepository
) : ViewModel() {
    
    private val _usersState = MutableStateFlow<UsersState>(UsersState.Loading)
    val usersState: StateFlow<UsersState> = _usersState.asStateFlow()
    
    init {
        loadUsers()
    }
    
    fun loadUsers() {
        viewModelScope.launch {
            _usersState.value = UsersState.Loading
            repository.getUsers()
                .onSuccess { users ->
                    _usersState.value = UsersState.Success(users)
                }
                .onFailure { error ->
                    _usersState.value = UsersState.Error(error.message ?: "Error al cargar usuarios")
                }
        }
    }

    fun createUser(request: com.mespinoza.appgastronomia.data.model.CreateUserRequest, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.createUser(request)
                .onSuccess {
                    loadUsers()
                    onDone()
                }
        }
    }

    fun updateUser(userId: String, request: com.mespinoza.appgastronomia.data.model.UpdateUserRequest, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.updateUser(userId, request)
                .onSuccess {
                    loadUsers()
                    onDone()
                }
        }
    }

    fun deleteUser(userId: String, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.deleteUser(userId)
                .onSuccess {
                    loadUsers()
                    onDone()
                }
        }
    }
}

sealed class UsersState {
    data object Loading : UsersState()
    data class Success(val users: List<User>) : UsersState()
    data class Error(val message: String) : UsersState()
}
