package tw.breadcrumbs.makeda

//import com.google.android.gms.location.LocationServices

//import sun.jvm.hotspot.utilities.IntArray
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.setting_fragment.*
import org.json.JSONObject
import tw.breadcrumbs.makeda.CloudObj.Cloud_Helper


class Setting_Fragment : androidx.fragment.app.Fragment() {
    val ACCOUNT_UPDATE_URL = "//TODO"
    val URL_USER_REGISTER = "//TODO"
    val URL_USER_LOGIN = "//TODO"
    val CloudHelperURL = "//TODO"
    private val debugmode = false
    val user_name_pref = "user_name"
    val user_level_pref = "user_level"
    val user_exp_pref = "user_exp"
    val user_next_exp_pref = "user_next_exp"
    val user_email_pref = "user_email"
    val user_phone_pref = "user_phone"
    val user_activity_key_pref = "activity_key"
    val dwn_pps_en_pref = "dwn_pps_en"
    val user_power_pref = "user_power"
    val user_status_pref = "user_status"
    val user_googleuid_pref = "googleUID"
    val user_serverauthcode_pref = "serverauthcode"
    val dash_country_pref = "dash_country_pref"
    val dash_city_pref = "dash_city_pref"
    val dash_img_dwn_pref = "dash_img_dwn_pref"
    private var settingInit = 0
    private var verNameText: TextView? = null
    var prefs: SharedPreferences? = null
    private val TAG = "SignInActivity"
    private val RC_SIGN_IN = 9001
    private var mGoogleSignInClient: GoogleSignInClient? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.setting_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()
        // [START on_start_sign_in]
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        val account = GoogleSignIn.getLastSignedInAccount(activity)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        readVersionName()
        if (settingInit == 0) {
            prefs = this@Setting_Fragment.context!!.getSharedPreferences("MAKEDA_USER_SET", MODE_PRIVATE)
            settingInit = 1
        }
        get_local_setting()
        val username = prefs!!.getString(user_name_pref, " ")

        if ( username!!.isEmpty() || username.equals(" ") || username.length < 2 || username.equals("not fill")) {
                button_click_listener()
        } else {
            val id = (activity as MainActivity).currFragmentID()
            if (id == 0) {
                val hello = HelloUser_Fragment()
                (activity as MainActivity).switchFragment(this@Setting_Fragment, hello, 0)
            }
            if (debugmode) Log.i("Setting_Fragment", "Current selected fragment id: $id")
        }
        forgetPasswordSetup()
        privacy_policySetup()
        (activity as MainActivity).setNavigationID(0)

