package app.phonepost

import android.content.Context
import com.moe.pushlibrary.MoEHelper
import com.moe.pushlibrary.PayloadBuilder

class MoengageUtil {
    companion object {

        // Typing pad properties
        const val PROP_VOICE_APP_LANGUAGE = "Language"
        const val PROP_WHATSAPP_DIRECT_NUMBER = "Number"
        const val PROP_WHATSAPP_DIRECT_STATUS = "Status"
        const val PROP_WORD_COUNT = "Word Count"
        // Permission properties
        val PROP_PERMISSION_GRANTED = "Permission Granted"
        val PROP_PERMISSION_DENIED = "Permission DENIED"

        const val EVENT_VOICE_APP_OPEN = "All Posts - Swipe to Voice App"
        const val EVENT_VOICE_APP_CLOSE = "Voice App - Swipe to All Posts"
        const val EVENT_VOICE_APP_RECORD = "Voice App - Record"
        const val EVENT_VOICE_APP_LANGUAGE = "Voice App - Language"
        const val EVENT_VOICE_APP_CANCEL = "Voice App - Cancel"
        const val EVENT_VOICE_APP_DONE = "Voice App - Done"
        const val EVENT_VOICE_APP_COPY = "Voice App - Copy"
        const val EVENT_VOICE_APP_WHATSAPP = "Voice App - WhatsApp"
        const val EVENT_VOICE_APP_WHATSAPP_DIRECT = "Voice App - WhatsApp Direct"
        const val EVENT_VOICE_APP_PHONEPOST = "Voice App - PhonePost"
        const val EVENT_VOICE_APP_SHARE = "Voice App - Share"
        const val EVENT_VOICE_APP_CLEAR = "Voice App - Clear"
        const val EVENT_VOICE_APP_COMMA = "Voice App - Comma"
        const val EVENT_VOICE_APP_FULLSTOP = "Voice App - Fullstop"
        const val EVENT_VOICE_APP_SPACE = "Voice App - Space"
        const val EVENT_VOICE_APP_BACKSPACE = "Voice App - Backspace"
        const val EVENT_VOICE_APP_ENTER = "Voice App - Enter"
        const val EVENT_VOICE_APP_KEYBOARD = "Voice App - Keyboard"
        const val EVENT_VOICE_APP_TEXTBOX = "Voice App - TextBox"
        const val EVENT_VOICE_APP_EDIT = "Voice App - Edit"
        const val EVENT_VOICE_APP_AUDIO_PERMISSION = "Voice App - Audio Permissions"
        const val EVENT_VOICE_APP_CLICK_PAD = "Voice App - Click Pad"

        fun sendMoEngageData(context: Context, type: String, vararg args: Pair<String, *>) {
            val builder = PayloadBuilder()
            for (arg in args) {
                if(arg.second is String)
                    builder.putAttrString(arg.first, arg.second as String)
                else if(arg.second is Long)
                    builder.putAttrLong(arg.first, arg.second as Long)
                else if(arg.second is Int)
                    builder.putAttrInt(arg.first, arg.second as Int)
                else if(arg.second is Boolean)
                    builder.putAttrBoolean(arg.first, arg.second as Boolean)
                else if(arg.second is Double)
                    builder.putAttrDouble(arg.first, arg.second as Double)
            }
            MoEHelper.getInstance(context)
                .trackEvent(type, builder)
        }
    }
}