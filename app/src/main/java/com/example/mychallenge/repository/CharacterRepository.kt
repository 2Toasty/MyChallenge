package com.example.mychallenge.repository

import com.example.mychallenge.api.ApiService
import com.example.mychallenge.model.Character
import com.example.mychallenge.model.ApiResponse
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CharacterRepository(private val apiService: ApiService, private val firestore: FirebaseFirestore) {

    // Función para obtener personajes desde la API
    suspend fun getCharacters(page: Int): ApiResponse {
        return apiService.getCharacters(page)
    }

    // Función para buscar personajes desde la API
    suspend fun searchCharacters(query: String): ApiResponse {
        return apiService.searchCharacters(query)
    }

    // Función para observar cambios en Firebase y actualizar la lista de favoritos
    fun observeFavorites(onUpdate: (List<Character>) -> Unit) {
        firestore.collection("favorites").addSnapshotListener { snapshot, error ->
            if (error != null) {
                error.printStackTrace()
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                val favorites = snapshot.documents.mapNotNull { it.toObject(Character::class.java) }
                onUpdate(favorites)
            }
        }
    }

    // Función para agregar un personaje a favoritos en Firebase
    suspend fun addToFavorites(character: Character) {
        firestore.collection("favorites").document(character.id.toString()).set(character).await()
    }

    // Función para eliminar un personaje de favoritos en Firebase
    suspend fun removeFromFavorites(character: Character) {
        firestore.collection("favorites").document(character.id.toString()).delete().await()
    }
}
