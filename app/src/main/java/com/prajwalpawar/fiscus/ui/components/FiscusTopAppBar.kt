package com.prajwalpawar.fiscus.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.prajwalpawar.fiscus.data.model.AppBarPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiscusTopAppBar (
    title: String,
    modifier: Modifier = Modifier,
    preference: AppBarPreference
) {
    val colors = TopAppBarDefaults.topAppBarColors()

    when (preference) {
        AppBarPreference.SMALL -> {
            TopAppBar(
                title = {
                    AppBarTitle(
                        title,
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                modifier = modifier,
                colors = colors
            )
        }

        AppBarPreference.MEDIUM -> {
            MediumTopAppBar(
                title = {
                    AppBarTitle(
                        title,
                        style = MaterialTheme.typography.headlineLarge
                    )
                },
                modifier = modifier,
                colors = colors
            )
        }

        AppBarPreference.LARGE -> {
            LargeTopAppBar(
                title = {
                    AppBarTitle(
                        title,
                        style = MaterialTheme.typography.displayMedium
                    )
                },
                modifier = modifier,
                colors = colors
            )
        }
    }
}

@Composable
private fun AppBarTitle (
    title: String,
    style: TextStyle
) {
    Text(
        text = title,
        style = style,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
}