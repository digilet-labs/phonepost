package app.phonepost

import android.content.Context
import android.preference.PreferenceManager
import com.lacronicus.easydatastorelib.*

interface PreferenceStore {

    companion object {
        fun create(context: Context): PreferenceStore {
            return DatastoreBuilder(PreferenceManager.getDefaultSharedPreferences(context)).create(
                PreferenceStore::class.java
            )
        }

        fun removeAll(context: Context) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().clear().apply()
        }
    }

    @Preference("access_token")
    fun accessToken(): StringEntry

    @Preference("refresh_token")
    fun refreshToken(): StringEntry

    @Preference("user_id")
    fun oldUserId(): IntEntry

    @Preference("new_user_id")
    fun userId(): LongEntry

    @Preference("user_number")
    fun userNumber(): StringEntry

    @Preference("speaker")
    fun isSpeakerOn(): BooleanEntry

    @Preference("maximize")
    fun isMaximized(): BooleanEntry

    @Preference("thread_sync")
    fun isThreadSyncDone(): BooleanEntry

    @Preference("messages_sync")
    fun isMessageSyncDone(): BooleanEntry

//    @Preference("recording_maximize")
//    fun isRecordingMaximized(): BooleanEntry

    @Preference("speed")
    fun isSpeedActivated(): BooleanEntry

    @Preference("update_app")
    fun shouldUpdateApp(): BooleanEntry

    @Preference("invite_message")
    fun inviteMessage(): StringEntry

    @Preference("invite_url")
    fun inviteUrl(): StringEntry

    @Preference("google_credential")
    fun googleCredential(): StringEntry

    @Preference("language")
    fun language(): StringEntry

    @Preference("own_name")
    fun ownName(): StringEntry

    @Preference("profile_pic")
    fun profilePic(): StringEntry

    @Preference("prefered_language")
    fun preferedLanguage(): StringEntry

    @Preference("tooltip_dialer")
    fun isTooltipDialerShown(): BooleanEntry

    @Preference("tooltip_record_message")
    fun isRecordMessageTooltipShown(): BooleanEntry

    @Preference("tooltip_typing_pad_message")
    fun isTypingPadTooltipShown(): BooleanEntry

    @Preference("tooltip_myself_3")
    fun isMyselfTooltipShown(): BooleanEntry

    @Preference("showcase_intro")
    fun isIntroShowcaseShown(): BooleanEntry

    @Preference("showcase_contact")
    fun isContactShowcaseShown(): BooleanEntry

    @Preference("ux_cam_first_time")
    fun isUxCamRecorded(): BooleanEntry

    @Preference("spoken_language")
    fun spokenLanguages(): ObjectEntry<List<String>>

    @Preference("config_updated_at")
    fun configUpdatedAt(): String

    @Preference("maximized_audio_listening")
    fun isMaximizedListening(): BooleanEntry

    @Preference("sounds_start_recording")
    fun soundStartRecording(): BooleanEntry

    @Preference("soundPostSent")
    fun soundPostSent(): BooleanEntry

    @Preference("vibration_start_recording")
    fun vibrationStartRecording(): BooleanEntry

    @Preference("vibration_post_sent")
    fun vibrationPostSent(): BooleanEntry

    @Preference("ivr_call")
    fun ivrCall(): BooleanEntry

    @Preference("ivr_call_confirmation")
    fun ivrCallConfirmation(): BooleanEntry

    @Preference("on_proximity_sensor_change")
    fun onProximitySensorChange(): BooleanEntry

    @Preference("contacts_uploaded")
    fun isContactsUploaded(): BooleanEntry

    @Preference("app_opened_count")
    fun appOpenedCount(): LongEntry

    @Preference("send_via_whatsapp_count")
    fun sendViaWhatsAppCount(): LongEntry

    @Preference("is_audio_message_sent")
    fun isAudioMessageSent(): BooleanEntry

    @Preference("is_myself_opened_first_time")
    fun isChatThreadOpenedFirstTime(): BooleanEntry

    @Preference("myself_dropdown_count")
    fun myselfDropdownCount(): LongEntry

    @Preference("is_my_best_friend_shown")
    fun isMyBestFriendShown(): BooleanEntry

    @Preference("is_my_family_shown")
    fun isMyFamilyShown(): BooleanEntry

    @Preference("onboarding_shown")
    fun onboardingShown(): IntEntry

    @Preference("is_message_sent_with_live_transcript")
    fun isMessageSentWithLiveTranscript(): BooleanEntry

    @Preference("typing_pad_text")
    fun typingPadText():StringEntry
}