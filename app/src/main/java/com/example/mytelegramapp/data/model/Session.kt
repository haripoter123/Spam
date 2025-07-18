package com.example.mytelegramapp.data.model

enum class VerificationStatus {
    NOT_VERIFIED, VERIFYING, VALID, INVALID
}

data class Session(
    val fileName: String,
    var status: VerificationStatus = VerificationStatus.NOT_VERIFIED
)
