package com.prajwalpawar.fiscus.ui.screens.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import com.prajwalpawar.fiscus.data.model.TransactionType
import com.prajwalpawar.fiscus.ui.theme.FiscusSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden
    )
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier
    ) {
        AddTransactionContent(
            onDismissRequest = onDismissRequest
        )
    }
}

@Composable
private fun AddTransactionContent(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var transactionType by remember {
        mutableStateOf(TransactionType.EXPENSE)
    }

    var amount by remember {
        mutableStateOf("")
    }

    var note by remember {
        mutableStateOf("")
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .imePadding()
            .padding(
                horizontal = FiscusSpacing.md,
                vertical = FiscusSpacing.sm
            ),
        verticalArrangement = Arrangement.spacedBy(FiscusSpacing.md)
    ) {
        Text(
            text = "Add transaction",
            style = MaterialTheme.typography.headlineSmall
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(FiscusSpacing.xs)
        ) {
            Text(
                text = "Amount",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { value ->
                    amount = value
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                prefix = {
                    Text(
                        text = "$"
                    )
                },
                placeholder = {
                    Text(
                        text = "0.00"
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                )
            )
        }

        FlowRow(
            Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                ButtonGroupDefaults.ConnectedSpaceBetween
            ),
            verticalArrangement = Arrangement.spacedBy(FiscusSpacing.md)
        ) {
            TransactionType.entries.forEachIndexed { index, type ->
                ToggleButton(
                    checked = transactionType == type,
                    onCheckedChange = {
                        transactionType = type
                    },
                    shapes = when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()

                        TransactionType.entries.lastIndex ->
                            ButtonGroupDefaults.connectedTrailingButtonShapes()

                        else ->
                            ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .semantics {
                        role = Role.RadioButton
                    }
                ) {
                    Text(
                        text = type.name
                    )
                }
            }
        }

        OutlinedTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            label = {
                Text("Category")
            },
            placeholder = {
                Text("Choose a category")
            },
            trailingIcon = {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.KeyboardArrowDown,
                    contentDescription = "Choose category"
                )
            }
        )

        OutlinedTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            label = {
                Text("Account")
            },
            placeholder = {
                Text("Choose an account")
            },
            trailingIcon = {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.KeyboardArrowDown,
                    contentDescription = "Choose account"
                )
            }
        )

        OutlinedTextField(
            value = note,
            onValueChange = {
                note = it
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Note")
            },
            placeholder = {
                Text("Optional")
            },
            minLines = 2,
            maxLines = 4
        )

        Button(
            onClick = {
                // Transaction creation will be connected later.
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Add transaction"
            )
        }
    }
}