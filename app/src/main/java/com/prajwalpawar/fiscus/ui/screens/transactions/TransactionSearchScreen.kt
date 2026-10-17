package com.prajwalpawar.fiscus.ui.screens.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberContainedSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prajwalpawar.fiscus.data.model.TransactionDetails
import com.prajwalpawar.fiscus.ui.components.FiscusTransactionRow
import com.prajwalpawar.fiscus.ui.navigation.ContainerKeys
import com.prajwalpawar.fiscus.ui.navigation.containerTransform
import com.prajwalpawar.fiscus.ui.theme.FiscusSpacing

// Full-page search, opened from the Transactions app bar. It draws its own search field
// as the header, the way Material search screens do, so the app title bar is hidden here.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionSearchScreen(
    viewModel: TransactionSearchViewModel,
    onBack: () -> Unit,
    onResultClick: (id: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val results by viewModel.results.collectAsStateWithLifecycle()

    val textFieldState = rememberTextFieldState()
    // Already open: this page is the expanded search.
    val searchBarState = rememberContainedSearchBarState(initialValue = SearchBarValue.Expanded)
    val colors = SearchBarDefaults.containedColors(state = searchBarState)
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(textFieldState) {
        snapshotFlow { textFieldState.text.toString() }.collect(viewModel::setQuery)
    }

    // Open with the keyboard up, ready to type.
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(modifier = modifier.fillMaxSize()) {
        SearchBarDefaults.InputField(
            textFieldState = textFieldState,
            searchBarState = searchBarState,
            onSearch = {},
            colors = colors.inputFieldColors,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = FiscusSpacing.md, vertical = FiscusSpacing.sm)
                .focusRequester(focusRequester),
            placeholder = {
                Text(
                    text = "Search transactions",
                    modifier = Modifier.clearAndSetSemantics {}
                )
            },
            leadingIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            trailingIcon = {
                if (textFieldState.text.isNotEmpty()) {
                    IconButton(onClick = { textFieldState.clearText() }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                    }
                }
            }
        )

        val query = textFieldState.text.toString()

        when {
            query.isBlank() -> SearchHint("Search titles and notes across all months")
            results.isEmpty() -> SearchHint("No transactions match \"${query.trim()}\"")
            else -> SearchResults(results = results, onResultClick = onResultClick)
        }
    }
}

@Composable
private fun SearchResults(
    results: List<TransactionDetails>,
    onResultClick: (id: Long) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = FiscusSpacing.md,
            end = FiscusSpacing.md,
            top = FiscusSpacing.sm,
            bottom = FiscusSpacing.lg
        ),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
    ) {
        itemsIndexed(results, key = { _, result -> result.id }) { index, result ->
            FiscusTransactionRow(
                transaction = result,
                shapes = ListItemDefaults.segmentedShapes(index = index, count = results.size),
                onClick = { onResultClick(result.id) },
                // Results span months, so each shows its date.
                showDate = true,
                modifier = Modifier.containerTransform(ContainerKeys.transaction(result.id))
            )
        }
    }
}

@Composable
private fun SearchHint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(FiscusSpacing.xl)
    )
}
