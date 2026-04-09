package me.ligaram.app

import android.app.Application
import com.google.android.gms.ads.MobileAds

class LigaramApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)
        // Firebase Crashlytics e Analytics iniciam automaticamente via google-services plugin
    }
}
