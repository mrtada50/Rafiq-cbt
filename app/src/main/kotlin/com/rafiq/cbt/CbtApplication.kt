package com.rafiq.cbt

import android.app.Application
import com.rafiq.cbt.data.AppDatabase

class CbtApplication : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
}
