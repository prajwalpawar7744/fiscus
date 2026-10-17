package com.prajwalpawar.fiscus.data.model

// How much went to (or came from) one category over a period, in hundredths.
data class CategoryTotal(
    // Null for the folded "Other" bucket.
    val categoryId: Long?,
    val name: String,
    val iconKey: String,
    val total: Long
)
