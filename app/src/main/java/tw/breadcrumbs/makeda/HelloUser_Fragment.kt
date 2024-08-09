package tw.breadcrumbs.makeda

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageInfo
import android.graphics.Color
import android.opengl.Visibility
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import kotlinx.android.synthetic.main.hello_user_fragment.*
import org.json.JSONObject
import tw.breadcrumbs.makeda.CloudObj.Cloud_Helper
import java.lang.Exception

class HelloUser_Fragment : androidx.fragment.app.Fragment() {
    private val debugmode = false
    val ACCOUNT_UPDATE_URL = "//TODO"
    private var userName:EditText? = null
    private var userEmail:TextView? = null
    private var userPhone:EditText? = null
    private var userLv = 1
    private var userExp = 0
    private var userNextExp = 100
    private var userNameChgBtn: Button? = null
    var userNameChanging = 0
    private var userPhoneChgBtn: Button? = null
    var userPhoneChanging = 0
    var set_frag:Setting_Fragment? = null
    private var verNameText: TextView? = null
    private var mGoogleSignInClient: GoogleSignInClient? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.hello_user_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        readVersionName()

        //val serverClientId = getString(R.string.server_client_id);
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            //.requestScopes(Scope(Scopes.DRIVE_APPFOLDER))
            //.requestServerAuthCode(serverClientId)
            .requestEmail()
            .build()
        // [END configure_signin]

        // [START build_client]
        // Build a GoogleSignInClient with the options specified by gso.
        // [END configure_signin]
        // [START build_client]
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient((activity as MainActivity), gso)

