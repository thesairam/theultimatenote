package com.theultimatenote.app.data.repository

import kotlin.random.Random

private const val ID_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

/** GitLive's Firestore `document()` requires an explicit id (unlike the Android SDK's auto-ID overload). */
fun randomFirestoreId(): String = (1..20).map { ID_CHARS[Random.nextInt(ID_CHARS.length)] }.joinToString("")
