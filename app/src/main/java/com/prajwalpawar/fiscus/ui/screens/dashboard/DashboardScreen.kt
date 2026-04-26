package com.prajwalpawar.fiscus.ui.screens.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.prajwalpawar.fiscus.domain.model.Account
import com.prajwalpawar.fiscus.domain.model.Category
import com.prajwalpawar.fiscus.domain.model.Transaction
import com.prajwalpawar.fiscus.domain.model.TransactionType
import com.prajwalpawar.fiscus.ui.screens.transactions.AddTransactionScreen
import com.prajwalpawar.fiscus.ui.screens.transactions.AddTransactionViewModel
import com.prajwalpawar.fiscus.ui.screens.transactions.TransactionDetailScreen
import com.prajwalpawar.fiscus.ui.utils.AnimatedAmount
import com.prajwalpawar.fiscus.ui.utils.EmptyState
import com.prajwalpawar.fiscus.ui.utils.fiscusClickable
import com.prajwalpawar.fiscus.ui.utils.formatCurrency
import com.prajwalpawar.fiscus.ui.utils.getCategoryIcon
import com.prajwalpawar.fiscus.ui.utils.rememberFiscusHaptic
import com.prajwalpawar.fiscus.ui.utils.staggeredVerticalFadeIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onSeeAllTransactions: () -> Unit,
    onManageAccounts: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val addTransactionViewModel: AddTransactionViewModel = hiltViewModel()
    val haptic = rememberFiscusHaptic()

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { true }
    )
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showDetailSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(showBottomSheet) {
        if (!showBottomSheet) {
            addTransactionViewModel.resetState()
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
                addTransactionViewModel.resetState()
            },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            AddTransactionScreen(
                viewModel = addTransactionViewModel,
                onDismiss = {
                    showBottomSheet = false
                }
            )
        }
    }

    if (showDetailSheet && uiState.selectedTransactionDetail != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showDetailSheet = false
                viewModel.clearSelectedTransaction()
            },
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            val transaction = uiState.selectedTransactionDetail!!
            TransactionDetailScreen(
                transaction = transaction,
                category = uiState.categories[transaction.categoryId],
                account = uiState.accountsMap[transaction.accountId],
                toAccount = transaction.toAccountId?.let { uiState.accountsMap[it] },
                currencyCode = uiState.currency,
                onEdit = {
                    showDetailSheet = false
                    addTransactionViewModel.setTransactionForEdit(transaction)
                    scope.launch {
                        delay(200)
                        showBottomSheet = true
                    }
                },
                onDelete = {
                    showDeleteDialog = true
                },
                onDismiss = {
                    showDetailSheet = false
                    viewModel.clearSelectedTransaction()
                },
                animationsEnabled = uiState.areAnimationsEnabled
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Transaction") },
            text = { Text("Are you sure you want to delete this transaction?") },
            confirmButton = {
                TextButton(onClick = {
                    uiState.selectedTransactionDetail?.let {
                        addTransactionViewModel.setTransactionForEdit(it)
                        addTransactionViewModel.deleteTransaction()
                    }
                    showDeleteDialog = false
                    showDetailSheet = false
                    viewModel.clearSelectedTransaction()
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val scrollBehavior = if (uiState.topBarStyle == "longtopbar") {
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    } else {
        TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            val titleContent = @Composable {
                Column {
                    val welcomeMessage = remember {
                        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                        when (hour) {
                            in 0..11 -> "Good Morning"
                            in 12..16 -> "Good Afternoon"
                            else -> "Good Evening"
                        }
                    }
                    Text(
                        text = welcomeMessage,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = uiState.userName.ifBlank { "User" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            val actionsContent = @Composable {
                Box(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.userPhotoUri != null) {
                        AsyncImage(
                            model = uiState.userPhotoUri,
                            contentDescription = "Profile Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            val colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
            )

            if (uiState.topBarStyle == "longtopbar") {
                LargeTopAppBar(
                    title = titleContent,
                    actions = { actionsContent() },
                    colors = colors,
                    scrollBehavior = scrollBehavior
                )
            } else {
                TopAppBar(
                    title = titleContent,
                    actions = { actionsContent() },
                    colors = colors,
                    scrollBehavior = scrollBehavior
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    haptic.click()
                    addTransactionViewModel.resetState()
                    showBottomSheet = true
                },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Add Transaction") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.large,
                expanded = scrollBehavior.state.collapsedFraction < 0.5f
            )
        }
    ) { padding ->
        androidx.compose.animation.AnimatedContent(
            targetState = uiState.isLoading,
            transitionSpec = {
                com.prajwalpawar.fiscus.ui.utils.FiscusAnimation.Navigation.Enter togetherWith 
                com.prajwalpawar.fiscus.ui.utils.FiscusAnimation.Navigation.Exit
            },
            label = "dashboardLoadingTransition"
        ) { isLoading ->
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(48.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        BalanceCard(
                            balance = uiState.balance,
                            income = uiState.totalIncome,
                            expense = uiState.totalExpense,
                            currencyCode = uiState.currency,
                            animationsEnabled = uiState.areAnimationsEnabled,
                            isMasked = uiState.isPrivacyModeEnabled,
                            isCompact = uiState.isCompactNumberFormatEnabled,
                            modifier = Modifier.staggeredVerticalFadeIn(0, uiState.areAnimationsEnabled)
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .staggeredVerticalFadeIn(1, uiState.areAnimationsEnabled),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Your Accounts",
                                style = MaterialTheme.typography.titleMedium
                            )
                            TextButton(onClick = {
                                haptic.click()
                                onManageAccounts()
                            }) {
                                Text("Manage")
                            }
                        }

                        if (uiState.accounts.isEmpty()) {
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                shape = MaterialTheme.shapes.large,
                                onClick = {
                                    haptic.click()
                                    onManageAccounts()
                                }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(24.dp)
                                        .fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddCircleOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Add your first account",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
                            ) {
                                itemsIndexed(
                                    items = uiState.accounts,
                                    key = { _, acc -> acc.account.id ?: acc.account.hashCode() }
                                ) { index, accountWithBalance ->
                                    AccountCard(
                                        modifier = Modifier.staggeredVerticalFadeIn(
                                            index,
                                            enabled = uiState.areAnimationsEnabled,
                                            initialDelay = 400
                                        ),
                                        accountWithBalance = accountWithBalance,
                                        currencyCode = uiState.currency,
                                        animationsEnabled = uiState.areAnimationsEnabled,
                                        isMasked = uiState.isPrivacyModeEnabled,
                                        isCompact = uiState.isCompactNumberFormatEnabled
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .staggeredVerticalFadeIn(2, uiState.areAnimationsEnabled),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Transactions",
                                style = MaterialTheme.typography.titleMedium
                            )
                            TextButton(onClick = {
                                haptic.click()
                                onSeeAllTransactions()
                            }) {
                                Text("See All")
                            }
                        }
                    }

                    itemsIndexed(
                        items = uiState.recentTransactions,
                        key = { _, it -> it.id ?: it.hashCode() }
                    ) { index, transaction ->
                        TransactionItem(
                            modifier = Modifier.staggeredVerticalFadeIn(
                                index + 3,
                                enabled = uiState.areAnimationsEnabled,
                                initialDelay = 100
                            ),
                            transaction = transaction,
                            animationsEnabled = uiState.areAnimationsEnabled,
                            category = uiState.categories[transaction.categoryId],
                            account = uiState.accountsMap[transaction.accountId],
                            toAccount = transaction.toAccountId?.let { uiState.accountsMap[it] },
                            currencyCode = uiState.currency,
                            isMasked = uiState.isPrivacyModeEnabled,
                            isCompact = uiState.isCompactNumberFormatEnabled,
                            onClick = {
                                viewModel.onTransactionClick(transaction)
                                showDetailSheet = true
                            }
                        )
                    }

                    if (uiState.recentTransactions.isEmpty()) {
                        item {
                            EmptyState(
                                message = "No transactions yet",
                                icon = Icons.AutoMirrored.Filled.ReceiptLong
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BalanceCard(
    balance: Double,
    income: Double,
    expense: Double,
    currencyCode: String,
    animationsEnabled: Boolean = true,
    isMasked: Boolean = false,
    isCompact: Boolean = false,
    modifier: Modifier = Modifier
) {
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        progress = 1f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = if (animationsEnabled) progress else 1f,
        animationSpec = tween(350, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "balanceCardProgress"
    )

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .graphicsLayer {
                alpha = animatedProgress
                translationY = (1f - animatedProgress) * 20f
            },
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total Balance",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            AnimatedAmount(
                targetAmount = balance,
                currencyCode = currencyCode,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                enabled = animationsEnabled,
                isMasked = isMasked,
                isCompact = isCompact
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                SummaryCard(
                    label = "Income",
                    amount = income,
                    icon = Icons.Default.ArrowUpward,
                    color = MaterialTheme.colorScheme.primary,
                    currencyCode = currencyCode,
                    modifier = Modifier.weight(1f),
                    animationsEnabled = animationsEnabled,
                    isMasked = isMasked,
                    isCompact = isCompact
                )

                SummaryCard(
                    label = "Expense",
                    amount = expense,
                    icon = Icons.Default.ArrowDownward,
                    color = MaterialTheme.colorScheme.error,
                    currencyCode = currencyCode,
                    modifier = Modifier.weight(1f),
                    animationsEnabled = animationsEnabled,
                    isMasked = isMasked,
                    isCompact = isCompact
                )
            }
        }
    }
}

@Composable
fun SummaryCard(
    label: String,
    amount: Double,
    icon: ImageVector,
    color: Color,
    currencyCode: String,
    modifier: Modifier = Modifier,
    animationsEnabled: Boolean = true,
    isMasked: Boolean = false,
    isCompact: Boolean = false
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = color.copy(alpha = 0.15f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            AnimatedAmount(
                targetAmount = amount,
                currencyCode = currencyCode,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                enabled = animationsEnabled,
                isMasked = isMasked,
                isCompact = isCompact
            )
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    category: Category?,
    currencyCode: String,
    modifier: Modifier = Modifier,
    animationsEnabled: Boolean = true,
    isMasked: Boolean = false,
    account: Account? = null,
    toAccount: Account? = null,
    isCompact: Boolean = false,
    onClick: () -> Unit = {}
) {
    val amountColor = when (transaction.type) {
        TransactionType.INCOME -> MaterialTheme.colorScheme.primary
        TransactionType.EXPENSE -> MaterialTheme.colorScheme.error
        TransactionType.TRANSFER -> MaterialTheme.colorScheme.secondary
    }


    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .fiscusClickable(
                haptic = rememberFiscusHaptic(),
                enabledAnimations = animationsEnabled
            ) {
                onClick()
            }
            .padding(vertical = 12.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = category?.color?.let { Color(it).copy(alpha = 0.12f) }
                ?: MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = getCategoryIcon(category?.icon ?: ""),
                    contentDescription = null,
                    tint = category?.color?.let { Color(it) }
                        ?: MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = category?.color?.let { Color(it).copy(alpha = 0.1f) } ?: MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        text = category?.name ?: "No Category",
                        style = MaterialTheme.typography.labelSmall,
                        color = category?.color?.let { Color(it) } ?: MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                if (account != null) {
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    ) {
                        Text(
                            text = account.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                if (toAccount != null) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    ) {
                        Text(
                            text = toAccount.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                if (transaction.note.isNotBlank()) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Notes,
                        contentDescription = "Has Note",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                when (transaction.type) {
                    TransactionType.INCOME -> Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Income",
                        modifier = Modifier.size(16.dp),
                        tint = amountColor
                    )
                    TransactionType.EXPENSE -> Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Expense",
                        modifier = Modifier.size(16.dp),
                        tint = amountColor
                    )
                    TransactionType.TRANSFER -> Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Transfer",
                        modifier = Modifier.size(16.dp),
                        tint = amountColor
                    )
                }
                Text(
                    text = formatCurrency(transaction.amount, currencyCode, isMasked, isCompact),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            val dateFormatter =
                remember { java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault()) }
            Text(
                text = dateFormatter.format(transaction.date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

}

@Composable
fun AccountCard(
    accountWithBalance: com.prajwalpawar.fiscus.domain.model.AccountWithBalance,
    currencyCode: String,
    animationsEnabled: Boolean,
    isMasked: Boolean = false,
    isCompact: Boolean = false,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .width(160.dp)
            .fiscusClickable(
                haptic = rememberFiscusHaptic(),
                enabledAnimations = animationsEnabled
            ) { /* No action yet, but can be added */ },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getCategoryIcon(accountWithBalance.account.icon),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = accountWithBalance.account.name,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            AnimatedAmount(
                targetAmount = accountWithBalance.balance,
                currencyCode = currencyCode,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                enabled = animationsEnabled,
                isMasked = isMasked,
                isCompact = isCompact
            )
        }
    }
}

