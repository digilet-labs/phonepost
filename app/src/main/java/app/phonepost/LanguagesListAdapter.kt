package app.phonepost


import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.phonepost.databinding.LanguageListItemBinding
import com.google.android.material.chip.Chip

class LanguagesListAdapter(
//    val fragment: ProfileFragment,

    val activity: MyLanguagesActivity,
    val spokenLanguagesMap: HashMap<String, LanguageHolder>
) :
    RecyclerView.Adapter<LanguagesListAdapter.ViewHolder>() {
    val languages = LanguagesListUtil.languages
    val TAG = "LanguageAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(activity)
        val binding = LanguageListItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return languages.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        binding.layout.removeAllViews()
        val lang = languages[position]

        val chip = Chip(activity)
        chip.text = lang.languageName
        chip.isCheckable = true
        chip.chipStrokeWidth = 1F
        chip.elevation = 10F


        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 48.dp)
        layoutParams.setMargins(2.dp, 2.dp, 2.dp, 5.dp)
        chip.layoutParams = layoutParams

        if (spokenLanguagesMap.containsKey(lang.languageCode)) {
            chip.isChipIconVisible = false
            chip.checkedIcon = ContextCompat.getDrawable(activity, R.drawable.ic_checked_chip_tick)
            chip.chipStrokeColor = ColorStateList.valueOf(
                ContextCompat.getColor(
                    activity,
                    R.color.phonepost_color
                )
            )
        } else {
            chip.isChipIconVisible = true
            chip.chipIcon = ContextCompat.getDrawable(activity, R.drawable.ic_unchecked_chip_tick)
            chip.chipStrokeColor = ColorStateList.valueOf(
                ContextCompat.getColor(
                    activity,
                    R.color.chip_outline_grey
                )
            )
        }
        chip.chipBackgroundColor =
            ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.white))
        chip.isChecked = spokenLanguagesMap.containsKey(lang.languageCode)

        chip.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
               // activity.sendSelectedLanguageEvent(lang.languageCode)
                chip.isChipIconVisible = false
                chip.checkedIcon =
                    ContextCompat.getDrawable(activity, R.drawable.ic_checked_chip_tick)
                chip.chipStrokeColor = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        activity,
                        R.color.phonepost_color
                    )
                )
                spokenLanguagesMap[lang.languageCode] = lang
                addCheckedLanguage(lang)
            } else {
              //  activity.sendRemovedLanguageEvent(lang.languageCode)
                chip.isChipIconVisible = true
                chip.chipIcon =
                    ContextCompat.getDrawable(activity, R.drawable.ic_unchecked_chip_tick)
                chip.chipStrokeColor = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        activity,
                        R.color.chip_outline_grey
                    )
                )
                spokenLanguagesMap.remove(lang.languageCode)
                removeCheckedLanguage(lang)
            }
            //activity.hideSoftKeyBoard()
        }

        binding.layout.addView(chip)
    }

    fun addCheckedLanguage(lang: LanguageHolder?) {

        if (lang != null) {
            activity.checkedLanguagesMap[lang.languageCode] = lang
            Log.d(TAG, "Adding Lnag: ${lang.languageCode}")
        }
    }

    fun removeCheckedLanguage(lang: LanguageHolder) {
        activity.checkedLanguagesMap.remove(lang.languageCode)
    }

    class ViewHolder(val binding: LanguageListItemBinding) : RecyclerView.ViewHolder(binding.root)
}