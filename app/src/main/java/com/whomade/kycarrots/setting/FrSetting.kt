package com.cashcuk.setting

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.whomade.kycarrots.R
import com.whomade.kycarrots.ui.dialog.RadioListAdapter
import com.whomade.kycarrots.loginout.AuthManager

class FrSetting : Fragment() {

    private lateinit var mActivity: Activity
    private lateinit var txtAccount: TextView
    private lateinit var chkSound: CheckBox
    private lateinit var chkVibrate: CheckBox
    private lateinit var txtSetSound: TextView
    private lateinit var layoutBG: LinearLayout

    private lateinit var rm: RingtoneManager
    private lateinit var arrSound: ArrayList<String>
    private var ringtoneTemp: Ringtone? = null

    private var selSoundPosition = 0
    private var saveSoundPosition = 0
    private var saveRbPosition = 0
    private var selRbPosition = 0
    private var strSelNoti: String = ""
    private lateinit var arrNotiType: ArrayList<String>

    private var mDlg: Dialog? = null
    private lateinit var mRadioListAdapter: RadioListAdapter
    private var mDlgMode = -1

    private val DIALOG_MODE_NOTI_SOUND = 0
    private val DIALOG_MODE_NOTI_TYPE = 1

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        mActivity = activity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fr_setting, container, false)

        layoutBG = view.findViewById(R.id.ll_bg)
        layoutBG.background = BitmapDrawable(resources, BitmapFactory.decodeResource(resources, R.drawable.display_bg))

        txtAccount = view.findViewById(R.id.txt_account)
        chkSound = view.findViewById(R.id.chk_sound)
        chkVibrate = view.findViewById(R.id.chk_vibrate)
        txtSetSound = view.findViewById(R.id.txt_setting_sound)

        txtAccount.text = mActivity.getSharedPreferences("SaveLoginInfo", Context.MODE_PRIVATE).getString("LogIn_ID", "")

        // 공통 클릭 리스너 연결
        val clickableIds = arrayOf(
            R.id.ll_account, R.id.ib_account, R.id.ll_notice, R.id.ib_notice,
            R.id.ll_sound, R.id.ll_setting_sound, R.id.ll_vibrate,
            R.id.ll_alrim_popup, R.id.ib_alrim_popup,
            R.id.ll_tutorial, R.id.ib_tutorial,
            R.id.ll_guide_ad, R.id.ib_guide_ad,
            R.id.ll_guide_character, R.id.ib_guide_character,
            R.id.ll_customer_service, R.id.ib_customer_service,
            R.id.ll_faq, R.id.ib_faq,
            R.id.ll_service_agreement, R.id.ib_service_agreement,
            R.id.ll_privacy_policy, R.id.ib_privacy_policy,
            R.id.ll_company_introduction, R.id.ib_company_introduction,
            R.id.ll_logout, R.id.ll_logout
        )

        clickableIds.forEach { id ->
            view.findViewById<View>(id).setOnClickListener(mNewActivity)
        }

        arrNotiType = arrayListOf(
            getString(R.string.str_set_nofi_dig_always),
            getString(R.string.str_set_nofi_dig_on_dp),
            getString(R.string.str_set_nofi_dig_off)
        )

        return view
    }

    override fun onResume() {
        super.onResume()
        getNotiSysSound()
        getNotiSound()
        setNotiSound()
        getNotiType()
        loadSettings()
    }

    private val mNewActivity = View.OnClickListener { v ->
        val viewId = v.id
        var intent: Intent? = null
        when (viewId) {
            /*
            R.id.ll_account, R.id.ib_account -> {
                intent = Intent(mActivity, AccountActivity::class.java).apply {
                    putExtra("Account", txtAccount.text.toString())
                }
            }
            R.id.ll_notice, R.id.ib_notice -> {
                intent = Intent(mActivity, SettingWebViewActivity::class.java).apply {
                    putExtra("DisplayMode", "Notice")
                }
            }
            R.id.ll_sound -> chkSound.isChecked = !chkSound.isChecked
            R.id.ll_setting_sound -> showDialog(DIALOG_MODE_NOTI_SOUND)
            R.id.ll_vibrate -> chkVibrate.isChecked = !chkVibrate.isChecked
            R.id.ll_alrim_popup, R.id.ib_alrim_popup -> showDialog(DIALOG_MODE_NOTI_TYPE)
            R.id.ll_tutorial, R.id.ib_tutorial -> {
                val userImages = arrayListOf(
                    R.drawable.user1, R.drawable.user2, R.drawable.user3,
                    R.drawable.user4, R.drawable.user5
                )
                intent = Intent(mActivity, GuideActivity::class.java).apply {
                    putIntegerArrayListExtra("GuideAD", userImages)
                }
            }
            R.id.ll_guide_ad, R.id.ib_guide_ad -> {
                val adImages = (1..9).map { resId ->
                    resources.getIdentifier("advertiser$resId", "drawable", mActivity.packageName)
                }.toCollection(ArrayList())
                intent = Intent(mActivity, GuideActivity::class.java).apply {
                    putIntegerArrayListExtra("GuideAD", adImages)
                }
            }
            R.id.ll_guide_character, R.id.ib_guide_character -> {
                val charImages = arrayListOf(
                    R.drawable.character1, R.drawable.character2,
                    R.drawable.character3, R.drawable.character4
                )
                intent = Intent(mActivity, GuideActivity::class.java).apply {
                    putIntegerArrayListExtra("GuideAD", charImages)
                }
            }
            R.id.ll_customer_service, R.id.ib_customer_service -> intent = Intent(mActivity, CustomerServiceActivity::class.java)
            R.id.ll_faq, R.id.ib_faq -> intent = Intent(mActivity, SettingWebViewActivity::class.java).apply { putExtra("DisplayMode", "FAQ") }
            R.id.ll_service_agreement, R.id.ib_service_agreement -> intent = Intent(mActivity, SettingWebViewActivity::class.java).apply { putExtra("DisplayMode", "Service_Agreement") }
            R.id.ll_privacy_policy, R.id.ib_privacy_policy -> intent = Intent(mActivity, SettingWebViewActivity::class.java).apply { putExtra("DisplayMode", "PrivacyPolicy") }
            R.id.ll_company_introduction, R.id.ib_company_introduction -> intent = Intent(mActivity, CompanyIntroductionActivity::class.java)
            */
            R.id.ll_logout, R.id.ll_logout -> AuthManager.logout(mActivity)

        }

        intent?.let { startActivity(it) }
    }

    private fun getNotiSysSound() {
        rm = RingtoneManager(mActivity).apply { setType(RingtoneManager.TYPE_NOTIFICATION) }
        arrSound = arrayListOf()
        rm.cursor.use {
            while (it.moveToNext()) {
                arrSound.add(it.getString(RingtoneManager.TITLE_COLUMN_INDEX))
            }
        }
    }

    private fun getNotiSound() {
        val prefs = mActivity.getSharedPreferences("SaveNotiSound", Context.MODE_PRIVATE)
        saveSoundPosition = prefs.getInt("svaeSoundPosition", 0)
    }

    private fun setNotiSound() {
        txtSetSound.text = rm.getRingtone(saveSoundPosition).getTitle(mActivity)
    }

    private fun getNotiType() {
        val prefs = mActivity.getSharedPreferences("SaveNoti", Context.MODE_PRIVATE)
        saveRbPosition = prefs.getInt("svaeRbPosition", 0)
    }

    private fun loadSettings() {
        val prefs = mActivity.getSharedPreferences("SaveSetting", Context.MODE_PRIVATE)
        chkSound.isChecked = prefs.getBoolean("setSound", true)
        chkVibrate.isChecked = prefs.getBoolean("setVibrate", true)
    }

    private fun saveNotiSound() {
        val prefs = mActivity.getSharedPreferences("SaveNotiSound", Context.MODE_PRIVATE).edit()
        prefs.putString("rmUri", rm.getRingtone(selSoundPosition).toString())
        prefs.putString("rmPath", rm.getRingtoneUri(selSoundPosition).toString())
        prefs.putInt("svaeSoundPosition", selSoundPosition)
        prefs.apply()
    }

    private fun saveNotiType() {
        val prefs = mActivity.getSharedPreferences("SaveNoti", Context.MODE_PRIVATE).edit()
        prefs.putString("setNoti", strSelNoti)
        prefs.putInt("svaeRbPosition", selRbPosition)
        prefs.apply()
    }

    private fun showDialog(mode: Int) {
        mDlgMode = mode
        mDlg = Dialog(mActivity).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
            setContentView(R.layout.dlg_radio_list)

            val txtTitle = findViewById<TextView>(R.id.txt_title)
            val lvData = findViewById<ListView>(R.id.lv_radio_data)
            val btnOK = findViewById<Button>(R.id.btn_list_dig_ok)
            val btnCancel = findViewById<Button>(R.id.btn_list_dig_cancel)

            when (mode) {
                DIALOG_MODE_NOTI_SOUND -> {
                    txtTitle.text = getString(R.string.str_set_nofi_sound_dig)
                    mRadioListAdapter = RadioListAdapter(mActivity, arrSound)
                    selSoundPosition = saveSoundPosition
                    mRadioListAdapter.setSelectedIndex(selSoundPosition)
                    lvData.adapter = mRadioListAdapter

                    lvData.setOnItemClickListener { _, _, position, _ ->
                        selSoundPosition = position
                        val ringtoneUri = rm.getRingtoneUri(position)
                        val ringtone = RingtoneManager.getRingtone(mActivity, ringtoneUri)
                        ringtone.streamType = AudioManager.STREAM_NOTIFICATION
                        ringtoneTemp?.stop()
                        ringtoneTemp = ringtone
                        ringtone.play()
                        mRadioListAdapter.setSelectedIndex(position)
                        mRadioListAdapter.notifyDataSetChanged()
                    }

                    btnOK.setOnClickListener {
                        saveSoundPosition = selSoundPosition
                        txtSetSound.text = rm.getRingtone(saveSoundPosition).getTitle(mActivity)
                        saveNotiSound()
                        dismiss()
                    }
                }
                DIALOG_MODE_NOTI_TYPE -> {
                    txtTitle.text = getString(R.string.str_setting_alrim_popup)
                    mRadioListAdapter = RadioListAdapter(mActivity, arrNotiType)
                    selRbPosition = saveRbPosition
                    mRadioListAdapter.setSelectedIndex(selRbPosition)
                    lvData.adapter = mRadioListAdapter

                    lvData.setOnItemClickListener { _, _, position, _ ->
                        selRbPosition = position
                        strSelNoti = arrNotiType[position]
                        mRadioListAdapter.setSelectedIndex(position)
                        mRadioListAdapter.notifyDataSetChanged()
                    }

                    btnOK.setOnClickListener {
                        saveRbPosition = selRbPosition
                        saveNotiType()
                        dismiss()
                    }
                }
            }

            btnCancel.setOnClickListener { dismiss() }
            show()
        }
    }

    private fun dismiss() {
        mDlgMode = -1
        mDlg?.dismiss()
    }

    override fun onPause() {
        super.onPause()
        val prefs = mActivity.getSharedPreferences("SaveSetting", Context.MODE_PRIVATE).edit()
        prefs.putBoolean("setSound", chkSound.isChecked)
        prefs.putBoolean("setVibrate", chkVibrate.isChecked)
        prefs.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        mDlg?.dismiss()
        (layoutBG.background as? BitmapDrawable)?.bitmap?.recycle()
        layoutBG.background = null
    }
}
