package com.theultimatenote.app.data

object ErrorMessages {

    fun fromException(e: Exception, fallback: String = "Something went wrong. Please try again."): String {
        val raw = e.message ?: return fallback
        return when {
            raw.contains("PERMISSION_DENIED", ignoreCase = true) ||
                raw.contains("insufficient permissions", ignoreCase = true) ->
                "You don't have permission to perform this action. Try signing out and back in."

            raw.contains("UNAVAILABLE", ignoreCase = true) ||
                raw.contains("network", ignoreCase = true) ||
                raw.contains("unreachable", ignoreCase = true) ||
                raw.contains("connect", ignoreCase = true) ||
                raw.contains("timeout", ignoreCase = true) ->
                "Connection error. Please check your internet and try again."

            raw.contains("UNAUTHENTICATED", ignoreCase = true) ||
                raw.contains("sign in", ignoreCase = true) ||
                raw.contains("re-authenticate", ignoreCase = true) ->
                "Your session has expired. Please sign in again."

            raw.contains("NOT_FOUND", ignoreCase = true) ->
                "The requested item was not found. It may have been deleted."

            raw.contains("ALREADY_EXISTS", ignoreCase = true) ->
                "This item already exists."

            raw.contains("RESOURCE_EXHAUSTED", ignoreCase = true) ||
                raw.contains("quota", ignoreCase = true) ->
                "Too many requests. Please wait a moment and try again."

            raw.contains("CANCELLED", ignoreCase = true) ->
                "The operation was cancelled. Please try again."

            else -> fallback
        }
    }

    fun forAuth(e: Exception): String {
        val raw = e.message ?: return "Something went wrong. Please try again."
        return when {
            raw.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
                raw.contains("password is invalid", ignoreCase = true) ||
                raw.contains("wrong password", ignoreCase = true) ||
                raw.contains("invalid credential", ignoreCase = true) ->
                "Incorrect email or password."

            raw.contains("no user record", ignoreCase = true) ||
                raw.contains("user not found", ignoreCase = true) ||
                raw.contains("no account", ignoreCase = true) ->
                "No account found with this email."

            raw.contains("already in use", ignoreCase = true) ||
                raw.contains("ALREADY_EXISTS", ignoreCase = true) ->
                "An account with this email already exists."

            raw.contains("weak", ignoreCase = true) ->
                "Password is too weak. Use at least 6 characters."

            raw.contains("badly formatted", ignoreCase = true) ||
                raw.contains("invalid email", ignoreCase = true) ->
                "Invalid email format."

            raw.contains("too many requests", ignoreCase = true) ||
                raw.contains("blocked", ignoreCase = true) ->
                "Too many attempts. Please wait a few minutes and try again."

            raw.contains("network", ignoreCase = true) ||
                raw.contains("unreachable", ignoreCase = true) ||
                raw.contains("connect", ignoreCase = true) ||
                raw.contains("timeout", ignoreCase = true) ->
                "Connection error. Please check your internet and try again."

            raw.contains("requires-recent-login", ignoreCase = true) ||
                raw.contains("re-authenticate", ignoreCase = true) ->
                "For security, please sign out and sign back in before trying again."

            raw.contains("disabled", ignoreCase = true) ->
                "This account has been disabled. Please contact support."

            else -> "Something went wrong. Please try again."
        }
    }
}
