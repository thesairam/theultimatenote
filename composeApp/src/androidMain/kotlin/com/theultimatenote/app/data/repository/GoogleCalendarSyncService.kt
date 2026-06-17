package com.theultimatenote.app.data.repository

import android.content.Context
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.firebase.auth.FirebaseAuth
import com.theultimatenote.app.data.model.Task
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

class GoogleCalendarSyncService(private val context: Context) {

    companion object {
        private const val CALENDAR_SCOPE = "oauth2:https://www.googleapis.com/auth/calendar.events"
        private const val CALENDAR_API = "https://www.googleapis.com/calendar/v3/calendars/primary/events"
    }

    private val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun getAccessToken(): String? {
        val email = FirebaseAuth.getInstance().currentUser?.email ?: return null
        return try {
            val account = android.accounts.Account(email, "com.google")
            GoogleAuthUtil.getToken(context, account, CALENDAR_SCOPE)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createEvent(accessToken: String, task: Task, projectName: String): String? {
        if (task.scheduledTime == null) return null

        val body = buildEventJson(task, projectName)

        return try {
            val response = httpClient.post(CALENDAR_API) {
                bearerAuth(accessToken)
                contentType(ContentType.Application.Json)
                setBody(body)
            }
            val responseText = response.bodyAsText()
            val json = Json.parseToJsonElement(responseText)
            (json as? JsonObject)?.get("id")
                ?.let { (it as? JsonPrimitive)?.content }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateEvent(accessToken: String, eventId: String, task: Task, projectName: String) {
        val body = buildEventJson(task, projectName)

        try {
            httpClient.patch("$CALENDAR_API/$eventId") {
                bearerAuth(accessToken)
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        } catch (_: Exception) {}
    }

    suspend fun deleteEvent(accessToken: String, eventId: String) {
        try {
            httpClient.delete("$CALENDAR_API/$eventId") {
                bearerAuth(accessToken)
            }
        } catch (_: Exception) {}
    }

    private fun buildEventJson(task: Task, projectName: String): String {
        val timeParts = task.scheduledTime?.split(":") ?: return ""
        val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 0
        val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

        val today = java.time.LocalDate.now()
        val startDateTime = "${today}T%02d:%02d:00".format(hour, minute)
        val endHour = if (minute + 30 >= 60) hour + 1 else hour
        val endMinute = (minute + 30) % 60
        val endDateTime = "${today}T%02d:%02d:00".format(endHour, endMinute)

        val tz = java.util.TimeZone.getDefault().id

        return buildJsonObject {
            put("summary", task.title)
            put("description", "Project: $projectName\nFrom: The Ultimate Note")
            putJsonObject("start") {
                put("dateTime", startDateTime)
                put("timeZone", tz)
            }
            putJsonObject("end") {
                put("dateTime", endDateTime)
                put("timeZone", tz)
            }
            if (task.isRecurring) {
                put("recurrence", kotlinx.serialization.json.JsonArray(listOf(
                    JsonPrimitive("RRULE:FREQ=DAILY")
                )))
            }
        }.toString()
    }
}
