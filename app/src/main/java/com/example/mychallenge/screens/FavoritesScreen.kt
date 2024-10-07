
package com.example.mychallenge.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import com.example.mychallenge.model.Character
import com.example.mychallenge.ui.components.CharacterCard
import com.example.mychallenge.viewmodel.MainViewModel

@Composable
fun FavoritesScreen(viewModel: MainViewModel, onCharacterClick: (Character) -> Unit) {
    LazyColumn {
        items(viewModel.favorites) { character ->
            CharacterCard(
                character = character,
                onClick = { onCharacterClick(character) }
            )


        }
    }
}
