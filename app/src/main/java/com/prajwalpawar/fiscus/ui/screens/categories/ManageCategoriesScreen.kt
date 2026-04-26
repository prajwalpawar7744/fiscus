package com.prajwalpawar.fiscus.ui.screens.categories

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prajwalpawar.fiscus.domain.model.Category
import com.prajwalpawar.fiscus.domain.model.TransactionType
import com.prajwalpawar.fiscus.ui.utils.fiscusClickable
import com.prajwalpawar.fiscus.ui.utils.fiscusScaleIn
import com.prajwalpawar.fiscus.ui.utils.rememberFiscusHaptic
import com.prajwalpawar.fiscus.ui.utils.staggeredVerticalFadeIn
import com.prajwalpawar.fiscus.ui.utils.getCategoryIcon
import com.prajwalpawar.fiscus.ui.components.ConfirmationDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCategoriesScreen(
    viewModel: ManageCategoriesViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = rememberFiscusHaptic()
    var showAddSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    val scrollBehavior = if (uiState.topBarStyle == "longtopbar") {
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    } else {
        TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            val titleContent = @Composable { Text("Categories", fontWeight = FontWeight.Bold) }
            val navigationIconContent = @Composable {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
            val appBarsColors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
            )

            if (uiState.topBarStyle == "longtopbar") {
                LargeTopAppBar(
                    title = titleContent,
                    navigationIcon = navigationIconContent,
                    colors = appBarsColors,
                    scrollBehavior = scrollBehavior
                )
            } else {
                TopAppBar(
                    title = titleContent,
                    navigationIcon = navigationIconContent,
                    colors = appBarsColors,
                    scrollBehavior = scrollBehavior
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    haptic.click()
                    showAddSheet = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Category") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.extraLarge
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val incomeCategories = uiState.categories.filter { it.type == TransactionType.INCOME }
            val expenseCategories =
                uiState.categories.filter { it.type == TransactionType.EXPENSE || it.type == null }

            if (expenseCategories.isNotEmpty()) {
                item {
                    Text(
                        "Expense Categories",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .staggeredVerticalFadeIn(0, enabled = uiState.areAnimationsEnabled)
                    )
                }
                itemsIndexed(
                    expenseCategories,
                    key = { _, it -> it.id ?: it.name }) { index, category ->
                    CategoryItem(
                        modifier = Modifier.staggeredVerticalFadeIn(
                            index + 1,
                            enabled = uiState.areAnimationsEnabled
                        ),
                        category = category,
                        onDelete = { categoryToDelete = it }
                    )
                }
            }

            if (incomeCategories.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(16.dp))
                    val expenseCount =
                        if (expenseCategories.isNotEmpty()) expenseCategories.size + 1 else 0
                    Text(
                        "Income Categories",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .staggeredVerticalFadeIn(
                                expenseCount,
                                enabled = uiState.areAnimationsEnabled
                            )
                    )
                }
                val startIdx =
                    (if (expenseCategories.isNotEmpty()) expenseCategories.size + 1 else 0) + 1
                itemsIndexed(
                    incomeCategories,
                    key = { _, it -> it.id ?: it.name }) { index, category ->
                    CategoryItem(
                        modifier = Modifier.staggeredVerticalFadeIn(
                            startIdx + index,
                            enabled = uiState.areAnimationsEnabled
                        ),
                        category = category,
                        onDelete = { categoryToDelete = it }
                    )
                }
            }
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = bottomSheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            AddCategorySheet(
                onAdd = { name, icon, color, type ->
                    viewModel.addCategory(name, icon, color, type)
                    showAddSheet = false
                },
                onCancel = { showAddSheet = false }
            )
        }
    }

    if (categoryToDelete != null) {
        ConfirmationDialog(
            onDismissRequest = { categoryToDelete = null },
            onConfirm = {
                categoryToDelete?.let { viewModel.deleteCategory(it) }
                categoryToDelete = null
            },
            title = "Delete Category?",
            text = "Are you sure you want to delete '${categoryToDelete?.name}'? This will not delete transactions using this category, but it will be removed from future selection.",
            confirmButtonText = "Delete",
            icon = Icons.Default.Delete
        )
    }
}

