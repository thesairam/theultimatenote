package com.theultimatenote.app

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import java.lang.ref.WeakReference

@SuppressLint("StaticFieldLeak")
object AndroidAppContext {
    lateinit var context: Context
        private set

    private var activityRef: WeakReference<Activity>? = null

    val currentActivity: Activity?
        get() = activityRef?.get()

    fun init(context: Context) {
        this.context = context.applicationContext
        if (context is Activity) {
            activityRef = WeakReference(context)
        }
    }

    fun setActivity(activity: Activity?) {
        activityRef = activity?.let { WeakReference(it) }
    }
}
