package com.prajwalpawar.fiscus.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ContainsLikePatternTest {

    @Test
    fun `blank query means no search`() {
        assertNull(containsLikePattern(""))
        assertNull(containsLikePattern("   "))
    }

    @Test
    fun `wraps the trimmed query in wildcards`() {
        assertEquals("%lunch%", containsLikePattern("  lunch "))
    }

    @Test
    fun `escapes LIKE wildcards and the escape character`() {
        assertEquals("%50\\% off\\_now\\\\%", containsLikePattern("50% off_now\\"))
    }
}
