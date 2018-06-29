package io.injam.injamsample

import android.app.Application
import com.cedarstudios.cedarmapssdk.CedarMaps

class SampleApp: Application() {
    override fun onCreate() {
        super.onCreate()
        CedarMaps.getInstance()
                .setClientID(Constants.cedarClientId)
                .setClientSecret(Constants.cedarClientSecret)
                .setContext(this)

//        Mapbox.getInstance(this, "pk.eyJ1IjoiYWxpaXoiLCJhIjoiY2ppcWRkdjdtMDZ3dzNwcGlxamxybDIwMSJ9.AGoK9iCCJ870dM-IGlbdpQ")
    }
}