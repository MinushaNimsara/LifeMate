package com.minusha.lifemate.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class ThemeViewModel : ViewModel() {

    var isDarkMode by mutableStateOf(false)
        private set

    fun toggleTheme() {
        isDarkMode = !isDarkMode
    }
}