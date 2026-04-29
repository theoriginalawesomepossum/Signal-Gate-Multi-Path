package com.signalgate.multipoint.utils

object PhoneNumberUtils {
    fun normalizePhoneNumber(phoneNumber: String): String {
        // Remove all non-digit characters except for a leading plus sign
        val digitsOnly = phoneNumber.replace(Regex("[^\\d+]"), "")

        // If it starts with '+', keep it. Otherwise, assume US format for simplicity
        // and add '+1' if it's a 10-digit number without a country code.
        if (digitsOnly.startsWith("+")) {
            return digitsOnly
        } else if (digitsOnly.length == 10) {
            return "+1" + digitsOnly
        }
        // For other cases, return as is or implement more complex logic
        return digitsOnly
    }
}
