@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.prajwalpawar.fiscus.ui.screens.home

import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import com.prajwalpawar.fiscus.ui.theme.FiscusMotion
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LoadingIndicator
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.getValue
import com.prajwalpawar.fiscus.ui.components.fiscusAnimateItem
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import com.prajwalpawar.fiscus.ui.components.FiscusEmptyState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prajwalpawar.fiscus.data.model.AccountBalance
import com.prajwalpawar.fiscus.data.model.PeriodTotals
import com.prajwalpawar.fiscus.data.model.TransactionDetails
import com.prajwalpawar.fiscus.ui.components.FiscusListIcon
import com.prajwalpawar.fiscus.ui.components.FiscusSectionHeader
import com.prajwalpawar.fiscus.ui.components.FiscusTransactionRow
import com.prajwalpawar.fiscus.ui.components.accountIcon
import com.prajwalpawar.fiscus.ui.components.transactionsByDay
import com.prajwalpawar.fiscus.ui.navigation.ContainerKeys
import com.prajwalpawar.fiscus.ui.navigation.containerTransform
import com.prajwalpawar.fiscus.ui.theme.FiscusSize
import com.prajwalpawar.fiscus.ui.theme.FiscusSpacing
import com.prajwalpawar.fiscus.util.formatMoney
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import com.prajwalpawar.fiscus.ui.theme.FiscusReducedMotion
import com.prajwalpawar.fiscus.ui.theme.FiscusShapes

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onSeeAllClick: () -> Unit,
    // Opens the Accounts list, so every account is reachable without swiping the carousel.
    onBalanceClick: () -> Unit,
    onAddAccountClick: () -> Unit,
    onAccountClick: (id: Long) -> Unit,
    onTransactionClick: (id: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // The wavy loading indicator until data arrives, then the content fades in quickly.
    AnimatedContent(
        targetState = uiState.isLoading,
        modifier = modifier.fillMaxSize(),
        transitionSpec = {
            fadeIn(tween(FiscusMotion.FADE_IN_MILLIS)) togetherWith
                fadeOut(tween(FiscusMotion.FADE_OUT_MILLIS))
        },
        label = "homeLoading"
    ) { loading ->
        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingIndicator()
            }
            return@AnimatedContent
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = FiscusSize.fabClearance),
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
        ) {
            item(key = "balance") {
                BalanceHero(
                    total = uiState.totalBalance,
                    accountCount = uiState.accounts.size,
                    onClick = onBalanceClick,
                    modifier = Modifier.padding(horizontal = FiscusSpacing.md)
                )
            }

            item(key = "month") {
                MonthSummary(
                    totals = uiState.monthTotals,
                    spentShare = uiState.spentShareOfIncome,
                    modifier = Modifier.padding(
                        start = FiscusSpacing.md,
                        end = FiscusSpacing.md,
                        top = FiscusSpacing.md
                    )
                )
            }

            item(key = "accounts") {
                AccountsCarousel(
                    accounts = uiState.accounts,
                    onAddAccountClick = onAddAccountClick,
                    onAccountClick = onAccountClick
                )
            }

            recentTransactions(
                recent = uiState.recent,
                onSeeAllClick = onSeeAllClick,
                onTransactionClick = onTransactionClick
            )
        }
    }
}

