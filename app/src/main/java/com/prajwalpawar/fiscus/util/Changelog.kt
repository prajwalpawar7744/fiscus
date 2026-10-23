package com.prajwalpawar.fiscus.util

// CHANGELOG.md (Keep a Changelog), parsed for the in-app "What's new" page.
data class ChangelogRelease(
    // "0.1.0-beta.4"
    val version: String,
    // "2026-10-23", as written; null if missing.
    val date: String?,
    val groups: List<ChangelogGroup>
)

// "Added", "Changed", "Fixed", "Known limitations" …
data class ChangelogGroup(
    val title: String,
    val entries: List<ChangelogEntry>
)

sealed interface ChangelogEntry {
    // A bold line on its own, grouping the bullets under it ("**Home**").
    data class Heading(val text: String) : ChangelogEntry

    // A bullet; [level] 0 for top level, 1 for a nested bullet. Text keeps its **bold**.
    data class Bullet(val text: String, val level: Int) : ChangelogEntry
}

// Newest release first, as the file is written. Lines before the first release (the
// intro), the [Unreleased] section (not shipped yet) and the link references at the
// bottom are skipped; wrapped bullet lines are joined back onto their bullet.
fun parseChangelog(markdown: String): List<ChangelogRelease> {
    val releases = mutableListOf<ChangelogRelease>()
    var version: String? = null
    var date: String? = null
    val groups = mutableListOf<ChangelogGroup>()
    var groupTitle: String? = null
    val entries = mutableListOf<ChangelogEntry>()

    fun closeGroup() {
        val title = groupTitle ?: return
        if (entries.isNotEmpty()) groups += ChangelogGroup(title, entries.toList())
        entries.clear()
        groupTitle = null
    }

    fun closeRelease() {
        closeGroup()
        val current = version ?: return
        if (!current.equals("Unreleased", ignoreCase = true)) {
            releases += ChangelogRelease(current, date, groups.toList())
        }
        groups.clear()
        version = null
        date = null
    }

    for (raw in markdown.lines()) {
        val line = raw.trimEnd()
        val trimmed = line.trimStart()
        when {
            line.startsWith("## ") -> {
                closeRelease()
                val heading = line.removePrefix("## ").trim()
                version = heading.substringAfter("[").substringBefore("]").ifBlank { heading }
                date = heading.substringAfter(" - ", "").trim().ifBlank { null }
            }

            version == null -> Unit

            // Link references ("[0.1.0]: https://…") end the content.
            Regex("""^\[[^\]]+]:\s""").containsMatchIn(line) -> closeRelease()

            line.startsWith("### ") -> {
                closeGroup()
                groupTitle = line.removePrefix("### ").trim()
            }

            trimmed.startsWith("- ") -> {
                val indent = line.length - trimmed.length
                entries += ChangelogEntry.Bullet(
                    text = trimmed.removePrefix("- ").trim(),
                    level = if (indent >= 2) 1 else 0
                )
            }

            trimmed.startsWith("**") && trimmed.endsWith("**") && trimmed.length > 4 ->
                entries += ChangelogEntry.Heading(trimmed.removeSurrounding("**"))

            trimmed.isNotEmpty() && line.first().isWhitespace() -> {
                // A wrapped line belongs to the bullet above it.
                val last = entries.lastOrNull()
                if (last is ChangelogEntry.Bullet) {
                    entries[entries.lastIndex] = last.copy(text = "${last.text} $trimmed")
                }
            }
        }
    }
    closeRelease()
    return releases
}