        if (set_frag == null)
            set_frag = (activity as MainActivity).setting_frag as Setting_Fragment
        readUserName()
        readUserEmail()
        readUserPhone()
        readUserLvInfo()
        setup_password_reset_button()
        setup_logout_button()
        downloadUserLvData()
        privacy_policySetup()
    }

    fun downloadUserLvData() {
        var googleuserid = ""
        var email = ""
        var activityKey = ""
        var cloud_cmd = ""
        try {
            set_frag!!.let {
                googleuserid =
                    set_frag!!.prefs!!.getString(set_frag!!.user_googleuid_pref, "") as String
                email = set_frag!!.prefs!!.getString(set_frag!!.user_email_pref, "") as String
                activityKey =
                    set_frag!!.prefs!!.getString(set_frag!!.user_activity_key_pref, "") as String
            }
            //val user_googleuid_pref = "googleUID"
            if (googleuserid.isNotEmpty() && !googleuserid.equals(" ") && !googleuserid.equals("null")) {
                cloud_cmd = "CMD=//TODO&email=$email&googleuserid=$googleuserid"
            } else if (activityKey.isNotEmpty() && !activityKey.equals(" ")) {
                cloud_cmd = "CMD=//TODO&email=$email&activity_key=$activityKey"
            }
            if (debugmode)
                Log.i("HelloUser_Fragment", "upToCloud : send: $cloud_cmd")
            Cloud_Helper(this@HelloUser_Fragment.context!!) {
                if (it == null) {
                    if (debugmode)
                        Log.i("HelloUser_Fragment", "Cloud Helper Failure")
                    return@Cloud_Helper
                } else {
                    if (debugmode) Log.i("HelloUser_Fragment", "Cloud Helper Resp : $it")
                    val cloud_db = JSONObject(it)
                    val status = cloud_db.get("status") as Boolean
                    if (status) {
                        if (debugmode) Log.i(
                            "HelloUser_Fragment",
                            " Download user level data success"
                        )
                        val user = cloud_db.get("user") as JSONObject
                        if (!user.isNull("level")) {
                            val userLv = user.get("level") as Int
                            set_frag!!.prefs!!.edit().putInt(set_frag!!.user_level_pref, userLv)
                                .apply()
                            levelImg?.post {
                                val lvText = getString(R.string.user_level) + "$userLv"
                                levelImg.setText(lvText)
                            }
                        }
                        if (!user.isNull("exp") && !user.isNull("next_exp")) {
                            val exp = user.get("exp") as Int
                            val next_exp = user.get("next_exp") as Int
                            userexp?.post {
                                val expText =
                                    " $exp   " + getString(R.string.user_exp_splite) + "   $next_exp"
                                userexp.setText(expText)
                                var prog = 0.0
                                if (exp > 0) {
                                    prog = exp.toFloat() / next_exp.toFloat() * 100.0
                                    if (debugmode) Log.i("HelloUser_Fragment", " prog = $prog exp = $exp, next_exp = $next_exp")
                                }
                                userexp_bar.secondaryProgress = prog.toInt()
                                set_frag!!.prefs!!.edit().putInt(set_frag!!.user_exp_pref, exp)
                                    .apply()
                                set_frag!!.prefs!!.edit()
                                    .putInt(set_frag!!.user_next_exp_pref, next_exp).apply()
                            }
                        }


                    } else {
                        Toast.makeText(
                                this@HelloUser_Fragment.context,
                                R.string.reset_name_FAIL_zh,
                                Toast.LENGTH_LONG
                            )
                            .show()
                    }
                }
            }.execute("POST", ACCOUNT_UPDATE_URL, cloud_cmd.toString())
        } catch (e: Exception) {

        }
    }

    fun privacy_policySetup() {
        hello_privacy_policy!!.setOnClickListener {
            val url = "//TODO"
            (activity as MainActivity).openWeb(url)
        }
    }

    fun readUserLvInfo() {
        //set_frag!!.prefs!!.edit().putInt(set_frag!!.user_level_pref, userLv)
        //    .apply()
        userLv = set_frag!!.prefs!!.getInt(set_frag!!.user_level_pref, 0)
        userExp = set_frag!!.prefs!!.getInt(set_frag!!.user_exp_pref, 0)
        userNextExp = set_frag!!.prefs!!.getInt(set_frag!!.user_next_exp_pref, 0)

        when {
            userLv > 0 -> {
                val lvText = getString(R.string.user_level) + "$userLv"
                levelImg.setText(lvText)
            }
            userExp > 0 && userNextExp > 0 -> {
                val expText =
                    " $userExp   " + getString(R.string.user_exp_splite) + "   $userNextExp"
                userexp.setText(expText)
                var prog = 0.0
                if (userExp > 0) {
                    prog = userExp.toFloat() / userNextExp.toFloat() * 100.0
                    if (debugmode) Log.i("HelloUser_Fragment", " prog = $prog userExp = $userExp, userNextExp = $userNextExp")
                }

                userexp_bar.secondaryProgress = prog.toInt()
            }
        }


    }

    fun readUserName() {
        userName = view!!.findViewById(R.id.user_name) as EditText
        userNameChgBtn = view!!.findViewById(R.id.user_name_change)
        disableEditText(userName!!)
        if(!set_frag!!.local_setting_isReady()) {
            set_frag!!.localSettingInit()
        }
        set_frag!!.let {
            userName!!.setText(set_frag!!.prefs!!.getString(set_frag!!.user_name_pref, ""))//text =
        }

        userNameChgBtn!!.setOnClickListener{
            if (userNameChanging == 0) {
                enableEditText(userName!!)
                userNameChgBtn!!.setText(getString(R.string.hello_changeOK_btn_zh))
                userNameChanging = 1
            } else {
                disableEditText(userName!!)
                userNameChgBtn!!.setText(getString(R.string.hello_change_btn_zh))
                userNameChanging = 0
                val new_name = userName!!.text.toString()
                renewUserName(new_name)
            }
        }
    }

    fun readUserEmail() {
        userEmail = view!!.findViewById(R.id.user_email_text) as TextView
        if(!set_frag!!.local_setting_isReady()) {
            set_frag!!.localSettingInit()
        }
        set_frag!!.let {
            userEmail!!.text = set_frag!!.prefs!!.getString(set_frag!!.user_email_pref, "")
        }
    }

    fun readUserPhone() {
        userPhone = view!!.findViewById(R.id.user_phone_text) as EditText
        userPhoneChgBtn = view!!.findViewById(R.id.user_phone_change) as Button
        disableEditText(userPhone!!)
        if(!set_frag!!.local_setting_isReady()) {
            set_frag!!.localSettingInit()
        }
        set_frag!!.let {
            val phone = set_frag!!.prefs!!.getString(set_frag!!.user_phone_pref, "")
            if (phone!!.equals("null")) {
                userPhone!!.setText(" ")//text =
            } else {
                userPhone!!.setText(phone)//text = phone
            }
        }

        userPhoneChgBtn!!.setOnClickListener {
            if (userPhoneChanging == 0) {
                enableEditText(userPhone!!)
                userPhoneChgBtn!!.setText(getString(R.string.hello_changeOK_btn_zh))
                userPhoneChanging = 1
            } else {
                userPhoneChanging = 0
                disableEditText(userPhone!!)
                userPhoneChgBtn!!.setText(getString(R.string.hello_change_btn_zh))
                val new_phone = userPhone!!.text.toString()
                renewUserPhone(new_phone)
            }
        }
    }

    fun renewUserName(name: String) {
        //"CMD": //TODO, "email": $email, "googleuserid": $googleuserid, "newname" : $newname
        var googleuserid = ""
        var email = ""
        var activityKey = ""
        var cloud_cmd = ""

        set_frag!!.let {
            googleuserid = set_frag!!.prefs!!.getString(set_frag!!.user_googleuid_pref, "") as String
            email = set_frag!!.prefs!!.getString(set_frag!!.user_email_pref, "") as String
            activityKey = set_frag!!.prefs!!.getString(set_frag!!.user_activity_key_pref, "") as String
        }
         //val user_googleuid_pref = "googleUID"
        if (googleuserid.isNotEmpty() && !googleuserid.equals(" ")  && !googleuserid.equals("null")) {
            cloud_cmd = "CMD=//TODO&email=$email&googleuserid=$googleuserid&" +
                    "newname=$name"
        } else if (activityKey.isNotEmpty() && !activityKey.equals(" ")) {
            cloud_cmd = "CMD=//TODO&email=$email&activity_key=$activityKey&" +
                    "newname=$name"
        }
        if (debugmode)
            Log.i("HelloUser_Fragment", "upToCloud : send: $cloud_cmd")
        Cloud_Helper(this@HelloUser_Fragment.context!!) {
            if (it == null) {
                if (debugmode)
                    Log.i("HelloUser_Fragment", "Cloud Helper Failure")
                return@Cloud_Helper
            } else {
                if (debugmode) Log.i("HelloUser_Fragment", "Cloud Helper Resp : $it")
                val cloud_db = JSONObject(it)
                val status = cloud_db.get("status") as Boolean
                if (status) {
                    if (debugmode) Log.i("HelloUser_Fragment", " User Name update Success")
                    Toast.makeText(
                        this@HelloUser_Fragment.context,
                        R.string.reset_name_OK_zh,
                        Toast.LENGTH_LONG
                    ).show()
                    set_frag!!.prefs!!.edit().putString(set_frag!!.user_name_pref, name).apply()
                    //logout()
                } else {
                    Toast.makeText(this@HelloUser_Fragment.context, R.string.reset_name_FAIL_zh, Toast.LENGTH_LONG)
                        .show()
                }
            }
        }.execute("POST", ACCOUNT_UPDATE_URL, cloud_cmd.toString())
    }

    fun renewUserPhone(phone: String) {
        //"CMD": //TODO, "email": $email, "googleuserid": $googleuserid, "phone" : $phone
        var googleuserid = ""
        var activityKey = ""
        var email = ""
        var cloud_cmd = ""
        set_frag!!.let {
            googleuserid = set_frag!!.prefs!!.getString(set_frag!!.user_googleuid_pref, "") as String
            email = set_frag!!.prefs!!.getString(set_frag!!.user_email_pref, "") as String
            activityKey = set_frag!!.prefs!!.getString(set_frag!!.user_activity_key_pref, "") as String
        }
        //val user_googleuid_pref = "googleUID"
        if (googleuserid.isNotEmpty() && !googleuserid.equals(" ")  && !googleuserid.equals("null")) {
            cloud_cmd = "CMD=//TODO&email=$email&googleuserid=$googleuserid&" +
                    "phone=$phone"
        } else if (activityKey.isNotEmpty()) {
            cloud_cmd = "CMD=//TODO&email=$email&//TODO=$activityKey&" +
                    "phone=$phone"
        }
        if (debugmode)
            Log.i("HelloUser_Fragment", "upToCloud : send: $cloud_cmd")
        Cloud_Helper(this@HelloUser_Fragment.context!!) {
            if (it == null) {
                //println("connection error")
                if (debugmode) Log.i("HelloUser_Fragment", "Cloud Helper Connect failure")
                return@Cloud_Helper
            } else {
                if (debugmode) Log.i("HelloUser_Fragment", "Cloud Helper Resp : $it")
                val cloud_db = JSONObject(it)
                val status = cloud_db.get("status") as Boolean
                if (status) {
                    if (debugmode) Log.i("HelloUser_Fragment", " User Phone update Success")
                    Toast.makeText(
                        this@HelloUser_Fragment.context,
                        R.string.reset_phone_OK_zh,
                        Toast.LENGTH_LONG
                    ).show()
                    set_frag!!.prefs!!.edit().putString(set_frag!!.user_phone_pref, phone).apply()
                    //getString(set_frag!!.user_phone_pref, "") as String
                    //logout()
                } else {
                    Toast.makeText(this@HelloUser_Fragment.context, R.string.reset_phone_FAIL_zh, Toast.LENGTH_LONG)
                        .show()
                }
            }
        }.execute("POST", ACCOUNT_UPDATE_URL, cloud_cmd.toString())
    }

    @SuppressLint("SetTextI18n")
    fun readVersionName() {
        verNameText = view!!.findViewById(R.id.hello_verName) as TextView
        var versionName : String? = null
        var packageManager = context!!.packageManager
        var packageInfo: PackageInfo? = null

        try {
            packageInfo=packageManager.getPackageInfo(context!!.packageName,0);
            versionName=packageInfo.versionName;
        } catch (e: Exception) {
            if (debugmode)
                Log.i("HelloUser_Fragment", "getVersionName : Error: $e")
            //e.printStackTrace();
        }
        val readstr = verNameText!!.text
        verNameText!!.text = "$readstr  $versionName"
    }

    fun setup_password_reset_button() {
        user_pass_change.setOnClickListener{
            var googleuserid = ""
            set_frag!!.let {
                googleuserid =
                    set_frag!!.prefs!!.getString(set_frag!!.user_googleuid_pref, "") as String
            }
            if (googleuserid.isNotEmpty() && !googleuserid.equals("null") && !googleuserid.equals(" ")) {
                //user_pass_change.setText()
            }
            resetPassAlert()
        }
    }

    fun setup_logout_button() {
        user_logout.setOnClickListener {
            logout()
        }
    }

    fun resetPassAlert(){
        //"CMD": //TODO, "email": $email, "googleuserid": $googleuserid, "password" : $password
        var googleuid = ""//set_frag!!.prefs!!.getString(set_frag!!.user_googleuid_pref, "")
        var mode = 0
        set_frag!!.let {
            googleuid = set_frag!!.prefs!!.getString(set_frag!!.user_googleuid_pref, "") as String
        }
        if (googleuid.isNotEmpty() && !googleuid.equals(" ") && !googleuid.equals("null")) {
            mode = 1
        }
        val builder = AlertDialog.Builder(this@HelloUser_Fragment.context)
        val inflater = activity!!.layoutInflater
        val dailogView = inflater.inflate(R.layout.reset_password_dailog, null)
        val oldPassText = dailogView!!.findViewById(R.id.old_password) as EditText
        val newPass1Text = dailogView.findViewById(R.id.reset_password_r1) as EditText
        val newPass2Text = dailogView.findViewById(R.id.reset_password_r2) as EditText
        if (mode == 1) {
            oldPassText.isEnabled = false
            oldPassText.visibility = View.INVISIBLE
        } else {
            oldPassText.isEnabled = true
            oldPassText.visibility = View.VISIBLE
        }
        val dialog: AlertDialog = builder.setTitle(R.string.hello_userpass_btn_zh)
            .setView(dailogView)
            .setPositiveButton("OK") { dialog, which ->
                var oldpass = ""
                if (mode == 0) {
                    oldpass = oldPassText.text.toString()
                }
                val input_1 = newPass1Text.text.toString()
                val input_2 = newPass2Text.text.toString()
                if ((mode == 0 && oldpass.isEmpty()) || input_1.isEmpty() || input_2.isEmpty()) {
                    Toast.makeText(this@HelloUser_Fragment.context, R.string.reset_password_null_fail_zh, Toast.LENGTH_LONG).show()
                } else if (input_1 != input_2) {
                    Toast.makeText(this@HelloUser_Fragment.context, R.string.reset_password_diff_fail_zh, Toast.LENGTH_LONG).show()
                } else {
                    val userEmail = set_frag!!.prefs!!.getString(set_frag!!.user_email_pref, "")
                    resetPassword(userEmail!!, oldpass, input_1, googleuid)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }

            .create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
    }

    fun resetPassword(userEmail: String, oldPass: String, newPass: String, googleuid : String) {
        var cloud_cmd = ""
        if (googleuid.isNotEmpty() && !googleuid.equals(" ") && !googleuid.equals("null")) {
            cloud_cmd = "CMD=//TODO&email=$userEmail&googleuserid=$googleuid&" +
                    "password=$newPass"
        } else {
            cloud_cmd = "CMD=//TODO&email=$userEmail&oldpassword=$oldPass&" +
                    "newpassword=$newPass"
        }
        if (debugmode)
            Log.i("Dash_Fragment", "upToCloud : send: $cloud_cmd")
        Cloud_Helper(this@HelloUser_Fragment.context!!) {
            if (it == null) {
                //println("connection error")
                return@Cloud_Helper
            } else {
                val cloud_db = JSONObject(it)
                val status = cloud_db.get("status") as Boolean
                if (status) {
                    Toast.makeText(
                        this@HelloUser_Fragment.context,
                        R.string.reset_password_success_zh,
                        Toast.LENGTH_LONG
                    ).show()
                    logout()
                } else {
                    Toast.makeText(this@HelloUser_Fragment.context, R.string.reset_password_fail_zh, Toast.LENGTH_LONG)
                        .show()
                }
            }
        }.execute("POST", ACCOUNT_UPDATE_URL, cloud_cmd.toString())
    }

    fun logout() {
        set_frag!!.prefs!!.edit().putString(set_frag!!.user_name_pref, "not fill").apply()
        set_frag!!.prefs!!.edit().putString(set_frag!!.user_email_pref, "not fill").apply()
        set_frag!!.prefs!!.edit().putString(set_frag!!.user_phone_pref, "not fill").apply()
        set_frag!!.prefs!!.edit().putInt(set_frag!!.user_power_pref, 0).apply()
        set_frag!!.prefs!!.edit().putInt(set_frag!!.user_status_pref, 0).apply()
        set_frag!!.prefs!!.edit().putString(set_frag!!.user_googleuid_pref, " ").apply()
        set_frag!!.prefs!!.edit().putString(set_frag!!.user_serverauthcode_pref, " ").apply()
        set_frag!!.prefs!!.edit().putString(set_frag!!.user_activity_key_pref, "").apply()
        mGoogleSignInClient!!.signOut()
            .addOnCompleteListener {

            }
        (activity as MainActivity).onBackPressed()
    }

    private fun enableEditText(editText: EditText) {
        if (debugmode) Log.i("HelloUser_Fragment : ", "enableEditText working")
        with(editText) {
            isFocusable = true
            isClickable = true
            isCursorVisible = true
            //isFocusableInTouchMode = true
            //isEnabled = true
            setTextIsSelectable(true)
            //movementMethod = ArrowKeyMovementMethod.getInstance()
            //inputType = type//InputType.TYPE_CLASS_TEXT
        }
    }

    private fun disableEditText(editText: EditText) {
        with(editText) {
            isFocusable = false
            isClickable = false
            isCursorVisible = false
            setTextIsSelectable(false)
            view!!.clearFocus()
            //movementMethod = null //ArrowKeyMovementMethod.getInstance()
            //val imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            //imm.hideSoftInputFromWindow(editText.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }
}