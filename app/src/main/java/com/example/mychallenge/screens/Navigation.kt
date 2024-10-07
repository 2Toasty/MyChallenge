package com.example.mychallenge.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mychallenge.viewmodel.MainViewModel

@Composable
fun Navigation(
    viewModel: MainViewModel,
    navController: NavHostController,
    onOpenDrawer: () -> Unit // Función para abrir el drawer
) {
    NavHost(navController = navController, startDestination = "characterList") {
        composable(route = "characterList") {
            CharacterListScreen(
                viewModel = viewModel,
                navController = navController,
                onFavoritesClick = {
                    navController.navigate("favorites") // Navegar a la pantalla de favoritos
                },
                onOpenDrawer = onOpenDrawer // Agregar la función para abrir el Drawer
            )
        }

        composable(route = "characterDetail/{characterId}") { backStackEntry ->
            val characterId = backStackEntry.arguments?.getString("characterId")?.toInt() ?: 0
            val character = viewModel.characters.firstOrNull { it.id == characterId }
            if (character != null) {
                CharacterDetailScreen(character = character, viewModel = viewModel)  // Pasa el viewModel aquí
            }
        }

        composable(route = "search") {
            SearchScreen(viewModel = viewModel, navController = navController)
        }

        composable(route = "favorites") {
            FavoritesScreen(viewModel = viewModel, onCharacterClick = { character ->
                navController.navigate("characterDetail/${character.id}")
            })
        }

        composable(route = "profile") {
            ProfileScreen()
        }
    }
}