@Composable
fun CategoryItem(
    modifier: Modifier = Modifier,
    category: Category,
    onDelete: (Category) -> Unit
) {
    val haptic = rememberFiscusHaptic()
    val categoryColor = Color(category.color)
    val typeLabel = when (category.type) {
        TransactionType.INCOME -> "Income"
        TransactionType.EXPENSE -> "Expense"
        TransactionType.TRANSFER -> "Transfer"
        null -> "Expense"
    }
    val typeContainerColor = when (category.type) {
        TransactionType.INCOME -> MaterialTheme.colorScheme.primaryContainer
        TransactionType.TRANSFER -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.error
    }
    val typeContentColor = when (category.type) {
        TransactionType.INCOME -> MaterialTheme.colorScheme.onPrimaryContainer
        TransactionType.TRANSFER -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onError
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(categoryColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(category.icon),
                    contentDescription = null,
                    tint = categoryColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = typeContainerColor
                ) {
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = typeContentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            if (category.isSystem) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "System category",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            } else {
                FilledTonalIconButton(
                    onClick = {
                        haptic.click()
                        onDelete(category)
                    },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddCategorySheet(
    onAdd: (String, String, Int, TransactionType?) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer,
        MaterialTheme.colorScheme.errorContainer,
        MaterialTheme.colorScheme.inversePrimary,
        MaterialTheme.colorScheme.surfaceVariant,
        MaterialTheme.colorScheme.outline
    )

    var selectedColor by remember { mutableStateOf(colors[0]) }
    var selectedIcon by remember { mutableStateOf("ShoppingBag") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    val haptic = rememberFiscusHaptic()

    val icons = listOf(
        "ShoppingBag", "Restaurant", "DirectionsBus", "LocalHospital",
        "School", "Entertainment", "Home", "ElectricalServices",
        "Savings", "AccountBalanceWallet", "Payments", "MonetizationOn"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
            .fiscusScaleIn(initialScale = 0.98f),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header row with live preview
        Row(
            modifier = Modifier.fillMaxWidth().staggeredVerticalFadeIn(0),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live icon preview
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(selectedColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(selectedIcon),
                    contentDescription = null,
                    tint = selectedColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column {
                Text(
                    "Create Category",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = name.ifBlank { "New Category" },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (name.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant
                    else selectedColor
                )
            }
        }

        // Type Selector
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth().staggeredVerticalFadeIn(1)
        ) {
            SegmentedButton(
                selected = selectedType == TransactionType.EXPENSE,
                onClick = { selectedType = TransactionType.EXPENSE },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) {
                Text("Expense")
            }
            SegmentedButton(
                selected = selectedType == TransactionType.INCOME,
                onClick = { selectedType = TransactionType.INCOME },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) {
                Text("Income")
            }
        }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Category Name") },
            placeholder = { Text("e.g. Subscriptions") },
            modifier = Modifier.fillMaxWidth().staggeredVerticalFadeIn(2),
            singleLine = true,
            shape = MaterialTheme.shapes.large,
            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Label, null) }
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.staggeredVerticalFadeIn(3)
        ) {
            Text(
                "Color",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(colors) { color ->
                    val isSelected = selectedColor == color
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 40.dp else 36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .then(
                                if (isSelected) Modifier.border(
                                    3.dp,
                                    MaterialTheme.colorScheme.onSurface,
                                    CircleShape
                                )
                                else Modifier
                            )
                            .fiscusClickable(haptic = haptic) { selectedColor = color },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.staggeredVerticalFadeIn(4)
        ) {
            Text(
                "Icon",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                modifier = Modifier.height(110.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(icons) { iconName ->
                    val isSelected = selectedIcon == iconName
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(
                                if (isSelected) selectedColor.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                            .then(
                                if (isSelected) Modifier.border(
                                    2.dp,
                                    selectedColor,
                                    MaterialTheme.shapes.medium
                                )
                                else Modifier
                            )
                            .fiscusClickable(haptic = haptic) { selectedIcon = iconName },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(iconName),
                            contentDescription = null,
                            tint = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().staggeredVerticalFadeIn(5),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(name, selectedIcon, selectedColor.toArgb(), selectedType)
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = name.isNotBlank(),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Create")
            }
        }
    }
}
