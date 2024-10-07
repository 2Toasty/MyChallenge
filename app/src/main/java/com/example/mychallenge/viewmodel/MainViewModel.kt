package com.example.mychallenge.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mychallenge.model.Character
import com.example.mychallenge.repository.CharacterRepository
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf

class MainViewModel(private val repository: CharacterRepository) : ViewModel() {

    var characters = mutableStateListOf<Character>()
    var favorites = mutableStateListOf<Character>()
    var searchResults = mutableStateListOf<Character>()

    var currentPage = mutableStateOf(1)
    var totalPages = mutableStateOf(1)

    init {
        fetchCharacters(1)
        observeFavoritesFromFirebase()
    }

    // Función para obtener personajes de la API
    fun fetchCharacters(page: Int) {
        viewModelScope.launch {
            try {
                val response = repository.getCharacters(page)
                characters.clear()
                characters.addAll(response.results)
                currentPage.value = page
                totalPages.value = response.info.pages
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Función para buscar personajes
    fun searchCharacters(query: String) {
        viewModelScope.launch {
            try {
                val response = repository.searchCharacters(query)
                searchResults.clear()
                searchResults.addAll(response.results)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Función para observar cambios en Firebase
    private fun observeFavoritesFromFirebase() {
        repository.observeFavorites { favoritesList ->
            // Asegúrate de evitar duplicados
            val distinctFavorites = favoritesList.distinctBy { it.id }
            favorites.clear()
            favorites.addAll(distinctFavorites)
        }
    }

    // Función para agregar un personaje a favoritos
    fun addToFavorites(character: Character) {
        if (!isFavorite(character)) {  // Verificar si el personaje ya está en favoritos
            viewModelScope.launch {
                try {
                    repository.addToFavorites(character)
                    favorites.add(character)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Función para eliminar un personaje de favoritos
    fun removeFromFavorites(character: Character) {
        if (isFavorite(character)) {  // Verificar si el personaje está en favoritos
            viewModelScope.launch {
                try {
                    repository.removeFromFavorites(character)
                    favorites.remove(character)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Función para verificar si un personaje está en favoritos
    fun isFavorite(character: Character): Boolean {
        return favorites.any { it.id == character.id }
    }
}
