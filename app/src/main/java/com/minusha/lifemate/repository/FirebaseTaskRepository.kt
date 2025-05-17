package com.minusha.lifemate.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.minusha.lifemate.model.Task
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseTaskRepository : TaskRepository {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    override val tasks: Flow<List<Task>> = callbackFlow {
        if (currentUserId.isBlank()) {
            trySend(emptyList())
            awaitClose { /* No listener to remove */ }
            return@callbackFlow
        }

        var listenerRegistration: ListenerRegistration? = null

        try {
            listenerRegistration = firestore.collection("users")
                .document(currentUserId)
                .collection("tasks")
                .orderBy("createdAt")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("FirebaseTaskRepo", "Error getting tasks: ${error.message}")
                        trySend(emptyList())
                        return@addSnapshotListener
                    }

                    val taskList = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(Task::class.java)
                    } ?: emptyList()

                    trySend(taskList)
                }
        } catch (e: Exception) {
            Log.e("FirebaseTaskRepo", "Error setting up task listener: ${e.message}")
            trySend(emptyList())
        }

        awaitClose {
            // This is the fix - properly remove the listener
            listenerRegistration?.remove()
        }
    }

    override suspend fun addTask(task: Task) {
        if (currentUserId.isBlank()) return

        try {
            firestore.collection("users")
                .document(currentUserId)
                .collection("tasks")
                .document(task.id)
                .set(task)
                .await()
        } catch (e: Exception) {
            Log.e("FirebaseTaskRepo", "Error adding task: ${e.message}")
            throw e
        }
    }

    override suspend fun updateTask(task: Task) {
        if (currentUserId.isBlank()) return

        try {
            firestore.collection("users")
                .document(currentUserId)
                .collection("tasks")
                .document(task.id)
                .set(task)
                .await()
        } catch (e: Exception) {
            Log.e("FirebaseTaskRepo", "Error updating task: ${e.message}")
            throw e
        }
    }

    override suspend fun deleteTask(taskId: String) {
        if (currentUserId.isBlank()) return

        try {
            firestore.collection("users")
                .document(currentUserId)
                .collection("tasks")
                .document(taskId)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e("FirebaseTaskRepo", "Error deleting task: ${e.message}")
            throw e
        }
    }

    override suspend fun toggleTaskDone(taskId: String) {
        if (currentUserId.isBlank()) return

        try {
            val task = getTaskById(taskId) ?: return
            val updatedTask = task.copy(isDone = !task.isDone)
            updateTask(updatedTask)
        } catch (e: Exception) {
            Log.e("FirebaseTaskRepo", "Error toggling task: ${e.message}")
            throw e
        }
    }

    override suspend fun getTaskById(taskId: String): Task? {
        if (currentUserId.isBlank()) return null

        try {
            val doc = firestore.collection("users")
                .document(currentUserId)
                .collection("tasks")
                .document(taskId)
                .get()
                .await()

            return doc.toObject(Task::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseTaskRepo", "Error getting task by ID: ${e.message}")
            throw e
        }
    }
}