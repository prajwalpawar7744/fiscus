package com.prajwalpawar.fiscus.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.prajwalpawar.fiscus.navigation.AppBarType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiscusTopAppBar (
    title: String,
    appBarType: AppBarType,
    scrollBehavior: TopAppBarScrollBehavior ?= null
) {
    when (appBarType) {
        AppBarType.SMALL -> {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }

        AppBarType.MEDIUM -> {
            MediumTopAppBar(
                title = {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                },
                scrollBehavior = scrollBehavior
            )
        }

        AppBarType.LARGE -> {
            LargeTopAppBar(
                title = {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                },
                scrollBehavior = scrollBehavior
            )
        }
    }
}