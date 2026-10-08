// Modified for Componentory: Document the static fallback and current-OS dynamic palette accurately.
@file:JvmName("ResourcesUtils")

package com.dede.android_eggs.system_colors

import android.content.Context
import android.content.res.Resources
import android.os.Build
import androidx.core.content.ContextCompat
import com.dede.basic.DefType
import com.dede.basic.getIdentifier

/**
 * Resolve the dynamic system color resources (`system_accent1_400` etc.):
 *
 * 1. The retained static palette below API 31.
 * 2. Real framework dynamic colors on API 31 and later.
 *
 * Java callers use [ResourcesUtils.getSystemColor] via the file @JvmName.
 */
@Throws(Resources.NotFoundException::class)
fun Context.getSystemColor(resName: String): Int {
    val id = getIdentifier(resName, DefType.COLOR)
    if (id != 0) {
        return ContextCompat.getColor(this, id)
    }
    val sysId = getIdentifier(resName, DefType.COLOR, "android")
    if (sysId != 0) {
        return ContextCompat.getColor(this, sysId)
    }
    throw Resources.NotFoundException(resName)
}
