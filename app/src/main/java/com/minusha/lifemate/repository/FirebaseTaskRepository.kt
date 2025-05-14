package com.minusha.lifemate.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.minusha.lifemate.model.Task
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseTaskRepository : TaskRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    override val tasks: Flow<List<Task>> = callbackFlow {
        if (currentUserId.isBlank()) {
            trySend(emptyList())
            return@callbackFlow
        }

        val listener = firestore.collection("users")
            .document(currentUserId)
            .collection("tasks")
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val taskList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Task::class.java)
                } ?: emptyList()

                trySend(taskList)
            }

        awaitClose {
            listener.remove()
        }
    }

    override suspend fun addTask(task: Task) {
        if (currentUserId.isBlank()) return

        firestore.collection("users")
            .document(currentUserId)
            .collection("tasks")
            .document(task.id)
            .set(task)
            .await()
    }

    override suspend fun updateTask(task: Task) {
        if (currentUserId.isBlank()) return

        firestore.collection("users")
            .document(currentUserId)
            .collection("tasks")
            .document(task.id)
            .set(task)
            .await()
    }

    override suspend fun deleteTask(taskId: String) {
        if (currentUserId.isBlank()) return

        firestore.collection("users")
            .document(currentUserId)
            .collection("tasks")
            .document(taskId)
            .delete()
            .await()
    }

    override suspend fun toggleTaskDone(taskId: String) {
        if (currentUserId.isBlank()) return

        val task = getTaskById(taskId) ?: return
        val updatedTask = task.copy(isDone = !task.isDone)
        updateTask(updatedTask)
    }

    override suspend fun getTaskById(taskId: String): Task? {
        if (currentUserId.isBlank()) return null

        val doc = firestore.collection("users")
            .document(currentUserId)
            .collection("tasks")
            .document(taskId)
            .get()
            .await()

        return doc.toObject(Task::class.java)
    }
}