package com.signalgate.multipoint

import com.signalgate.multipoint.db.UnifiedEntry
import com.signalgate.multipoint.db.Source
import org.junit.Assert.assertEquals
import org.junit.Test

class CallScreeningServiceTest {

    // Mocking the priority logic for unit testing
    private fun mockCheckBlockingLogic(
        normalizedNumber: String,
        matches: List<UnifiedEntry>,
        enabledSources: Map<Int, Source>,
        patterns: List<UnifiedEntry>
    ): String {
        // Priority 1: Manual Allow
        if (matches.any { it.sourceId == 0 && it.action == "ALLOW" }) return "ALLOW: Manual Allow"

        // Priority 2: Manual Block
        if (matches.any { it.sourceId == 0 && it.action == "BLOCK" }) return "BLOCK: Manual Block"

        // Priority 3: Patterns
        for (pattern in patterns) {
            if (normalizedNumber.startsWith(pattern.phoneNumber)) {
                return if (pattern.action == "ALLOW") "ALLOW: Pattern" else "BLOCK: Pattern"
            }
        }

        // Priority 4: Hub Sources
        val hubMatches = matches
            .filter { it.sourceId != 0 && enabledSources.containsKey(it.sourceId) }
            .sortedBy { enabledSources[it.sourceId]?.priority ?: 999 }

        if (hubMatches.isNotEmpty()) {
            val best = hubMatches.first()
            return if (best.action == "ALLOW") "ALLOW: Hub" else "BLOCK: Hub"
        }

        return "ALLOW: Default"
    }

    @Test
    fun `manual allow overrides hub block`() {
        val matches = listOf(
            UnifiedEntry(phoneNumber = "123", action = "ALLOW", sourceId = 0), // Manual Allow
            UnifiedEntry(phoneNumber = "123", action = "BLOCK", sourceId = 1)  // Hub Block
        )
        val sources = mapOf(1 to Source(id = 1, name = "Hub", type = "URL", pathOrUrl = "", priority = 1))
        
        val result = mockCheckBlockingLogic("123", matches, sources, emptyList())
        assertEquals("ALLOW: Manual Allow", result)
    }

    @Test
    fun `hub priority respects sorting`() {
        val matches = listOf(
            UnifiedEntry(phoneNumber = "123", action = "BLOCK", sourceId = 2), // Priority 10
            UnifiedEntry(phoneNumber = "123", action = "ALLOW", sourceId = 1)  // Priority 5
        )
        val sources = mapOf(
            1 to Source(id = 1, name = "High Priority", type = "URL", pathOrUrl = "", priority = 5),
            2 to Source(id = 2, name = "Low Priority", type = "URL", pathOrUrl = "", priority = 10)
        )
        
        val result = mockCheckBlockingLogic("123", matches, sources, emptyList())
        assertEquals("ALLOW: Hub", result) // Priority 5 wins
    }
}
