package tw.breadcrumbs.makeda

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.*
import android.content.ContentValues.TAG
import android.text.InputType
import kotlinx.android.synthetic.main.review_fragment.*
import android.content.Context
import android.opengl.Visibility
import android.text.method.LinkMovementMethod
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.view.*
import tw.breadcrumbs.makeda.CommentsHandler.Comments_Fragment
import tw.breadcrumbs.makeda.DBModules.ItemDAO
import tw.breadcrumbs.makeda.DBModules.TripPlanDAO
import tw.breadcrumbs.makeda.TripPlanning.PlanningHelper
import tw.breadcrumbs.makeda.TripPlanning.TripPlanning_Fragment
import tw.breadcrumbs.makeda.dataModel.CountriesNCities
import tw.breadcrumbs.makeda.dataModel.PPModel
import java.net.URLEncoder


class Review_Fragment : androidx.fragment.app.Fragment() {
    private val debugmode = false
    private var sel_country = String()
    private var sel_city = String()
    private var item_dao : ItemDAO? = null
    private var pp_model_seted : PPModel? = null
    var set_frag:Setting_Fragment? = null
    private var user_power = 0
    private var useremail : String? = null

    private var commentsMoreBtn:Button? = null
    private var updateBtn:ImageButton? = null
    private var editBtn:ImageButton? = null
    private var backBtn:ImageButton? = null
    private var mapBtn:ImageButton? = null
    private var callBtn:ImageButton? = null
    private var webBtn:ImageButton? = null
    private var fbBtn:ImageButton? = null
    private var bloggerBtn:ImageButton? = null
    private var intoTripBtn:ImageButton? = null
    private var webSearch:WebView? = null
    private var webUrl: EditText? = null

