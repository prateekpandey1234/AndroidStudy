package com.prateek.androidstudy

import android.app.Application
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class MyApplication : Application() {
    // This class can be empty.
    // The annotation processor generates the necessary Hilt code.
}