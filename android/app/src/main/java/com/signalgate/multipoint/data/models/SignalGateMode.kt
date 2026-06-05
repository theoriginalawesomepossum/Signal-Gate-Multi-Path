package com.signalgate.multipoint.data.models

enum class SignalGateMode(
    val title: String, 
    val label: String, 
    val description: String
) {
    FULL_THROTTLE("Full Throttle", "MAX", "Evaluates all layers concurrently. Max threat mitigation."),
    MULTI_GATE("Multi-Gate", "BALANCED", "Tiered routing pipeline. Optimized for performance and power."),
    LPP("Least Ports Possible", "LPP", "Minimalist footprint. Suspends heavy remote lookups.")
}