    private var comments_frag : Comments_Fragment? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.review_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (pp_model_seted != null) {
            set_frag = (activity as MainActivity).setting_frag as Setting_Fragment
            if (set_frag!!.local_setting_isReady()) {
                useremail = set_frag!!.prefs!!.getString(set_frag!!.user_email_pref, " ")
                user_power = set_frag!!.prefs!!.getInt(set_frag!!.user_power_pref, 0)
            } else {
                user_power = 0
            }
            if (debugmode) Log.i("Review_Fragment", "User Power : $user_power")
            item_dao = ItemDAO(this@Review_Fragment.context!!)

            SetupView()
            pp_setup()
            pp_model_seted!!.let {
                comments_frag = Comments_Fragment()
                comments_frag!!.setPPModel(it)
                comments_frag!!.downloadCmts(this@Review_Fragment.context!!, false)
                val cmtsThread = Thread(Runnable {
                    var status = true
                    while (status) {
                        status = comments_frag!!.getCmtsDownloadStatus()

                        Thread.sleep((1000).toLong())
                    }
                    val cmtsCnt = comments_frag!!.getCmtsCount()
                    cmtsCnt.let {
                        commentsMoreBtn!!.post(Runnable {
                            val str = "已有" + cmtsCnt.toString() + "評論"
                            if (cmtsCnt > 0) {
                                commentsMoreBtn!!.text = str
                            }
                        })
                    }
                })
                cmtsThread.start()
            }
            webUrl = view!!.findViewById(R.id.webUrlText)
            if (debugmode) Log.i(
                "Review_Fragment",
                "useremail : $useremail , length = ${useremail!!.length}"
            )
            if (useremail!!.length < 5 || !useremail!!.contains("@") || useremail.equals("not fill")) {
                webUrl!!.visibility = View.INVISIBLE
                webBackButton.visibility = View.INVISIBLE
            } else {
                webUrl!!.visibility = View.VISIBLE
                webBackButton.visibility = View.VISIBLE
                webViewSetup()
            }
        } else {
            val dash_f = (activity as MainActivity).dash_frag as Dash_Fragment
            (activity as MainActivity).switchFragment(this@Review_Fragment, dash_f, 2)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun webViewSetup() {
        if (pp_model_seted == null) {
            return
        }
        val name = pp_model_seted!!.name
        val area = pp_model_seted!!.country.split(" | ")
        val country = area[0]
        val city = area[1]
        val searchKey = URLEncoder.encode(name, "UTF-8")+
                                "+"+URLEncoder.encode(country, "UTF-8")+
                                "+"+URLEncoder.encode(city, "UTF-8")
        webSearch = view!!.findViewById(R.id.webView)

        webSearch!!.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                webUrl!!.setText(url)
                view?.loadUrl(url)

                review_scroll?.scrollTo(0, view?.y!!.toInt())
                return true
            }
        }
        val frist_url = "https://www.google.com/search?q=$searchKey"
        webSearch!!.settings.javaScriptEnabled = true
        webSearch!!.loadUrl(frist_url)
        webBackButton.setOnClickListener{
            if (webSearch!!.canGoBack()) {
                webSearch!!.goBack()
                webUrl!!.setText("")
            }
        }
    }

    fun pp_set(pp_model: PPModel) {
        pp_model.let { pp_model_seted = pp_model }
    }

    private fun disableSwitch(switch: Switch) {
        with(switch) {
            isFocusable = false
            isClickable = false
            //isCursorVisible = true
            isFocusableInTouchMode = false
            //isEnabled = true
            //setTextIsSelectable(true)
            //movementMethod = ArrowKeyMovementMethod.getInstance()
            //inputType = type//InputType.TYPE_CLASS_TEXT
        }
    }

    private fun enableSwitch(switch: Switch) {
        with(switch) {
            isFocusable = true
            isClickable = true
            //isCursorVisible = true
            isFocusableInTouchMode = true
            //isEnabled = true
            //setTextIsSelectable(true)
            //movementMethod = ArrowKeyMovementMethod.getInstance()
            //inputType = type//InputType.TYPE_CLASS_TEXT
        }
    }

    private fun enableSpinner(spin: Spinner) {
        with(spin) {
            isFocusable = true
            isClickable = true
            //isCursorVisible = true
            isFocusableInTouchMode = true
            //isEnabled = true
            //setTextIsSelectable(true)
            //movementMethod = ArrowKeyMovementMethod.getInstance()
            //inputType = type//InputType.TYPE_CLASS_TEXT
        }
    }

    private fun disableSpinner(spin: Spinner) {
        with(spin) {
            isFocusable = false
            isClickable = false
            view!!.clearFocus()
            val imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(spin.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    private fun enableEditText(editText: EditText, type:Int) {
        if (debugmode) Log.i("Review_Fragment : ", "enableEditText working")
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

    fun pp_setup(){
        if (pp_model_seted == null) return
        pp_model_seted.let {
            pp_name_view.setText(pp_model_seted!!.name)
            disableEditText(pp_name_view)

            pp_phone_view.setText(pp_model_seted!!.phone)
            disableEditText(pp_phone_view)

            pp_addr_view.setText(pp_model_seted!!.addr)
            disableEditText(pp_addr_view)

            pp_fb_view.setText(pp_model_seted!!.fb)
            disableEditText(pp_fb_view)

            pp_web_view.setText(pp_model_seted!!.web)
            disableEditText(pp_web_view)

            pp_bloginfo_view.setText(pp_model_seted!!.blogInfo)
            disableEditText(pp_bloginfo_view)

            pp_opentime_view.setText(pp_model_seted!!.opentime)
            disableEditText(pp_opentime_view)

            pp_tag_note_view.setText(pp_model_seted!!.tag_note)
            disableEditText(pp_tag_note_view)

            pp_descrip_view.setText(pp_model_seted!!.descrip)
            disableEditText(pp_descrip_view)
            pp_descrip_view.movementMethod = LinkMovementMethod.getInstance()


        }
    }

    fun spin_data_setup() {
        // update city and country select item
        var idx = 0
        for (i in CountriesNCities().country_str) {
            idx += 1
            if (sel_country == i) {
                break
            }
        }
        var cities:Array<String> = arrayOf()
        pp_country_view.setSelection(idx - 1)

        // setup City
        idx = 0
        when (sel_country) {
            CountriesNCities().country_str[0]->
                cities = CountriesNCities().TW_areas_str
            CountriesNCities().country_str[1]->
                cities = CountriesNCities().JP_areas_ZH_str
            CountriesNCities().country_str[2]->
                cities = CountriesNCities().CN_areas_str
        }
        for (i in cities) {
            idx += 1
            if (i == sel_city) {
                break
            }
        }
        //setup_city_spin(R.id.pp_city_view, cities)
        if (debugmode) Log.i(TAG, "Review_Fragment : spin_data_setup : city select index : "+ idx)
        pp_city_view.setSelection(idx - 1)

        disableSpinner(pp_country_view)
        disableSpinner(pp_city_view)
    }

    fun pp_view_cleanup() {
        pp_name_view.setText("")
        pp_phone_view.setText("")
        pp_addr_view.setText("")
        pp_fb_view.setText("")
        pp_web_view.setText("")
        pp_bloginfo_view.setText("")
        pp_opentime_view.setText("")
        pp_tag_note_view.setText("")
        pp_descrip_view.setText("")
    }

    /**
     * Called when the "Save" button is clicked.
     */
    private fun SetupView() {
        var country_org = ""
        Log.i("Review_Fragment", " pp_model_seted = $pp_model_seted")
        if (pp_model_seted != null) {
            if (!pp_model_seted!!.country.isEmpty()) {
                country_org = pp_model_seted!!.country
            } else {
                val dash_f = (activity as MainActivity).dash_frag as Dash_Fragment
                (activity as MainActivity).switchFragment(this@Review_Fragment, dash_f, 2)
            }
            val temp = country_org.split(" | ")
            if (debugmode) Log.i(TAG, "Review_Fragment : pp_setup : " + temp[0])
            sel_country = temp[0]
            sel_city = temp[1]

            commentsMoreBtn = view!!.findViewById(R.id.pp_comments_view)
            updateBtn = view!!.findViewById(R.id.pp_updateButton)
            editBtn = view!!.findViewById(R.id.pp_editeButton)
            backBtn = view!!.findViewById(R.id.backButton)
            mapBtn = view!!.findViewById(R.id.pp_mapButton)
            callBtn = view!!.findViewById(R.id.pp_callButton)
            webBtn = view!!.findViewById(R.id.pp_webButton)
            fbBtn = view!!.findViewById(R.id.pp_fbButton)
            bloggerBtn = view!!.findViewById(R.id.pp_bloggerButton)
            intoTripBtn = view!!.findViewById(R.id.pp_intoTripPlan)

            setup_country_spin(R.id.pp_country_view)
            setup_city_spin(R.id.pp_city_view)
            spin_data_setup()
            updateBtn!!.visibility = View.GONE
            if (user_power == 1 && pp_model_seted!!.id.toInt() == 0) {
                disableSwitch(pp_status_view)
                pp_status_view.visibility = View.VISIBLE
                var bStatus = false
                if (pp_model_seted!!.status == 1) bStatus = true
                pp_status_view.isChecked = bStatus
                pp_status_view.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        pp_model_seted!!.status = 1
                    } else {
                        pp_model_seted!!.status = 0
                    }
                }
            }

            commentsMoreBtn!!.setOnClickListener {
                //(activity as MainActivity).onBackPressed()
                //calDistanceTaskStop()
                (activity as MainActivity).switchFragment(this@Review_Fragment, comments_frag!!, 2)
            }

            backBtn!!.setOnClickListener {
                (activity as MainActivity).onBackPressed()
            }

            updateBtn!!.setOnClickListener {
                disableEditText(pp_name_view)
                disableEditText(pp_phone_view)
                disableEditText(pp_addr_view)
                disableEditText(pp_fb_view)
                disableEditText(pp_web_view)
                disableEditText(pp_bloginfo_view)
                disableEditText(pp_opentime_view)
                disableEditText(pp_tag_note_view)
                disableEditText(pp_descrip_view)
                disableSpinner(pp_country_view)
                disableSpinner(pp_city_view)
                disableSwitch(pp_status_view)
                if (pp_model_seted!!.id.toInt() == 0) {
                    if (user_power == 1) {
                        val dash_f = (activity as MainActivity).dash_frag as Dash_Fragment
                        var status = 0
                        if (pp_status_view.isChecked) status = 1
                        pp_model_seted!!.name = pp_name_view.text.toString()
                        pp_model_seted!!.phone = pp_phone_view.text.toString()
                        pp_model_seted!!.addr = pp_addr_view.text.toString()
                        pp_model_seted!!.fb = pp_fb_view.text.toString()
                        pp_model_seted!!.web = pp_web_view.text.toString()
                        pp_model_seted!!.blogInfo = pp_bloginfo_view.text.toString()
                        pp_model_seted!!.opentime = pp_opentime_view.text.toString()
                        pp_model_seted!!.tag_note = pp_tag_note_view.text.toString()
                        pp_model_seted!!.descrip = pp_descrip_view.text.toString()
                        pp_model_seted!!.status = status
                        if (debugmode) Log.i("Review_Fragment", "pp_model_seted: $pp_model_seted")
                        dash_f.cloudUpdatePP(pp_model_seted!!)
                    } else {
                        Toast.makeText(
                            this@Review_Fragment.context,
                            "無法更新，此為線上資料喔。",
                            Toast.LENGTH_LONG
                        ).show()
                        pp_name_view.setText(pp_model_seted!!.name)
                        pp_phone_view.setText(pp_model_seted!!.phone)
                        pp_addr_view.setText(pp_model_seted!!.addr)
                        pp_fb_view.setText(pp_model_seted!!.fb)
                        pp_web_view.setText(pp_model_seted!!.web)
                        pp_bloginfo_view.setText(pp_model_seted!!.blogInfo)
                        pp_opentime_view.setText(pp_model_seted!!.opentime)
                        pp_tag_note_view.setText(pp_model_seted!!.tag_note)
                        pp_descrip_view.setText(pp_model_seted!!.descrip)
                    }
                } else {
                    pp_model_seted!!.name = pp_name_view.text.toString()
                    pp_model_seted!!.phone = pp_phone_view.text.toString()
                    pp_model_seted!!.addr = pp_addr_view.text.toString()
                    pp_model_seted!!.fb = pp_fb_view.text.toString()
                    pp_model_seted!!.web = pp_web_view.text.toString()
                    pp_model_seted!!.blogInfo = pp_bloginfo_view.text.toString()
                    pp_model_seted!!.opentime = pp_opentime_view.text.toString()
                    pp_model_seted!!.tag_note = pp_tag_note_view.text.toString()
                    pp_model_seted!!.descrip = pp_descrip_view.text.toString()
                    item_dao!!.update(pp_model_seted!!)
                }
                updateBtn!!.visibility = View.GONE
                editBtn!!.visibility = View.VISIBLE
            }

            editBtn!!.setOnClickListener {
                if (debugmode) Log.i(
                    "Add_Fragment : ",
                    "setOnClickListener: enable editText edit function"
                )
                enableEditText(pp_name_view, InputType.TYPE_CLASS_TEXT)
                enableEditText(pp_phone_view, InputType.TYPE_CLASS_PHONE)
                enableEditText(pp_addr_view, InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS)
                enableEditText(pp_fb_view, InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS)
                enableEditText(pp_web_view, InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS)
                enableEditText(pp_bloginfo_view, InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS)
                enableEditText(pp_opentime_view, InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                enableEditText(pp_tag_note_view, InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                enableEditText(pp_descrip_view, InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE)
                if (user_power == 1) {
                    enableSwitch(pp_status_view)
                }
                //enableSpinner(pp_country_view)
                //enableSpinner(pp_city_view)
                updateBtn!!.visibility = View.VISIBLE
                editBtn!!.visibility = View.GONE
            }

            mapBtn!!.setOnClickListener {
                (activity as MainActivity).mapNavi(pp_model_seted!!.addr)
            }

            callBtn!!.setOnClickListener {
                (activity as MainActivity).callPhone(pp_model_seted!!.phone)
            }

            webBtn!!.setOnClickListener {
                (activity as MainActivity).openWeb(pp_model_seted!!.web)
            }

            fbBtn!!.setOnClickListener {
                (activity as MainActivity).openFB(pp_model_seted!!.fb)
            }

            bloggerBtn!!.setOnClickListener {
                (activity as MainActivity).openWeb(pp_model_seted!!.blogInfo)
            }

            intoTripBtn!!.setOnClickListener {
                save_to_tripPlanning(pp_model_seted!!)
            }
        } else {
            val dash_f = (activity as MainActivity).dash_frag as Dash_Fragment
            (activity as MainActivity).switchFragment(this@Review_Fragment, dash_f, 2)
        }
    }

    fun setup_country_spin(ViewID: Int) {

        val globalCountries = CountriesNCities()
        //對應控件
        val country_spinner = view!!.findViewById(ViewID) as Spinner
        val adapter = ArrayAdapter(this@Review_Fragment.context!!, R.layout.spinner_item, globalCountries.country_str)
        //設置下拉列表的風格
        //adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        //將adapter 添加到spinner中
        country_spinner.adapter = adapter
        country_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(arg0: AdapterView<*>, arg1: View?, position: Int, id: Long) {

                if (sel_country != arg0.selectedItem.toString()) {
                    //if (debugmode) Toast.makeText(this@Add_Fragment.context, "您選擇" + arg0.selectedItem.toString(), Toast.LENGTH_LONG).show()
                    sel_country = arg0.selectedItem.toString()
                    if (arg0.selectedItemId.toInt() == 0) {
                        setup_city_spin(R.id.pp_city_view)
                    } else if (arg0.selectedItemId.toInt() == 1) {
                        setup_city_spin(R.id.pp_city_view)
                    } else if (arg0.selectedItemId.toInt() == 2) {
                        setup_city_spin(R.id.pp_city_view)
                    }
                }
            }

            override fun onNothingSelected(arg0: AdapterView<*>) {
                //if (debugmode) Toast.makeText(this@Add_Fragment.context, "您沒有選擇任何項目", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun setup_city_spin(ViewID: Int) {
        if (sel_country.isEmpty()) return
        var cites_arr:Array<String>? = null
        val city_spinner = view!!.findViewById(ViewID) as Spinner
        when (sel_country) {
            CountriesNCities().country_str[0]->
                cites_arr = CountriesNCities().TW_areas_str
            CountriesNCities().country_str[1]->
                cites_arr = CountriesNCities().JP_areas_ZH_str
            CountriesNCities().country_str[2]->
                cites_arr = CountriesNCities().CN_areas_str
        }
        val adapter = ArrayAdapter(this@Review_Fragment.context!!, R.layout.spinner_item, cites_arr!!)
        //設置下拉列表的風格
        //adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        //將adapter 添加到spinner中
        city_spinner.adapter = adapter
        city_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(arg0: AdapterView<*>, arg1: View?, position: Int, id: Long) {
                if (sel_city != arg0.selectedItem.toString()) {
                    //if (debugmode) Toast.makeText(
                    //    this@Add_Fragment.context,
                    //    "您選擇" + arg0.selectedItem.toString(),
                    //    Toast.LENGTH_LONG
                    //).show()
                    sel_city = arg0.selectedItem.toString()
                }
            }

            override fun onNothingSelected(arg0: AdapterView<*>) {
                //if (debugmode) Toast.makeText(this@Add_Fragment.context, "您沒有選擇任何項目", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun save_to_tripPlanning(pp: PPModel) {
        val tpf = TripPlanning_Fragment()
        val tripPlanList = tpf.getAllTripList(context!!)
        if (tripPlanList.count() == 0) {
            return
        }
        val tripPlanListArray:ArrayList<String> = arrayListOf()
        tripPlanList.forEach {
            tripPlanListArray.add(it.planname)
        }
        val array = arrayOfNulls<String>(tripPlanListArray.size)
        val tripListBuilder = AlertDialog.Builder(context)
        val tripListdialog: AlertDialog = tripListBuilder.setTitle(R.string.dash_tripList_sel_zh)
            .setSingleChoiceItems(tripPlanListArray.toArray(array),-1) { dialog, which->

                // Try to parse user selected color string
                try {
                    val tripPlanDao = TripPlanDAO(context!!)
                    val tripPlanModel = tripPlanDao.findName(tripPlanListArray[which])
                    val ph = PlanningHelper()
                    if (debugmode)
                        Log.i("DashListRC",  "tripPlanModel!!.id : ${tripPlanModel!!.id}, displayList!![position] name : ${pp.name}")
                    if (debugmode)
                        Log.i("DashListRC",  "useremail!! = $useremail")
                    ph.add_pp_into(context!!, tripPlanModel!!.id, pp, "UNDEF")
                    val tpf = TripPlanning_Fragment()
                    tpf.addPP_toCloud(context!!, useremail!!, tripPlanModel, "UNDEF", pp.cloudID.toString())//addPP_toCloud(this@PlanningHelper.context!!, pH_List!!, item, cloudIDstr)
                    // Change the layout background color using user selection
                    //Toast.makeText(this@Dash_Fragment.context, "Clicked : ${tripPlanListArray[which]}", Toast.LENGTH_LONG).show()
                }catch (e:IllegalArgumentException){
                    // Catch the color string parse exception

                }

                // Dismiss the dialog
                dialog.dismiss()
            }
            .create()

        tripListdialog.show()
    }
}