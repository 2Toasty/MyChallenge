package com.example.mychallenge.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mychallenge.model.Character
import com.example.mychallenge.viewmodel.MainViewModel

@Composable
fun CharacterDetailScreen(
    character: Character,
    viewModel: MainViewModel
) {
    var showMessage by remember { mutableStateOf(false) }
    val isFavorite = viewModel.isFavorite(character)

    Scaffold { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            AsyncImage(model = character.image, contentDescription = character.name)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Origen: ${character.origin.name}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (isFavorite) {
                        viewModel.removeFromFavorites(character)
                        showMessage = true

                    } else {
                        viewModel.addToFavorites(character)
                        showMessage = true

                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(if (isFavorite) "Quitar de Favoritos" else "Agregar a Favoritos")
            }

            if (showMessage) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { showMessage = false }) {
                            Text("Cerrar")
                        }
                    }
                ) {
                    Text(if (isFavorite) "Personaje agregado a Favoritos" else "Personaje eliminado de Favoritos")
                }
            }
        }
    }
}
