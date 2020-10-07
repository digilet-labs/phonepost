package app.phonepost
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

import app.phonepost.databinding.ActivityMyLanguageBinding


class MyLanguagesActivity : AppCompatActivity(){

    lateinit var binding: ActivityMyLanguageBinding
    lateinit var prefs: PreferenceStore
    var preferredLanguage: String? = null
    var checkedLanguagesMap = HashMap<String, LanguageHolder>()
    val spokenLanguages = ArrayList<String>()
    val TAG = "MyLanguagesActivity"
    var redirect = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_language)
        val bundle: Bundle? = intent.extras
        prefs = PreferenceStore.create(this)
        redirect = intent.getIntExtra("redirect", -1)
       // StatusBarUtil.setLightMode(this)

        setOnClickListeners()
        setUpLanguageList()
        setUpUserLanguages()
        //hideSoftKeyBoard()
    }

    private fun setOnClickListeners() {
//        binding.back.setOnClickListener {
//            startActivity(Intent(this, ProfileSettingsActivity::class.java))
//        }

        binding.btSaveLanguage.setOnClickListener {

            if(checkedLanguagesMap.values.size < 1) {
                Toast.makeText(
                    applicationContext,
                    "Please select atleast one language",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

           /* if (redirect == ProfileSettingsActivity.SETTINGS_REDIRECT) {
                sendSavedLanguageEvent(checkedLanguagesMap.values.map { it.languageCode })
                val result = Intent(applicationContext, ProfileSettingsActivity::class.java)
                ProfileSettingsActivity.checkedLanguagesMap = checkedLanguagesMap
                setResult(Activity.RESULT_OK, result)
            }
            if (redirect == CreateNewProfile.NEW_PROFILE_REDIRECT) {
                val result = Intent(applicationContext, CreateNewProfile::class.java)
                CreateNewProfile.checkedLanguagesMap = checkedLanguagesMap
                setResult(Activity.RESULT_OK, result)
            }*/

            checkedLanguagesMap.values.map {
                it.languageCode }

            checkedLanguagesMap.forEach {

                val spokenLanguage =
                    it.value.languageCode
                spokenLanguages.add(spokenLanguage)
            }
            val preferenceStore = PreferenceStore.create(applicationContext)
            preferenceStore.spokenLanguages().put(spokenLanguages)
            val result = Intent(applicationContext, VoicePadActivity::class.java)
        //    ProfileSettingsActivity.checkedLanguagesMap = checkedLanguagesMap
            setResult(Activity.RESULT_OK, result)

            finish()
        }
    }

    override fun onBackPressed() {
       // sendSelectBackEvent()
        super.onBackPressed()
    }

    private fun setUpUserLanguages() {
        val spokenLanguages = prefs.spokenLanguages().get()

        if (spokenLanguages != null) {

            spokenLanguages.forEach {
                addCheckedLanguage(LanguagesListUtil.map[it])
                Log.d(TAG, "Lang: $it")
            }

            Log.d(TAG, "Spoken Langs: ${spokenLanguages.size}")

        }
    }

    fun addCheckedLanguage(lang: LanguageHolder?) {

        if (lang != null) {
            checkedLanguagesMap[lang.languageCode] = lang
            Log.d(TAG, "Adding Lnag: ${lang.languageCode}")
        }
    }

    fun removeCheckedLanguage(lang: LanguageHolder) {
        checkedLanguagesMap.remove(lang.languageCode)
    }

    private fun setUpLanguageList() {

        binding.languagesList.layoutManager =
            GridLayoutManager(this, 2)
        binding.languagesList.adapter =
            LanguagesListAdapter(this, checkedLanguagesMap)
    }

//    fun hideSoftKeyBoard() {
//        val imm = getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
//        if (imm.isAcceptingText()) {
//            imm.hideSoftInputFromWindow(getCurrentFocus()!!.getWindowToken(), 0)
//        }
//    }

   /* fun sendSelectedLanguageEvent(languageCode: String){
        val builder = PayloadBuilder()
        builder.putAttrLong(MoengageUtil.PROP_USER_ID, prefs.userId().get(-1))
        builder.putAttrString(MoengageUtil.PROP_LANGUAGE, languageCode)

        if (redirect == ProfileSettingsActivity.SETTINGS_REDIRECT) {
            MoEHelper.getInstance(this)
                .trackEvent(MoengageUtil.EVENT_LANGUAGE_SETTINGS_SELECTED_LANGUAGE, builder)
        } else {
            MoEHelper.getInstance(this)
                .trackEvent(MoengageUtil.EVENT_ONBOARDING_SELECTED_LANGUAGE, builder)
        }
    }

    fun sendRemovedLanguageEvent(languageCode: String){
        val builder = PayloadBuilder()
        builder.putAttrLong(MoengageUtil.PROP_USER_ID, prefs.userId().get(-1))
        builder.putAttrString(MoengageUtil.PROP_LANGUAGE, languageCode)

        if (redirect == ProfileSettingsActivity.SETTINGS_REDIRECT) {
            MoEHelper.getInstance(this)
                .trackEvent(MoengageUtil.EVENT_LANGUAGE_SETTINGS_REMOVED_LANGUAGE, builder)
        } else {
            MoEHelper.getInstance(this)
                .trackEvent(MoengageUtil.EVENT_ONBOARDING_REMOVED_LANGUAGE, builder)
        }
    }
*/
  /*  fun sendSavedLanguageEvent(updatedLangagues: List<String>){
        val builder = PayloadBuilder()
        builder.putAttrLong(MoengageUtil.PROP_USER_ID, prefs.userId().get(-1))
        builder.putAttrString(MoengageUtil.PROP_UPDATED_LANGUAGES, updatedLangagues.joinToString(","))

        if (redirect == ProfileSettingsActivity.SETTINGS_REDIRECT) {
            MoEHelper.getInstance(this)
                .trackEvent(MoengageUtil.EVENT_LANGUAGE_SETTINGS_SAVED_PREFERENCES, builder)
        } else {

            MoEHelper.getInstance(this)
                .trackEvent(MoengageUtil.EVENT_ONBOARDING_SUBMITED_LANGUAGE_PREFS, builder)
        }

        MoEHelper.getInstance(this).setUserAttribute(MoengageUtil.USER_ATTRIBUTE_SPOKEN_LANGUAGES, updatedLangagues.joinToString(","))
    }

    fun sendSelectBackEvent(){
        val builder = PayloadBuilder()
        builder.putAttrLong(MoengageUtil.PROP_USER_ID, prefs.userId().get(-1))
        if (redirect == ProfileSettingsActivity.SETTINGS_REDIRECT) {
            MoEHelper.getInstance(this)
                .trackEvent(MoengageUtil.EVENT_LANGUAGE_SETTINGS_SELECTED_BACK, builder)
        }
    }*/
}
