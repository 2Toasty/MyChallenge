package com.example.mychallenge.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mychallenge.ui.components.CharacterCard
import com.example.mychallenge.viewmodel.MainViewModel

@Composable
fun SearchScreen(
    viewModel: MainViewModel,
    navController: NavHostController // Añadimos el NavController como parámetro
) {
    var query by remember { mutableStateOf("") }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Campo de texto para la búsqueda
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Buscar personaje") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            Button(
                onClick = { viewModel.searchCharacters(query) },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Buscar")
            }

            // Mostrar los resultados de la búsqueda
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(viewModel.searchResults) { character ->
                    CharacterCard(
                        character = character,
                        onClick = {
                            // Usar el NavController para navegar al detalle del personaje
                            navController.navigate("characterDetail/${character.id}")
                        }
                    )
                }
            }
        }
    }
}
