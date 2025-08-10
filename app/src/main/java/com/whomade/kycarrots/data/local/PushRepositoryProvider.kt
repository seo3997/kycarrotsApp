// com.whomade.kycarrots.data.local.PushRepositoryProvider.kt
package com.whomade.kycarrots.data.local

import android.content.Context

object PushRepositoryProvider {
    fun get(context: Context): PushRepository =
        PushRepository(AppDatabase.get(context).pushDao())
}
