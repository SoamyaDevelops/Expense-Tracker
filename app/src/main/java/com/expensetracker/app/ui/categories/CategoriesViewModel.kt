package com.expensetracker.app.ui.categories

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.app.ExpenseApp
import com.expensetracker.app.data.Category
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoriesViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as ExpenseApp).repository

    val categories: StateFlow<List<Category>> = repo.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCategory(name: String, colorHex: String, budget: Double) {
        viewModelScope.launch {
            repo.addCategory(Category(name = name, colorHex = colorHex, iconKey = "category", monthlyBudget = budget))
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch { repo.updateCategory(category) }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch { repo.deleteCategory(category) }
    }
}
