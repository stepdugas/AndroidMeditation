package com.thesecretplace.app.ui.screens.meditate

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.thesecretplace.app.model.Meditation
import com.thesecretplace.app.model.MeditationCategory
import com.thesecretplace.app.ui.components.*
import com.thesecretplace.app.ui.navigation.Routes
import com.thesecretplace.app.ui.theme.*
import com.thesecretplace.app.util.MeditationImage

@Composable
fun MeditateScreen(
    navController: NavController,
    viewModel: MeditateViewModel = hiltViewModel()
) {
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val filteredMeditations by viewModel.filteredMeditations.collectAsState()
    val availableCategories by viewModel.availableCategories.collectAsState()

    ThemedBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            // Title
            Text(
                text = "Meditations",
                fontFamily = SerifDisplay,
                fontWeight = FontWeight.Bold,
                fontSize = 34.sp,
                color = Color.White,
                modifier = Modifier.padding(start = 18.dp, top = 16.dp, bottom = 4.dp)
            )

            // Category pills
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                availableCategories.forEach { category ->
                    CategoryPill(
                        category = category,
                        isSelected = selectedCategory == category,
                        onClick = { viewModel.selectCategory(category) }
                    )
                }
            }

            ThemeDivider(modifier = Modifier.padding(horizontal = 18.dp))

            // Meditation list
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredMeditations, key = { it.id }) { meditation ->
                    MeditationCard(
                        meditation = meditation,
                        onClick = { navController.navigate(Routes.detail(meditation.id)) }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryPill(
    category: MeditationCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) category.color else Surface.copy(alpha = 0.65f)
    val fg = if (isSelected) Color.Black else Color.White.copy(alpha = 0.80f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = categoryIcon(category),
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = category.displayName,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = fg
        )
    }
}

@Composable
fun MeditationCard(meditation: Meditation, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CardRadius))
            .background(Surface.copy(alpha = 0.70f))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail
        MeditationImage(
            imageName = meditation.imageName,
            contentDescription = meditation.title,
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(ThumbRadius))
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            if (meditation.isNew) {
                Text(
                    text = "NEW",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier
                        .background(Accent, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                text = meditation.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = meditation.description,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.55f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    tint = Accent.copy(alpha = 0.80f),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = meditation.duration,
                    fontSize = 12.sp,
                    color = Accent
                )
            }
        }
    }
}

fun categoryIcon(category: MeditationCategory): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        MeditationCategory.MORNING -> Icons.Default.WbSunny
        MeditationCategory.SLEEP -> Icons.Default.NightsStay
        MeditationCategory.STRESS_RELIEF -> Icons.Default.Spa
        MeditationCategory.BREATHWORK -> Icons.Default.Air
        MeditationCategory.SOUNDSCAPES -> Icons.Default.GraphicEq
        MeditationCategory.MEDITATIO -> Icons.Default.FavoriteBorder
        MeditationCategory.MENTAL_TRAINING -> Icons.Default.Psychology
    }
}