        // [START configure_signin]
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val serverClientId = getString(R.string.server_client_id);
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
    }

    // [START onActivityResult]
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) { // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task =
                GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }
    // [END onActivityResult]
    // [START handleSignInResult]
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(
                ApiException::class.java
            )
            if (debugmode) {
                Log.w(
                    "Setting_Fragment",
                    "signInResult:account=" + account +
                            "\n Google User Name = " + completedTask.result?.displayName +
                            "\n Google User Email = " + completedTask.result?.email +
                            "\n Google User ID = " + completedTask.result?.id +
                            "\n Google serverAuthCode = " + completedTask.result?.serverAuthCode +
                            "\n Google isExpired = " + completedTask.result?.isExpired +
                            "\n Google photoUrl = " + completedTask.result?.photoUrl +
                            "\n Google grantedScopes = " + completedTask.result?.grantedScopes
                )
            }
            val email = completedTask.result?.email
            val serverauthcode = completedTask.result?.serverAuthCode
            val googleUserID = completedTask.result?.id
            val username = completedTask.result?.displayName
            val cloud_cmd = "CMD=//TODO&email=$email&serverauthcode=$serverauthcode&googleUserID=$googleUserID&name=$username"
            Cloud_Helper(this@Setting_Fragment.context!!) {
                if (debugmode) Log.i("Setting_Fragment", "google oauth login feeback: $it")
                val cloud_db = JSONObject(it)
                val status = cloud_db.get("status") as Boolean
                if (!status) {
                    signOut()
                }
                user_feeback_setup(it)
            }.execute("POST", CloudHelperURL, cloud_cmd.toString())
            // Signed in successfully, show authenticated UI.
            //updateUI(account)
        } catch (e: ApiException) { // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            //if (debugmode) {
                Log.w(
                    "Setting_Fragment",
                    "signInResult:failed code=" + e.statusCode +
                            "error : " + e
                )
            //}

            //updateUI(null)
        }
    }
    // [END handleSignInResult]

    // [END handleSignInResult]
    // [START signIn]
    private fun signIn() {
        val signInIntent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(
            signInIntent,
            RC_SIGN_IN
        )
    }
    // [END signIn]

    // [END signIn]
    // [START signOut]
    private fun signOut() {
        mGoogleSignInClient!!.signOut()
            .addOnCompleteListener {

            }
    }


    @SuppressLint("SetTextI18n")
    fun readVersionName() {
        verNameText = view!!.findViewById(R.id.verName) as TextView
        var versionName : String? = null
        val packageManager = context!!.getPackageManager()
        var packageInfo: PackageInfo? = null

        try {
            packageInfo=packageManager.getPackageInfo(context!!.getPackageName(),0);
            versionName=packageInfo.versionName;
        } catch (e:Exception) {
            if (debugmode) Log.i("Setting_Fragment", "getVersionName : Error: $e")
            //e.printStackTrace();
        }
        val readstr = verNameText!!.text
        verNameText!!.text = "$readstr  $versionName"
    }

    fun local_setting_isReady():Boolean {
        if (prefs == null) {
            return false
        }
        val value = prefs!!.getString(user_name_pref, " ")

        return !value!!.isEmpty()
    }

    fun localSettingInit() {
        prefs = this@Setting_Fragment.context?.getSharedPreferences("MAKEDA_USER_SET", MODE_PRIVATE)
        if (prefs != null) {
            prefs!!.edit().putString(user_name_pref, " ").apply()
            prefs!!.edit().putString(user_email_pref, " ").apply()
            prefs!!.edit().putString(user_phone_pref, " ").apply()
            prefs!!.edit().putInt(user_power_pref, 0).apply()
            prefs!!.edit().putInt(user_status_pref, 0).apply()
            prefs!!.edit().putBoolean(dwn_pps_en_pref, true).apply()
            prefs!!.edit().putString(user_googleuid_pref, " ").apply()
            prefs!!.edit().putString(user_serverauthcode_pref, " ").apply()
            prefs!!.edit().putString(dash_country_pref, " ").apply()
            prefs!!.edit().putString(dash_city_pref, " ").apply()
            prefs!!.edit().putInt(dash_img_dwn_pref, 0).apply()
        }
    }

    fun get_local_setting() {
        if (!local_setting_isReady()) {
            if (debugmode) Log.i("Setting_Fragment", "Get user name , is empty")
            localSettingInit()
        }
    }

    fun button_click_listener() {
        user_login.setOnClickListener {
            if (debugmode) {
                Log.i("Setting_Fragment", "input user email ${user_email.text.toString()} ," +
                        " user password ${user_password.text.toString()}")
            }
            val email = user_email.text.toString()
            val pass = user_password.text.toString()
            val cloud_cmd = "//TODO=$email&//TODO=$pass"
            Cloud_Helper(this@Setting_Fragment.context!!) {
                if (debugmode) Log.i("Setting_Fragment", "user_login feeback: $it")
                user_feeback_setup(it)
            }.execute("POST", URL_USER_LOGIN, cloud_cmd.toString())
        }

        google_login.setOnClickListener {
            signIn()
        }

        user_register.setOnClickListener {
            if (debugmode) {
                Log.i("Setting_Fragment", "input user name ${user_name_r.text.toString()}" +
                    " user email ${user_email_r.text.toString()} ," +
                        " user phone ${user_phone_r.text.toString()}" +
                        " user password ${user_password_r.text.toString()}" +
                        " user password 2 ${user_password2_r.text.toString()}")
            }
            val name = user_name_r.text.toString()
            val phone = user_phone_r.text.toString()
            val email = user_email_r.text.toString()
            val pass = user_password_r.text.toString()
            val pass2 = user_password_r.text.toString()
            if (pass != pass2) {
                Toast.makeText(this@Setting_Fragment.context, "請確認輸入的密碼一樣", Toast.LENGTH_LONG).show()
            } else {
                val cloud_cmd = "//TODO=$email&//TODO=$pass&//TODO=$name&//TODO=$phone"
                Cloud_Helper(this@Setting_Fragment.context!!) {
                    if (debugmode) Log.i("Setting_Fragment", "user_login feeback: $it")
                    user_feeback_setup(it!!)
                }.execute("POST", URL_USER_REGISTER, cloud_cmd.toString())
            }
        }
    }

    fun privacy_policySetup() {
        privacy_policy!!.setOnClickListener {
            val url = "//TODO"
            (activity as MainActivity).openWeb(url)
        }
    }

    fun forgetPasswordSetup() {
        val forgetPassBtn = view!!.findViewById(R.id.user_forget_pass) as Button
        forgetPassBtn.setOnClickListener {
            forgetPassAlert()
        }
    }

    fun forgetPassAlert(){
        val builder = AlertDialog.Builder(this@Setting_Fragment.context)
        val inflater = activity!!.layoutInflater
        val dailogView = inflater.inflate(R.layout.forget_password_dailog, null)
        val userEmailText = dailogView!!.findViewById(R.id.user_email_input) as EditText
        val dialog: AlertDialog = builder.setTitle(R.string.forget_password_hint_zh)
            .setView(dailogView)
            .setPositiveButton("OK") { dialog, which ->
                val userEmail = userEmailText.text.toString()
                if (userEmail.isEmpty() || !userEmail.contains('@')) {
                    Toast.makeText(this@Setting_Fragment.context, R.string.forget_password_null_fail_zh, Toast.LENGTH_LONG).show()
                } else {
                    forgetPasswordAction(userEmail)
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


    fun forgetPasswordAction(userEmail:String) {
        val cloud_cmd = "CMD=//TODO&email=$userEmail"
        if (debugmode) Log.i("Dash_Fragment", "upToCloud : send: $cloud_cmd")
        Cloud_Helper(this@Setting_Fragment.context!!) {
            if (it == null) {
                //println("connection error")
                return@Cloud_Helper
            } else {
                val cloud_db = JSONObject(it)
                val status = cloud_db.get("status") as Boolean
                if (status) {
                    Toast.makeText(
                        this@Setting_Fragment.context,
                        R.string.forget_password_success_zh,
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(this@Setting_Fragment.context, R.string.forget_password_fail_zh, Toast.LENGTH_LONG)
                        .show()
                    //Toast.makeText(this@Setting_Fragment.context, R.string.reset_password_fail_zh, Toast.LENGTH_LONG).show()
                }
            }
        }.execute("POST", ACCOUNT_UPDATE_URL, cloud_cmd.toString())
    }

    fun user_feeback_setup(feeback: String?) {
        val user_set = JSONObject(feeback)
        if (!user_set.isNull("status")) {
            val status = user_set.get("status") as Boolean
            if (status) {
                if (debugmode) Log.i("Setting_Fragment", "user_login.setOnClickListener : status:$status")
                if (!user_set.isNull("user")) {
                    val user = user_set.get("user") as JSONObject
                    prefs!!.edit().putString(user_name_pref, user.get("name").toString()).apply()
                    prefs!!.edit().putString(user_email_pref, user.get("email").toString()).apply()
                    prefs!!.edit().putString(user_phone_pref, user.get("phone").toString()).apply()
                    prefs!!.edit().putInt(user_power_pref, user.get("power") as Int).apply()
                    prefs!!.edit().putInt(user_status_pref, user.get("status") as Int).apply()
                    prefs!!.edit().putString(user_googleuid_pref, user.get("googleUID").toString()).apply()
                    prefs!!.edit().putString(user_serverauthcode_pref, user.get("serverauthcode").toString()).apply()
                    prefs!!.edit().putString(user_activity_key_pref, user.get("activity_key").toString()).apply()

                    val hello = HelloUser_Fragment()
                    (activity as MainActivity).switchFragment(this@Setting_Fragment, hello, 0)
                }
            } else {
                if (!user_set.isNull("message")) {
                    val msg = user_set.get("message").toString()
                    if (msg == "User already exist") {
                        Toast.makeText(this@Setting_Fragment.context, "EMail或手機已被註冊過！", Toast.LENGTH_LONG).show()
                    }
                }
                Toast.makeText(this@Setting_Fragment.context, "哎唷！帳號或密碼錯誤！", Toast.LENGTH_LONG).show()
            }
        }
    }
}