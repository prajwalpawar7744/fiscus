package com.prajwalpawar.fiscus.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ChangelogTest {
    private val sample = """
        # Changelog

        Intro text that isn't shown.

        ## [Unreleased]

        ### Added

        - Not shipped yet.

        ## [0.1.0-beta.2] - 2026-10-19

        ### Added

        - **Profile**: add your name and a photo,
          which stays on your phone.
        - Smoother motion:
          - Tabs fade.

        ### Known limitations

        - English only.

        ## [0.1.0-beta.1] - 2026-10-18

        ### Added

        **Home**
        - Total balance.

        [0.1.0-beta.2]: https://example.com/compare
        [0.1.0-beta.1]: https://example.com/tag
    """.trimIndent()

    @Test
    fun readsReleasesNewestFirstWithDatesSkippingUnreleased() {
        val releases = parseChangelog(sample)
        assertEquals(listOf("0.1.0-beta.2", "0.1.0-beta.1"), releases.map { it.version })
        assertEquals("2026-10-19", releases[0].date)
    }

    @Test
    fun joinsWrappedLinesAndKeepsNesting() {
        val added = parseChangelog(sample)[0].groups[0]
        assertEquals("Added", added.title)
        assertEquals(
            listOf(
                ChangelogEntry.Bullet("**Profile**: add your name and a photo, which stays on your phone.", 0),
                ChangelogEntry.Bullet("Smoother motion:", 0),
                ChangelogEntry.Bullet("Tabs fade.", 1)
            ),
            added.entries
        )
    }

    @Test
    fun readsBoldHeadingsAndSkipsIntroAndLinks() {
        val releases = parseChangelog(sample)
        assertEquals(
            listOf(ChangelogEntry.Heading("Home"), ChangelogEntry.Bullet("Total balance.", 0)),
            releases[1].groups[0].entries
        )
        assertEquals(listOf("Added", "Known limitations"), releases[0].groups.map { it.title })
    }
}
