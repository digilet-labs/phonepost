package app.phonepost


import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import app.phonepost.databinding.BottomSheetWhatsappDirectBinding

import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class WhatsappDirectBottomSheet(): BottomSheetDialogFragment() {

    lateinit var binding: BottomSheetWhatsappDirectBinding
    lateinit var prefs: PreferenceStore
    lateinit var parentactivity: VoicePadActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferenceStore.create(context!!.applicationContext)
        parentactivity = activity as VoicePadActivity
        setStyle(STYLE_NORMAL, R.style.MyBottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_whatsapp_direct, container, false)
        binding.ccp.registerCarrierNumberEditText(binding.phoneNumber)
        binding.send.setOnClickListener {
            if (binding.ccp.isValidFullNumber) {
                parentactivity.sendWhatsappMessage(binding.ccp.fullNumber)
                dismiss()
            }
            else{
                Toast.makeText(activity, getString(R.string.incorrect_number), Toast.LENGTH_SHORT).show()
            }
        }
        binding.phoneNumber.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun afterTextChanged(text: Editable?) {
                if(Build.VERSION.SDK_INT > 21 ) {
                    if (text.toString().isEmpty()) {
                        binding.send.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.light_button))
                    } else {
                        binding.send.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.phonepost_color))
                    }
                }
            }
        })
        return binding.root
    }

    interface Listener {
        fun sendWhatsappMessage(phoneNumber: String)
    }


    companion object {
       // fun newInstance(listener: Listener) = WhatsappDirectBottomSheet(listener)
        fun newInstance(listener: Listener): WhatsappDirectBottomSheet {
            val fragment = WhatsappDirectBottomSheet()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }

        /* fun getInstance(
             listener: Listener
         ): WhatsappDirectBottomSheet {

             val fragment = WhatsappDirectBottomSheet(listener)
             val bundle = Bundle()
             fragment.arguments = bundle
             return fragment
         }*/
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
       
    }
}