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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prajwalpawar.fiscus.domain.model.Category
import com.prajwalpawar.fiscus.domain.model.TransactionType
import com.prajwalpawar.fiscus.ui.utils.fiscusClickable
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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
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
            val expenseCategories = uiState.categories.filter { it.type == TransactionType.EXPENSE || it.type == null }

            if (expenseCategories.isNotEmpty()) {
                item {
                    Text(
                        "Expense Categories",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(expenseCategories, key = { it.id ?: it.name }) { category ->
                    CategoryItem(
                        category = category,
                        onDelete = { categoryToDelete = it }
                    )
                }
            }

            if (incomeCategories.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Income Categories",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(incomeCategories, key = { it.id ?: it.name }) { category ->
                    CategoryItem(
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
            sheetState = sheetState,
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
    category: Category,
    onDelete: (Category) -> Unit
) {
    val haptic = rememberFiscusHaptic()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.large,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(category.color).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(category.icon),
                    contentDescription = null,
                    tint = Color(category.color),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            
            if (!category.isSystem) {
                IconButton(onClick = { 
                    haptic.click()
                    onDelete(category) 
                }) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
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
    var selectedColor by remember { mutableStateOf(Color(0xFF006D39)) }
    var selectedIcon by remember { mutableStateOf("Category") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    val haptic = rememberFiscusHaptic()

    val colors = listOf(
        Color(0xFF006D39), Color(0xFF415AA9), Color(0xFF006493),
        Color(0xFFB91C1C), Color(0xFFEAB308), Color(0xFF8B5CF6),
        Color(0xFFEC4899), Color(0xFF0EA5E9), Color(0xFF10B981),
        Color(0xFFF97316), Color(0xFF6366F1), Color(0xFF84CC16),
        Color(0xFF06B6D4), Color(0xFFD946EF), Color(0xFFF43F5E)
    )

    val icons = listOf(
        "ShoppingBag", "Restaurant", "DirectionsBus", "LocalHospital",
        "School", "Entertainment", "Home", "ElectricalServices",
        "Savings", "AccountBalanceWallet", "Payments", "MonetizationOn"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("Create New Category", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        // Type Selector
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
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
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = MaterialTheme.shapes.large
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Select Color", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(colors) { color ->
                    val isSelected = selectedColor == color
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                shape = CircleShape
                            )
                            .fiscusClickable(haptic = haptic) {
                                selectedColor = color
                            }
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Select Icon", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                modifier = Modifier.height(110.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(icons) { iconName ->
                    val isSelected = selectedIcon == iconName
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .fiscusClickable(haptic = haptic) {
                                selectedIcon = iconName
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(iconName),
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.large
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
                shape = MaterialTheme.shapes.large
            ) {
                Text("Create")
            }
        }
    }
}
