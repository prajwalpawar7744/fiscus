package com.prajwalpawar.fiscus.ui.feature.categories

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.prajwalpawar.fiscus.model.Category
import com.prajwalpawar.fiscus.model.CategoryKind
import com.prajwalpawar.fiscus.model.CategoryWithUsage
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CategoriesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val food = Category(1, "Food", CategoryKind.EXPENSE, "restaurant", isSeeded = true)
    private val gym = Category(2, "Gym", CategoryKind.EXPENSE, "fitness_center", archived = true)

    @Test
    fun showsUsageAndOpensAndAdds() {
        var opened: Long? = null
        var added = false
        var selected: CategoryKind? = null
        composeRule.setContent {
            FiscusTheme(dynamicColor = false) {
                CategoriesScreen(
                    uiState = CategoriesUiState(
                        isLoading = false,
                        active = listOf(CategoryWithUsage(food, 3)),
                        archived = listOf(CategoryWithUsage(gym, 1)),
                    ),
                    onEvent = { if (it is CategoriesEvent.KindSelected) selected = it.kind },
                    onBack = {},
                    onOpenCategory = { opened = it },
                    onAddCategory = { added = true },
                )
            }
        }

        composeRule.onNodeWithText("Expense · Default · 3 transactions", useUnmergedTree = true)
            .assertIsDisplayed()
        composeRule.onNodeWithText("Expense · Custom · 1 transaction", useUnmergedTree = true)
            .assertIsDisplayed()
        composeRule.onNodeWithText("Food", useUnmergedTree = true).performClick()
        assertEquals(1L, opened)
        composeRule.onNodeWithText("Income", useUnmergedTree = true).performClick()
        assertEquals(CategoryKind.INCOME, selected)
        composeRule.onNodeWithText("Add category", useUnmergedTree = true).performClick()
        assertTrue(added)
    }
}
