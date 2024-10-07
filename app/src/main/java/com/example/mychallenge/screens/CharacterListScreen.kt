package com.example.mychallenge.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.mychallenge.model.Character
import com.example.mychallenge.model.Origin
import com.example.mychallenge.ui.components.BottomNavigationBar
import com.example.mychallenge.ui.components.CharacterCard
import com.example.mychallenge.viewmodel.MainViewModel

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

import com.example.mychallenge.ui.components.CharacterCard




import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*


import com.example.mychallenge.ui.components.CharacterCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterListScreen(
    viewModel: MainViewModel,
    navController: NavHostController,
    onFavoritesClick: () -> Unit, // Evento para el botón de favoritos
    onOpenDrawer: () -> Unit // Función para abrir el drawer
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista de Personajes") },
                navigationIcon = {
                    IconButton(onClick = { onOpenDrawer() }) { // Botón para abrir el Drawer
                        Icon(Icons.Default.Menu, contentDescription = "Abrir Drawer")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                viewModel = viewModel, // Añadir viewModel aquí
                onFavoritesClick = onFavoritesClick
            )
        }
    ) { paddingValues ->
        // Contenido de la pantalla aquí
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(viewModel.characters) { character ->
                    CharacterCard(character = character, onClick = {
                        navController.navigate("characterDetail/${character.id}")
                    })
                }
            }

            // Botones de paginación si es necesario
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    enabled = viewModel.currentPage.value > 1,
                    onClick = { viewModel.fetchCharacters(viewModel.currentPage.value - 1) }
                ) {
                    Text("Página anterior")
                }

                Button(
                    enabled = viewModel.currentPage.value < viewModel.totalPages.value,
                    onClick = { viewModel.fetchCharacters(viewModel.currentPage.value + 1) }
                ) {
                    Text("Página siguiente")
                }
            }
        }
    }
}





