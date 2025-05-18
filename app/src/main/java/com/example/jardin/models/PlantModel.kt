package com.example.jardin.models

data class PlantModel(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val description: String = "",
    val wateringFrequency: Int = 0, // in days
    val sunlightNeeds: String = "",
    val imageUrl: String = "",
    val userId: String = "",
    val createdAt: Long = 0
)