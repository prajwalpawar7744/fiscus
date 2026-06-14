package com.prajwalpawar.fiscus.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prajwalpawar.fiscus.navigation.AppBarType
import com.prajwalpawar.fiscus.ui.tokens.FiscusElevation
import com.prajwalpawar.fiscus.ui.tokens.FiscusShapes
import com.prajwalpawar.fiscus.ui.tokens.FiscusSpacing


@Composable
fun SettingsScreen() {
    val viewModel: SettingsViewModel = viewModel()

    val darkTheme by viewModel.darkTheme.collectAsState()
    val appBarStyle by viewModel.appBarStyle.collectAsState()

    val currentAppBarStyle = AppBarType.valueOf(appBarStyle)

    var showAppBarDialog by remember {
        mutableStateOf(false)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(FiscusSpacing.Large),
        verticalArrangement = Arrangement.spacedBy(FiscusSpacing.Large)
    ) {
        // profile
        item {
            ElevatedCard(
                shape = FiscusShapes.large,
                elevation = CardDefaults.elevatedCardElevation(
                    defaultElevation = FiscusElevation.Level3
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(FiscusSpacing.Large),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(FiscusSpacing.xxxl)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = "Profile Picture",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(FiscusSpacing.Huge)
                            )
                        }
                    }

                    Spacer(
                        Modifier.height(FiscusSpacing.Medium)
                    )

                    Text(
                        text = "Prajwal",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "@fiscus_user",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(FiscusSpacing.Medium))

                    FilledTonalButton(onClick = {}) {
                        Text("Edit Profile")
                    }
                }
            }
        }

        // appearance
        item {
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = FiscusSpacing.Small)
            )
        }

        item {
            ElevatedCard(
                shape = FiscusShapes.large,
                elevation = CardDefaults.elevatedCardElevation(
                    defaultElevation = FiscusElevation.Level2
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                ListItem(
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent
                    ),
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.DarkMode,
                            contentDescription = null
                        )
                    },
                    headlineContent = {
                        Text("Dark Theme")
                    },
                    supportingContent = {
                        Text("Use dark colors throughout the app")
                    },
                    trailingContent = {
                        Switch(
                            checked = darkTheme,
                            onCheckedChange = {
                                viewModel.toggleDarkTheme(it)
                            }
                        )
                    }
                )

                HorizontalDivider()

                ListItem(
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent
                    ),
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.ViewAgenda,
                            contentDescription = null
                        )
                    },
                    headlineContent = {
                        Text("App Bar Style")
                    },
                    supportingContent = {
                        Text(
                            currentAppBarStyle.name
                                .lowercase()
                                .replaceFirstChar { it.uppercase() }
                        )
                    },
                    trailingContent = {
                        AssistChip(
                            onClick = {
                                showAppBarDialog = true
                            },
                            label = {
                                Text(
                                    currentAppBarStyle.name
                                        .lowercase()
                                        .replaceFirstChar { it.uppercase() }
                                )
                            }
                        )
                    }
                )
            }
        }

        // about
        item {
            Text(
                text = "About",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = FiscusSpacing.Small)
            )
        }

        item {
            ElevatedCard(
                shape = FiscusShapes.large,
                elevation = CardDefaults.elevatedCardElevation(
                    defaultElevation = FiscusElevation.Level2
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                ListItem(
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent
                    ),
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null
                        )
                    },
                    headlineContent = {
                        Text("Version")
                    },
                    supportingContent = {
                        Text("Fiscus v1.0.0")
                    }
                )

                HorizontalDivider()

                ListItem(
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent
                    ),
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.Description,
                            contentDescription = null
                        )
                    },
                    headlineContent = {
                        Text("Licenses")
                    },
                    supportingContent = {
                        Text("Open source libraries")
                    }
                )
            }
        }
    }

    if (showAppBarDialog) {
        AlertDialog(
            onDismissRequest = {
                showAppBarDialog = false
            },
            title = {
                Text("App Bar Style")
            },
            text = {
                Column {
                    AppBarType.entries.forEach { type ->
                        ListItem(
                            colors = ListItemDefaults.colors(
                                containerColor = Color.Transparent
                            ),
                            headlineContent = {
                                Text(
                                    type.name
                                        .lowercase()
                                        .replaceFirstChar { it.uppercase() }
                                )
                            },
                            trailingContent = {
                                RadioButton(
                                    selected = currentAppBarStyle == type,
                                    onClick = {
                                        viewModel.setAppBarStyle(type.name)
                                        showAppBarDialog = false
                                    }
                                )
                            }
                        )
                    }
                }
            },
            confirmButton = {}
        )
    }
}