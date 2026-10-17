package com.prajwalpawar.fiscus.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.prajwalpawar.fiscus.data.model.AppBarPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiscusTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    preference: AppBarPreference,
    scrollBehavior: TopAppBarScrollBehavior,
    subtitle: String? = null,
    // Shows a back arrow on pages below the top-level tabs.
    onNavigateUp: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val navigationIcon: @Composable () -> Unit = {
        if (onNavigateUp != null) {
            IconButton(onClick = onNavigateUp) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }
    }

    // Keep the default container colors so the bar tints when content scrolls under it.
    val colors = TopAppBarDefaults.topAppBarColors(
        titleContentColor = MaterialTheme.colorScheme.primary
    )

    // The bars pick the title size for each state; overriding it clips the collapsed title.
    val titleContent: @Composable () -> Unit = {
        Text(
            text = title,
            fontWeight = FontWeight.Bold
        )
    }

    val subtitleContent: (@Composable () -> Unit)? = subtitle?.let {
        { Text(text = it) }
    }

    when (preference) {
        AppBarPreference.SMALL -> {
            if (subtitleContent != null) {
                TopAppBar(
                    title = titleContent,
                    subtitle = subtitleContent,
                    modifier = modifier,
                    navigationIcon = navigationIcon,
                    actions = actions,
                    colors = colors,
                    scrollBehavior = scrollBehavior
                )
            } else {
                TopAppBar(
                    title = titleContent,
                    modifier = modifier,
                    navigationIcon = navigationIcon,
                    actions = actions,
                    colors = colors,
                    scrollBehavior = scrollBehavior
                )
            }
        }

        AppBarPreference.MEDIUM -> {
            MediumFlexibleTopAppBar(
                title = titleContent,
                subtitle = subtitleContent,
                modifier = modifier,
                navigationIcon = navigationIcon,
                actions = actions,
                colors = colors,
                scrollBehavior = scrollBehavior
            )
        }

        AppBarPreference.LARGE -> {
            LargeFlexibleTopAppBar(
                title = titleContent,
                subtitle = subtitleContent,
                modifier = modifier,
                navigationIcon = navigationIcon,
                actions = actions,
                colors = colors,
                scrollBehavior = scrollBehavior
            )
        }
    }
}
