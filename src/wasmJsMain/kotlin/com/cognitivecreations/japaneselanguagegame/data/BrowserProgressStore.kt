package com.cognitivecreations.japaneselanguagegame.data

import kotlinx.browser.localStorage

/**
 * Browser localStorage. The JSON shape matches the desktop progress file.
 */
class BrowserLocalStorage : KeyValueStorage {
    override fun getItem(key: String): String? = localStorage.getItem(key)

    override fun setItem(key: String, value: String) {
        localStorage.setItem(key, value)
    }
}

actual fun createProgressStore(): ProgressStore =
    KeyValueProgressStore(BrowserLocalStorage())
