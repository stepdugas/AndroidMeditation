package com.thesecretplace.app.util

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource

// Resolves an iOS image name (e.g. "morningBeach") to an Android drawable resource ID
fun resolveImageRes(context: android.content.Context, imageName: String): Int {
    // Convert camelCase/hyphenated iOS name to snake_case Android name
    val androidName = imageName
        .replace(Regex("([a-z])([A-Z])")) { "${it.groupValues[1]}_${it.groupValues[2]}" }
        .lowercase()
        .replace("-", "_")
    return context.resources.getIdentifier(androidName, "drawable", context.packageName)
}

@Composable
fun MeditationImage(
    imageName: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val resId = resolveImageRes(context, imageName)
    if (resId != 0) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    } else {
        // Fallback: empty box (image not found)
        Box(modifier = modifier)
    }
}
