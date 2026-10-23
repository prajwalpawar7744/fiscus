package com.prajwalpawar.fiscus.ui.screens.settings

import com.prajwalpawar.fiscus.ui.theme.FiscusReducedMotion
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.Icon
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.Icons
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.prajwalpawar.fiscus.ui.components.FiscusSectionHeader
import com.prajwalpawar.fiscus.ui.theme.FiscusSpacing
import com.prajwalpawar.fiscus.util.ChangelogEntry
import com.prajwalpawar.fiscus.util.ChangelogRelease
import com.prajwalpawar.fiscus.util.parseChangelog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

// "What's new": CHANGELOG.md, bundled as an asset at build time (see app/build.gradle.kts),
// newest release first, one card per release. The installed release (or the newest, if the
// installed one isn't listed) is emphasised with a tonal container and starts expanded;
// older releases are collapsed to their heading and expand when tapped.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ChangelogScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val releases by produceState<List<ChangelogRelease>?>(initialValue = null) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                context.assets.open("CHANGELOG.md").bufferedReader().use { parseChangelog(it.readText()) }
            }.getOrDefault(emptyList())
        }
    }
    // Debug builds add "-debug"; the changelog lists the release name.
    val installed = remember(context) {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
            ?.removeSuffix("-debug")
    }

    val loaded = releases
    if (loaded == null) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LoadingIndicator()
        }
        return
    }

    val featured = loaded.firstOrNull { it.version == installed }?.version
        ?: loaded.firstOrNull()?.version
    var expanded by rememberSaveable(featured) { mutableStateOf(setOfNotNull(featured)) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(FiscusSpacing.md),
        verticalArrangement = Arrangement.spacedBy(FiscusSpacing.md)
    ) {
        items(loaded, key = { it.version }) { release ->
            ReleaseCard(
                release = release,
                isInstalled = release.version == installed,
                isFeatured = release.version == featured,
                expanded = release.version in expanded,
                onToggle = {
                    expanded = if (release.version in expanded) {
                        expanded - release.version
                    } else {
                        expanded + release.version
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ReleaseCard(
    release: ChangelogRelease,
    isInstalled: Boolean,
    isFeatured: Boolean,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    val motionScheme = MaterialTheme.motionScheme
    val reducedMotion = FiscusReducedMotion.enabled
    // M3 colour pairing: secondary text on a tonal container uses its "on" colour.
    // Primary only pairs with surface roles; on the tonal card accents use its "on" colour.
    val accentText = if (isFeatured) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.primary
    }
    val secondaryText = if (isFeatured) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = if (reducedMotion) snap() else motionScheme.defaultSpatialSpec(),
        label = "releaseArrow"
    )

    // The whole card toggles, so the tap target is generous; TalkBack hears the state.
    Card(
        onClick = onToggle,
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                stateDescription = if (expanded) "Expanded" else "Collapsed"
            },
        shape = MaterialTheme.shapes.large,
        colors = if (isFeatured) {
            // Emphasis for the release that matters most (M3 expressive: contrast in colour).
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        } else {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        }
    ) {
        Column(
            modifier = Modifier.padding(FiscusSpacing.md),
            verticalArrangement = Arrangement.spacedBy(FiscusSpacing.sm)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(FiscusSpacing.sm)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = release.version,
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        modifier = Modifier.semantics { heading() }
                    )
                    release.date?.let { date ->
                        Text(
                            text = readableDate(date),
                            style = MaterialTheme.typography.bodyMedium,
                            color = secondaryText
                        )
                    }
                }
                // A marker, not an action, so plain text rather than a (tappable) chip.
                if (isInstalled) {
                    Text(
                        text = "Installed",
                        style = MaterialTheme.typography.labelLarge,
                        color = accentText
                    )
                }
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.graphicsLayer { rotationZ = arrowRotation }
                )
            }

            // M3 enter/exit: expands down from the heading; a fade with reduced motion.
            AnimatedVisibility(
                visible = expanded,
                enter = if (reducedMotion) {
                    fadeIn(motionScheme.fastEffectsSpec())
                } else {
                    expandVertically(motionScheme.defaultSpatialSpec(), expandFrom = Alignment.Top) +
                        fadeIn(motionScheme.fastEffectsSpec())
                },
                exit = if (reducedMotion) {
                    fadeOut(motionScheme.fastEffectsSpec())
                } else {
                    shrinkVertically(motionScheme.fastSpatialSpec(), shrinkTowards = Alignment.Top) +
                        fadeOut(motionScheme.fastEffectsSpec())
                }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(FiscusSpacing.sm)) {
                    release.groups.forEach { group ->
                        FiscusSectionHeader(
                            title = group.title,
                            color = accentText,
                            modifier = Modifier.padding(top = FiscusSpacing.sm)
                        )
                        group.entries.forEach { entry ->
                            when (entry) {
                                is ChangelogEntry.Heading -> Text(
                                    text = entry.text,
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(top = FiscusSpacing.xs)
                                )

                                is ChangelogEntry.Bullet -> Row(
                                    modifier = Modifier.padding(
                                        start = if (entry.level > 0) FiscusSpacing.md else FiscusSpacing.none
                                    ),
                                    horizontalArrangement = Arrangement.spacedBy(FiscusSpacing.sm)
                                ) {
                                    Text(
                                        text = if (entry.level > 0) "◦" else "•",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = secondaryText
                                    )
                                    Text(
                                        text = withBold(entry.text),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// "**Profile**: add …" → "Profile" in bold, the rest plain.
private fun withBold(text: String): AnnotatedString = buildAnnotatedString {
    text.split("**").forEachIndexed { index, part ->
        if (index % 2 == 1) withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(part) } else append(part)
    }
}

// "2026-10-23" → "23 Oct 2026" in the phone's locale; left as written if it doesn't parse.
private fun readableDate(date: String): String =
    runCatching {
        LocalDate.parse(date).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
    }.getOrDefault(date)
