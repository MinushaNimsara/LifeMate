package com.minusha.lifemate.repository

import com.minusha.lifemate.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    val tasks: Flow<List<Task>>
    suspend fun addTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(taskId: String)
    suspend fun toggleTaskDone(taskId: String)
    suspend fun getTaskById(taskId: String): Task?
}