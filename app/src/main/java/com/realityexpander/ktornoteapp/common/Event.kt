package com.realityexpander.ktornoteapp.common

open class Event<out T>(private val content: T) {
    var hasBeenHandled = false
        private set // Allow external read but not write

    // For error handling/display, in order to show content only once.
    // And also prevents processing content after configuration change. (ie: an error message)
    fun getContentOnlyOnce(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    fun peekContent(): T = content
}