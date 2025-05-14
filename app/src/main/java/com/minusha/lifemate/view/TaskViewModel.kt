package com.minusha.lifemate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minusha.lifemate.model.Task
import com.minusha.lifemate.repository.FirebaseTaskRepository
import com.minusha.lifemate.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

class TaskViewModel : ViewModel() {

    private val repository: TaskRepository = FirebaseTaskRepository()

    private val _tasksState = MutableStateFlow<TasksState>(TasksState.Loading)
    val tasksState: StateFlow<TasksState> = _tasksState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.tasks.collectLatest { tasks ->
                _tasksState.value = TasksState.Success(tasks)
            }
        }
    }

    fun addTask(title: String, description: String, dueDate: Long? = null, priority: Int = 0) {
        viewModelScope.launch {
            val task = Task(
                id = UUID.randomUUID().toString(),
                title = title,
                description = description,
                dueDate = dueDate,
                priority = priority
            )
            repository.addTask(task)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

    fun toggleTaskDone(taskId: String) {
        viewModelScope.launch {
            repository.toggleTaskDone(taskId)
        }
    }

    sealed class TasksState {
        object Loading : TasksState()
        data class Success(val tasks: List<Task>) : TasksState()
        data class Error(val message: String) : TasksState()
    }
}