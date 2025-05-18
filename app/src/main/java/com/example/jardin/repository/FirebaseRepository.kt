package com.example.jardin.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.example.jardin.models.PlantModel
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // Plants collection reference
    private val plantsCollection = db.collection("plants")

    // Get all plants for current user
    suspend fun getUserPlants(): List<PlantModel> {
        val userId = auth.currentUser?.uid ?: return emptyList()

        return try {
            plantsCollection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(PlantModel::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Add a new plant
    suspend fun addPlant(plant: PlantModel, imageUri: Uri?): Boolean {
        val userId = auth.currentUser?.uid ?: return false

        return try {
            // Generate a unique ID for the plant
            val plantId = UUID.randomUUID().toString()

            // Upload image if provided
            val imageUrl = if (imageUri != null) {
                uploadImage(imageUri, plantId)
            } else ""

            // Create the plant object with user ID and image URL
            val newPlant = plant.copy(
                id = plantId,
                userId = userId,
                imageUrl = imageUrl,
                createdAt = System.currentTimeMillis()
            )

            // Save to Firestore
            plantsCollection.document(plantId).set(newPlant).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Upload an image to Firebase Storage
    private suspend fun uploadImage(imageUri: Uri, plantId: String): String {
        val storageRef = storage.reference.child("plant_images/$plantId.jpg")
        storageRef.putFile(imageUri).await()
        return storageRef.downloadUrl.await().toString()
    }

    // Update a plant
    suspend fun updatePlant(plant: PlantModel): Boolean {
        return try {
            plantsCollection.document(plant.id).set(plant).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Delete a plant
    suspend fun deletePlant(plantId: String): Boolean {
        return try {
            plantsCollection.document(plantId).delete().await()

            // Delete image if it exists
            try {
                storage.reference.child("plant_images/$plantId.jpg").delete().await()
            } catch (e: Exception) {
                // Image might not exist, ignore
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    // Get user profile information
    suspend fun getUserProfile(): Map<String, Any>? {
        val userId = auth.currentUser?.uid ?: return null

        return try {
            db.collection("users").document(userId)
                .get()
                .await()
                .data
        } catch (e: Exception) {
            null
        }
    }

    // Sign out user
    fun signOut() {
        auth.signOut()
    }
}