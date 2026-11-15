package com.prajwalpawar.fiscus.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Shared: shows a currency [symbol] in front of the typed digits of an amount field. It's display
 * only (Compose's public [VisualTransformation] API), so the field's value stays plain digits for
 * `parseMoneyInput`. Unlike a `TextField` `prefix`, the symbol moves with the text, so it also works
 * with centered text. Nothing is added while the field is empty, so a placeholder can show instead.
 */
class CurrencyPrefixTransformation(private val symbol: String) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        if (text.isEmpty() || symbol.isEmpty()) return TransformedText(text, OffsetMapping.Identity)
        val offset = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = offset + symbol.length
            override fun transformedToOriginal(offset: Int): Int =
                (offset - symbol.length).coerceIn(0, text.length)
        }
        return TransformedText(AnnotatedString(symbol) + text, offset)
    }

    override fun equals(other: Any?): Boolean = other is CurrencyPrefixTransformation && other.symbol == symbol

    override fun hashCode(): Int = symbol.hashCode()
}
