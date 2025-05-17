package com.minusha.lifemate.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minusha.lifemate.model.Task
import com.minusha.lifemate.repository.FirebaseTaskRepository
import com.minusha.lifemate.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

class TaskViewModel : ViewModel() {

    // Use lazy initialization to avoid initialization issues
    private val repository: TaskRepository by lazy { FirebaseTaskRepository() }

    private val _tasksState = MutableStateFlow<TasksState>(TasksState.Loading)
    val tasksState: StateFlow<TasksState> = _tasksState.asStateFlow()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            try {
                repository.tasks
                    .catch { e ->
                        _tasksState.value = TasksState.Error("Failed to load tasks: ${e.localizedMessage}")
                    }
                    .collectLatest { tasks ->
                        _tasksState.value = TasksState.Success(tasks)
                    }
            } catch (e: Exception) {
                _tasksState.value = TasksState.Error("Failed to load tasks: ${e.localizedMessage}")
            }
        }
    }

    fun addTask(title: String, description: String, dueDate: Long? = null, priority: Int = 0) {
        viewModelScope.launch {
            try {
                val task = Task(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    priority = priority
                )
                repository.addTask(task)
            } catch (e: Exception) {
                // Silently handle errors when adding task
                Log.e("TaskViewModel", "Error adding task: ${e.message}")
            }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            try {
                repository.updateTask(task)
            } catch (e: Exception) {
                // Silently handle errors when updating task
                Log.e("TaskViewModel", "Error updating task: ${e.message}")
            }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                repository.deleteTask(taskId)
            } catch (e: Exception) {
                // Silently handle errors when deleting task
                Log.e("TaskViewModel", "Error deleting task: ${e.message}")
            }
        }
    }

    fun toggleTaskDone(taskId: String) {
        viewModelScope.launch {
            try {
                repository.toggleTaskDone(taskId)
            } catch (e: Exception) {
                // Silently handle errors when toggling task
                Log.e("TaskViewModel", "Error toggling task: ${e.message}")
            }
        }
    }

    sealed class TasksState {
        object Loading : TasksState()
        data class Success(val tasks: List<Task>) : TasksState()
        data class Error(val message: String) : TasksState()
    }
}