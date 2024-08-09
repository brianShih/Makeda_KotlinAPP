package tw.breadcrumbs.makeda


import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

import kotlinx.android.synthetic.main.dash_fragment.*
import androidx.recyclerview.widget.ItemTouchHelper
import kotlinx.android.synthetic.main.dash_fragment.view.*
import org.json.JSONObject
import tw.breadcrumbs.makeda.CloudObj.Cloud_Helper
import tw.breadcrumbs.makeda.DBModules.ItemDAO
import tw.breadcrumbs.makeda.DBModules.TripPlanDAO
import tw.breadcrumbs.makeda.TripPlanning.PlanningHelper
import tw.breadcrumbs.makeda.TripPlanning.TripPlanning_Fragment
import tw.breadcrumbs.makeda.dataModel.CountriesNCities
import tw.breadcrumbs.makeda.dataModel.PPModel
import java.io.IOException
import java.net.URLEncoder


class Dash_Fragment : androidx.fragment.app.Fragment() {
    private val debugmode = false
    val CloudHelperURL = "//TODO"
    val UserTraceURL = "//TODO"
    val globalCountries = CountriesNCities()
    private lateinit var popupWindow: PopupWindow
    var country_spinner:Spinner? = null
    var city_spinner:Spinner? = null
    var sel_country:String = ""
    var sel_city:String = ""
    var item_dao : ItemDAO? = null
    var dash_list_rc: DashListRC? = null
    var pp_filterB:ImageButton? = null
    var searchBtn : ImageButton? = null
    var imageCtrlBtn:ImageButton? = null
    var dashTrackerBtn:ImageButton? = null
    var speedHdlrBtn:ImageButton? = null
    var dashTripPlanListBtn:ImageButton? = null
    var displayList:MutableList<PPModel>? = mutableListOf()
    var global_pp_list:MutableList<PPModel>? = mutableListOf()
    var cloud_PPModelList: MutableList<PPModel>? = mutableListOf()
    var db_pp_list: MutableList<PPModel>? = mutableListOf()
    var filterTags:MutableList<String>? = mutableListOf()
    var set_frag:Setting_Fragment? = null
    var useremail : String? = ""
    var location : Location? = null
    private var quickTagClicked: MutableList<Int>? = mutableListOf()
    private var quickTagsList: MutableList<String>? = mutableListOf()
    private var quickTagFilterAdapter: FilterCustomGrid? = null
    private var grid : GridView? = null
    private var imageDownload = 0
    private var cloud_download_ready = 0
    private var cloud_download = false
    private var downloadHdler : Cloud_Helper? = null
    private var dashCalHelper: calcuteDistanceHelper? = null
    private var cloudCheck:ImageButton? = null
    private var cloudIcon: Drawable? = null
    private var city_array:Array<String>? = arrayOf()
    private var city_adapter: ArrayAdapter<String>? = null
    private var country_adapter: ArrayAdapter<String>? = null //<T>(this@Dash_Fragment.context!!, R.layout.spinner_item)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.dash_fragment, container, false)
    }

    override fun onStop() {
        super.onStop()
        //popupWindow.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as MainActivity).setNavigationID(2)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        set_frag = (activity as MainActivity).setting_frag as Setting_Fragment
        cloudButtonCheckFun()
        if (set_frag!!.local_setting_isReady()){
            val temp = set_frag!!.prefs!!.getBoolean(set_frag!!.dwn_pps_en_pref, true)
            if (cloud_download != temp) {
                global_pp_clean()
            }
            cloud_download = temp
            useremail = set_frag!!.prefs!!.getString(set_frag!!.user_email_pref, " ")
            val pref_country = set_frag!!.prefs!!.getString(set_frag!!.dash_country_pref, " ")
            val pref_city = set_frag!!.prefs!!.getString(set_frag!!.dash_city_pref, " ")
            if (debugmode) Log.i("Dash_Fragment", "pref_country = $pref_country, pref_city = $pref_city")
            if (!pref_country!!.equals(" ") && pref_country.isNotEmpty()) {
                sel_country = pref_country
            }
            if (!pref_city!!.equals(" ") && pref_city.isNotEmpty()) {
                sel_city = pref_city
            }
        } else {
            cloud_download = false
        }

        item_dao = ItemDAO(this@Dash_Fragment.context!!)

        SetupView()
        upToCloud()
        //userOnline()
        searchSetup()
        trackerSetup()
        imageSetup()
        //speedHdlrSetup()
        tripPlanningListSetup()
        quickTagSetup()
        val notif_f = (activity as MainActivity).notif_frag as Notif_Fragment
        notif_f.recentlyPP_Task()
    }

    private fun getDistance(dLoc : Location) : Float {
        val currLoc: Location = (activity as MainActivity).getLastLocation() ?: return 0f
        return currLoc.distanceTo(dLoc)
    }

    fun getLocationFromAddress(strAddress:String) : Location? {

        val coder : Geocoder = Geocoder(this@Dash_Fragment.context)
        var address : List<Address>? = null
        val p1 : Location = Location(LocationManager.PASSIVE_PROVIDER)

        try {

            address = coder.getFromLocationName(strAddress,1)
            if (address == null) {
                return null
            }
            val addrLoc = address[0]
            p1.latitude = addrLoc.latitude
            p1.longitude = addrLoc.longitude

        } catch (e : Exception) {
            if (debugmode)
                Log.i("Dash_Fragment", "getLocationFromAddress : Error: $e")
        }
        return p1
    }

    private fun parserLocation(loc: Location) : List<String>? {
        var city = ""
        var country = ""
        var town = ""
        val retList: MutableList<String> = mutableListOf()

        try {
            if (context != null) {
                val geoCoder = Geocoder(context)
                val placemark = geoCoder.getFromLocation(loc.latitude, loc.longitude, 1)
                if (placemark[0].subAdminArea == null) {
                    city = placemark[0].adminArea.toString()
                } else if (placemark[0].adminArea == null) {
                    city = placemark[0].subAdminArea.toString()
                } else if (placemark[0].adminArea == null && placemark[0].subAdminArea == null) {
                    //TODO
                }
                if (placemark[0].locality != null) {
                    town = placemark[0].locality.toString()
                }
                country = placemark[0].countryName.toString()
                retList.add(0, country)
                retList.add(1, city)
                retList.add(2, town)
            }

        } catch (e: IOException) {
            if (debugmode)
                Log.i("Dash_Fragment", " error : e")
        }
        return retList
    }

    private fun updateSpinnerSelect(loc: Location) {
        var countryIndex = -1
        var cityIndex = -1
        val ret = parserLocation(loc)
        if (ret!!.count() == 0) return
        ret.let {
            var cities:Array<String>? = null
            var idx = 0
            for (i in CountriesNCities().country_str) {
                if (it!!.get(0) == i) {
                    countryIndex = idx
                }
                idx += 1
            }
            if (countryIndex == -1) return
            sel_country = CountriesNCities().country_str[countryIndex]
            set_frag?.let {
                set_frag!!.prefs!!.edit().putString(set_frag!!.dash_country_pref, sel_country)
                    .apply()
            }
            setup_city_spin(R.id.pp_city_dash)

            pp_country_dash.setSelection(countryIndex)
            val lang = CountriesNCities().isCJK(it!!.get(1))
            when (sel_country) {
                CountriesNCities().country_str[0]->
                    cities = CountriesNCities().TW_areas_str
                CountriesNCities().country_str[1]->
                    cities = if (lang) {
                        CountriesNCities().JP_areas_ZH_str
                    } else {
                        CountriesNCities().JPAreasENstr
                    }
                CountriesNCities().country_str[2]->
                    cities = CountriesNCities().CN_areas_str
            }
            idx = 0
            for (i in cities!!) {
                //Log.i("Dash_Fragment", " city search : $i")
                if (i == it[1]) {
                    cityIndex = idx
                }
                idx += 1
            }
            if (cityIndex == -1) return
            sel_city = cities[cityIndex] // bug
            set_frag?.let {
                set_frag!!.prefs!!.edit().putString(set_frag!!.dash_city_pref, sel_city)
                    .apply()
            }
            if (sel_country == CountriesNCities().country_str[1]) {
                if (!lang) {
                    sel_city = CountriesNCities().JP_areas_ZH_str[cityIndex]
                    set_frag?.let {
                        set_frag!!.prefs!!.edit().putString(set_frag!!.dash_city_pref, sel_city)
                            .apply()
                    }
                }
            }

            pp_city_dash.setSelection(cityIndex)
            global_pp_clean()
            updatePPList()
        }
    }

    private fun searchSetup() {
        searchBtn = view!!.findViewById(R.id.search_button)
        searchBtn!!.setOnClickListener{
            val searchfrag = Search_Fragment()
            (activity as MainActivity).switchFragment(this@Dash_Fragment, searchfrag, 2)
        }
    }

    private fun imageSetup() {
        imageCtrlBtn = view!!.findViewById(R.id.img_dwn)
        set_frag?.let{
            if (set_frag!!.local_setting_isReady()) {
                imageDownload = set_frag!!.prefs!!.getInt(set_frag!!.dash_img_dwn_pref, 1)
                if (imageDownload == 1) {
                    imageCtrlBtn!!.setImageDrawable(
                        ContextCompat.getDrawable(
                            context!!,
                            R.drawable.images_unsel
                        )
                    )
                } else {
                    imageCtrlBtn!!.setImageDrawable(
                        ContextCompat.getDrawable(
                            context!!,
                            R.drawable.images
                        )
                    )
                }
            }

            imageCtrlBtn!!.setOnClickListener {
                if (imageDownload == 1) {
                    imageDownload = 0
                    imageCtrlBtn!!.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.images))
                } else if (imageDownload == 0) {
                    imageDownload = 1
                    imageCtrlBtn!!.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.images_unsel))
                }
                recycleViewInit(this@Dash_Fragment.context!!, DashListAction(displayList!!), PPListRecyclerView, displayList!!)
                set_frag?.let {
                    set_frag!!.prefs!!.edit().putInt(set_frag!!.dash_img_dwn_pref, imageDownload)
                        .apply()
                }
            }
        }

    }

    private fun trackerSetup() {
        dashTrackerBtn = view!!.findViewById(R.id.dash_tracker)
        dashTrackerBtn!!.setOnClickListener{
            if (location == null) {
                location = (activity as MainActivity).getLastLocation()
            }
            if (location != null) updateSpinnerSelect(location!!)
        }
    }

    private fun quickTagSetup() {
        //var locThread : Thread?
        location = (activity as MainActivity).getLastLocation()
        quickTag_reload(quickTagClicked!!)
        try {
            val locThread = Thread(Runnable {
                while (true) {
                    if (location == null && activity != null)
                        location = (activity as MainActivity).getLastLocation()
                    if (location != null) {
                        userOnline()
                        val locInfo:MutableList<String>? = mutableListOf()
                        val ret = parserLocation(location!!)
                        if (ret!!.count() < 3) continue
                        ret.forEach {retStr ->
                            when {
                                retStr.isNotEmpty() -> {
                                    locInfo!!.add(retStr)
                                }
                            }
                        }
                        quick_tag?.post(Runnable {
                            if (locInfo!!.get(2).isNotEmpty() && locInfo.get(1) == sel_city && locInfo.get(0) == sel_country) {
                                val clickedList = quickTagFilterAdapter!!.get_selectList()

                                var changed = 0
                                val cr_town = ret.get(2)
                                filterTags!!.forEach {
                                    if (it == cr_town) {
                                        var diff = 0
                                        quickTagsList?.forEach { qT ->
                                            if (qT != it) {
                                                diff = 1
                                            }
                                        }
                                        if (diff == 1)
                                            changed = 1
                                    }
                                }

                                if (quickTagsList!!.count() == 0) {
                                    changed = 1
                                } else if (cr_town != quickTagsList!!.get(0)) {
                                    changed = 1
                                }
                                if (changed == 1) {
                                    quickTagsList = mutableListOf()
                                    quickTagsList!!.add(cr_town)
                                    quickTag_reload(quickTagClicked!!)
                                }

                                if (debugmode) Log.i(
                                    "Dash_Fragment",
                                    " quickTagsList = $quickTagsList"
                                )
                                if (debugmode) Log.i(
                                    "Dash_Fragment",
                                    " quickTagClicked = $quickTagClicked"
                                )
                                if (debugmode) Log.i("Dash_Fragment", " clickedList = $clickedList")
                                quickTagClicked = mutableListOf()
                                quickTagClicked = quickTagFilterAdapter!!.get_selectList()

                                if (quickTagClicked!!.get(0) == 1) {
                                    if (filterTags!!.count() == 0) {
                                        filterTags!!.add("#$cr_town")
                                        updatePPList()
                                    } else {
                                        var same = 0
                                        filterTags!!.forEach {
                                            if (it == "#$cr_town") {
                                                same = 1
                                            }
                                        }
                                        if (same == 0) {
                                            filterTags!!.add("#$cr_town")
                                            updatePPList()
                                        }
                                    }
                                } else if (quickTagClicked!!.get(0) == 0) {
                                    if (filterTags!!.count() == 0) {
                                        //filterTags!!.add(cr_town)
                                        //updatePPList()
                                    } else {
                                        filterTags!!.forEach {
                                            if (it == "#$cr_town") {
                                                filterTags!!.remove("#$cr_town")
                                                updatePPList()
                                            }
                                        }
                                    }
                                }
                            } else {
                                if (quickTagsList!!.count() > 0) {
                                    quickTagsList = mutableListOf()
                                    quickTagClicked = mutableListOf()
                                    quickTag_reload(quickTagClicked!!)
                                }
                            }
                        })

                    }
                    Thread.sleep((3 * 1000).toLong())
                }
            })
            locThread.start()
        } catch (e:IllegalArgumentException){
            // Catch the color string parse exception

        }

    }

    private fun tripPlanningListSetup() {
        dashTripPlanListBtn = view!!.findViewById(R.id.dash_tripsch)
        dashTripPlanListBtn!!.setOnClickListener{
            val tripplanlistfrag = TripPlanning_Fragment()
            (activity as MainActivity).switchFragment(this@Dash_Fragment, tripplanlistfrag, 2)
        }
    }

    fun setfilterTags( filterTagsList:MutableList<String> ) {
        filterTags = filterTagsList
    }

    fun getfilterTags() : MutableList<String> {
        return filterTags!!
    }

    fun filterPPsList(globalList : MutableList<PPModel>) : MutableList<PPModel> {
        if (debugmode)
            Log.i("Dash_Fragment", " filterPPsList : start - filterTags : $filterTags")
        val ret_list : MutableList<PPModel> = mutableListOf()
        for (g in globalList)
        {
            var not_include = 0
            for (i in filterTags!!) {
                if (!g.tag_note.contains(i)) {
                    not_include = 1
                }
            }
            if (not_include == 0) {
                ret_list.add(g)
            }
        }

        return ret_list
    }

    fun cloudUpdatePP(ppModel: PPModel) {
        if (useremail == null)
            useremail = set_frag!!.prefs!!.getString(set_frag!!.user_email_pref, " ")
        if (useremail.isNullOrEmpty() || useremail == " ") {
            if (debugmode)
                Log.i("Dash_Fragment", "cloudUpdatePP- user not login")
            return
        }

        var phone : String = ppModel.phone.toString()
        if (phone.isEmpty() || phone == " ") phone = "0"

        var country = ppModel.country

        var addr = ppModel.addr
        if (addr.isEmpty() || addr == " ") addr = "-"

        var fb : String = ppModel.fb.toString()
        if (fb.isEmpty() || fb == " ") fb = "-"

        var web : String = ppModel.web.toString()
        if (web.isEmpty() || web == " ") web = "-"

        var bloginfo : String = ppModel.blogInfo.toString()
        if (bloginfo.isEmpty() || bloginfo == " ") bloginfo = "-"

        var tag_note : String = ppModel.tag_note
        if (tag_note.isEmpty() || tag_note == " ") tag_note = "-"

        var opentime : String = ppModel.opentime
        if (opentime.isEmpty() || opentime == " ") opentime = "-"
        val score = 0
        val pic_url = "-"
        var descrip = ppModel.descrip
        if (descrip.isEmpty() || descrip == " ") descrip = "-"
        val commet = "-"
        val status = ppModel.status

        Toast.makeText(this@Dash_Fragment.context, "更新 ${ppModel.name}", Toast.LENGTH_LONG).show()
        val cloud_cmd = "CMD=//TODO&email=$useremail&name=${URLEncoder.encode(ppModel.name, "UTF-8")}&" +
                "phone=$phone&country=$country&address=$addr" +
                "&fb=$fb&web=$web&bloggerIntro=$bloginfo&tag_note=$tag_note" +
                "&opentime=$opentime&score=$score&pic_url=$pic_url" +
                "&description=${URLEncoder.encode(descrip, "UTF-8")}&status=$status&comment=${URLEncoder.encode(commet, "UTF-8")}"
        if (debugmode)
            Log.i("Dash_Fragment", "cloudUpdatePP : cloud_cmd : $cloud_cmd")

        Cloud_Helper(this@Dash_Fragment.context!!) {
            if (it == null) {
                return@Cloud_Helper
            }
            if (debugmode)
                Log.i("Dash_Fragment", " PP Cloud Update response : $it")

        }.execute("POST", CloudHelperURL, cloud_cmd.toString())
    }

    fun cloud_download_pps() {
        if (cloud_PPModelList!!.size == 0) {
            val sa_country = sel_country
            val sa_city = sel_city
            Toast.makeText(this@Dash_Fragment.context, "開始連接 $sa_country | $sa_city 線上資料", Toast.LENGTH_LONG).show()
            val cloud_cmd = "CMD=//TODO&Country=$sa_country&City=$sa_city"

            Cloud_Helper(this@Dash_Fragment.context!!) {
                if (it == null) {
                    Toast.makeText(this@Dash_Fragment.context, R.string.network_not_stable_zh, Toast.LENGTH_LONG).show()
                    return@Cloud_Helper
                }

                var cloud_db = JSONObject(it)
                if (!cloud_db.isNull("count")) {
                    val cloud_count = cloud_db.get("count") as Int
                    for (i in 1..cloud_count) {
                        val tempDB = cloud_db.get(i.toString()) as JSONObject
                        val cloudID = tempDB.get("cloudID").toString()
                        var ppmodel = PPModel(
                            0, cloudID.toLong(),
                            tempDB.get("name").toString(), tempDB.get("phone").toString(),
                            tempDB.get("country").toString(), tempDB.get("address").toString(),
                            tempDB.get("fb").toString(), tempDB.get("web").toString(),
                            tempDB.get("bloggerintro").toString(), tempDB.get("opentime").toString(),
                            tempDB.get("tag_note").toString(), tempDB.get("description").toString(),
                            tempDB.get("pic_url").toString(),
                            tempDB.get("score") as Int, tempDB.get("status") as Int
                        )
                        if (debugmode)
                            Log.i("Dash_Fragment", " insert to cloud_PPModelList: index: $i / ${tempDB.get("name").toString()}")
                        cloud_PPModelList!!.add(ppmodel)
                    }
                    updatePPList()
                } else {
                    Toast.makeText(this@Dash_Fragment.context, "$sa_country | $sa_city 【無】線上資料", Toast.LENGTH_LONG)
                        .show()
                }
            }.execute("POST", CloudHelperURL, cloud_cmd.toString())
        }
    }

    fun updatePPList() {
        if (sel_country.isNotEmpty() && sel_city.isNotEmpty() && PPListRecyclerView != null) {
            db_pp_list = item_dao!!.getSelect(sel_country, sel_city) as MutableList<PPModel>

            if (cloud_download) {
                cloud_download_pps()
            }

            if (db_pp_list!!.isNotEmpty()) {
                for (temp in db_pp_list!!) {
                    var sameInList = 0
                    for (g in global_pp_list!!) {
                        if (g.name == temp.name) {
                            sameInList = 1
                        }
                    }
                    if (sameInList == 0) {
                        global_pp_list!!.add(temp)
                    }
                }
            }
            if (debugmode)
                Log.i("Dash_Fragment", "updatePPList cloud_download_ready = $cloud_download_ready")
            if (cloud_PPModelList!!.size > 0 && cloud_download) {
                for (temp in cloud_PPModelList!!) {
                    var sameInList = 0
                    for (g in global_pp_list!!) {
                        if (g.name == temp.name) {
                            sameInList = 1
                        }
                    }
                    if (sameInList == 0) {
                        global_pp_list!!.add(temp)
                    }
                }
                //downloadHdler!!.cancel(true)
                //downloadHdler = null
                //cloud_download_ready = 0
            }
            displayList = filterPPsList(global_pp_list!!)

            recycleViewInit(this@Dash_Fragment.context!!, DashListAction(displayList!!), PPListRecyclerView, displayList!!)
        } else {
            if (debugmode)
                Log.i("DASH_FRAGMENT: ", "select data not ready")
        }
    }

    fun recycleViewInit(context: Context, listaction : ListAction, recycleView : androidx.recyclerview.widget.RecyclerView, list : MutableList<PPModel>) {
        if (useremail.equals("not fill") || useremail.isNullOrEmpty() || !useremail!!.contains("@")) {
            val prefs = context.getSharedPreferences("MAKEDA_USER_SET",
                Context.MODE_PRIVATE
            )
            useremail = prefs!!.getString(Setting_Fragment().user_email_pref, " ")
        }
        if (debugmode) Log.i("Dash_Fragment", "recycleViewInit useremail = $useremail")
        recycleView.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(context)
        item_dao = ItemDAO(context)
        dash_list_rc = DashListRC(list, { partItem : PPModel -> listaction.pp_onClick(partItem) })
        dash_list_rc!!.setListion(listaction)
        dash_list_rc!!.setImageDwnHdlr(imageDownload)
        dash_list_rc!!.set_useremail(useremail!!)
        dash_list_rc!!.setItemDAO(item_dao!!)
        dash_list_rc!!.onLineIcon = ContextCompat.getDrawable(context, R.drawable.online_icon)
        dash_list_rc!!.newIcon = ContextCompat.getDrawable(context, R.drawable.new_icon)
        dash_list_rc!!.localIcon = ContextCompat.getDrawable(context, R.drawable.book_icon)
        val adapter = DashListAdapter()
        adapter.deleteIcon = ContextCompat.getDrawable(context, R.drawable.del_icon)
        adapter.saveIcon = ContextCompat.getDrawable(context, R.drawable.plus_icon)
        recycleView.adapter = dash_list_rc
        val actions = listaction

        adapter.swipeController(actions)
        val itemTouchhelper = ItemTouchHelper(adapter)
        itemTouchhelper.attachToRecyclerView(recycleView)
    }

    /**
     * Called when the "Save" button is clicked.
     */
    private fun SetupView() {
        pp_filterB = view!!.findViewById(R.id.pp_filterButton) as ImageButton
        var city = ""
        var country = ""

        setup_filter_button()
        if (sel_country.isEmpty() || sel_city.isEmpty()) {
            sel_country = globalCountries.country_str[0]
            sel_city = globalCountries.TW_areas_str[0]
        } else {
            city = sel_city
            country = sel_country
        }

        city_spinner = view!!.findViewById(R.id.pp_city_dash) as Spinner
        country_spinner = view!!.findViewById(R.id.pp_country_dash) as Spinner
        city_adapter = ArrayAdapter(this@Dash_Fragment.context!!, R.layout.spinner_item)
        country_adapter = ArrayAdapter(this@Dash_Fragment.context!!, R.layout.spinner_item)
        setup_country_spin(R.id.pp_country_dash)
        if (!sel_country.isEmpty()) {
            setup_city_spin(R.id.pp_city_dash)
        }

        setup_spinner_listener()
        if (city.isNotEmpty() && country.isNotEmpty()) {
            val countryIndex = globalCountries.country_str.indexOf(country)
            var cities:Array<String>? = null
            pp_country_dash.setSelection(countryIndex)
            when (country) {
                CountriesNCities().country_str[0]->
                    cities = CountriesNCities().TW_areas_str
                CountriesNCities().country_str[1]->
                    cities = CountriesNCities().JPAreasENstr
                CountriesNCities().country_str[2]->
                    cities = CountriesNCities().CN_areas_str
            }
            pp_city_dash.setSelection(cities!!.indexOf(city))
            sel_country = country
            sel_city = city
        }
        updatePPList()
    }

    fun userOnline() {
        if (set_frag!!.local_setting_isReady()) {
            //useremail = set_frag!!.prefs!!.getString(set_frag!!.user_email_pref, " ")
            if (useremail.isNullOrEmpty() || useremail == " ") {
                //Toast.makeText(this@Dash_Fragment.context, "登入會員，就可以開始跟大家分享景點喔！", Toast.LENGTH_LONG).show()
                return
            }
        } else {
            return
        }
        if (location == null) {
            location = (activity as MainActivity).getLastLocation()
            return
        } else {
            var loc = ""
            location?.let{
                val cloc = parserLocation(it)
                if (cloc!!.count() >= 2) {
                    if (cloc[0].isNotEmpty()) loc = cloc[0]
                    if (cloc[1].isNotEmpty()) loc += " ${cloc[1]}"
                    if (cloc[2].isNotEmpty()) loc += " ${cloc[2]}"
                }
            }
            val cloud_cmd = "CMD=USER_ONLINE&email=$useremail&location=$loc"
            Cloud_Helper(this@Dash_Fragment.context!!) {
                if (it == null) {
                    //println("connection error")
                    return@Cloud_Helper
                } else {
                    if (debugmode)
                        Log.i("Dash_Fragment", "resp : $it")
                }
            }.execute("POST", UserTraceURL, cloud_cmd.toString())
        }
    }

    fun upToCloud(){
        if (set_frag!!.local_setting_isReady()) {
            //useremail = set_frag!!.prefs!!.getString(set_frag!!.user_email_pref, " ")
            if (useremail.isNullOrEmpty() || useremail == " ") {
                Toast.makeText(this@Dash_Fragment.context, R.string.un_login_requirment_zh, Toast.LENGTH_LONG).show()
                return
            }
        } else {
            return
        }

        val all:List<PPModel> = item_dao!!.all

        for (pp in all) {
            if (pp.status == 1) {
                continue
            }

            val name = pp.name.toString()
            if (name.isEmpty()) continue

            var phone : String = pp.phone.toString()
            if (phone.isEmpty() || phone == " ") phone = "0"

            val addr = pp.addr
            if (addr.isEmpty() || addr == " ") continue

            var fb : String = pp.fb.toString()
            if (fb.isEmpty() || fb == " ") fb = "-"

            var web : String = pp.web.toString()
            if (web.isEmpty() || web == " ") web = "-"

            var bloginfo : String = pp.blogInfo.toString()
            if (bloginfo.isEmpty() || bloginfo == " ") bloginfo = "-"

            var tag_note : String = pp.tag_note
            if (tag_note.isEmpty() || tag_note == " ") tag_note = "-"

            var opentime : String = pp.opentime
            if (opentime.isEmpty() || opentime == " ") opentime = "-"
            val score = 0
            val pic_url = "-"
            var descrip = pp.descrip
            if (descrip.isEmpty() || descrip == " ") descrip = "-"
            val commet = "-"


            val cloud_cmd = "CMD=//TODO&email=$useremail&name=$name&" +
                    "phone=$phone&country=${pp.country}&address=$addr" +
                    "&fb=$fb&web=$web&bloggerIntro=$bloginfo&tag_note=$tag_note" +
                    "&opentime=$opentime&score=$score&pic_url=$pic_url" +
                    "&description=$descrip&comment=$commet"
            if (debugmode)
                Log.i("Dash_Fragment", "upToCloud : send: $cloud_cmd")
            Cloud_Helper(this@Dash_Fragment.context!!) {
                if (it == null) {
                    //println("connection error")
                    return@Cloud_Helper
                } else {
                    //Log.i("Dash_Fragment","upToCloud : feeback $it")
                    val cloud_db = JSONObject(it)
                    val status = cloud_db.get("status") as Boolean
                    if (status) {
                        pp.status = 1
                        item_dao!!.update(pp)
                        val str = name + " - " + getString(R.string.pp_new_success_get_exp_zh)
                        Toast.makeText(this@Dash_Fragment.context, str, Toast.LENGTH_LONG).show()
                    } else {
                        val msg = cloud_db.get("message")
                        if (msg == "PP Already Exist") {
                            pp.status = 1
                            item_dao!!.update(pp)
                        }
                    }

                }
            }.execute("POST", CloudHelperURL, cloud_cmd.toString())
        }
    }

    fun setup_spinner_listener() {
        if (country_spinner!!.onItemSelectedListener == null) {
            country_spinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(arg0: AdapterView<*>, arg1: View?, position: Int, id: Long) {
                    if (sel_country != arg0.selectedItem.toString()) {

                        sel_country = arg0.selectedItem.toString()
                        set_frag?.let {
                            set_frag!!.prefs!!.edit().putString(set_frag!!.dash_country_pref, sel_country)
                                .apply()
                        }
                        if (debugmode) Log.i("Dash_Fragment", "setup_spinner_listener : sel_country = $sel_country")
                        if (arg0.selectedItemId.toInt() == 0) {
                            setup_city_spin(R.id.pp_city_dash)//, globalCountries.TW_areas_str)
                        } else if (arg0.selectedItemId.toInt() == 1) {
                            setup_city_spin(R.id.pp_city_dash)//, globalCountries.JP_areas_str)
                        } else if (arg0.selectedItemId.toInt() == 2) {
                            setup_city_spin(R.id.pp_city_dash)//, globalCountries.CN_areas_str)
                        }
                    }

                }

                override fun onNothingSelected(arg0: AdapterView<*>) {
                    if (debugmode) Toast.makeText(this@Dash_Fragment.context, "您沒有選擇任何項目", Toast.LENGTH_LONG).show()
                }

            }
        }
        //city_spinner!!.onItemSelectedListener.let {
        if (city_spinner!!.onItemSelectedListener == null) {
            city_spinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(arg0: AdapterView<*>, arg1: View?, position: Int, id: Long) {
                    //
                    //
                    if (debugmode)
                        Log.i("Dash_Fragment", "city_spinner!! : sel_city : $sel_city, arg0.selectedItem : ${arg0.selectedItem.toString()}")
                    if (sel_city != arg0.selectedItem.toString()) {
                        //if (downloadHdler != null) {
                        //    downloadHdler!!.cancel(true)
                        //    downloadHdler = null
                        //}
                        //Toast.makeText(this@Dash_Fragment.context, arg0.selectedItem.toString(), Toast.LENGTH_LONG).show()

                        sel_city = arg0.selectedItem.toString()
                        set_frag?.let {
                            set_frag!!.prefs!!.edit().putString(set_frag!!.dash_city_pref, sel_city)
                                .apply()
                        }
                        if (debugmode)
                            Log.i("Dash_Fragment", "city_spinner!!.onItemSelectedListener : select : $sel_city")
                        global_pp_clean()
                        updatePPList()
                    }
                }

                override fun onNothingSelected(arg0: AdapterView<*>) {
                    //if (debugmode) Toast.makeText(this@Dash_Fragment.context, "您沒有選擇任何項目", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setup_country_spin(ViewID: Int) {
        //country_spinner!!.gravity = Gravity.CENTER
        val adapter = ArrayAdapter(this@Dash_Fragment.context!!, R.layout.spinner_item, globalCountries.country_str)
        //設置下拉列表的風格
        //adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        //將adapter 添加到spinner中
        country_spinner!!.setAdapter(adapter)
    }

    fun setup_city_spin(ViewID: Int) {
        if (!sel_country.isEmpty()) {
            when(sel_country) {
                globalCountries.country_str[0] ->
                    if (!city_array!!.contentEquals(globalCountries.TW_areas_str)) {
                        city_array = globalCountries.TW_areas_str
                    }
                globalCountries.country_str[1] ->
                    if (!city_array!!.contentEquals(globalCountries.JP_areas_ZH_str)) {
                        city_array = globalCountries.JP_areas_ZH_str
                    }
                globalCountries.country_str[2] ->
                    if (!city_array!!.contentEquals(globalCountries.CN_areas_str)) {
                        city_array = globalCountries.CN_areas_str
                    }
            }
        }
        //val adapter = ArrayAdapter(this@Dash_Fragment.context!!, R.layout.spinner_item, city_array)
        //設置下拉列表的風格
        //adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        //將adapter 添加到spinner中
        city_adapter!!.clear()
        city_array!!.forEach {
            city_adapter!!.add(it)
        }
        city_spinner!!.setAdapter(city_adapter)
    }

    fun setup_filter_button() {
        //pp_filterButton
        pp_filterB!!.setOnClickListener {
            call_filterView(global_pp_list!!)
        }
    }

    private fun cloudButtonCheckFun() {
        var toggleCheck:Boolean = true
        if (set_frag!!.local_setting_isReady()) {
            toggleCheck = set_frag!!.prefs!!.getBoolean(set_frag!!.dwn_pps_en_pref, true)
        }

        cloudCheck = view!!.findViewById(R.id.downloadPPsOnOff) as ImageButton
        if (toggleCheck) {
            cloudIcon = ContextCompat.getDrawable(context!!, R.drawable.online_icon)
            cloudCheck!!.setImageDrawable(cloudIcon)
        } else {
            cloudIcon = ContextCompat.getDrawable(context!!, R.drawable.non_cloud_icon)
            cloudCheck!!.setImageDrawable(cloudIcon)//.setImageDrawable(cloudIcon)
        }

        cloudCheck!!.setOnClickListener {

            if (set_frag!!.local_setting_isReady()) {
                toggleCheck = set_frag!!.prefs!!.getBoolean(set_frag!!.dwn_pps_en_pref, true)
            }

            if (toggleCheck) {
                set_frag!!.prefs!!.edit().putBoolean(set_frag!!.dwn_pps_en_pref, false).apply()
                cloud_download = false
                cloudIcon = ContextCompat.getDrawable(context!!, R.drawable.non_cloud_icon)
                it.downloadPPsOnOff.setImageDrawable(cloudIcon)
            } else {
                Log.d("Setting_Fragment", "Toggle Button Checked: FALSE")
                set_frag!!.prefs!!.edit().putBoolean(set_frag!!.dwn_pps_en_pref, true).apply()
                cloud_download = true
                cloudIcon = ContextCompat.getDrawable(context!!, R.drawable.online_icon)
                it.downloadPPsOnOff.setImageDrawable(cloudIcon)
            }
            global_pp_clean()
            updatePPList()
        }
    }


    fun global_pp_clean(){
        filterTags = mutableListOf()
        db_pp_list = mutableListOf()
        cloud_PPModelList = mutableListOf()
        global_pp_list = mutableListOf()
        displayList = mutableListOf()
    }

    fun pp_insert(pp: PPModel) : PPModel? {
        return item_dao!!.insert(pp)
    }

    fun pp_delete(id:Long):Boolean {

        return item_dao!!.delete(id)
    }

    fun call_filterView(pp_list_model: List<PPModel>) {
        val filter_f = Filter_Fragment()
        filter_f.pp_set(pp_list_model)
        (activity as MainActivity).switchFragment(this@Dash_Fragment, filter_f, 2)
    }

    inner class DashListAction(ppModelItems: List<PPModel>): ListAction() {

        override fun openImageView(imgViewer: ImageViewer_Fragment) {
            (activity as MainActivity).switchFragment(this@Dash_Fragment, imgViewer, 2)
        }

        override fun pp_onClick(partItem: PPModel) {
            //Log.i("DashListAction :", "pp_onClicked : " + partItem)
            var ppModel : PPModel? = null

            val review_f = Review_Fragment()
            partItem.let { ppModel =  partItem}
            review_f.pp_set(ppModel!!)
            (activity as MainActivity).switchFragment(this@Dash_Fragment, review_f, 2)
        }

        fun saveModelSelect(position: Int) {
            val builder = AlertDialog.Builder(this@Dash_Fragment.context)
            val inflater = activity!!.layoutInflater
            val dailogView = inflater.inflate(R.layout.dash_save_sel, null)
            val saveBook = dailogView.findViewById(R.id.dash_saveBook_group) as Button
            val saveTripList = dailogView.findViewById(R.id.dash_saveTripList_group) as Button
            val dialog: AlertDialog = builder.setTitle(R.string.dash_save_select_zh)
                .setView(dailogView)
                .create()

            dialog.show()

            saveBook.setOnClickListener{
                val ret = pp_insert(displayList!![position])
                if (ret == null) {
                    Toast.makeText(this@Dash_Fragment.context, "存入手冊 " + displayList!![position].name + " 失敗",
                        Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@Dash_Fragment.context, "存入手冊 " + displayList!![position].name + " 成功",
                        Toast.LENGTH_LONG).show()
                    displayList!!.get(position).id = ret.id
                    dash_list_rc!!.notifyDataSetChanged()
                }
                dialog.dismiss()
            }
            saveTripList.setOnClickListener {
                val tripPlanList = TripPlanning_Fragment().getAllTripList(this@Dash_Fragment.context!!)
                if (tripPlanList.count() == 0) {
                    Toast.makeText(this@Dash_Fragment.context, R.string.dash_tripList_null_zh,
                        Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                var tripPlanListArray:ArrayList<String> = arrayListOf()
                tripPlanList.forEach {
                    tripPlanListArray.add(it.planname)
                }
                val array = arrayOfNulls<String>(tripPlanListArray.size)
                val tripListBuilder = AlertDialog.Builder(this@Dash_Fragment.context)
                val tripListdialog: AlertDialog = tripListBuilder.setTitle(R.string.dash_tripList_sel_zh)
                    .setSingleChoiceItems(tripPlanListArray.toArray(array),-1) { dialog, which->

                        // Try to parse user selected color string
                        try {
                            val tripPlanDao = TripPlanDAO(this@Dash_Fragment.context!!)
                            val tripPlanModel = tripPlanDao.findName(tripPlanListArray[which])
                            val ph = PlanningHelper()
                            //Toast.makeText(this@Dash_Fragment.context, "tripPlanModel!!.id : ${tripPlanModel!!.id}, displayList!![position] name : ${displayList!![position].name}", Toast.LENGTH_LONG).show()
                            if (debugmode)
                                Log.i("Dash_Fragment",  "tripPlanModel!!.id : ${tripPlanModel!!.id}, displayList!![position] name : ${displayList!![position].name}")
                            ph.add_pp_into(this@Dash_Fragment.context!!, tripPlanModel!!.id, displayList!![position], "UNDEF")
                            // Change the layout background color using user selection
                            //Toast.makeText(this@Dash_Fragment.context, "Clicked : ${tripPlanListArray[which]}", Toast.LENGTH_LONG).show()
                        }catch (e:IllegalArgumentException){
                            // Catch the color string parse exception

                        }

                        // Dismiss the dialog
                        dialog.dismiss()
                    }
                    .create()
                    //.setPositiveButton("OK") { dialog, which ->
                    //    Toast.makeText(this@Dash_Fragment.context, "Clicked : $arrayChecked", Toast.LENGTH_LONG).show()
                    //}
                    //.setNegativeButton("Cancel") { dialog, which ->
                    //    dialog.dismiss()
                    //}


                tripListdialog.show()
                //tripListdialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
                //tripListdialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
                dash_list_rc!!.notifyDataSetChanged()
                dialog.dismiss()
            }
            //dialog.dismiss()


            //dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
            //dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
        }

        override fun onSwiped(position: Int) {
            //Log.i("DashListAction :", "onClicked : $position") //ppModelItems[position])
            var cloud = false
            if (displayList!![position].id.toInt() == 0) cloud = true
            if (cloud) {
                //saveModelSelect(position)
                val ret = pp_insert(displayList!![position])
                if (ret == null) {
                    Toast.makeText(this@Dash_Fragment.context, "存入手冊 " + displayList!![position].name + " 失敗",
                        Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@Dash_Fragment.context, "存入手冊 " + displayList!![position].name + " 成功",
                        Toast.LENGTH_LONG).show()
                    displayList!!.get(position).id = ret.id
                    dash_list_rc!!.notifyDataSetChanged()
                }
            } else {
                if (pp_delete(displayList!![position].id.toLong())) {
                    Toast.makeText(this@Dash_Fragment.context, "刪除 " + displayList!![position].name + " 完成",
                        Toast.LENGTH_LONG).show()
                    displayList!![position].id = 0
                    dash_list_rc!!.notifyDataSetChanged()
                    //displayList!!.removeAt(position)
                    //global_pp_list!![position].id = 0
                    //global_pp_list!!.removeAt(position)
                    //dash_list_rc!!.notifyItemRemoved(position);
                    //dash_list_rc!!.notifyItemRangeChanged(position, dash_list_rc!!.getItemCount())
                    updatePPList()
                } else {
                    Toast.makeText(this@Dash_Fragment.context, "刪除 " + displayList!![position].name + " 失敗",
                        Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    fun quickTag_reload(selected : MutableList<Int>) {
        quickTagFilterAdapter = FilterCustomGrid(this@Dash_Fragment.context!!, quickTagsList!!)
        if (selected.isNotEmpty())
            quickTagFilterAdapter!!.set_selectList(selected)
        toggleBTNInit(quickTagFilterAdapter!!, R.id.quick_tag, quickTagsList!!)
    }

    fun toggleBTNInit(adapter:FilterCustomGrid, layoutID:Int, list:MutableList<String>) {
        adapter.selectListInit()
        grid = view!!.findViewById(layoutID)
        grid!!.setAdapter(adapter)
    }
}