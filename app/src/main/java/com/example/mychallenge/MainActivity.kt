package com.example.mychallenge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.mychallenge.api.RetrofitInstance
import com.example.mychallenge.repository.CharacterRepository
import com.example.mychallenge.screens.Navigation
import com.example.mychallenge.ui.components.AppDrawer
import com.example.mychallenge.ui.theme.MyChallengeTheme
import com.example.mychallenge.viewmodel.MainViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = CharacterRepository(RetrofitInstance.api, FirebaseFirestore.getInstance())
        val viewModel = MainViewModel(repository)

        setContent {
            MyChallengeTheme {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        AppDrawer(navController = navController, drawerState = drawerState)
                    },
                    content = {
                        Navigation(
                            viewModel = viewModel,
                            navController = navController,
                            onOpenDrawer = { scope.launch { drawerState.open() } } // Abrir el Drawer
                        )
                    }
                )
            }
        }
    }
}
