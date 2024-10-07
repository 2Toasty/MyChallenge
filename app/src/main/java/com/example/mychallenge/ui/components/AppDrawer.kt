package com.example.mychallenge.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch

@Composable
fun AppDrawer(
    navController: NavHostController,
    drawerState: DrawerState // Recibimos el estado del drawer
) {
    val scope = rememberCoroutineScope()

    ModalDrawerSheet {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Menú", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            // Opción Home
            Text("Home", modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    scope.launch {
                        drawerState.close() // Cierra el drawer antes de navegar
                        navController.navigate("characterList") {
                            popUpTo("characterList") { inclusive = true }
                        }
                    }
                }
                .padding(16.dp))

            // Opción Buscar
            Text("Buscar", modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    scope.launch {
                        drawerState.close() // Cierra el drawer
                        navController.navigate("search")
                    }
                }
                .padding(16.dp))

            // Opción Perfil
            Text("Perfil", modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    scope.launch {
                        drawerState.close() // Cierra el drawer
                        navController.navigate("profile")
                    }
                }
                .padding(16.dp))
        }
    }
}
