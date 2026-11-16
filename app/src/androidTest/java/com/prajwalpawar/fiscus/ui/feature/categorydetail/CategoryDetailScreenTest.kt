package com.prajwalpawar.fiscus.ui.feature.categorydetail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.prajwalpawar.fiscus.model.Category
import com.prajwalpawar.fiscus.model.CategoryKind
import com.prajwalpawar.fiscus.model.CategoryWithUsage
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CategoryDetailScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val food = Category(1, "Food", CategoryKind.EXPENSE, "restaurant")

    /** Same priority as the account page (ADR-042): Unarchive first, then Delete, then Archive. */
    @Test
    fun firstActionFollowsTheAccountPageOrder() {
        var item by mutableStateOf(CategoryWithUsage(food.copy(archived = true), 0))
        val events = mutableListOf<CategoryDetailEvent>()
        composeRule.setContent {
            FiscusTheme(dynamicColor = false) {
                CategoryDetailScreen(
                    uiState = CategoryDetailUiState(
                        categoryId = 1,
                        isLoading = false,
                        category = item,
                    ),
                    onEvent = { events += it },
                    onBack = {},
                    onEdit = {},
                )
            }
        }

        // Archived and unused: Unarchive, not Delete.
        composeRule.onNodeWithText("Unarchive", useUnmergedTree = true).performClick()
        assertEquals(CategoryDetailEvent.ArchiveToggled, events.last())

        item = CategoryWithUsage(food, 0)
        composeRule.onNodeWithText("Delete", useUnmergedTree = true).performClick()
        assertEquals(CategoryDetailEvent.DeleteRequested, events.last())

        item = CategoryWithUsage(food, 2)
        composeRule.onNodeWithText("Archive", useUnmergedTree = true).assertIsDisplayed()
    }
}
