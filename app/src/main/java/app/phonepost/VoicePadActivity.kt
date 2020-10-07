package app.phonepost

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.graphics.Rect
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
//import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import app.phonepost.MoengageUtil.Companion.PROP_VOICE_APP_LANGUAGE
import app.phonepost.MoengageUtil.Companion.PROP_WHATSAPP_DIRECT_NUMBER
import app.phonepost.MoengageUtil.Companion.PROP_WHATSAPP_DIRECT_STATUS
import app.phonepost.MoengageUtil.Companion.PROP_WORD_COUNT
import app.phonepost.MoengageUtil.Companion.sendMoEngageData
/*import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings*/
import app.phonepost.databinding.ActivityMainBinding
import java.net.URLEncoder
import java.util.*
import kotlin.collections.ArrayList

class VoicePadActivity : AppCompatActivity(),PermissionResultCallback,
        LanguagePickerDialogFragment.Listener, TextWatcher,WhatsappDirectBottomSheet.Listener {

    lateinit var binding: ActivityMainBinding
    lateinit var fileName: String
    var cursorPosStart: Int = 0
    var cursorPosEnd: Int = 0
    val TAG = "Pad"
    val VIBRATION_DURATION = 50L
    var isCanceled = false
    var isPaused = false
    var isRecording = false
    var isKeyboardVisible: Boolean = false
    lateinit var userName: String
    //  lateinit var prefs: PreferenceStore
    lateinit var preferedLanguage: String
    var countOnResults: Int = 0

    private var isAudioPermissionGranted = false
    private val permissions = java.util.ArrayList<String>()
    private lateinit var permissionsUtil: PermissionUtils

    var isTranscriptError = false
    var transcription: String? = null
    var transcriptFinalMsg: SpannableStringBuilder = SpannableStringBuilder("")

    private var timer: CountDownTimer? = null

    private lateinit var audioManagerService: AudioManager
    //  lateinit var remoteConfig: FirebaseRemoteConfig
    private var languagePickerFragment: LanguagePickerDialogFragment? = null

    private lateinit var mSpeechRecoginizerHelper: SpeechRecoginizerHelper
    var numberOfTimesRecordButtonClicked: Int = 0
    var isActionUp: Boolean = true
    lateinit var onLongBackPressClick: Thread
    lateinit var prefs: PreferenceStore


    companion object {
        const val AUDIO_PERMISSIONS_STATUS_CODE = 109
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        //setWindowActionBar(false)
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.title = ""
        // supportActionBar!!.elevation = 4.dp.toFloat()
        prefs = PreferenceStore.create(this.applicationContext)
        //  preferedLanguage = prefs.spokenLanguages().get().get(0)
        preferedLanguage = "en-In"
        audioManagerService = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        getRemoteConfigInstance()

        binding.languageButton.setOnClickListener {
            val intent = Intent(this, MyLanguagesActivity::class.java)
            startActivity(intent)
        }

        binding.record.setOnClickListener {
            numberOfTimesRecordButtonClicked++
            sendMoEngageData(
                this,
                MoengageUtil.EVENT_VOICE_APP_RECORD
            )
            getPermissions()
        }

        binding.record2.setOnClickListener {
            numberOfTimesRecordButtonClicked++
            countOnResults = 0
            sendMoEngageData(
                this,
                MoengageUtil.EVENT_VOICE_APP_RECORD
            )
            getPermissions()
        }

        binding.record.setOnLongClickListener {
            numberOfTimesRecordButtonClicked++
            countOnResults = 0
            sendMoEngageData(
                this,
                MoengageUtil.EVENT_VOICE_APP_RECORD
            )
            getPermissions()
            true
        }

        binding.recordPause.setOnClickListener {
            sendMoEngageData(
                this,
                MoengageUtil.EVENT_VOICE_APP_DONE
            )
            isCanceled = false
            pauseRecording()
        }

        binding.recordCancel.setOnClickListener {
            sendMoEngageData(
                this,
                MoengageUtil.EVENT_VOICE_APP_CANCEL
            )
            isCanceled = true
            isPaused = false
            isRecording = false
            mSpeechRecoginizerHelper.stopSpeech()
            binding.recordCancel.visibility = View.GONE
            binding.recordingView.visibility = View.GONE
            binding.recordPause.visibility = View.GONE
            binding.recordingTv.text = "Cancelling..."
        }

        binding.optionCopy.setOnClickListener {
            sendMoEngageData(
                this,
                MoengageUtil.EVENT_VOICE_APP_COPY,
                Pair(PROP_WORD_COUNT, calcWords(transcriptFinalMsg.toString()))
            )
            if (transcriptFinalMsg.isEmpty()) {
                showToastMsgForSelectedOptions("Cannot copy an empty message")
                return@setOnClickListener
            }
            /* val clipboard =
                     this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
             val clip = ClipData.newPlainText("label", transcriptFinalMsg.toString())
             clipboard.primaryClip = clip*/
            showToastMsgForSelectedOptions("Message copied successfully")
        }

        binding.optionWhatsapp.setOnClickListener {
            sendMoEngageData(
                this,
                MoengageUtil.EVENT_VOICE_APP_WHATSAPP,
                Pair(PROP_WORD_COUNT, calcWords(transcriptFinalMsg.toString()))
            )
            if (transcriptFinalMsg.isEmpty()) {
                showToastMsgForSelectedOptions("Cannot share an empty message")
                return@setOnClickListener
            }
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_TEXT, transcriptFinalMsg.toString())
            intent.type = "text/plain"
            intent.setPackage("com.whatsapp")
            try {
                startActivity(intent)
                showToastMsgForSelectedOptions("Opening WhatsApp")
            } catch (ex: ActivityNotFoundException) {
                showToastMsgForSelectedOptions("WhatsApp isn't installed on your device")
            }
        }

        binding.optionWhatsappDirect.setOnClickListener {
            if (transcriptFinalMsg.isEmpty()) {
                showToastMsgForSelectedOptions("Cannot share an empty message")
                return@setOnClickListener
            }
            openWhatsappDirectBottomSheet()
        }

        /*binding.optionPhonepost.setOnClickListener {

            if (transcriptFinalMsg.isEmpty()) {
                showToastMsgForSelectedOptions("Cannot post an empty message")
                return@setOnClickListener
            }

            showToastMsgForSelectedOptions("Sending on PhonePost")
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_TEXT, transcriptFinalMsg.toString())
            intent.type = "text/plain"
            intent.setPackage("app.phonepost")
            try {
                startActivity(intent)
            } catch (ex: Exception) {
            }
        }*/

        binding.optionShare.setOnClickListener {
            sendMoEngageData(
                this,
                MoengageUtil.EVENT_VOICE_APP_SHARE,
                Pair(PROP_WORD_COUNT, calcWords(transcriptFinalMsg.toString()))
            )
            if (transcriptFinalMsg.isEmpty()) {
                showToastMsgForSelectedOptions("Cannot share an empty message")
                return@setOnClickListener
            }

            showToastMsgForSelectedOptions("Opening share options")

            val intent = Intent()
            intent.action = Intent.ACTION_SEND
//            intent.putExtra(Intent.EXTRA_SUBJECT, "Phonepost's Voice Typing Pad")
            intent.putExtra(Intent.EXTRA_TEXT, transcriptFinalMsg.toString())
            intent.type = "text/plain"
            startActivity(Intent.createChooser(intent, "Share this message"))
        }

        binding.clear.setOnClickListener {
            sendMoEngageData(
                this,
                MoengageUtil.EVENT_VOICE_APP_CLEAR
            )
            if (transcriptFinalMsg.isEmpty()) {
                showToastMsgForSelectedOptions("Nothing to clear")
                return@setOnClickListener
            } else showToastMsgForSelectedOptions("cleared")

            resetTypingPad()
        }

        binding.keyboard.setOnClickListener {
            sendMoEngageData(
                this,
                MoengageUtil.EVENT_VOICE_APP_KEYBOARD
            )
            binding.keyboard.visibility = View.GONE
            binding.record.visibility = View.GONE
            binding.record2.visibility = View.VISIBLE
            binding.formatBar.visibility = View.VISIBLE

            binding.msg.isFocusableInTouchMode = true
            binding.msg.requestFocusFromTouch()
            val imm =
                this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(
                InputMethodManager.SHOW_IMPLICIT,
                InputMethodManager.HIDE_IMPLICIT_ONLY
            )
        }

        binding.formatComma.setOnClickListener {
            sendMoEngageData(
                this,
                MoengageUtil.EVENT_VOICE_APP_COMMA
            )
            setFormatCharacter(",")
        }

        binding.formatDot.setOnClickListener {
            sendMoEngageData(
                this,
                MoengageUtil.EVENT_VOICE_APP_FULLSTOP
            )
            setFormatCharacter(".")
        }

        binding.formatSpace.setOnClickListener {
            sendMoEngageData(
                this,
                MoengageUtil.EVENT_VOICE_APP_SPACE
            )
            setFormatCharacter(" ")
        }

        binding.formatBackspace.setOnClickListener {
            sendMoEngageData(
                this,
                MoengageUtil.EVENT_VOICE_APP_BACKSPACE
            )
            getCursorPos()
            if (cursorPosStart == cursorPosEnd) {
                if (cursorPosStart == 0)
                    return@setOnClickListener
                transcriptFinalMsg.replace(cursorPosStart - 1, cursorPosStart, "")
                /*transcriptFinalMsg = transcriptFinalMsg.substring(
                    0,
                    cursorPosStart - 1
                ) + transcriptFinalMsg.substring(
                    cursorPosEnd
                )*/
                cursorPosStart -= 1
                cursorPosEnd = cursorPosStart
            } else {
                transcriptFinalMsg.replace(cursorPosStart, cursorPosEnd, "")
                cursorPosEnd = cursorPosStart
            }
            runOnUiThread {
                binding.msg.setText(transcriptFinalMsg)
                binding.msg.setSelection(cursorPosStart)
                binding.msg.isCursorVisible = true
                binding.msg.requestFocusFromTouch()
            }
        }


        binding.formatBackspace.setOnTouchListener(View.OnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    isActionUp = false
                    onLongBackPressClick = Thread {
                        getCursorPos()

                        while (!isActionUp && cursorPosStart > 1) {
                            Thread.sleep(100)

                            runOnUiThread {
                                binding.msg.setText(
                                    transcriptFinalMsg.replace(
                                        cursorPosStart - 1,
                                        cursorPosStart,
                                        ""
                                    )
                                )
                                binding.msg.setSelection(cursorPosStart - 1)
                                binding.msg.isCursorVisible = true
                                cursorPosStart -= 1
                                cursorPosEnd = cursorPosStart
                            }
                        }
                    }
                    onLongBackPressClick.start()
                    return@OnTouchListener true

                }
                MotionEvent.ACTION_UP -> {
                    isActionUp = true
                    return@OnTouchListener true
                }
            }
            return@OnTouchListener false
        })

        binding.formatEnter.setOnClickListener {
            sendMoEngageData(
                this,
                MoengageUtil.EVENT_VOICE_APP_ENTER
            )
            setFormatCharacter("\n")
        }

        binding.msg.setOnClickListener {
            sendMoEngageData(
                this,
                MoengageUtil.EVENT_VOICE_APP_CLICK_PAD
            )
        }



        binding.msg.addTextChangedListener(this)
        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            binding.root.getWindowVisibleDisplayFrame(r)
            val screenHeight = binding.root.getRootView().getHeight()
            val keypadHeight = screenHeight - r.bottom
            if (keypadHeight > screenHeight * 0.15) {
                if (isKeyboardVisible) return@addOnGlobalLayoutListener
                isKeyboardVisible = true
                binding.recorderPane.visibility = View.GONE
                val params = binding.record2.layoutParams as ConstraintLayout.LayoutParams
                params.bottomMargin = dpToPix(68f).toInt()
                binding.record2.layoutParams = params
                if (!transcriptFinalMsg.isEmpty())
                    binding.record2.visibility = View.VISIBLE

            } else {
                if (!isKeyboardVisible) return@addOnGlobalLayoutListener
                isKeyboardVisible = false
                if (!isRecording) binding.recorderPane.visibility = View.VISIBLE
                val params = binding.record2.layoutParams as ConstraintLayout.LayoutParams
                params.bottomMargin = dpToPix(12f).toInt()
                binding.record2.layoutParams = params
            }


        }
        resetTypingPad()
        // return binding.root

    }


    private fun dpToPix(i: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, i, resources.displayMetrics)
    }

    private fun getCursorPos() {
        cursorPosStart = binding.msg.selectionStart
        if (cursorPosStart == -1) {
            cursorPosStart = binding.msg.text.toString().length
            cursorPosEnd = cursorPosStart
        } else cursorPosEnd = binding.msg.selectionEnd
    }

    private fun setFormatCharacter(i: String) {
        getCursorPos()
        transcriptFinalMsg.insert(cursorPosStart, i)
        /*transcriptFinalMsg = transcriptFinalMsg.substring(
            0,
            cursorPosStart
        ) + i + transcriptFinalMsg.substring(
            cursorPosEnd
        )*/
        cursorPosStart += i.length
        cursorPosEnd = cursorPosStart
        runOnUiThread {
            binding.msg.setText(transcriptFinalMsg)
            binding.msg.setSelection(cursorPosStart)
            binding.msg.isCursorVisible = true
            binding.msg.requestFocusFromTouch()
        }
    }

    private fun getRemoteConfigInstance() {
        /* remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        remoteConfig.fetchAndActivate()*/
    }

    private fun getPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
            isAudioPermissionGranted = true

        permissions.clear()
        permissions.add(Manifest.permission.RECORD_AUDIO)

        permissionsUtil = PermissionUtils(
            this, this,
            AUDIO_PERMISSIONS_STATUS_CODE
        )
        permissionsUtil.checkPermission(permissions)
    }

    private fun showDefaultLanguagePicker() {
         val spokenLanguages = prefs.spokenLanguages().get()
         if (spokenLanguages != null && spokenLanguages.size > 1) {
             languagePickerFragment =
                     LanguagePickerDialogFragment.getInstance(
                             "Typing Pad",
                             "en-IN",
                             LanguagePickerDialogFragment.TYPE_MESSAGE_TYPING_PAD,
                             this
                     )
             languagePickerFragment!!.isCancelable = true

             supportFragmentManager.beginTransaction()
                 .add(languagePickerFragment!!, getString(R.string.tag_language_picker))
                 .commitAllowingStateLoss()
         } else {
             if (spokenLanguages != null) {
                 val lang = LanguagesListUtil.map[spokenLanguages[0]]
                 val ln = LanguageHolder("English (India)", "en-IN", "English")
                 //val lang = "en-IN"
                 if (lang != null)
                     onLanguageSelected(ln, false)
                 else
                     Toast.makeText(this, "No language selected", Toast.LENGTH_SHORT).show()
             } else {
                 val intent = Intent(this, MyLanguagesActivity::class.java)
                 startActivity(intent)
             }
         }

    }

    fun resetTypingPad() {
        binding.recorderPane.visibility = View.VISIBLE
        binding.record.visibility = View.VISIBLE
        binding.record2.visibility = View.GONE
        binding.options.visibility = View.VISIBLE
        binding.formatBar.visibility = View.GONE
        binding.keyboard.visibility = View.VISIBLE
        binding.progress.visibility = View.GONE
        transcriptFinalMsg.clearSpans()
        transcriptFinalMsg.clear()
        binding.msg.setText(transcriptFinalMsg)
        binding.msgHolder.visibility = View.VISIBLE
        binding.recordingTimer.stop()
        binding.recordingPane.visibility = View.GONE
        binding.helpMsg.setText(R.string.typing_pad_help_record)
        binding.helpMsg.visibility = View.VISIBLE
        isCanceled = false
        isPaused = false
    }

    private fun startRecording(languageCode: String) {

        /*  if (prefs.soundStartRecording().get(true))
              playStartSound(R.raw.start_recording)

          if (prefs.vibrationStartRecording().get(true))
              startVibration()

          if (!prefs.isTypingPadTooltipShown().get(false)) {
              showToastTooltip()
          }*/

//        exoPlayer?.playWhenReady = false

        resetAfterAudioSend()
        fileName = "${this.filesDir.absolutePath}/audio-${Date().time}.wav"
        isCanceled = false
        isPaused = false
        getCursorPos()
        binding.msg.isEnabled = false
        binding.recorderPane.visibility = View.INVISIBLE
        binding.record2.visibility = View.GONE
        binding.helpMsg.setText(R.string.typing_pad_help_done)

        binding.recordingPane.visibility = View.VISIBLE
        binding.recordingTimer.base = SystemClock.elapsedRealtime()
        binding.recordingTimer.start()
        isRecording = true

        sendMoEngageData(
            this,
            MoengageUtil.EVENT_VOICE_APP_LANGUAGE,
            Pair(PROP_VOICE_APP_LANGUAGE, languageCode)
        )

        mSpeechRecoginizerHelper.startSpeech()
    }

    private fun playStartSound(sourceId: Int) {
        val uri = Uri.parse("android.resource://" + this.packageName + "/${sourceId}")
        /*   MediaPlayer().apply {
            setAudioStreamType(AudioManager.STREAM_RING)
            setDataSource(this, uri)
            prepare()
            start()
        }*/
    }

    private fun startVibration() {
        /* val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
             vibrator.vibrate(
                 VibrationEffect.createOneShot(
                     VIBRATION_DURATION,
                     VibrationEffect.DEFAULT_AMPLITUDE
                 )
             )
         } else {
             vibrator.vibrate(VIBRATION_DURATION)
         }*/
    }

    private fun showToastTooltip() {
        val tooltipToast =
            Toast.makeText(
                this,
                getString(R.string.tooltip_typing_pad_text),
                Toast.LENGTH_LONG
            )

        tooltipToast.setGravity(Gravity.BOTTOM, 0, 300)
        val view = tooltipToast.view
        view.background?.setColorFilter(
            ContextCompat.getColor(this, R.color.player_option_button),
            PorterDuff.Mode.SRC_IN
        )

        val text = view.findViewById<TextView>(android.R.id.message)
        text.setTextColor(ContextCompat.getColor(this, R.color.white))
        text.textSize = 20F
        text.gravity = Gravity.CENTER
        tooltipToast.show()
        //  prefs.isTypingPadTooltipShown().put(true)
    }

    private fun showToastForBeep() {
        val tooltipToast =
            Toast.makeText(
                this,
                "Please speak after the beep",
                Toast.LENGTH_SHORT
            )

        tooltipToast.setGravity(Gravity.BOTTOM, 0, 300)
        val view = tooltipToast.view
        view.background?.setColorFilter(
            ContextCompat.getColor(this, R.color.player_option_button),
            PorterDuff.Mode.SRC_IN
        )

        val text = view.findViewById<TextView>(android.R.id.message)
        text.setTextColor(ContextCompat.getColor(this, R.color.white))
        text.textSize = 20F
        text.gravity = Gravity.CENTER
        tooltipToast.show()
        // prefs.isTypingPadTooltipShown().put(true)
    }

    private fun showToastMsgForSelectedOptions(toastMsg: String) {
        val toastForSharing =
            Toast.makeText(
                this,
                toastMsg,
                Toast.LENGTH_LONG
            )

        val view = toastForSharing.view
        if (transcriptFinalMsg.isEmpty()) {
            val text = view.findViewById<TextView>(android.R.id.message)
            text.setTextColor(ContextCompat.getColor(this, R.color.white))
            view.background?.setColorFilter(
                ContextCompat.getColor(this, R.color.beautiful_red_dark),
                PorterDuff.Mode.SRC_IN
            )
        }
        toastForSharing.show()
    }

    private fun pauseRecording() {
        /* if (prefs.vibrationStartRecording().get(true))
             startVibration()*/


        isPaused = true
        isRecording = false

        binding.helpMsg.setText(R.string.typing_pad_help_record)
        binding.msg.isEnabled = true

        binding.recordingTimer.stop()
        binding.recorderPane.visibility = View.VISIBLE
        if (transcriptFinalMsg.isNotEmpty())
            binding.record2.visibility = View.VISIBLE

        binding.recordingPane.visibility = View.GONE

        binding.recordCancel.visibility = View.VISIBLE
        binding.recordingView.visibility = View.VISIBLE
        binding.recordPause.visibility = View.VISIBLE
        binding.recordingTv.text = "Recording..."

    }

    fun cancelRecording() {
        binding.recorderPane.visibility = View.VISIBLE
        if (transcriptFinalMsg.isNotEmpty())
            binding.record2.visibility = View.VISIBLE
        binding.recordingPane.visibility = View.GONE
        binding.recordingTimer.stop()

        binding.helpMsg.setText(R.string.typing_pad_help_record)
        binding.msg.isEnabled = true

        binding.recordCancel.visibility = View.VISIBLE
        binding.recordingView.visibility = View.VISIBLE
        binding.recordPause.visibility = View.VISIBLE
        binding.recordingTv.text = "Recording..."

        /*if (prefs.vibrationPostSent().get(true))
            startVibration()

        if (prefs.soundPostSent().get(true))
            playStartSound(R.raw.end_recording)*/
    }

    fun afterCompletingStreaming() {
        if (binding.progress.visibility == View.VISIBLE) {
            this.runOnUiThread {
                binding.progress.visibility = View.GONE
            }
        }
        if (isCanceled)
            resetAfterAudioSend()
    }

    private fun resetAfterAudioSend() {
        transcription = null
        timer?.cancel()
        timer = null
        isTranscriptError = false
    }

    fun onLanguageSelected(language: LanguageHolder, isCallingFromLanguagePicker: Boolean) {
        if (languagePickerFragment != null)
            languagePickerFragment!!.dismiss()
        if (isCallingFromLanguagePicker) {
            preferedLanguage = language.languageCode
            initVoiceRecognizationWithoutGoogleDialog()
        }

        showToastForBeep()
        startRecording(language.languageCode)

    }

    private fun openWhatsappDirectBottomSheet() {


     /*   languagePickerFragment =
            WhatsappDirectBottomSheet.getInstance(

                this)*/

         val sheet = WhatsappDirectBottomSheet()
         sheet.show(supportFragmentManager, sheet.tag)
    }


    override fun onDismiss() {

    }

    override fun allPermissionsGranted(requestCode: Int) {
        when (requestCode) {
            AUDIO_PERMISSIONS_STATUS_CODE -> {
                showDefaultLanguagePicker()
                sendMoEngageData(
                    this,
                    MoengageUtil.EVENT_VOICE_APP_AUDIO_PERMISSION,
                    Pair(MoengageUtil.PROP_PERMISSION_GRANTED, true)
                )
            }
        }
    }

    override fun permissionsNeverAskAgain(requestCode: Int) {
        when (requestCode) {
            AUDIO_PERMISSIONS_STATUS_CODE -> {
                sendMoEngageData(
                    this,
                    MoengageUtil.EVENT_VOICE_APP_AUDIO_PERMISSION,
                    Pair(MoengageUtil.PROP_PERMISSION_DENIED, false)
                )
                Toast.makeText(
                    this,
                    getString(R.string.audio_permission_error),
                    Toast.LENGTH_LONG
                ).show()
            }

            else -> {

            }
        }
    }

    override fun permissionsDenied(requestCode: Int) {
        when (requestCode) {
            AUDIO_PERMISSIONS_STATUS_CODE -> {
                sendMoEngageData(
                    this,
                    MoengageUtil.EVENT_VOICE_APP_AUDIO_PERMISSION,
                    Pair(MoengageUtil.PROP_PERMISSION_DENIED, false)
                )
                Toast.makeText(
                    this,
                    getString(R.string.audio_permission_error),
                    Toast.LENGTH_LONG
                ).show()

            }
        }
    }

    override fun partialPermissionsGranted(
        requestCode: Int,
        pendingPermissions: ArrayList<String>
    ) {
        when (requestCode) {
            AUDIO_PERMISSIONS_STATUS_CODE -> {
                sendMoEngageData(
                    this,
                    MoengageUtil.EVENT_VOICE_APP_AUDIO_PERMISSION,
                    Pair(MoengageUtil.PROP_PERMISSION_GRANTED, true)
                )
                if (!isAudioPermissionGranted)

                    showDefaultLanguagePicker()
            }

            else -> {

            }
        }
    }

    override fun afterTextChanged(s: Editable?) {
        transcriptFinalMsg = binding.msg.text as SpannableStringBuilder
        this.runOnUiThread {
            if (transcriptFinalMsg.isEmpty()) {
                binding.options.visibility = View.VISIBLE
                binding.helpMsg.visibility = View.VISIBLE
            } else {
                binding.options.visibility = View.VISIBLE
                binding.record.visibility = View.GONE
                if (!isRecording) binding.record2.visibility = View.VISIBLE
                binding.formatBar.visibility = View.VISIBLE
                binding.keyboard.visibility = View.GONE
                binding.helpMsg.visibility = View.GONE
            }
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }
/*
    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        when (item!!.itemId) {
            *//*  R.id.txt_bold -> {
                getCursorPos()
                transcriptFinalMsg.setSpan(
                        StyleSpan(Typeface.BOLD),
                        cursorPosStart,
                        cursorPosEnd,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                )
                this.runOnUiThread {
                    binding.msg.setText(transcriptFinalMsg)
                    cursorPosStart = cursorPosEnd
                    binding.msg.setSelection(cursorPosStart)
                    binding.msg.isCursorVisible = true
                    binding.msg.requestFocusFromTouch()
                }
                return true
            }
            R.id.txt_italic -> {
                getCursorPos()
                transcriptFinalMsg.setSpan(
                        StyleSpan(Typeface.ITALIC),
                        cursorPosStart,
                        cursorPosEnd,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                )
                this.runOnUiThread {
                    binding.msg.setText(transcriptFinalMsg)
                    cursorPosStart = cursorPosEnd
                    binding.msg.setSelection(cursorPosStart)
                    binding.msg.isCursorVisible = true
                    binding.msg.requestFocusFromTouch()
                }
                return true
            }
            R.id.txt_underline -> {
                getCursorPos()
                transcriptFinalMsg.setSpan(
                        UnderlineSpan(),
                        cursorPosStart,
                        cursorPosEnd,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                )
                this.runOnUiThread {
                    binding.msg.setText(transcriptFinalMsg)
                    cursorPosStart = cursorPosEnd
                    binding.msg.setSelection(cursorPosStart)
                    binding.msg.isCursorVisible = true
                    binding.msg.requestFocusFromTouch()
                }
                return true
            }*//*
        }
        return false
    }*/

  /*  override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        mode!!.menuInflater.inflate(R.menu.menu_typingpad, menu)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // menu?.removeItem(android.R.id.shareText)
        }
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        *//*  menu?.findItem(android.R.id.cut)?.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        menu?.findItem(android.R.id.copy)?.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)*//*
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {

    }
*/
    /* override fun sendWhatsappMessage(phoneNumber: String) {

         val url = "whatsapp://send?text=${URLEncoder.encode(
                 transcriptFinalMsg.toString(),
                 "utf-8"
         )}&phone=$phoneNumber"
         val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
         try {
             startActivity(intent)
             showToastMsgForSelectedOptions("Opening WhatsApp")
         } catch (ex: ActivityNotFoundException) {
             showToastMsgForSelectedOptions("WhatsApp isn't installed on your device")
         }
     }*/

    private fun formatTextToWhatsAppStyle(s: SpannableStringBuilder): String {
        return s.toString()
    }


    private fun initVoiceRecognizationWithoutGoogleDialog() {
        mSpeechRecoginizerHelper = SpeechRecoginizerHelper(
            this,
            preferedLanguage,
            object : SpeechCallback {
                override fun onSpeechStart() {
                    transcription = null
                }

                override fun onSpeechStop() {
                }

                override fun onSpeechResult(result: ArrayList<String>?) {
                    if (transcription == null) {
                        transcription = ""
                    }
                    if (countOnResults == 0) {
                        countOnResults++
                        transcription = result!![0]
                        if (transcription == null) {
                            transcription = ""
                            /* if (!isCanceled) {
                                     val snackbar = Snackbar.make(
                                             binding.root,
                                             R.string.empty_transcription_message,
                                             Snackbar.LENGTH_INDEFINITE
                                     )
                                     snackbar.setAction(R.string.dismiss) {
                                         snackbar.dismiss()
                                     }.setActionTextColor(
                                             ContextCompat.getColor(
                                                     this,
                                                     R.color.white
                                             )
                                     )
                                     snackbar.show()
                                 }*/
                        }
                        pauseRecording()
                        afterCompletingStreaming()
                        if (!isCanceled) {
                            if (transcription == "" && transcriptFinalMsg.isEmpty()) {
                                runOnUiThread {
                                    resetTypingPad()
                                }
                            } else {
                                var space = ""
                                var space2 = ""
                                if (cursorPosStart != 0 && !Character.isWhitespace(
                                        transcriptFinalMsg.get(cursorPosStart - 1)
                                    )
                                )
                                    space = " "
                                if (cursorPosEnd != transcriptFinalMsg.length && !Character.isWhitespace(
                                        transcriptFinalMsg.get(cursorPosEnd)
                                    )
                                )
                                    space2 = " "
                                runOnUiThread {
                                    transcriptFinalMsg.replace(
                                        cursorPosStart,
                                        cursorPosEnd,
                                        space + transcription + space2
                                    )
                                    cursorPosStart += transcription!!.length + space.length + space2.length
                                    cursorPosEnd = cursorPosStart
                                    binding.msg.setText(transcriptFinalMsg)
                                    binding.msg.setSelection(cursorPosStart)
                                    binding.msg.isCursorVisible = true
                                    binding.msg.requestFocusFromTouch()
                                }
                            }
                        }
                    } else {
                        countOnResults = 0
                    }
                }

                override fun onSpeechError(message: String) {
                    var errorCode = message.split(":")[1].trim()
                    isTranscriptError = true
                    transcription = null
                    afterCompletingStreaming()
                    pauseRecording()
                    cancelRecording()
                    /* if (!isCanceled && errorCode.toInt() == 4) {
                             val snackbar = Snackbar.make(
                                     binding.root,
                                     R.string.transcription_failed_message,
                                     Snackbar.LENGTH_INDEFINITE
                             )
                             snackbar.setAction(R.string.dismiss) {
                                 snackbar.dismiss()
                             }.setActionTextColor(ContextCompat.getColor(this, R.color.white))
                             snackbar.show()
                         } else if (!isCanceled && (errorCode.toInt() == 7 || errorCode.toInt() == 6)) {

                             val snackbar = Snackbar.make(
                                     binding.root,
                                     "Invalid input",
                                     Snackbar.LENGTH_INDEFINITE
                             )
                             snackbar.setAction(R.string.dismiss) {
                                 snackbar.dismiss()
                             }.setActionTextColor(ContextCompat.getColor(this, R.color.white))
                             snackbar.show()
                         }*/
                }
            })
    }

    override fun onStop() {
        super.onStop()
        mSpeechRecoginizerHelper.stopSpeech()
    }


    override fun onResume() {
        super.onResume()
        if(prefs.spokenLanguages().get() == null)
            preferedLanguage = "en-IN"
        else
            preferedLanguage = prefs.spokenLanguages().get().get(0)
        initVoiceRecognizationWithoutGoogleDialog()
    }


    /* override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.home_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {

            *//* R.id.new_group -> {

                 true
             }*//*

           *//* R.id.chat -> {
                val intent = Intent(this, LaunchActivity::class.java)
                intent.putExtra("fromVoicePad", true)
                startActivity(intent)
                finish()
                true
            }*//*
            *//*R.id.app_link -> {
                sendUpdateAllPostsEvent()

                val appPackageName = BuildConfig.APPLICATION_ID
                try {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + appPackageName)
                        )
                    );
                } catch (anfe: ActivityNotFoundException) {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)
                        )
                    );
                }
                true
            }*//*
            *//* R.id.new_broadcast -> {


                 true
             }*//*
            else -> super.onOptionsItemSelected(item)
        }*/
    //}


    /*override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_typingpad, menu)

        return super.onCreateOptionsMenu(menu)
    }*/


    /*override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {

            R.id.lanPicker -> {
                val intent = Intent(this, MyLanguagesActivity::class.java)
                startActivity(intent)

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }*/


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

       // prefs.spokenLanguages().put(spokenLanguages)
    }


    override fun sendWhatsappMessage(phoneNumber: String) {
        sendMoEngageData(
            this,
            MoengageUtil.EVENT_VOICE_APP_WHATSAPP_DIRECT,
            Pair(PROP_WHATSAPP_DIRECT_NUMBER, phoneNumber),
            Pair(PROP_WHATSAPP_DIRECT_STATUS, "Sent"),
            Pair(PROP_WORD_COUNT, calcWords(transcriptFinalMsg.toString()))
        )
        val url = "whatsapp://send?text=${URLEncoder.encode(
            transcriptFinalMsg.toString(),
            "utf-8"
        )}&phone=$phoneNumber"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            startActivity(intent)
            showToastMsgForSelectedOptions("Opening WhatsApp")
        } catch (ex: ActivityNotFoundException) {
            showToastMsgForSelectedOptions("WhatsApp isn't installed on your device")
        }
    }

    private fun calcWords(s: String): Int {
        var flag = false
        var count = 0
        var i = 0
        while (i < s.length) {
            if (s.get(i).isLetterOrDigit()) {
                if (!flag) {
                    flag = true
                    count++
                }
            } else
                flag = false
            i++
        }
        return count
    }


}




