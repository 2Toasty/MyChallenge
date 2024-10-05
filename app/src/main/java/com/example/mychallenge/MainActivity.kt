package com.example.mychallenge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Data Model para el JSON y Paginación
data class Character(
    val id: Int = 0,
    val name: String = "",
    val origin: Origin = Origin(),
    val image: String = ""
)

data class Origin(val name: String = "")

data class ApiResponse(val results: List<Character>, val info: Info)
data class Info(val count: Int, val pages: Int, val next: String?, val prev: String?)

// Retrofit API Service con paginación
interface ApiService {
    @GET("character")
    suspend fun getCharacters(@Query("page") page: Int): ApiResponse
}

object RetrofitInstance {
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://rickandmortyapi.com/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

// ViewModel que maneja la lógica
class MainViewModel : ViewModel() {
    var characters = mutableStateListOf<Character>()
    var favorites = mutableStateListOf<Character>()
    var currentPage by mutableStateOf(1)
    var totalPages by mutableStateOf(1)

    // Firebase Firestore
    private val firestore = FirebaseFirestore.getInstance()

    init {
        fetchCharacters(currentPage)
        listenToFavoritesInFirebase()  // Escuchar cambios en tiempo real
    }

    // Obtener personajes con paginación
    fun fetchCharacters(page: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getCharacters(page)
                characters.clear()
                characters.addAll(response.results)
                currentPage = page
                totalPages = response.info.pages
            } catch (e: Exception) {
                // Manejar el error
            }
        }
    }

    // Escuchar cambios en favoritos en tiempo real
    private fun listenToFavoritesInFirebase() {
        firestore.collection("favorites")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    favorites.clear()
                    for (document in snapshots) {
                        val character = document.toObject(Character::class.java)
                        favorites.add(character)
                    }
                }
            }
    }

    // Agregar personaje a favoritos
    fun addToFavorites(character: Character, onSuccess: () -> Unit) {
        if (!favorites.contains(character)) {
            favorites.add(character)
            saveFavoriteToFirebase(character)
            onSuccess()
        }
    }

    // Guardar en Firebase en la colección "favorites"
    private fun saveFavoriteToFirebase(character: Character) {
        firestore.collection("favorites").document(character.id.toString())
            .set(character)
    }

    // Eliminar de favoritos
    fun removeFromFavorites(character: Character, onSuccess: () -> Unit) {
        favorites.remove(character)
        firestore.collection("favorites").document(character.id.toString()).delete()
        onSuccess()
    }

    // Verifica si el personaje está en favoritos
    fun isFavorite(character: Character): Boolean {
        return favorites.contains(character)
    }
}

// Pantalla principal con paginación
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterListScreen(
    viewModel: MainViewModel = viewModel(),
    navController: NavHostController,
    onFavoritesClick: () -> Unit
) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                viewModel = viewModel,
                onFavoritesClick = onFavoritesClick // Pasamos el evento para manejar el FAB desde aquí
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f) // Asegurar que la lista ocupe el espacio restante
            ) {
                items(viewModel.characters) { character ->
                    CharacterRow(character = character, onCharacterClick = {
                        navController.navigate("characterDetail/${character.id}")
                    })
                }
            }

            // Botones de paginación
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    enabled = viewModel.currentPage > 1,
                    onClick = { viewModel.fetchCharacters(viewModel.currentPage - 1) }
                ) {
                    Text("Página anterior")
                }

                Button(
                    enabled = viewModel.currentPage < viewModel.totalPages,
                    onClick = { viewModel.fetchCharacters(viewModel.currentPage + 1) }
                ) {
                    Text("Página siguiente")
                }
            }
        }
    }
}


// Barra de Navegación Inferior con botón de "Home" que lleva a la página 1
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

            // Dejar un espacio en el centro para el FloatingActionButton
            Spacer(modifier = Modifier.weight(1f))

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

