package com.example.mychallenge.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mychallenge.viewmodel.MainViewModel

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    viewModel: MainViewModel,
    onFavoritesClick: () -> Unit // Pasamos el evento para manejar el FAB
) {
    Box {
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            NavigationBarItem(
                icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                label = { Text("Home") },
                selected = false,
                onClick = {
                    viewModel.fetchCharacters(1)  // Cargar la página 1
                    navController.navigate("characterList") {
                        popUpTo("characterList") { inclusive = true }
                    }
                }
            )

            // Ajuste de padding horizontal para mover el ícono de búsqueda
            NavigationBarItem(
                icon = { Icon(Icons.Filled.Search, contentDescription = "Buscar") },
                label = { Text("Buscar") },
                selected = false,
                modifier = Modifier.padding(horizontal = 10.dp),  // Ajusta este valor para mover el ícono
                onClick = {
                    navController.navigate("search")  // Navegar a la pantalla de búsqueda
                }
            )

            Spacer(modifier = Modifier.weight(1.8f))  // Espacio para el FAB

            NavigationBarItem(
                icon = { Icon(Icons.Filled.Person, contentDescription = "Perfil") },
                label = { Text("Perfil") },
                selected = false,
                onClick = { navController.navigate("profile") }
            )
        }

        // FAB en el centro de la barra de navegación
        FloatingActionButton(
            onClick = onFavoritesClick,
            containerColor = MaterialTheme.colorScheme.error, // Cambiar el color a rojo
            modifier = Modifier
                .align(Alignment.Center) // Alinear el FAB al centro
                .offset(y = (-28).dp) // Ajustar la posición del FAB hacia arriba para alinearlo
        ) {
            Icon(Icons.Filled.Favorite, contentDescription = "Favoritos")
        }
    }
}