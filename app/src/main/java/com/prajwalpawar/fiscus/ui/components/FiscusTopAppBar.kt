package com.prajwalpawar.fiscus.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
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
fun FiscusTopAppBar (
    title: String,
    modifier: Modifier = Modifier,
    preference: AppBarPreference,
    scrollBehavior: TopAppBarScrollBehavior
) {
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

    when (preference) {
        AppBarPreference.SMALL -> {
            TopAppBar(
                title = titleContent,
                modifier = modifier,
                colors = colors,
                scrollBehavior = scrollBehavior
            )
        }

        AppBarPreference.MEDIUM -> {
            MediumFlexibleTopAppBar(
                title = titleContent,
                modifier = modifier,
                colors = colors,
                scrollBehavior = scrollBehavior
            )
        }

        AppBarPreference.LARGE -> {
            LargeFlexibleTopAppBar(
                title = titleContent,
                modifier = modifier,
                colors = colors,
                scrollBehavior = scrollBehavior
            )
        }
    }
}
