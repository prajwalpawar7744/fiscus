package com.prajwalpawar.fiscus.data.report

import com.prajwalpawar.fiscus.domain.model.Account
import com.prajwalpawar.fiscus.domain.model.Category
import com.prajwalpawar.fiscus.domain.model.Transaction
import com.prajwalpawar.fiscus.domain.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportManager @Inject constructor() {
    
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun generateCsvReport(
        transactions: List<Transaction>,
        categories: Map<Long, Category>,
        accounts: Map<Long, Account>
    ): String {
        val builder = StringBuilder()
        
        // CSV Header
        builder.append("Date,Title,Amount,Type,Category,Account,Note\n")
        
        transactions.forEach { transaction ->
            val dateStr = dateFormatter.format(transaction.date)
            val title = escapeCsv(transaction.title)
            val amount = transaction.amount
            val type = transaction.type.name
            val category = escapeCsv(categories[transaction.categoryId]?.name ?: "Unknown")
            val account = escapeCsv(accounts[transaction.accountId]?.name ?: "Unknown")
            val note = escapeCsv(transaction.note)
            
            builder.append("$dateStr,$title,$amount,$type,$category,$account,$note\n")
        }
        
        return builder.toString()
    }

    private fun escapeCsv(value: String): String {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"${value.replace("\"", "\"\"")}\""
        }
        return value
    }
}
