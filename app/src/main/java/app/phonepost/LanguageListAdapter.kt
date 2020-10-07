package app.phonepost


import android.app.Activity
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.phonepost.databinding.ItemLanguageNameBinding
/*import app.phonepost.invite.InviteActivity
import app.phonepost.loginscreen.LanguageHolder
import app.phonepost.mainscreen.HomeActivity*/

class LanguageListAdapter(
    val activity: Activity,
    val languageList: List<LanguageHolder?>,
    val preferedLang: String,
    val languageSelectionType: Int
) :
    RecyclerView.Adapter<LanguageListAdapter.ViewHolder>() {

    val TAG = "LanguageListAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(activity.applicationContext)
        val binding = ItemLanguageNameBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val lang = languageList[position]!!
//
        holder.binding.language.text = lang.displayName

        Log.d(TAG, "prefe--  $preferedLang")

        if (preferedLang == lang.languageCode) {
            holder.binding.language.setTextColor(ContextCompat.getColor(activity, R.color.white))
            holder.binding.language.setTypeface(holder.binding.language.typeface, Typeface.BOLD)
            holder.binding.language.setBackgroundResource(R.drawable.background_round_login_button)
        } else {
            holder.binding.language.setTextColor(
                ContextCompat.getColor(
                    activity,
                    R.color.lang_selection
                )
            )
            holder.binding.language.setTypeface(holder.binding.language.typeface, Typeface.NORMAL)
            holder.binding.language.setBackgroundResource(R.drawable.background_round_non_selected_language)
        }

        holder.binding.language.setOnClickListener {

           when (languageSelectionType) {
               /* LanguagePickerDialogFragment.TYPE_INVITE_SCREEN -> {

                    val inviteActivity = activity as InviteActivity
                    inviteActivity.onLanguageSelected(lang)
                }

                LanguagePickerDialogFragment.TYPE_MESSAGE_LANG_SCREEN -> {
                    val chatActivity = activity as ChatScreenActivity
                    chatActivity.messageLangSelected(lang)
                }

                LanguagePickerDialogFragment.TYPE_NEW_RECORDING_SCREEN -> {

                    val chatActivity = activity as ChatScreenActivity
                    chatActivity.onLanguageSelected(lang)
                }
*/
                LanguagePickerDialogFragment.TYPE_MESSAGE_TYPING_PAD -> {
                    val typingPad = (activity as VoicePadActivity)
                    typingPad!!.onLanguageSelected(lang, true)
                }

            }
        }

    }

    override fun getItemCount(): Int {
        return languageList.size
    }

    class ViewHolder(val binding: ItemLanguageNameBinding) : RecyclerView.ViewHolder(binding.root)

}