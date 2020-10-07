package app.phonepost

import android.app.Application
import android.os.Looper
import com.moengage.core.MoEngage

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initMoEngage()
    }

    private fun initMoEngage(){
        val moEngage = MoEngage.Builder(this, "MOENGAGE KEY HERE")
            .setSenderId("phonepost-ffc39")
            .setNotificationSmallIcon(R.mipmap.ic_launcher)
            .setNotificationLargeIcon(R.mipmap.ic_launcher)
            .build()
        MoEngage.initialise(moEngage)
    }

    /*fun getSpeechToTextCredentials(): GoogleCredentials {
        val stream: InputStream = "".byteInputStream()//( //File("").inputStream()
        val credentials = GoogleCredentials.fromStream(stream)
        val token = credentials.refreshAccessToken()
        val creds = GoogleCredentials.create(token)
        return creds
    }*/

}