@Composable
private fun BalanceHero(
    total: Long,
    accountCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressMotion = !FiscusReducedMotion.enabled && pressed
    val scale by animateFloatAsState(
        targetValue = if (pressMotion) FiscusMotion.HERO_PRESS_SCALE else 1f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "heroScale"
    )
    val rotation by animateFloatAsState(
        targetValue = if (pressMotion) FiscusMotion.HERO_PRESS_ROTATION else 0f,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "heroShapeRotation"
    )
    val corner = heroDecorationCorner

    // A slow, steady spin (linear, so it never seems to speed up or stop); none with
    // reduced motion.
    val reducedMotion = FiscusReducedMotion.enabled
    val spin = if (reducedMotion) {
        null
    } else {
        rememberInfiniteTransition(label = "heroSpin").animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(FiscusMotion.HERO_SPIN_MILLIS, easing = LinearEasing)
            ),
            label = "heroSpinAngle"
        )
    }

    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .semantics { onClick(label = "Open accounts", action = null) },
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        val shape = FiscusShapes.icon
        val decorationColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = HERO_DECORATION_ALPHA)

        // Decoration only: the user's icon shape, large and faint, drawn behind the content
        // so it never changes the card's size; the card clips what pokes out past the corner.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    val side = FiscusSize.heroDecoration.toPx()
                    val inset = FiscusSize.heroDecorationOverhang.toPx() - side / 2
                    val x = if (layoutDirection == LayoutDirection.Rtl) -corner.x else corner.x
                    val center = Offset(
                        x = (if (x > 0) size.width else 0f) + x * inset,
                        y = (if (corner.y > 0) size.height else 0f) + corner.y * inset
                    )
                    val outline = shape.createOutline(Size(side, side), layoutDirection, this)
                    rotate(rotation + (spin?.value ?: 0f), pivot = center) {
                        translate(center.x - side / 2, center.y - side / 2) {
                            drawOutline(outline, decorationColor)
                        }
                    }
                }
        ) {
            Column(
                modifier = Modifier.padding(FiscusSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(FiscusSpacing.xs)
            ) {
                Text(
                    text = "Total balance",
                    style = MaterialTheme.typography.labelLarge
                )

                val heroStyle = MaterialTheme.typography.displayMediumEmphasized
                val shown = animatedAmount(total)

                // Shrinks to fit large amounts on one line instead of wrapping. TalkBack reads
                // the final amount, not the numbers it rolls through.
                Text(
                    text = formatMoney(shown),
                    style = heroStyle,
                    maxLines = 1,
                    autoSize = TextAutoSize.StepBased(maxFontSize = heroStyle.fontSize),
                    modifier = Modifier.semantics { contentDescription = formatMoney(total) }
                )

                Text(
                    text = if (accountCount == 1) "Across 1 account" else "Across $accountCount accounts",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private const val HERO_DECORATION_ALPHA = 0.08f

// Where the hero's decorative shape sits (x: -1 start, 1 end; y: -1 top, 1 bottom).
private enum class HeroCorner(val x: Int, val y: Int) {
    TopStart(-1, -1),
    TopEnd(1, -1),
    BottomStart(-1, 1),
    BottomEnd(1, 1)
}

// Picked once per launch, so it stays put while moving around the app.
private val heroDecorationCorner = HeroCorner.entries.random()

// Rolls from the previously shown amount to [target] when it changes; the first value shows
// as is. With reduced motion it jumps straight there.
@Composable
private fun animatedAmount(target: Long): Long {
    val reducedMotion = FiscusReducedMotion.enabled
    val spec = MaterialTheme.motionScheme.slowEffectsSpec<Float>()
    var from by remember { mutableLongStateOf(target) }
    var to by remember { mutableLongStateOf(target) }
    val progress = remember { Animatable(1f) }

    // Long maths, since Float can't hold large amounts exactly.
    val shown = from + ((to - from) * progress.value.toDouble()).roundToLong()

    LaunchedEffect(target) {
        if (target == to) return@LaunchedEffect
        from = from + ((to - from) * progress.value.toDouble()).roundToLong()
        to = target
        if (reducedMotion) {
            progress.snapTo(1f)
        } else {
            progress.snapTo(0f)
            progress.animateTo(1f, spec)
        }
    }

    return shown
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MonthSummary(
    totals: PeriodTotals,
    spentShare: Float?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(FiscusSpacing.md)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(FiscusSpacing.sm)
        ) {
            SummaryTile(
                label = "Income",
                amount = totals.income,
                icon = Icons.AutoMirrored.Rounded.TrendingUp,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.weight(1f)
            )

            SummaryTile(
                label = "Spent",
                amount = totals.expense,
                icon = Icons.AutoMirrored.Rounded.TrendingDown,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f)
            )
        }

        // Without budgets, spending is measured against this month's income.
        if (spentShare != null) {
            val animatedShare by animateFloatAsState(
                targetValue = spentShare.coerceIn(0f, 1f),
                animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                label = "spentShare"
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(FiscusSpacing.sm)
            ) {
                LinearWavyProgressIndicator(
                    // The spring can overshoot; keep the bar inside its track.
                    progress = { animatedShare.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = if (spentShare > 1f) {
                        "You've spent more than this month's income"
                    } else {
                        "You've spent ${(spentShare * 100).roundToInt()}% of this month's income"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (spentShare > 1f) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SummaryTile(
    label: String,
    amount: Long,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Column(
            modifier = Modifier.padding(FiscusSpacing.md),
            verticalArrangement = Arrangement.spacedBy(FiscusSpacing.xs)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(FiscusSpacing.xs)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null
                )

                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            val amountStyle = MaterialTheme.typography.titleLargeEmphasized

            Text(
                text = formatMoney(amount),
                style = amountStyle,
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(maxFontSize = amountStyle.fontSize)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountsCarousel(
    accounts: List<AccountBalance>,
    onAddAccountClick: () -> Unit,
    onAccountClick: (id: Long) -> Unit
) {
    Column(
        modifier = Modifier.padding(top = FiscusSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(FiscusSpacing.sm)
    ) {
        FiscusSectionHeader(
            title = "Accounts",
            modifier = Modifier.padding(horizontal = FiscusSpacing.md)
        )

        // The last card adds an account, so the carousel is never empty.
        val itemCount = accounts.size + 1
        val carouselState = rememberCarouselState { itemCount }

        // Uncontained: M3 says multi-browse is for visual items, not text-heavy cards
        // (it shrinks items and crops their text); uncontained keeps every card full size.
        HorizontalUncontainedCarousel(
            state = carouselState,
            itemWidth = FiscusSize.accountCardWidth,
            itemSpacing = FiscusSpacing.sm,
            contentPadding = PaddingValues(horizontal = FiscusSpacing.md),
            modifier = Modifier.height(FiscusSize.accountCardHeight)
        ) { index ->
            val account = accounts.getOrNull(index)

            // Each card grows into the page it opens.
            val cardModifier = Modifier
                .fillMaxSize()
                .containerTransform(
                    key = account?.let { ContainerKeys.account(it.id) }
                        ?: ContainerKeys.NEW_ACCOUNT,
                    clipShape = MaterialTheme.shapes.extraLarge
                )
                .maskClip(MaterialTheme.shapes.extraLarge)

            if (account != null) {
                AccountCard(
                    account = account,
                    onClick = { onAccountClick(account.id) },
                    modifier = cardModifier
                )
            } else {
                AddAccountCard(
                    onClick = onAddAccountClick,
                    modifier = cardModifier
                )
            }
        }
    }
}

@Composable
private fun AccountCard(
    account: AccountBalance,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // M3 filled card; the carousel's mask gives it its shape, so the card itself is square.
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RectangleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(FiscusSpacing.md),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            FiscusListIcon(icon = accountIcon(account.type))

            Column {
                Text(
                    text = account.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val balanceStyle = MaterialTheme.typography.titleLargeEmphasized

                Text(
                    text = formatMoney(account.balance),
                    style = balanceStyle,
                    maxLines = 1,
                    autoSize = TextAutoSize.StepBased(maxFontSize = balanceStyle.fontSize)
                )
            }
        }
    }
}

@Composable
private fun AddAccountCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RectangleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(FiscusSpacing.md),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            FiscusListIcon(icon = Icons.Rounded.Add)

            Text(
                text = "Add account",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

private fun LazyListScope.recentTransactions(
    recent: List<TransactionDetails>,
    onSeeAllClick: () -> Unit,
    onTransactionClick: (id: Long) -> Unit
) {
    item(key = "recentHeader") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = FiscusSpacing.md,
                    end = FiscusSpacing.xs,
                    top = FiscusSpacing.lg
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FiscusSectionHeader(
                title = "Recent",
                modifier = Modifier.weight(1f)
            )

            if (recent.isNotEmpty()) {
                TextButton(onClick = onSeeAllClick, shapes = ButtonDefaults.shapes()) {
                    Text("See all")
                }
            }
        }
    }

    if (recent.isEmpty()) {
        item(key = "recentEmpty") {
            FiscusEmptyState(
                icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                title = "No transactions yet",
                message = "Tap Add transaction to record your first one.",
                modifier = fiscusAnimateItem()
            )
        }
        return
    }

    transactionsByDay(recent) { transaction, shapes ->
        FiscusTransactionRow(
            transaction = transaction,
            shapes = shapes,
            onClick = { onTransactionClick(transaction.id) },
            modifier = Modifier
                .padding(horizontal = FiscusSpacing.md)
                .containerTransform(ContainerKeys.transaction(transaction.id))
        )
    }
}
