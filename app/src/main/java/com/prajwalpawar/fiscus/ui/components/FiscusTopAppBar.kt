@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.prajwalpawar.fiscus.ui.components

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
            IconButton(onClick = onNavigateUp, shapes = IconButtonDefaults.shapes()) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }
    }

    // Default colors: on-surface title, and the bar tints when content scrolls under it.
    val colors = TopAppBarDefaults.topAppBarColors()

    // The bars pick the title size for each state; overriding it clips the collapsed title.
    val titleContent: @Composable () -> Unit = {
        Text(text = title)
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
