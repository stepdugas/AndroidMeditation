package com.thesecretplace.app.ui.screens.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.thesecretplace.app.data.PreferencesManager
import com.thesecretplace.app.data.sampleMeditations
import com.thesecretplace.app.model.Meditation
import com.thesecretplace.app.ui.components.*
import com.thesecretplace.app.ui.navigation.Routes
import com.thesecretplace.app.ui.screens.meditate.categoryIcon
import com.thesecretplace.app.ui.theme.*
import com.thesecretplace.app.util.MeditationImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@Composable
fun FavoritesScreen(
    navController: NavController,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val favorites by viewModel.favorites.collectAsState()

    ThemedBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                "Favorites",
                fontFamily = SerifDisplay,
                fontWeight = FontWeight.Bold,
                fontSize = 34.sp,
                color = Color.White,
                modifier = Modifier.padding(start = 18.dp, top = 16.dp, bottom = 8.dp)
            )

            if (favorites.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(contentAlignment = Alignment.Center) {
                            Box(modifier = Modifier.size(80.dp).background(Accent.copy(0.10f), CircleShape))
                            Icon(Icons.Default.StarBorder, null, tint = Accent.copy(0.70f), modifier = Modifier.size(36.dp))
                        }
                        Spacer(Modifier.height(20.dp))
                        Text("No Favorites Yet", fontFamily = SerifDisplay, fontSize = 22.sp, color = Color.White)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Tap the star on any meditation\nto save it here.",
                            fontSize = 15.sp, color = Color.White.copy(0.52f),
                            lineHeight = 22.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(favorites, key = { it.id }) { meditation ->
                        FavoriteRow(
                            meditation = meditation,
                            onClick = { navController.navigate(Routes.detail(meditation.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteRow(meditation: Meditation, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CardRadius))
            .background(Surface.copy(0.70f))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        MeditationImage(
            imageName = meditation.imageName,
            contentDescription = meditation.title,
            modifier = Modifier
                .size(68.dp)
                .clip(RoundedCornerShape(ThumbRadius))
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(meditation.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, null, tint = Accent.copy(0.80f), modifier = Modifier.size(11.dp))
                Spacer(Modifier.width(4.dp))
                Text(meditation.duration, fontSize = 13.sp, color = Accent)
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(categoryIcon(meditation.category), null, tint = meditation.category.color, modifier = Modifier.size(11.dp))
                Spacer(Modifier.width(4.dp))
                Text(meditation.category.displayName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = meditation.category.color)
            }
        }
        Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(0.25f), modifier = Modifier.size(13.dp))
    }
}

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val prefs: PreferencesManager
) : ViewModel() {
    private val _favorites = MutableStateFlow<List<Meditation>>(emptyList())
    val favorites: StateFlow<List<Meditation>> = _favorites

    init { refresh() }

    fun refresh() {
        val ids = prefs.favoriteMeditationIDs.split(",").filter { it.isNotEmpty() }.toSet()
        _favorites.value = sampleMeditations.filter { it.id in ids }
    }
}
