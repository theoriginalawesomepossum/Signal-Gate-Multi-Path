package com.signalgate.multipoint.utils

object PhoneNumberUtils {

    /**
     * Formats a phone number string for display.
     *
     * Rules:
     *  - Bare 10-digit numbers          → (NXX) NXX-XXXX
     *  - +1 prefixed 11-digit numbers   → +1 (NXX) NXX-XXXX
     *  - Other 11-digit numbers         → +CC (NXX) NXX-XXXX
     *  - Anything else                  → returned as-is
     */
    fun formatPhoneNumberForDisplay(phoneNumber: String): String {
        if (phoneNumber.isEmpty()) return ""

        return try {
            val hasPlus = phoneNumber.trimStart().startsWith("+")
            val digits = phoneNumber.replace(Regex("[^0-9]"), "")

            when {
                digits.length == 10 ->
                    formatLocalNumber(digits)

                digits.length == 11 && hasPlus ->
                    "+${digits[0]} ${formatLocalNumber(digits.substring(1))}"

                digits.length == 11 ->
                    formatLocalNumber(digits.substring(1))

                else -> phoneNumber
            }
        } catch (e: Exception) {
            phoneNumber
        }
    }

    /**
     * Formats a 10-digit string as (NXX) NXX-XXXX.
     */
    private fun formatLocalNumber(digits: String): String {
        require(digits.length == 10) { "Expected 10 digits, got ${digits.length}" }
        return "(${digits.substring(0, 3)}) ${digits.substring(3, 6)}-${digits.substring(6)}"
    }

    /**
     * Normalizes a phone number by stripping all formatting characters,
     * preserving a leading '+' if present.
     *
     * Examples:
     *  "+1-800-555-1212"  → "+18005551212"
     *  "(310) 555-1212"   → "3105551212"
     */
    fun normalizePhoneNumber(phoneNumber: String): String {
        return phoneNumber.replace(Regex("[^0-9+]"), "")
    }
}
