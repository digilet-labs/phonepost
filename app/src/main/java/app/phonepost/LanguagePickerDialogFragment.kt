package app.phonepost

import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import app.phonepost.databinding.DialogMessageLanguageSelectionBinding

class LanguagePickerDialogFragment(val listener: Listener) : DialogFragment() {

    lateinit var binding: DialogMessageLanguageSelectionBinding
    lateinit var otherUserName: String
    lateinit var preferedLanguage: String
    var languageSelectionType: Int = -1

    companion object {

        val EXTRAS_LANGUAGE_SELECTION_TYPE = "extras_language_selection_type"
        val EXTRAS_USER_NAME = "chat_head"
        val EXTRAS_THREAD_ID = "thread_id"
        val EXTRAS_AVATAR = "avatar"
        val EXTRAS_USER_NUMBER = "number"
        val EXTRAS_RECORDING_BASE = "recording_base"
        val EXTRAS_PARTICIPANT = "participant"
        val EXTRAS_START_RECORDING = "start_recording"
        val EXTRAS_PREFERED_LANGUAGE = "prefered_language"
        val EXTRAS_DONT_SHOW_AGAIN = "dont_show_again"
        val EXTRAS_USER_COLOR = "extras_user_color"
        val TYPE_INVITE_SCREEN = 1
        val TYPE_NEW_RECORDING_SCREEN = 2
        val TYPE_MESSAGE_LANG_SCREEN = 3
        val TYPE_MESSAGE_TYPING_PAD = 4

        fun getInstance(
            otherUserName: String,
            preferedLanguage: String,
            screenType: Int,
            listener: Listener
        ): LanguagePickerDialogFragment {

            val fragment = LanguagePickerDialogFragment(listener)
            val bundle = Bundle()
            bundle.putString(EXTRAS_USER_NAME, otherUserName)
            bundle.putString(EXTRAS_PREFERED_LANGUAGE, preferedLanguage)
            bundle.putInt(EXTRAS_LANGUAGE_SELECTION_TYPE, screenType)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listener.onDismiss()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_message_language_selection, container, false)
        otherUserName = arguments!!.getString(EXTRAS_USER_NAME)!!
        preferedLanguage = arguments!!.getString(EXTRAS_PREFERED_LANGUAGE)!!
        languageSelectionType = arguments!!.getInt(EXTRAS_LANGUAGE_SELECTION_TYPE)

        val window = dialog!!.window
        window!!.setGravity(Gravity.BOTTOM)

        val params = window.attributes
        params.x = 0
        params.y = 150
        window.attributes = params

        val prefs = PreferenceStore.create(activity!!)
        val spokenLanguages = prefs.spokenLanguages().get()

        if (spokenLanguages != null) {
            val languages = spokenLanguages.map {
                LanguagesListUtil.map[it]
            }

            val linearViewManager = LinearLayoutManager(activity)

            val languageAdapter =
                LanguageListAdapter(activity!!, languages, preferedLanguage, languageSelectionType)

            binding.languagesList.apply {
                layoutManager = linearViewManager
                adapter = languageAdapter
            }
        }

        when (languageSelectionType) {
           /* TYPE_INVITE_SCREEN -> {
                binding.dont.visibility = View.GONE
                binding.langHead.text = getString(R.string.please_select_the_language_of_this_post)
            }

            TYPE_MESSAGE_LANG_SCREEN -> {
                binding.langHead.text = getString(R.string.message_lang_hint)
                binding.dont.visibility = View.GONE

            }

            TYPE_NEW_RECORDING_SCREEN -> {
                binding.langHead.text = getString(R.string.please_select_the_language_of_this_post)
            }*/

            TYPE_MESSAGE_TYPING_PAD -> {
                binding.langHead.text = getString(R.string.please_select_the_language_of_this_post)
                binding.dont.visibility = View.GONE
            }
        }

        binding.dont.text = activity!!.getString(R.string.set_as_default_for_this_user, otherUserName)

        return binding.root
    }

    interface Listener{
        fun onDismiss()
    }
}