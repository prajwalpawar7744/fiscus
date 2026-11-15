package com.prajwalpawar.fiscus.model

/** A transaction with the account and category it refers to, for lists. */
data class TransactionListEntry(
    val transaction: Transaction,
    val account: Account?,
    /** Destination account; only for transfers. */
    val toAccount: Account?,
    /** Null for transfers, or when the category was removed. */
    val category: Category?,
)

/** Resolves each transaction's account(s) and category by id; order is kept. */
fun List<Transaction>.toListEntries(
    accounts: List<Account>,
    categories: List<Category>,
): List<TransactionListEntry> {
    val accountsById = accounts.associateBy { it.id }
    val categoriesById = categories.associateBy { it.id }
    return map { transaction ->
        TransactionListEntry(
            transaction = transaction,
            account = accountsById[transaction.accountId],
            toAccount = transaction.toAccountId?.let(accountsById::get),
            category = transaction.categoryId?.let(categoriesById::get),
        )
    }
}