// Pantalla de detalle con confirmación
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailScreen(character: Character, viewModel: MainViewModel = viewModel()) {
    var showMessage by remember { mutableStateOf(false) }
    val isFavorite = viewModel.isFavorite(character)

    Scaffold { padding ->
        Column(modifier = Modifier.padding(padding)) {
            AsyncImage(model = character.image, contentDescription = character.name)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Origen: ${character.origin.name}", style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (isFavorite) {
                        viewModel.removeFromFavorites(character) {
                            showMessage = true
                        }
                    } else {
                        viewModel.addToFavorites(character) {
                            showMessage = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
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
                    Text(if (isFavorite) "Personaje eliminado de Favoritos" else "Personaje agregado a Favoritos")
                }
            }
        }
    }
}

// Pantalla de lista de favoritos
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(viewModel: MainViewModel = viewModel(), onCharacterClick: (Character) -> Unit) {
    Scaffold { paddingValues ->  // Recibimos el padding del Scaffold
        LazyColumn(
            modifier = Modifier.padding(paddingValues)  // Aplicamos el padding del Scaffold
        ) {
            items(viewModel.favorites) { character ->
                CharacterRow(
                    character = character,
                    onCharacterClick = {
                        onCharacterClick(character)
                    },
                    modifier = Modifier.padding(8.dp)  // Añadimos un padding a cada fila
                )
            }
        }
    }
}

// Pantalla de perfil con datos simulados
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    Scaffold { paddingValues ->  // Recibimos el padding del Scaffold
        Column(
            modifier = Modifier
                .padding(paddingValues)  // Aplicamos el padding del Scaffold
                .padding(16.dp)  // Añadimos un padding adicional de 16.dp
        ) {
            Text("Nombre: Martin")
            Text("E-Mail: xxx@gmail.com")
            Text("Teléfono: +5493123135")
            Text("Sitio Web: www.google.com")
            Text("Contraseña: ************")
        }
    }
}

// Composable para cada personaje en la lista
@Composable
fun CharacterRow(character: Character, onCharacterClick: (Character) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCharacterClick(character) }
            .padding(8.dp),  // Añadimos padding dentro de la fila para separación
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(character.name, modifier = Modifier.weight(1f))
        AsyncImage(
            model = character.image,
            contentDescription = character.name,
            modifier = Modifier.size(64.dp)
        )
    }
}

// Configuración de la navegación
@Composable
fun Navigation(viewModel: MainViewModel = viewModel()) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "characterList") {
        composable("characterList") {
            CharacterListScreen(
                viewModel = viewModel,
                navController = navController,
                onFavoritesClick = {
                    navController.navigate("favorites")
                }
            )
        }
        composable("characterDetail/{characterId}") { backStackEntry ->
            val characterId = backStackEntry.arguments?.getString("characterId")?.toInt() ?: 0
            val character = viewModel.characters.firstOrNull { it.id == characterId } ?:
            viewModel.favorites.firstOrNull { it.id == characterId }
            if (character != null) {
                CharacterDetailScreen(character = character)
            }
        }
        composable("favorites") {
            FavoritesScreen(viewModel = viewModel, onCharacterClick = { character ->
                navController.navigate("characterDetail/${character.id}")
            })
        }
        composable("profile") {
            ProfileScreen()
        }
    }
}

// MainActivity que aloja todo
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializa Firebase
        FirebaseFirestore.setLoggingEnabled(true)
        setContent {
            MyApp()
        }
    }
}

// La función principal de la app
@Composable
fun MyApp(viewModel: MainViewModel = viewModel()) {
    MaterialTheme {
        Navigation(viewModel)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCharacterListScreen() {
    val viewModel = MainViewModel() // Aquí podrías usar un mock si prefieres
    val navController = rememberNavController()

    CharacterListScreen(
        viewModel = viewModel,
        navController = navController,
        onFavoritesClick = {}
    )
}
