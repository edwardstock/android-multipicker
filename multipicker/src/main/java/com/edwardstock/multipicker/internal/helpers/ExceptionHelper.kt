package com.edwardstock.multipicker.internal.helpers

import timber.log.Timber
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Dogsy. 2017
 * @author Eduard Maximovich <edward.vstock></edward.vstock>@gmail.com>
 */
object ExceptionHelper {
    fun getStackTrace(t: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        t.printStackTrace(pw)
        return sw.toString()
    }

    @JvmOverloads
    fun doubleTryOOM(action: () -> Unit, doActionName: String? = "<unknown>") {
        doubleTryOOM(action, null, doActionName)
    }

    @JvmStatic
    fun doubleTryOOM(action: () -> Unit, onError: ((OutOfMemoryError) -> Unit)?, doActionName: String? = null) {
        try {
            action()
        } catch (e: OutOfMemoryError) {
            System.gc()
            try {
                action()
            } catch (e2: OutOfMemoryError) {
                Timber.w(e2, "Not enough memory (%s): %s", e2.message, doActionName)
                if (onError != null) {
                    try {
                        onError(e2)
                    } catch (e1: Exception) {
                        e1.printStackTrace()
                    }
                }
            }
        }
    }
}