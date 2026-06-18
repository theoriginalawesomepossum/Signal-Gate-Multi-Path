package com.signalgate.multipoint.data.sources

/**
 * Curated catalog of vetted, reliable data sources for spam/scam number lists.
 * Each entry maps to a SourceEntity when the user opts in during onboarding
 * or via Settings. URLs point to publicly maintained, machine-readable feeds.
 *
 * Trust tiers:
 *  - GOVERNMENT: Official US federal sources (FTC, FCC)
 *  - COMMUNITY_VETTED: Actively maintained, widely used open-source block lists
 *  - COMMUNITY_FUTURE: Placeholder for SignalGate's own community repo (not yet live)
 */
object TrustedSourceCatalog {

    enum class TrustTier {
        GOVERNMENT,
        COMMUNITY_VETTED,
        COMMUNITY_FUTURE
    }

    data class TrustedSource(
        val name: String,
        val description: String,
        val url: String,
        val type: String, // matches SourceEntity.type: "CSV", "URL"
        val tier: TrustTier,
        val defaultPriority: Int,
        val defaultEnabled: Boolean
    )

    val sources: List<TrustedSource> = listOf(
        TrustedSource(
            name = "FTC Do Not Call Complaints",
            description = "Numbers reported to the Federal Trade Commission's Do Not Call registry complaint database.",
            url = "https://www.ftc.gov/system/files/ftc_gov/data/do-not-call-data.csv",
            type = "URL",
            tier = TrustTier.GOVERNMENT,
            defaultPriority = 90,
            defaultEnabled = true
        ),
        TrustedSource(
            name = "FCC Robocall Complaint Data",
            description = "Consumer-reported robocall and unwanted call complaints filed with the Federal Communications Commission.",
            url = "https://opendata.fcc.gov/api/views/robocall-complaints/rows.csv",
            type = "URL",
            tier = TrustTier.GOVERNMENT,
            defaultPriority = 90,
            defaultEnabled = true
        ),
        TrustedSource(
            name = "Should I Answer Community List",
            description = "Community-maintained spam number database, actively updated on GitHub.",
            url = "https://raw.githubusercontent.com/shouldianswer/spam-numbers/main/numbers.csv",
            type = "URL",
            tier = TrustTier.COMMUNITY_VETTED,
            defaultPriority = 60,
            defaultEnabled = false
        ),
        TrustedSource(
            name = "SignalGate Community Repo",
            description = "SignalGate's own community-contributed block list. Coming soon.",
            url = "",
            type = "URL",
            tier = TrustTier.COMMUNITY_FUTURE,
            defaultPriority = 50,
            defaultEnabled = false
        )
    )

    fun sourcesByTier(tier: TrustTier): List<TrustedSource> =
        sources.filter { it.tier == tier }
}
