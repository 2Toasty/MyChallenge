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
import kotlinx.coroutines.delay
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

// Retrofit API Service con búsqueda y paginación
interface ApiService {
    @GET("character")
    suspend fun getCharacters(@Query("page") page: Int): ApiResponse

    @GET("character")
    suspend fun searchCharacters(@Query("name") name: String): ApiResponse  // Buscar personajes por nombre
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
    // Lista de personajes actuales
    var characters = mutableStateListOf<Character>() // Lista de personajes
    var favorites = mutableStateListOf<Character>()  // Lista de favoritos

    // Variables para la paginación
    var currentPage by mutableStateOf(1)  // Página actual
    var totalPages by mutableStateOf(1)   // Total de páginas disponibles

    // Resultados de búsqueda
    var searchResults = mutableStateListOf<Character>()  // Lista para almacenar los resultados de la búsqueda

    // Firebase Firestore
    private val firestore = FirebaseFirestore.getInstance()

    init {
        fetchCharacters(currentPage)
        observeFavoritesFromFirebase() // Cargar favoritos desde Firebase al inicio
    }

    // Función para buscar personajes por nombre en la API
    fun searchCharacters(query: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.searchCharacters(query)
                searchResults.clear()
                searchResults.addAll(response.results)  // Almacenar los resultados en searchResults
            } catch (e: Exception) {
                e.printStackTrace()
                searchResults.clear()  // Limpiar si ocurre un error
            }
        }
    }

    // Obtiene personajes con paginación
    fun fetchCharacters(page: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getCharacters(page)
                characters.clear()
                characters.addAll(response.results)
                currentPage = page
                totalPages = response.info.pages
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Listener para observar cambios en Firebase en tiempo real
    private fun observeFavoritesFromFirebase() {
        firestore.collection("favorites").addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Manejar error si ocurre
                error.printStackTrace()
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                favorites.clear()  // Limpiamos la lista antes de actualizarla
                for (document in snapshot.documents) {
                    val character = document.toObject(Character::class.java)
                    if (character != null) {
                        favorites.add(character)
                    }
                }
            }
        }
    }

    // Función para agregar un personaje a favoritos
    fun addToFavorites(character: Character, onSuccess: () -> Unit) {
        if (!favorites.contains(character)) {
            favorites.add(character)
            saveFavoriteToFirebase(character)
            onSuccess()
        }
    }

    // Función para eliminar un personaje de favoritos
    fun removeFromFavorites(character: Character, onSuccess: () -> Unit) {
        favorites.remove(character)
        firestore.collection("favorites").document(character.id.toString()).delete()
        onSuccess()
    }

    // Guardar un personaje en Firebase Firestore
    private fun saveFavoriteToFirebase(character: Character) {
        firestore.collection("favorites").document(character.id.toString()).set(character)
    }

    // Verificar si un personaje está en favoritos
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: MainViewModel = viewModel()) {
    var query by remember { mutableStateOf("") }
    var debounceQuery by remember { mutableStateOf("") }

    // Aplicamos un debounce de 500ms para la búsqueda
    LaunchedEffect(query) {
        delay(500)  // Esperar 500ms antes de ejecutar la búsqueda
        debounceQuery = query  // Actualizar la query a la versión debounced
    }

    // Realizar la búsqueda cada vez que el texto debounced cambie
    LaunchedEffect(debounceQuery) {
        if (debounceQuery.isNotEmpty()) {
            viewModel.searchCharacters(debounceQuery)  // Buscar personajes en la API
        } else {
            viewModel.searchResults.clear()  // Limpiar los resultados si no hay consulta
        }
    }

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

            // Mostrar los personajes filtrados de la API
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(viewModel.searchResults) { character ->
                    CharacterRow(character = character, onCharacterClick = {
                        // Aquí puedes navegar al detalle del personaje si lo deseas
                    })
                }
            }
        }
    }
}

// Pantalla de detalle del personaje
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
                    Text(if (isFavorite) "Personaje eliminado de Favoritos" else "Personaje agregado a Favoritos")
                }
            }
        }
    }
}

// Pantalla de lista de favoritos
@Composable
fun FavoritesScreen(viewModel: MainViewModel = viewModel(), onCharacterClick: (Character) -> Unit) {
    Scaffold { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues)
        ) {
            items(viewModel.favorites) { character ->
                CharacterRow(character = character, onCharacterClick = {
                    onCharacterClick(character)  // Navegar al detalle con el personaje correcto
                })
            }
        }
    }
}

// Pantalla de perfil con datos simulados
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
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

// Pantalla principal con navegación lateral
@Composable
fun MainContent(viewModel: MainViewModel = viewModel(), navController: NavHostController) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Menú", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Opción Home
                    Text("Home", modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch { drawerState.close() }
                            viewModel.fetchCharacters(1)
                            navController.navigate("characterList") {
                                popUpTo("characterList") { inclusive = true }
                            }
                        }
                        .padding(16.dp))

                    // Opción Buscar
                    Text("Buscar", modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch { drawerState.close() }
                            navController.navigate("search")
                        }
                        .padding(16.dp))

                    // Opción Perfil
                    Text("Perfil", modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch { drawerState.close() }
                            navController.navigate("profile")
                        }
                        .padding(16.dp))

                    // Opción genérica 1
                    Text("Opción Genérica 1", modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch { drawerState.close() }
                            // Acción para la opción genérica 1
                        }
                        .padding(16.dp))

                    // Opción genérica 2
                    Text("Opción Genérica 2", modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch { drawerState.close() }
                            // Acción para la opción genérica 2
                        }
                        .padding(16.dp))
                }
            }
        },
        content = {
            Scaffold(
                bottomBar = {
                    BottomNavigationBar(
                        navController = navController,
                        viewModel = viewModel,
                        onFavoritesClick = { navController.navigate("favorites") }
                    )
                }
            ) { paddingValues ->
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .padding(paddingValues)
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

                    // Floating Action Button (FAB) centrado
                    FloatingActionButton(
                        onClick = { navController.navigate("favorites") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)  // Centrar el FAB en la parte inferior
                            .offset(y = (-28).dp)  // Ajustar la posición para que no interfiera con la barra de navegación
                    ) {
                        Icon(Icons.Filled.Favorite, contentDescription = "Favoritos")
                    }
                }
            }
        }
    )
}

// Configuración de la navegación
@Composable
fun Navigation(viewModel: MainViewModel = viewModel()) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "characterList") {
        composable("characterList") {
            MainContent(viewModel = viewModel, navController = navController)
        }
        composable("characterDetail/{characterId}") { backStackEntry ->
            val characterId = backStackEntry.arguments?.getString("characterId")?.toInt() ?: 0
            val character = viewModel.characters.firstOrNull { it.id == characterId }
            if (character != null) {
                CharacterDetailScreen(character = character)
            }
        }
        composable("search") {
            SearchScreen(viewModel = viewModel)  // Pantalla de búsqueda
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
    val viewModel = MainViewModel()
    val navController = rememberNavController()

    CharacterListScreen(
        viewModel = viewModel,
        navController = navController,
        onFavoritesClick = {}
    )
}
