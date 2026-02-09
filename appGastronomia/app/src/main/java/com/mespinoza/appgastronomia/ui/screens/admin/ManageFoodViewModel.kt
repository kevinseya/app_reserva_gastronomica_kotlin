package com.mespinoza.appgastronomia.ui.screens.admin

import android.util.Log
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mespinoza.appgastronomia.data.model.FoodCategory
import com.mespinoza.appgastronomia.data.model.FoodItem
import com.mespinoza.appgastronomia.data.repository.GastronomiaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageFoodViewModel @Inject constructor(
    private val repository: GastronomiaRepository
) : ViewModel() {
    
    private val _categories = MutableStateFlow<List<FoodCategory>>(emptyList())
    val categories: StateFlow<List<FoodCategory>> = _categories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadMenu()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun loadMenu() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getMenu()
                .onSuccess { _categories.value = it }
                .onFailure { 
                    Log.e("ManageFoodVM", "Error loading menu", it)
                    _errorMessage.value = "Error al cargar menú: ${it.message}"
                }
            _isLoading.value = false
        }
    }

    fun createCategory(name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.createCategory(name)
                .onSuccess { 
                    loadMenu() 
                }
                .onFailure {
                    Log.e("ManageFoodVM", "Error creating category", it)
                    _errorMessage.value = "Error al crear categoría: ${it.message}"
                }
            _isLoading.value = false
        }
    }

    fun createItem(categoryId: String, name: String, desc: String, price: Double, imageUri: Uri?) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.createFoodItem(categoryId, name, desc.ifBlank { null }, price, imageUri)
                .onSuccess { loadMenu() }
                .onFailure {
                    Log.e("ManageFoodVM", "Error creating item", it)
                    _errorMessage.value = "Error al crear plato: ${it.message}"
                }
            _isLoading.value = false
        }
    }

    fun updateItem(id: String, categoryId: String?, name: String, desc: String, price: Double, imageUri: Uri?) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.updateFoodItem(id, categoryId, name, desc.ifBlank { null }, price, imageUri)
                .onSuccess { loadMenu() }
                .onFailure {
                    Log.e("ManageFoodVM", "Error updating item", it)
                    _errorMessage.value = "Error al actualizar plato: ${it.message}"
                }
            _isLoading.value = false
        }
    }
    
    fun deleteItem(item: FoodItem) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.deleteFoodItem(item.id)
                .onSuccess { loadMenu() }
                .onFailure {
                    Log.e("ManageFoodVM", "Error deleting item", it)
                    _errorMessage.value = "Error al eliminar: ${it.message}"
                }
            _isLoading.value = false
        }
    }
}