package com.theultimatenote.app.util

/** Zero-pads to 2 digits, e.g. 5 -> "05". `String.format` is JVM-only, so this is the KMP-safe equivalent. */
fun Int.pad2(): String = if (this in 0..9) "0$this" else "$this"

fun formatClock(hour: Int, minute: Int): String = "${hour.pad2()}:${minute.pad2()}"
