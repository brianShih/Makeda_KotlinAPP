package tw.breadcrumbs.makeda.TripPlanning

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.Fragment
//import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.opentripplan_rc_item.*
import kotlinx.android.synthetic.main.trip_planninglist_fragment.*
import org.json.JSONArray
import org.json.JSONObject
import tw.breadcrumbs.makeda.CloudObj.Cloud_Helper
import tw.breadcrumbs.makeda.DBModules.TripPlanDAO
import tw.breadcrumbs.makeda.DBModules.TripPlanDB_PPModelDAO
import tw.breadcrumbs.makeda.MainActivity
import tw.breadcrumbs.makeda.OpenTripplan_Fragment
import tw.breadcrumbs.makeda.R
import tw.breadcrumbs.makeda.Setting_Fragment
import tw.breadcrumbs.makeda.dataModel.PPModel
import tw.breadcrumbs.makeda.dataModel.TripListModel
import tw.breadcrumbs.makeda.dataModel.TripPlan_PPModel
import java.net.URLEncoder
import java.util.*
import kotlin.collections.ArrayList

class TripPlanning_Fragment : androidx.fragment.app.Fragment() {
    private val TripPlanCloudHelperURL = "//TODO"
    private val debugmode = false
    private var cloudHdler : Cloud_Helper? = null
    private var tripplanListRC : TripPlanListRC? = null
    var set_frag: Setting_Fragment? = null
    var useremail : String? = ""
    var tripPlanDB: TripPlanDAO? = null
    var tripPlanDB_PPModelDAO: TripPlanDB_PPModelDAO? = null
    var title_input_view: View? = null
    var firstLayerTripList: MutableList<TripListModel>? = mutableListOf()//CmtModel

    private val CloudHelperURL = "//TODO"
    var pH_List: TripListModel? = null
    private var cloudPPReady = 0
    var cloud_KeyItem_ppList : MutableList<PPModel> ? = null
    var cloud_Undef_ppList : MutableList<PPModel> ? = null
    /*private val productsRefreshListener = androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener{
        // 模擬加載時間
        Thread.sleep(200)

        if (useremail!!.isNotEmpty())
            cloud_downloadTripPlans()
        recyclerViewLoad(1)
        productsRefreshLayout.isRefreshing = false
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.trip_planninglist_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (tripPlanDB_PPModelDAO == null) {
            tripPlanDB_PPModelDAO = TripPlanDB_PPModelDAO(this@TripPlanning_Fragment.context!!)
        }
        tripPlanDB = TripPlanDAO(this@TripPlanning_Fragment.context!!)

        SetupView()
    }

    fun SetupView() {
        if (set_frag == null) {
            set_frag = (activity as MainActivity).setting_frag as Setting_Fragment
            if (set_frag!!.local_setting_isReady())
                useremail = set_frag!!.prefs!!.getString(set_frag!!.user_email_pref, " ")
        }
        back_button_setup()
        add_button_setup()
        openTripPlan_setup()
        sync_button_setup()
        firstLayerTripList = mutableListOf()
        recyclerViewLoad(0)
        if (useremail!!.isNotEmpty())
            cloud_downloadTripPlans()
    }

    open fun getAllTripList(context : Context):List<TripListModel>  {
        if (tripPlanDB == null) {
            tripPlanDB = TripPlanDAO(context)
        }
        val alllist = tripPlanDB!!.all
        return alllist
    }

    fun recyclerViewLoad(mode : Int) {
        // sync trip plan list
        if (mode == 1) firstLayerTripList = mutableListOf()
        if (this@TripPlanning_Fragment.context == null) return
        val alllist= getAllTripList(this@TripPlanning_Fragment.context!!)
        if (firstLayerTripList!!.count() == 0)
            firstLayerTripList!!.addAll(alllist)

        // init recycler view
        tripplanlistRecyclerView?.let {
            tripplanlistRecyclerView.layoutManager =
                androidx.recyclerview.widget.LinearLayoutManager(this@TripPlanning_Fragment.context!!)
            tripplanListRC = TripPlanListRC(
                triplistModel = firstLayerTripList!!,
                clickListener = { partItem: TripListModel, opt: Int ->
                    click(
                        partItem,
                        opt
                    )
                })
            tripplanListRC!!.useremail = useremail
            tripplanListRC!!.set_TripPlanDB(tripPlanDB!!)
            tripplanlistRecyclerView.adapter = tripplanListRC
        }
    }

    private fun click(partItem : TripListModel, opt: Int) {
        if (opt == 0) {
            if (debugmode) Toast.makeText(this@TripPlanning_Fragment.context, " click : ${partItem.planname}", Toast.LENGTH_LONG)
                .show()
            if (cloudHdler != null)
                cloudHdler!!.cancel(true)
            val pH = PlanningHelper()
            val tripPlan = tripPlanDB!!.findName(partItem.planname)
            if (tripPlan != null) {
                pH.setTripListModel(tripPlan)
                (activity as MainActivity).switchFragment(this@TripPlanning_Fragment, pH, 2)
            }
        } else if (opt == 1) {
            if(set_frag == null) {
                Toast.makeText(this@TripPlanning_Fragment.context, R.string.un_login_tripplan_zh, Toast.LENGTH_LONG).show()
                return
            }
            if (useremail == null) {
                Toast.makeText(this@TripPlanning_Fragment.context, R.string.un_login_tripplan_cloudservice_zh, Toast.LENGTH_LONG).show()
                return
            }
            if (partItem.author.equals(useremail)) {
                val builder = AlertDialog.Builder(this@TripPlanning_Fragment.context)
                val dialog: AlertDialog = builder.setTitle(R.string.tripPlan_delCloud_zh)
                    .setPositiveButton("OK") { dialog, which ->
                        cloud_deleteTripPlan(partItem.planname, useremail!!, partItem.tripplanID)
                        if (partItem.tripplanID.toInt() == 0) {
                            tripPlanDB_PPModelDAO!!.deleteTripPlanPPList(partItem.id)
                        } else {
                            tripPlanDB_PPModelDAO!!.deleteTripPlanPPList(partItem.tripplanID.toLong())
                        }
                        //tripPlanDB_PPModelDAO!!.close()
                        tripPlanDB!!.delete(partItem.id)
                        //tripPlanDB!!.close()
                        //firstLayerTripList!!.remove(partItem)//removeAt()
                        //tripplanListRC!!.notifyDataSetChanged()
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialog, which ->
                        if (partItem.tripplanID.equals("")) {
                            tripPlanDB_PPModelDAO!!.deleteTripPlanPPList(partItem.id)
                        } else {
                            tripPlanDB_PPModelDAO!!.deleteTripPlanPPList(partItem.tripplanID.toLong())
                        }
                        //tripPlanDB_PPModelDAO!!.close()
                        tripPlanDB!!.delete(partItem.id)
                        //tripPlanDB!!.close()
                        //firstLayerTripList!!.remove(partItem)//removeAt()
                        //tripplanListRC!!.notifyDataSetChanged()
                        dialog.dismiss()
                    }

                    .create()
                dialog.show()

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
            }
            recyclerViewLoad(1)
        }
    }

    fun sync_button_setup() {
        /*
        tripPlanningCloudSync.setOnClickListener{
            if (useremail!!.isNotEmpty())
                cloud_downloadTripPlans()
            recyclerViewLoad(1)
        }*/
    }

    fun openTripPlan_setup() {
        openTripPlan.setOnClickListener{
            val opentripplan_frag = OpenTripplan_Fragment()
            (activity as MainActivity).switchFragment(this@TripPlanning_Fragment, opentripplan_frag, 2)
        }
    }

    fun add_button_setup() {
        tripPlanningAddButton.setOnClickListener {
            val builder = AlertDialog.Builder(this@TripPlanning_Fragment.context)
            val inflater = activity!!.layoutInflater
            val dailogView = inflater.inflate(R.layout.trip_title_input, null)
            val nameText = dailogView!!.findViewById(R.id.tripPlanTitle) as EditText
            if(set_frag == null) {
                Toast.makeText(this@TripPlanning_Fragment.context, R.string.un_login_tripplan_zh, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val author = set_frag!!.prefs!!.getString(set_frag!!.user_email_pref, " ")
            if (author == null) {
                Toast.makeText(this@TripPlanning_Fragment.context, R.string.un_login_tripplan_cloudservice_zh, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val log0 = JSONObject()
            log0.put("DATE", Date().toString())
            log0.put("USER", author)
            log0.put("OPTION","ADD")
            val log_jsonArray = JSONArray()
            log_jsonArray.put(0, log0)

            val group0 = JSONObject()
            group0.put("ID", 0)
            group0.put("USEREMAIL", author)
            val group_jsonArray = JSONArray()
            group_jsonArray.put(0, group0)

            val dialog: AlertDialog = builder.setTitle(R.string.trip_titleName_zh)
                .setView(dailogView)
                .setPositiveButton("OK") { dialog, which ->
                    val title = nameText.text.toString()
                    Toast.makeText(this@TripPlanning_Fragment.context, " input : $title", Toast.LENGTH_LONG).show()
                    tripplanlistRecyclerView?.post {
                        val temp = tripPlanDB!!.findName(title)
                        if (temp == null) {
                            val model =
                                TripListModel(
                                    0,
                                    title,
                                    author!!,
                                    "",//group_jsonArray.toString(),
                                    "",
                                    "",
                                    "",
                                    log_jsonArray.toString(),
                                    0
                                )
                            //val useremail:String? = set_frag!!.prefs!!.getString(set_frag!!.user_email_pref, " ")
                            cloud_insertTripPlan(title, useremail!!, model)
                            val modelv = tripPlanDB!!.insert(model)
                            //firstLayerTripList!!.add(modelv!!)
                            recyclerViewLoad(1)
                        } else {
                            Toast.makeText(this@TripPlanning_Fragment.context, R.string.trip_title_duplicate_zh, Toast.LENGTH_LONG).show()
                        }
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
    }

    fun back_button_setup() {
        tripPlanningBackButton.setOnClickListener {
            if (cloudHdler != null ) cloudHdler!!.cancel(false)
            (activity as MainActivity).onBackPressed()
        }
    }

    fun cloud_tripPlan_UNDEF_Sync(planname : String) {
        val needAddLocArr : ArrayList<String>? = arrayListOf()
        val needAddCloudArr : ArrayList<String>? = arrayListOf()
        // re-load trip plan
        val tripPlanList = tripPlanDB!!.findName(planname)
        if (tripPlanList == null) return
        if (tripPlanList.contentJsonFormat.isNotEmpty()) {
            val contentJsonfromat = JSONObject(tripPlanList.contentJsonFormat)
            when {
                !contentJsonfromat.isNull("UNDEF") -> {
                    val undefJsonArray = contentJsonfromat.getJSONArray("UNDEF")
                    (0..undefJsonArray.length() - 1).forEach { i ->
                        val pp = JSONObject(undefJsonArray.get(i).toString())
                        val pp_cloudID = pp.get("cloudID").toString()
                        val cloudUndefArr = getCloudContentArray( planname,"UNDEF")

                        var same = 0
                        if (!cloudUndefArr.isNullOrEmpty()) {
                            cloudUndefArr.forEach {
                                if (pp_cloudID == it) same = 1
                            }
                        }
                        when {
                            same == 0 -> {
                                needAddCloudArr!!.add(pp_cloudID)
                            }
                        }
                    }
                    local_convTO_CloudContentArray(planname,"UNDEF", needAddCloudArr!!)
                }
            }
        }
        if (tripPlanList!!.cloudContent.isNotEmpty()) {
            val cloudContent = JSONObject(tripPlanList!!.cloudContent)
            when {
                !cloudContent.isNull("UNDEF") -> {
                    val cloud_undefJsonArray = cloudContent.get("UNDEF") as JSONArray//getJSONArray("UNDEF")
                    if (debugmode) Log.i("TripPlanning_Fragment","cloudContent.get(\"UNDEF\") as JSONArray : $cloud_undefJsonArray")
                    (0..cloud_undefJsonArray.length() - 1).forEach { i ->
                        val pp_cloudID = cloud_undefJsonArray.get(i).toString()
                        val localUndefArr = getLocalContentArray( planname,"UNDEF")
                        var same = 0
                        if (!localUndefArr.isNullOrEmpty()) {
                            localUndefArr.forEach {
                                if (pp_cloudID == it.cloudID.toString()) same = 1
                            }
                        }
                        when {
                            same == 0 -> {
                                //addLocalContentArray("UNDEF", pp_cloudID)
                                needAddLocArr!!.add(pp_cloudID)
                            }
                        }
                    }
                    cloud_convTO_LocalContentArray( planname,"UNDEF", needAddLocArr!!)
                }
            }
        }
    }

    fun cloud_tripPlan_DAYKey_Sync(planname : String) {
        var needAddLocArr : ArrayList<String>? = null//arrayListOf()
        var needAddCloudArr : ArrayList<String>? = null//arrayListOf()
        val tripPlanList = tripPlanDB!!.findName(planname)
        for (i in 1..PlanningHelper().daysLimit) {
            needAddCloudArr = arrayListOf()
            needAddLocArr = arrayListOf()
            val item = "DAY ${i}"
            if (tripPlanList!!.contentJsonFormat.isNotEmpty() && tripPlanList.cloudContent.isNotEmpty()) {
                val cl_content = JSONObject(tripPlanList.cloudContent)
                val content = JSONObject(tripPlanList.contentJsonFormat)
                if (content.isNull(item) && cl_content.isNull(item)) return
            }

            val localItemArr = getLocalContentArray(planname, item)
            val cloudItemArr = getCloudContentArray(planname, item)
            if (localItemArr == null && cloudItemArr == null) return
            if (debugmode) Log.i("TripPlanning_Fragment", "cloud_tripPlan_DAYKey_Sync - localItemArr : $localItemArr, cloudItemArr = $cloudItemArr, DAY = $item")

            if (localItemArr != null) localItemArr.forEach {
                var same = 0
                if (cloudItemArr != null) cloudItemArr.forEach { cloud_it ->
                    if (cloud_it == it.cloudID.toString()) same = 1
                }
                when {
                    same == 0 -> {
                        needAddCloudArr!!.add(it.cloudID.toString())
                    }
                }
            }
            if (needAddCloudArr!!.count() > 0)
                local_convTO_CloudContentArray(planname, item, needAddCloudArr)

            if (cloudItemArr != null) cloudItemArr.forEach {
                var same = 0
                if (localItemArr != null) {
                    localItemArr.forEach { local_it ->
                        if (local_it.cloudID.toString() == it) same = 1
                    }
                }
                when {
                    same == 0 -> {
                        needAddLocArr!!.add(it)
                    }
                }
            }
            if (needAddLocArr!!.count() > 0)
                cloud_convTO_LocalContentArray(planname, item, needAddLocArr)
        }
        //val localUndefArr = getLocalContentArray("UNDEF")
    }

    fun cloud_convTO_LocalContentArray(planname: String, item: String, valueArray : ArrayList<String>) {
        if (debugmode) Log.i("Tripplanning_Fragment", "cloud_convTO_LocalContentArray - item : $item, valueArray = $valueArray")
        val tripplanList = tripPlanDB!!.findName(planname)
        if (valueArray.count() > 0) {
            val getCloudRespThread = Thread(Runnable {
                while (cloudPPReady == 1) {
                    Thread.sleep(1000)
                }
                download_MultiPP(this@TripPlanning_Fragment.context!!, tripplanList!!, item, valueArray)
            })
            getCloudRespThread.start()
        }
    }

    fun local_convTO_CloudContentArray(planname: String, item: String, valueArray : ArrayList<String>) {
        if (debugmode) Log.i("Tripplanning_Fragment", "local_convTO_CloudContentArray - item : $item, valueArray = $valueArray")
        val planList = tripPlanDB!!.findName(planname)
        var jsonArr : JSONArray? = JSONArray()
        var cloudContent: JSONObject? = JSONObject()
        if (debugmode) Log.i("Tripplanning_Fragment", "local_convTO_CloudContentArray- cloudContent : ${planList!!.cloudContent}, content : ${planList!!.contentJsonFormat}")
        when {
            planList!!.cloudContent.isNotEmpty() -> {
                cloudContent = JSONObject(planList.cloudContent)
                if (!cloudContent.isNull(item))
                    jsonArr = cloudContent.getJSONArray(item)
            }
            else -> {
                cloudContent = JSONObject()
            }
        }
        valueArray.forEach {
            jsonArr!!.put(it)
        }
        cloudContent.put(item, jsonArr!!)
        planList.cloudContent = cloudContent.toString()
        tripPlanDB!!.update(planList)
        if (debugmode) Log.i("Tripplanning_Fragment", "local_convTO_CloudContentArray- call : valueArray : $valueArray")
        //sync to cloud data
        valueArray.forEach {
            //val useremail:String? = set_frag!!.prefs!!.getString(set_frag!!.user_email_pref, " ")
            addPP_toCloud(this@TripPlanning_Fragment.context!!, useremail!!, planList, item, it)
        }
    }

    fun getCloudContentArray(planname: String, item: String) : ArrayList<String>? {
        var ret : ArrayList<String>? = null//arrayListOf()//null
        val tripPlanList = tripPlanDB!!.findName(planname)
        if (tripPlanList!!.cloudContent.isNotEmpty()) {
            val cloudContent = JSONObject(tripPlanList.cloudContent)
            when {
                !cloudContent.isNull(item) -> {
                    ret = arrayListOf()
                    val json_arr = cloudContent.getJSONArray(item)
                    (0..json_arr.length()).forEach { i ->
                        if (json_arr.isNull(i)) {
                            return@forEach
                        }
                        var same = 0
                        ret.forEach {
                            if (it == json_arr.get(i)) {
                                same = 1
                            }
                        }
                        when (same) {
                            0 -> {
                                ret.add(json_arr.get(i).toString())
                            }
                        }
                    }
                }
            }
        }
        return ret
    }

    fun getLocalContentArray(planname : String, item: String) : ArrayList<PPModel>? {
        var ret : ArrayList<PPModel>? = null//arrayListOf()
        val tripPlanList = tripPlanDB!!.findName(planname)
        if (tripPlanList!!.contentJsonFormat.isNotEmpty()) {
            val content = JSONObject(tripPlanList.contentJsonFormat)
            when {
                !content.isNull(item) -> {
                    ret = arrayListOf()
                    var json_arr : JSONArray? = null//JSONArray()
                    if (content.get(item).toString().isNotEmpty()) {
                        json_arr = content.get(item) as JSONArray
                        (0..json_arr.length() - 1).forEach { i ->
                            //if (json_arr.isNull(i)) {
                            //    return@forEach
                            //}
                            var same = 0
                            ret.forEach {
                                val pp = JSONObject(json_arr.get(i).toString())
                                val cloudID = pp.get("cloudID") as Int
                                if (debugmode) Log.i("TripPlanning_Fragment", "getLocalContentArray - $item cloudID = $cloudID")
                                if (it.cloudID.toString() == cloudID.toString()) {
                                    same = 1
                                }
                            }
                            when (same) {
                                0 -> {
                                    //val ppStr =
                                    val pp = JSONObject(json_arr.get(i).toString())
                                    val ppID = pp.get("id") as Int
                                    val cloudID = pp.get("cloudID") as Int
                                    val ppModel = PPModel(
                                        ppID.toLong(),
                                        cloudID.toLong(),
                                        pp.get("name").toString(),
                                        pp.get("phone").toString(),
                                        pp.get("country").toString(),
                                        pp.get("addr").toString(),
                                        pp.get("fb").toString(),
                                        pp.get("web").toString(),
                                        pp.get("blogInfo").toString(),
                                        pp.get("opentime").toString(),
                                        pp.get("tag_note").toString(),
                                        pp.get("descrip").toString(),
                                        pp.get("distance").toString()
                                    )
                                    ret.add(ppModel)
                                }
                            }
                        }
                    }
                }
            }
        }
        return ret
    }

    fun local_syncCloudTripPlans(cloud_tripplanAll:List<TripListModel>) {
        val loc_tripplanAll = tripPlanDB!!.all
        loc_tripplanAll.forEach {
            var same = 0
            cloud_tripplanAll.forEach{ cloudTripListModel ->
                if (it.tripplanID == cloudTripListModel.tripplanID) same = 1
            }
            if (same == 0) {
                // cloud_insertTripPlan
                cloud_insertTripPlan(it.planname, useremail!!, it)
            }
        }
    }

    fun cloud_downloadTripPlans() {
        val cloud_tripplanAll : ArrayList<TripListModel>? = arrayListOf()
        if(set_frag == null) {
            Toast.makeText(this@TripPlanning_Fragment.context, R.string.un_login_tripplan_zh, Toast.LENGTH_LONG).show()
            return
        }
        //val useremail:String? = set_frag!!.prefs!!.getString(set_frag!!.user_email_pref, " ")
        if (useremail == null) {
            Toast.makeText(this@TripPlanning_Fragment.context, R.string.un_login_tripplan_cloudservice_zh, Toast.LENGTH_LONG).show()
            return
        }

        val cloud_cmd = "CMD=//TODO&useremail=$useremail"
        cloudHdler = Cloud_Helper(this@TripPlanning_Fragment.context!!) {
            var cloudSync_basic_Opt_done = 0
            if (it == null) {
                Toast.makeText(this@TripPlanning_Fragment.context, "網路不穩定，請稍後再試", Toast.LENGTH_LONG).show()
                return@Cloud_Helper
            }
            if (debugmode) Log.i("TripPlanning_Fragment", " cloud_downloadTripPlans - Cloud Update response : $it")
            val cloud_db = JSONObject(it)
            if (!cloud_db.isNull("count")) {
                val cloud_count = cloud_db.get("count") as Int
                if (debugmode) Log.i("TripPlanning_Fragment", "cloud_downloadTripPlans - cloud count = $cloud_count")
                val ctrlArr = IntArray(cloud_count)
                for (i in 1..cloud_count) {
                    ctrlArr[i - 1] = 0
                    if (cloud_db.isNull(i.toString())) continue
                    val tempDB = cloud_db.get(i.toString()) as JSONObject

                    val cloud_tripplan = TripListModel(0, tempDB.get("tripplanName").toString(),
                        tempDB.get("author").toString(),
                        tempDB.get("grouplist").toString(),
                        "",
                        tempDB.get("contentJsonFormat").toString(),
                        tempDB.get("tripplanID").toString(),
                        tempDB.get("log").toString(), 0)
                    cloud_tripplanAll!!.add(cloud_tripplan)
                    if (debugmode) Log.i("TripPlanning_Fragment", " cloud_downloadTripPlans: index: $i / $cloud_tripplan")
                    if (tripPlanDB == null) {
                        tripPlanDB = TripPlanDAO(context!!)
                    }

                    val localTripplan = tripPlanDB!!.get_useTripPlanID(cloud_tripplan.tripplanID.toLong())//findName(cloud_tripplan.planname)
                    if (localTripplan != null) { //need to sync
                        localTripplan.grouplist = cloud_tripplan.grouplist
                        tripPlanDB!!.update(localTripplan)
                        val localNotExistID = compareCloudContent(localTripplan, cloud_tripplan, 1)
                        val cloudNotExistID = compareCloudContent(localTripplan, cloud_tripplan, 0)
                        //compareCloudContent(localTripplan, cloud_tripplan, 0)
                        if (localNotExistID!!.count() > 0 || cloudNotExistID!!.count() > 0) {
                            //get local not exist
                            if (localNotExistID.count() > 0) {
                                if (debugmode) Log.i("TripPlanning_Fragment", "cloud_downloadTripPlans - need to Sync / local not exist some PP")
                                val getLocalSyncThread = Thread(Runnable {
                                    while (ctrlArr[i - 1] > 0) {
                                        Thread.sleep(1000)
                                    }
                                    userAlertDailog("是否下載雲端景點至 ${localTripplan.planname} 中", {
                                        val items = getCloudContentItems(cloud_tripplan)
                                        items!!.forEach { item ->
                                            val downArr: ArrayList<String> = arrayListOf()
                                            val arr = getPPModelFromItem(cloud_tripplan, item)
                                            arr!!.forEach { itemOfPPid ->
                                                var same = 0
                                                localNotExistID.forEach { notExistID ->
                                                    if (itemOfPPid.equals(notExistID)) same = 1
                                                }
                                                if (same == 1) downArr.add(itemOfPPid)
                                            }
                                            download_MultiPP(
                                                this@TripPlanning_Fragment.context!!,
                                                cloud_tripplan,
                                                item,
                                                downArr
                                            )
                                            localTripplan.cloudContent = cloud_tripplan.cloudContent
                                            tripPlanDB!!.update(localTripplan)
                                        }
                                        //cloudSync_basic_Opt_done = 1
                                        ctrlArr[i - 1] = 1
                                    })
                                })
                                getLocalSyncThread.start()
                            } else ctrlArr[i - 1] = 1//cloudSync_basic_Opt_done = 1
                            //get cloud not exist
                            if (debugmode) Log.i("TripPlanning_Fragment", "cloud_downloadTripPlans - cloudNotExistID = $cloudNotExistID")
                            val list = arrayListOf<String>()
                            list.add("刪除本地端資料")
                            list.add("上載至雲端")
                            val array = arrayOfNulls<String>(list.size)
                            if (cloudNotExistID!!.count() > 0) {
                                if (debugmode) Log.i("TripPlanning_Fragment", "cloud_downloadTripPlans - need to Sync / cloud not exist some PP")
                                val getCloudSyncThread = Thread(Runnable {
                                    while (ctrlArr[i - 1] < 1) {
                                        Thread.sleep(1000)
                                    }
                                    userAlertDailogListMode(
                                        "${localTripplan.planname} 部分資料不存在雲端中，處理的方式？",
                                        list.toArray(array),
                                        { clickSel ->
                                            if (debugmode) Log.i(
                                                "TripPlanning_Fragment",
                                                "cloud_downloadTripPlans - userAlertDailogListMode click = ${clickSel.get(
                                                    0
                                                )} , ${clickSel.get(1)}"
                                            )
                                            if (clickSel.get(0)) { //delet local pp
                                                cloudNotExistID.forEach {
                                                    tripPlanDB_PPModelDAO!!.deleteUsingCloudID(
                                                        it.toLong(),
                                                        localTripplan.tripplanID.toLong()
                                                    )
                                                }
                                                localTripplan.cloudContent = cloud_tripplan.cloudContent
                                                tripPlanDB!!.update(localTripplan)
                                            } else if (clickSel.get(1)) { //up to cloud
                                                cloudNotExistID.forEach {
                                                    var ID: Long = 0
                                                    if (localTripplan.tripplanID.equals("") || localTripplan.tripplanID.isEmpty()) {
                                                        ID = localTripplan.id
                                                    } else ID = localTripplan.tripplanID.toLong()
                                                    val pp = tripPlanDB_PPModelDAO!!.get_useCloudID(
                                                        it.toLong(),
                                                        ID
                                                    )
                                                    if (pp != null) {
                                                        addPP_toCloud(
                                                            this@TripPlanning_Fragment.context!!,
                                                            useremail!!,
                                                            localTripplan,
                                                            pp.setTripPlanItem,
                                                            it
                                                        )
                                                    }
                                                }
                                                cloud_tripplan.cloudContent = localTripplan.cloudContent

                                                //cloudSync_basic_Opt_done = 2
                                                //
                                            } else {
                                                Toast.makeText(
                                                    this@TripPlanning_Fragment.context,
                                                    "請重新按下選項",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                            ctrlArr[i - 1] = 2
                                        })
                                })
                                getCloudSyncThread.start()
                            } else ctrlArr[i - 1] = 2//cloudSync_basic_Opt_done = 2

                            if (debugmode) Log.i("TripPlanning_Fragment", " cloud content or group list is different, update to DB: index: $i / ${tempDB.get("tripplanName").toString()}")
                        } else ctrlArr[i - 1] = 2

                        var localMoved = compare_ppInItem(localTripplan, cloud_tripplan, 1)
                        var cloudMoved = compare_ppInItem(localTripplan, cloud_tripplan, 0)
                        if (debugmode) Log.i("TripPlanning_Fragment", "cloud_downloadTripPlans - localMoved = $localMoved, cloudMoved = $cloudMoved")
                        if (localMoved.count() > 0 || cloudMoved.count() > 0) {
                            if (debugmode) Log.i("TripPlanning_Fragment", "cloud_downloadTripPlans - need to Sync / content different : ctrlArr[i - 1] = ${ctrlArr[i - 1]}")
                            val getContentSyncThread = Thread(Runnable {
                                while (ctrlArr[i - 1] < 2) {
                                    Thread.sleep(1000)
                                }
                                // update again
                                localMoved = compare_ppInItem(localTripplan, cloud_tripplan, 1)
                                cloudMoved = compare_ppInItem(localTripplan, cloud_tripplan, 0)
                                if (localMoved.count() <= 0 && cloudMoved.count() <= 0) {
                                    return@Runnable
                                }
                                val movelist = arrayListOf<String>()
                                movelist.add("同步至本地端資料")
                                movelist.add("同步至雲端資料")
                                val mv_array = arrayOfNulls<String>(movelist.size)
                                userAlertDailogListMode("${localTripplan.planname} 部分資料已被搬移，處理的方式？",
                                    movelist.toArray(mv_array),
                                    { clickSel ->
                                        if (clickSel.get(0)) {
                                            for (cloudMV in cloudMoved) {
                                                val id = cloudMV.key
                                                val item = cloudMV.value
                                                if (debugmode) Log.i(
                                                    "TripPlanning_Fragment",
                                                    "cloud_downloadTripPlans - cloudMV = $cloudMV"
                                                )
                                                val pp = tripPlanDB_PPModelDAO!!.get_useCloudID(
                                                    id,
                                                    localTripplan.tripplanID.toLong()
                                                )
                                                if (debugmode) Log.i(
                                                    "TripPlanning_Fragment",
                                                    "cloud_downloadTripPlans - pp = $pp"
                                                )
                                                if (pp != null) {
                                                    pp.setTripPlanItem = item
                                                    tripPlanDB_PPModelDAO!!.update(pp)
                                                }
                                            }
                                            for (localMV in localMoved) {
                                                val id = localMV.key
                                                val item = localMV.value
                                                if (cloudMoved.count() <= 0) {
                                                    // clean all of local items
                                                    tripPlanDB_PPModelDAO?.deleteUsingCloudID(id, localTripplan.tripplanID.toLong())
                                                }
                                            }
                                            localTripplan.cloudContent = cloud_tripplan.cloudContent
                                            tripPlanDB!!.update(localTripplan)
                                        } else if (clickSel.get(1)) {
                                            for (localMV in localMoved) {
                                                val id = localMV.key
                                                val item = localMV.value
                                                if (debugmode) Log.i(
                                                    "TripPlanning_Fragment",
                                                    "cloud_downloadTripPlans - localMV = $localMV"
                                                )
                                                val pp = tripPlanDB_PPModelDAO!!.get_useCloudID(
                                                    id,
                                                    cloud_tripplan.tripplanID.toLong()
                                                )
                                                if (pp != null) {
                                                    //pp.setTripPlanItem = item
                                                    //tripPlanDB_PPModelDAO!!.update(pp!!)
                                                    movePP_onCloud(
                                                        context!!,
                                                        useremail!!,
                                                        cloud_tripplan,
                                                        pp.setTripPlanItem,
                                                        item,
                                                        pp.cloudID.toString()
                                                    )
                                                }
                                            }
                                        } else {
                                            Toast.makeText(
                                                this@TripPlanning_Fragment.context,
                                                "請重新按下選項",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                        ctrlArr[i - 1] = 0
                                        //cloudSync_basic_Opt_done = 0
                                    })
                            })
                            getContentSyncThread.start()
                        } else ctrlArr[i - 1] = 0//cloudSync_basic_Opt_done = 0

                    } else {
                        if (debugmode) Log.i("TripPlanning_Fragment", " ${tempDB.get("tripplanName").toString()} : local data is not exist")
                        //val sameName_localTripplan = tripPlanDB!!.findName(cloud_tripplan.planname)
                        //if (sameName_localTripplan!!.tripplanID != cloud_tripplan.tripplanID) {
                        //    sameName_localTripplan.tripplanID = cloud_tripplan.tripplanID
                        //}
                        tripPlanDB!!.insert(cloud_tripplan)
                        val items = getCloudContentItems(cloud_tripplan)
                        items!!.forEach{ item->
                            val downArr : ArrayList<String> = arrayListOf()
                            val arr = getPPModelFromItem(cloud_tripplan, item)
                            download_MultiPP(this@TripPlanning_Fragment.context!!, cloud_tripplan, item, arr!!)
                        }
                        firstLayerTripList!!.add(cloud_tripplan)
                    }
                    /*
                    firstLayerTripList!!.forEach {
                        cloud_tripPlan_UNDEF_Sync(it.planname)
                        cloud_tripPlan_DAYKey_Sync(it.planname)
                    }
                     */

                    tripplanListRC!!.notifyDataSetChanged()
                }
            }
            if (cloud_tripplanAll!!.count() > 0) {
                local_syncCloudTripPlans(cloud_tripplanAll)
            }

        }
        cloudHdler!!.execute("POST", TripPlanCloudHelperURL, cloud_cmd.toString())
    }

    fun getCloudContentFromTripPlan(tripplan: TripListModel) : ArrayList<String>? {
        val idStrArr : ArrayList<String>? = arrayListOf()
        var cloudContentJsonObj: JSONObject? = null
        //getCloudContentArray
        if (!(tripplan.cloudContent.isEmpty() || tripplan.cloudContent.equals(""))) {
            cloudContentJsonObj = JSONObject(tripplan.cloudContent)
            if (!cloudContentJsonObj.isNull("UNDEF")) {
                if (cloudContentJsonObj.get("UNDEF") is JSONArray) {
                    val undefArrStr = cloudContentJsonObj.getJSONArray("UNDEF")
                    for (i in 0..undefArrStr.length() - 1) {
                        if (!undefArrStr.isNull(i))
                            idStrArr!!.add(undefArrStr.get(i).toString())
                    }
                }
            }
            for ( dayIndex in 1.. PlanningHelper().daysLimit) {
                val dayStr = "DAY $dayIndex"
                if (!cloudContentJsonObj.isNull(dayStr)) {
                    if (cloudContentJsonObj.get(dayStr) is JSONArray) {
                        val dayXItemArrStr = cloudContentJsonObj.getJSONArray(dayStr)
                        for (i in 0..dayXItemArrStr.length() - 1) {
                            if (!dayXItemArrStr.isNull(i))
                                idStrArr!!.add(dayXItemArrStr.get(i).toString())
                        }
                    }
                }
            }
        }

        return idStrArr
    }

    fun getPPModelFromItem(tripplan: TripListModel, item:String) : ArrayList<String>? {
        var ret : ArrayList<String>? = arrayListOf()
        var cloudContentJsonObj: JSONObject? = null
        if (!(tripplan.cloudContent.isEmpty() || tripplan.cloudContent.equals(""))) {
            cloudContentJsonObj = JSONObject(tripplan.cloudContent)
            if (!cloudContentJsonObj.isNull(item)) {
                val ArrStr = cloudContentJsonObj.getJSONArray(item)
                for (i in 0..ArrStr.length() - 1) {
                    if (!ArrStr.isNull(i))
                        ret!!.add(ArrStr.get(i).toString())
                }
            }
        }

        return ret
    }

    fun getCloudContentItems(tripplan: TripListModel) : ArrayList<String>? {
        var ret : ArrayList<String>? = arrayListOf()
        var cloudContentJsonObj: JSONObject? = null
        if (!(tripplan.cloudContent.isEmpty() || tripplan.cloudContent.equals(""))) {
            cloudContentJsonObj = JSONObject(tripplan.cloudContent)
            if (!cloudContentJsonObj.isNull("UNDEF")) {
                ret!!.add("UNDEF")
            }
            for ( dayIndex in 1.. PlanningHelper().daysLimit) {
                val dayStr = "DAY $dayIndex"
                if (!cloudContentJsonObj.isNull(dayStr)) {
                    ret!!.add(dayStr)
                }
            }
        }
        return ret
    }

    fun compareCloudContent (localTripplan: TripListModel, cloudTripPlan: TripListModel, CL: Int) : ArrayList<String>?{
        // CL 0: return cloud Not Exist cloud ID
        // CL 1: return local Not Exist Cloud ID
        var ret : ArrayList<String>? = null
        val cloud_NotExist : ArrayList<String>? = arrayListOf()
        if (tripPlanDB_PPModelDAO == null) TripPlanDB_PPModelDAO(context!!)
        val locTripplanPPList = tripPlanDB_PPModelDAO!!.findTripPlan(localTripplan.tripplanID.toLong())//.getItemX(localTripplan.tripplanID.toLong(), item)
        val locCloudID_StrArr = getCloudContentFromTripPlan(localTripplan)
        val cloud_CloudID_StrArr = getCloudContentFromTripPlan(cloudTripPlan)
        if (debugmode) Log.i("TripPlanning_Fragment", "compareCloudContent - locCloudID = $locCloudID_StrArr, cloud_CloudID = $cloud_CloudID_StrArr")
        if (debugmode) Log.i("TripPlanning_Fragment", "locTripplanPPList = $locTripplanPPList")

        if (CL == 1) {
            // local not exist
            val locNotExist: ArrayList<String>? = arrayListOf()
            cloud_CloudID_StrArr?.forEach { cloud ->
                var same = 0
                locTripplanPPList!!.forEach { loctripplanPP->
                    if (cloud.equals(loctripplanPP.cloudID.toString())) {
                        same = 1
                    }
                }

                locCloudID_StrArr?.forEach { local ->
                    if (cloud.equals(local)) {
                        same = 1
                    }
                }
                if (same == 0) locNotExist!!.add(cloud)
            }
            ret = locNotExist
        } else if (CL == 0) {
            locTripplanPPList?.forEach { tripplanPP ->
                var not_exist = 0
                locCloudID_StrArr?.forEach {
                    if (!tripplanPP.cloudID.equals(it.toLong())) {
                        not_exist = 1
                    }
                }
                if (not_exist == 1) locCloudID_StrArr!!.add(tripplanPP.cloudID.toString())
            }

            locCloudID_StrArr?.forEach { local ->
                var same = 0
                cloud_CloudID_StrArr?.forEach { cloud ->
                    if (local.equals(cloud)) {
                        same = 1
                    }
                }
                if (same == 0) cloud_NotExist!!.add(local)

            }
            ret = cloud_NotExist

        }
        return ret
    }

    fun compare_ppInItem(localTripplan: TripListModel, cloudTripPlan: TripListModel, CL: Int) : MutableMap<Long, String> {
        //getItemX
        if (tripPlanDB_PPModelDAO == null) TripPlanDB_PPModelDAO(context!!)
        val cloud_items = getCloudContentItems(cloudTripPlan)
        val loc_items = tripPlanDB_PPModelDAO!!.getItems(localTripplan.tripplanID.toLong())
        var searchItems : ArrayList<String> = arrayListOf()
        val cloudPPListMoveList: MutableMap<Long, String> = mutableMapOf()//mapOf()
        val localPPListMoveList: MutableMap<Long, String> = mutableMapOf()//mapOf()
        if (debugmode) {
            Log.i("TripPlanning_Fragment", "compare_ppInItem - tripplan planname = ${localTripplan.planname}")
            Log.i("TripPlanning_Fragment", "compare_ppInItem - cloud_items = $cloud_items, loc_items = $loc_items")
        }
/*
        if (cloud_items?.count() != loc_items?.count()) {
            cloud_items?.forEach { cloudItem ->
                var same = 0
                loc_items?.forEach { locItem ->
                    if (cloudItem.equals(locItem) ) same = 1
                }
                if (same == 0) {
                    PlanningHelper().addDayOption(context!!, localTripplan, cloudItem)
                    //loc_items.add(cloudItem)
                }
            }
        }

 */

        //if (cloud_items?.count() == loc_items?.count()) {
            cloud_items?.forEach { item ->
                val loc_ppList = tripPlanDB_PPModelDAO!!.getItemX(localTripplan.tripplanID.toLong(), item)
                val cloud_ppIDList = getPPModelFromItem(cloudTripPlan, item)
                if (debugmode) {
                    Log.i("TripPlanning_Fragment", "compare_ppInItem - item : $item, cloud_ppIDList = $cloud_ppIDList")
                    loc_ppList?.forEach {
                        Log.i("TripPlanning_Fragment", "compare_ppInItem - item : $item, loc_ppList = ${it.cloudID}")
                    }
                }
                loc_ppList?.forEach{ loc_pp ->
                    var same = 0
                    cloud_ppIDList?.forEach { cloud_ppID ->
                        if (loc_pp.cloudID.equals(cloud_ppID.toLong())) same = 1
                    }
                    if (same == 0) {
                        if (debugmode) Log.i("TripPlanning_Fragment", "compare_ppInItem - put ${loc_pp.cloudID}, $item into localPPListMoveList")
                        localPPListMoveList.put(loc_pp.cloudID, item)
                    }
                }
                cloud_ppIDList?.forEach { cloud_ppID ->
                    var same = 0
                    loc_ppList?.forEach { loc_pp ->
                        if (cloud_ppID.equals(loc_pp.cloudID.toString())) same = 1
                    }
                    if (same == 0) {
                        Log.i("TripPlanning_Fragment", "compare_ppInItem - put $cloud_ppID, $item into cloudPPListMoveList")
                        cloudPPListMoveList.put(cloud_ppID.toLong(), item)
                    }
                }
            }
        //}
        if (CL == 0) return cloudPPListMoveList
        else return localPPListMoveList

    }

    fun userAlertDailogListMode(titleStr : String, list: Array<String>, okOption: (click: Array<Boolean>) -> Unit) {
        tripplanning_layout?.post {
            val title = titleStr
            val clickBool: Array<Boolean> = arrayOf(false, false)
            val alertBuilder = AlertDialog.Builder(context, R.style.AlertDialogStyle)
            val alertdialog: AlertDialog = alertBuilder.setTitle(title)
                .setSingleChoiceItems(list, -1) { dialog, which ->
                    when (which) {
                        0 -> {
                            clickBool[0] = true
                            clickBool[1] = false
                        }

                        1 -> {
                            clickBool[0] = false
                            clickBool[1] = true
                        }
                    }

                }
                .setPositiveButton("OK") { dialog, which ->
                    okOption(clickBool)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, which ->
                    dialog.dismiss()
                }
                .create()

            alertdialog.show()
        }
    }

    fun userAlertDailog(titleStr : String, okOption: () -> Unit) {
        //Looper.prepare()
        tripplanning_layout?.post {
            val title = titleStr
            val alertBuilder = AlertDialog.Builder(context, R.style.AlertDialogStyle)
            val alertdialog: AlertDialog = alertBuilder.setTitle(title)
                .setPositiveButton("OK") { dialog, which ->
                    okOption()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, which ->
                    dialog.dismiss()
                }
                .create()

            alertdialog.show()
        }
        //Looper.loop()
    }

    fun syncGroupList_local_n_cloud(planname: String, localTripPlan: TripListModel, cloudTripPlan: TripListModel) {
        val Cloud_notExist: ArrayList<String> = arrayListOf()
        val Local_notExist: ArrayList<String> = arrayListOf()
        val loc_group: ArrayList<String> = arrayListOf()
        val cloud_group: ArrayList<String> = arrayListOf()
        if (localTripPlan.grouplist.isEmpty() && cloudTripPlan.grouplist.isEmpty()) return
        if (localTripPlan.grouplist.isNotEmpty()) {
            val temp = localTripPlan.grouplist.split(", ").map { it.toString() }
            temp.forEach {
                loc_group.add(it)
            }
        }

        if (cloudTripPlan.grouplist.isNotEmpty()) {
            val temp = localTripPlan.grouplist.split(", ").map { it.toString() }
            temp.forEach {
                cloud_group.add(it)
            }
        }
        loc_group.forEach { loc ->
            var notExit = 0
            cloud_group.forEach { cloud ->
                if (loc != cloud) notExit = 1
            }
            if (notExit == 1) {
                Cloud_notExist.add(loc)
                cloud_group.add(loc)
            }
        }

        cloud_group.forEach { cloud ->
            var notExit = 0
            loc_group.forEach { loc ->
                if (loc != cloud) notExit = 1
            }
            if (notExit == 1) {
                Local_notExist.add(cloud)
                loc_group.add(cloud)
            }
        }


        Cloud_notExist.forEach{
            if (loc_group.count() > 0) {
                //val useremail:String? = set_frag!!.prefs!!.getString(set_frag!!.user_email_pref, " ")
                localTripPlan.grouplist = loc_group.toString()
                tripPlanDB!!.update(localTripPlan)
                inviteUser_onCloud(
                    this@TripPlanning_Fragment.context!!,
                    useremail!!,
                    localTripPlan,
                    it
                )
            }
        }

    }

    fun sync_Undef_Content_local_n_cloud(planname: String, localTripPlan: TripListModel, cloudTripPlan: TripListModel) {
        val undefItem = "UNDEF"
        val cloud_notExist_arr : ArrayList<String> = arrayListOf()
        val local_notExist_arr : ArrayList<String> = arrayListOf()
        var localContentJson : JSONObject? = null
        var cloudContentJson : JSONObject? = null
        if (localTripPlan.cloudContent.isEmpty()) localContentJson = JSONObject()
        else localContentJson = JSONObject(localTripPlan.cloudContent)
        if (cloudTripPlan.cloudContent.isEmpty()) cloudContentJson = JSONObject()
        else cloudContentJson = JSONObject(cloudTripPlan.cloudContent)
        if (debugmode) {
            Log.i("TripPlanning_Fragment", "sync_Undef_Content_local_n_cloud - local cloudContent- ${localTripPlan.cloudContent}")
            Log.i("TripPlanning_Fragment", "sync_Undef_Content_local_n_cloud - cloud cloudContent- ${cloudTripPlan.cloudContent}")
        }
        val cloudIDarr : ArrayList<String> = arrayListOf()
        var cloudArr : JSONArray? = null
        var localArr : JSONArray? = null
        if (!localContentJson.isNull(undefItem)) {
            localArr = localContentJson.get(undefItem) as JSONArray
        } else localArr = JSONArray()

        if (!cloudContentJson.isNull(undefItem)) {
            cloudArr = cloudContentJson.get(undefItem) as JSONArray
            for (i in 0..cloudArr.length()-1) {
                cloudIDarr.add(cloudArr.get(i).toString())
            }
        } else cloudArr = JSONArray()

        val loc_ppList = getLocalContentArray(planname, undefItem)

        if (cloudArr.length() == 0) {
            loc_ppList!!.forEach {
                cloud_notExist_arr.add(it.cloudID.toString())
                local_convTO_CloudContentArray(planname, undefItem, cloud_notExist_arr)
            }
        } else {
            loc_ppList!!.forEach {loc_ppMod->
                var same = 0
                cloudIDarr.forEach { cloudID ->
                    if (cloudID == loc_ppMod.cloudID.toString()) same = 1
                }
                if (same == 0) {
                    cloud_notExist_arr.add(loc_ppMod.cloudID.toString())
                }
            }
            local_convTO_CloudContentArray(planname, undefItem, cloud_notExist_arr)

            cloudIDarr.forEach {cloudID ->
                var same = 0
                loc_ppList.forEach { loc_ppMod->
                    if (cloudID == loc_ppMod.cloudID.toString()) same = 1
                }
                if (same == 0) {
                    local_notExist_arr.add(cloudID)
                }
            }
            cloud_convTO_LocalContentArray(planname, undefItem, local_notExist_arr)
        }
    }

    fun syncContent_local_n_cloud(planname: String, localTripPlan: TripListModel, cloudTripPlan: TripListModel) {
        var cloudLastIndex = 0
        var localLastIndex = 0
        var dayStr = "DAY "
        var localContentJson : JSONObject? = null
        var cloudContentJson : JSONObject? = null
        if (localTripPlan.cloudContent.isEmpty()) localContentJson = JSONObject()
        else localContentJson = JSONObject(localTripPlan.cloudContent)
        if (cloudTripPlan.cloudContent.isEmpty()) cloudContentJson = JSONObject()
        else cloudContentJson = JSONObject(cloudTripPlan.cloudContent)
        if (debugmode) {
            Log.i("TripPlanning_Fragment", "sync_local_n_cloud - local cloudContent- ${localTripPlan.cloudContent}")
            Log.i("TripPlanning_Fragment", "sync_local_n_cloud - cloud cloudContent- ${cloudTripPlan.cloudContent}")
        }
        var cloudNullcnt = 0
        var localNullcnt = 0
        for ( i in 1.. PlanningHelper().daysLimit) {
            val dayItem = dayStr+i;
            if(!localContentJson.isNull(dayItem)) {
                localLastIndex = i
                localNullcnt = 0
            } else {
                if (localNullcnt > 5) break
                localNullcnt ++
            }
            if(!cloudContentJson.isNull(dayItem)) {
                cloudLastIndex = i
                cloudNullcnt = 0
            } else {
                if (cloudNullcnt > 5) break
                cloudNullcnt ++
            }
        }
        if (debugmode) Log.i("TripPlanning_Fragment", "sync_local_n_cloud - cloud last index = $cloudLastIndex, local last index = $localLastIndex")
        for ( i in 1.. localLastIndex) {
            val dayItem = dayStr+i;
            if( !localContentJson.isNull(dayItem) && cloudContentJson.isNull(dayItem) ){
                val loc_ppID_arr : ArrayList<String>? = arrayListOf()
                val loc_arr = getLocalContentArray(planname, dayItem)
                loc_arr?.forEach{
                    loc_ppID_arr!!.add(it.cloudID.toString())
                }
                if (debugmode) Log.i("TripPlanning_Fragment", "sync_local_n_cloud - call local_convTO_CloudContentArray : $loc_ppID_arr / getLocalContentArray return $loc_arr")
                local_convTO_CloudContentArray(planname, dayItem, loc_ppID_arr!!)
            }
            if (localLastIndex > cloudLastIndex) cloudLastIndex = localLastIndex
        }
        for ( i in 1.. cloudLastIndex) {
            val dayItem = dayStr+i;
            if( !cloudContentJson!!.isNull(dayItem) && localContentJson!!.isNull(dayItem) ){
                val cloud_arr = getCloudContentArray(planname, dayItem)
                if (debugmode) Log.i("TripPlanning_Fragment", "sync_local_n_cloud - call cloud_convTO_LocalContentArray : $cloud_arr / getCloudContentArray return $cloud_arr")
                cloud_convTO_LocalContentArray(planname, dayItem, cloud_arr!!)
            }
            if (localLastIndex < cloudLastIndex) localLastIndex = cloudLastIndex
        }

        // same size in cloud and local, so merge two list
        if (localLastIndex == cloudLastIndex) {
            for (i in 1..localLastIndex) {
                val cloud_notExist_arr : ArrayList<String> = arrayListOf()
                val local_notExist_arr : ArrayList<String> = arrayListOf()
                val dayItem = dayStr+i;
                // get the same day list
                val loc_arr = getLocalContentArray(planname, dayItem)
                val cloud_arr = getCloudContentArray(planname, dayItem)
                loc_arr?.forEach{ loc->
                    var cloud_notExist = 0
                    cloud_arr?.forEach { cloud->
                        if (loc.cloudID.toString() != cloud) {
                            cloud_notExist = 1
                        }
                    }
                    if (cloud_notExist == 1) cloud_notExist_arr.add(loc.cloudID.toString())
                }
                cloud_arr?.forEach { cloud ->
                    var loc_notExist = 0
                    loc_arr?.forEach { loc ->
                        if (loc.cloudID.toString() != cloud) {
                            loc_notExist = 1
                        }
                    }
                    if (loc_notExist == 1) local_notExist_arr.add(cloud)
                }
                Log.i("TripPlanning_Fragment", "sync_local_n_cloud - local_notExist_arr: $local_notExist_arr")
                cloud_convTO_LocalContentArray(planname, dayItem, local_notExist_arr)
                Log.i("TripPlanning_Fragment", "sync_local_n_cloud - cloud_notExist_arr: $cloud_notExist_arr")
                local_convTO_CloudContentArray(planname, dayItem, cloud_notExist_arr)
            }
        }
        if (debugmode) {
            val curr = tripPlanDB!!.findName(planname)
            Log.i("TripPlanning_Fragment", "sync_local_n_cloud - cloudContent: ${curr!!.cloudContent} ,, contentJsonFormat: ${curr!!.contentJsonFormat}")
        }
    }

    fun cloud_switchTripPlan(context: Context, useremail:String, tripplan: TripListModel) {
        if (useremail.isEmpty() || !useremail.contains("@")) {
            Toast.makeText(this@TripPlanning_Fragment.context, R.string.un_login_tripplan_cloudservice_zh, Toast.LENGTH_LONG).show()
            return
        }
        if (tripPlanDB == null) tripPlanDB = TripPlanDAO(context)
        val planname = tripplan.planname
        val planID = tripplan.tripplanID
        var cloud_cmd = ""
        //"email" : email, "PlanID" : id, "Planname" : planname
        if (tripplan.updated == 0)
            cloud_cmd = "CMD=//TODO_CLOSE&email=$useremail&Planname=${URLEncoder.encode(planname, "UTF-8")}&PlanID=$planID"
        else
            cloud_cmd = "CMD=//TODO_OPEN&email=$useremail&Planname=${URLEncoder.encode(planname, "UTF-8")}&PlanID=$planID"

        cloudHdler = Cloud_Helper(context) {
            if (it == null) {
                Toast.makeText(context, "網路不穩定，請稍後再試", Toast.LENGTH_LONG).show()
                return@Cloud_Helper
            }
            if (debugmode)
                Log.i("TripPlanning_Fragment", " cloud_checkTripPlan - Cloud Update response : $it")
            val cloud_db = JSONObject(it)
            val status = cloud_db.get("status") as Boolean
            if (status) {
                //Toast.makeText(context, R.string.tripPlan_delCloudOK_zh, Toast.LENGTH_LONG).show()
            } else {
                val failMsg = cloud_db.get("message") as String
                Toast.makeText(context, failMsg, Toast.LENGTH_LONG).show()
                if (tripplan.updated == 0) tripplan.updated = 1
                else tripplan.updated = 0
                tripPlanDB!!.update(tripplan)
                if (tripplanListRC != null) tripplanListRC!!.notifyDataSetChanged()
            }
        }
        cloudHdler!!.execute("POST", TripPlanCloudHelperURL, cloud_cmd.toString())
    }

    fun cloud_checkTripPlan(context: Context, useremail:String, tripplan: TripListModel) {
        if (useremail.isEmpty() || !useremail.contains("@")) {
            Toast.makeText(context, R.string.un_login_tripplan_cloudservice_zh, Toast.LENGTH_LONG).show()
            return
        }
        if (tripPlanDB == null) tripPlanDB = TripPlanDAO(context)
        val planname = tripplan.planname
        val planID = tripplan.tripplanID
        //"email" : email, "PlanID" : id, "Planname" : planname
        val cloud_cmd = "CMD=//TODO_CHECK&email=$useremail&Planname=${URLEncoder.encode(planname, "UTF-8")}&PlanID=$planID"

        cloudHdler = Cloud_Helper(context) {
            if (it == null) {
                Toast.makeText(context, "網路不穩定，請稍後再試", Toast.LENGTH_LONG).show()
                return@Cloud_Helper
            }
            if (debugmode)
                Log.i("TripPlanning_Fragment", " cloud_checkTripPlan - Cloud Update response : $it")
            val cloud_db = JSONObject(it)
            val status = cloud_db.get("status") as Boolean
            if (status) {
                tripplan.updated = cloud_db.get("tripplan_status") as Int
                tripPlanDB!!.update(tripplan)
                //Toast.makeText(context, R.string.tripPlan_delCloudOK_zh, Toast.LENGTH_LONG).show()
                recyclerViewLoad(1)
            } else {
                val failMsg = cloud_db.get("message") as String
                Toast.makeText(context, failMsg, Toast.LENGTH_LONG).show()
            }
        }
        cloudHdler!!.execute("POST", TripPlanCloudHelperURL, cloud_cmd.toString())
    }

    fun cloud_deleteTripPlan(planname: String, useremail: String,planID : String) {
        //if(set_frag == null) {
        //    Toast.makeText(this@TripPlanning_Fragment.context, R.string.un_login_tripplan_zh, Toast.LENGTH_LONG).show()
        //    return
        //}
        //val useremail:String? = set_frag!!.prefs!!.getString(set_frag!!.user_email_pref, " ")
        if (useremail.isEmpty() || !useremail.contains("@")) {
            Toast.makeText(this@TripPlanning_Fragment.context, R.string.un_login_tripplan_cloudservice_zh, Toast.LENGTH_LONG).show()
            return
        }
        val cloud_cmd = "CMD=DELETE_//TODO&email=$useremail&Planname=${URLEncoder.encode(planname, "UTF-8")}&PlanID=$planID"

        cloudHdler = Cloud_Helper(this@TripPlanning_Fragment.context!!) {
            if (it == null) {
                Toast.makeText(this@TripPlanning_Fragment.context, "網路不穩定，請稍後再試", Toast.LENGTH_LONG).show()
                return@Cloud_Helper
            }
            if (debugmode)
                Log.i("TripPlanning_Fragment", " cloud_deleteTripPlan - Cloud Update response : $it")
            val cloud_db = JSONObject(it)
            val status = cloud_db.get("status") as Boolean
            if (status) {
                Toast.makeText(this@TripPlanning_Fragment.context, R.string.tripPlan_delCloudOK_zh, Toast.LENGTH_LONG).show()
            } else {
                val failMsg = cloud_db.get("message") as String
                Toast.makeText(this@TripPlanning_Fragment.context, failMsg, Toast.LENGTH_LONG).show()
            }
        }
        cloudHdler!!.execute("POST", TripPlanCloudHelperURL, cloud_cmd.toString())
    }

    fun cloud_insertTripPlan(planname : String, useremail: String,tripPlan: TripListModel) {
        val grouplist = tripPlan.grouplist//""
        val ContentJsonFormat = tripPlan.contentJsonFormat//""
        //if(set_frag == null) {
        //    Toast.makeText(this@TripPlanning_Fragment.context, R.string.un_login_tripplan_zh, Toast.LENGTH_LONG).show()
        //    return
        //}
        //val useremail:String? = set_frag!!.prefs!!.getString(set_frag!!.user_email_pref, " ")
        if (useremail.isEmpty() || !useremail.contains("@")) {
            Toast.makeText(this@TripPlanning_Fragment.context, R.string.un_login_tripplan_cloudservice_zh, Toast.LENGTH_LONG).show()
            return
        }
        val cloud_cmd = "CMD=//TODO&email=$useremail&Planname=${URLEncoder.encode(planname, "UTF-8")}&Author=$useremail&Grouplist=$grouplist&ContentJsonFormat=$ContentJsonFormat"

        cloudHdler = Cloud_Helper(this@TripPlanning_Fragment.context!!) {
            if (it == null) {
                Toast.makeText(this@TripPlanning_Fragment.context, "網路不穩定，請稍後再試", Toast.LENGTH_LONG).show()
                return@Cloud_Helper
            }
            if (debugmode)
                Log.i("TripPlanning_Fragment", " cloud_insertTripPlan - Cloud Update response : $it")
            val cloud_db = JSONObject(it)
            val status = cloud_db.get("status") as Boolean
            if (status) {
                val cloudID = cloud_db.get("TripPlanID") as Int
                tripPlan.tripplanID = cloudID.toString()
                val result = tripPlanDB!!.update(tripPlan)
                if ( result )
                {
                    if (debugmode) Log.i("TripPlanning_Fragment", " error - save id failure")
                } else {
                    Toast.makeText(
                        this@TripPlanning_Fragment.context,
                        R.string.tripPlan_cloudInsertOK_zh,
                        Toast.LENGTH_LONG
                    ).show()
                    recyclerViewLoad(1)
                }
            } else {
                val failMsg = cloud_db.get("message") as String
                if (failMsg == "Unknow User")
                    Toast.makeText(this@TripPlanning_Fragment.context, R.string.un_login_tripplan_zh, Toast.LENGTH_LONG).show()
            }
            //val modelv = tripPlanDB!!.insert(model)
            //tripPlanDB!!.update()
        }
        cloudHdler!!.execute("POST", TripPlanCloudHelperURL, cloud_cmd.toString())
    }

    fun download_MultiPP(context: Context, tripplan: TripListModel, item: String, multiPPid: ArrayList<String>) {
        //py = {'CMD' : 'DOWNLOAD_MULTI_PP', 'id_array' : id_arr_str}
        //var cloudresp_MultiPP : MutableList<PPModel> = mutableListOf()
        val tripplandbPpmodeldao = TripPlanDB_PPModelDAO(context)
        //val tripPlanList = tripPlanDB!!.findName(planname)
        if (debugmode) Log.i("TripPlanning_Fragment", " id list - $multiPPid")
        var cloud_cmd = "CMD=//TODO&id_array="
        for ( i in 0.. multiPPid.count() - 1) {
            cloud_cmd += multiPPid[i]
            if (i < multiPPid.count() - 1) cloud_cmd += ","
        }
        if (debugmode) Log.i("TripPlanning_Fragment", "cloud command : $cloud_cmd")

        cloudHdler = Cloud_Helper(context) {
            if (it == null) {
                Toast.makeText(context, "網路不穩定，請稍後再試", Toast.LENGTH_LONG).show()
                return@Cloud_Helper
            }
            if (debugmode)
                Log.i("TripPlanning_Fragment", "download_MultiPP - Cloud response : $it")
            val cloud_db = JSONObject(it)
            if (!cloud_db.isNull("count")) {
                val cloud_count = cloud_db.get("count") as Int
                for (i in 1..cloud_count) {
                    val tempDB = cloud_db.get(i.toString()) as JSONObject
                    val cloudID = tempDB.get("cloudID").toString()
                    //val cloudresp_MultiPP : TripPlan_PPModel? = mutableListOf()
                    val tripPlan_ppmodel = TripPlan_PPModel(
                        0, cloudID.toLong(),
                        tempDB.get("name").toString(), tempDB.get("phone").toString(),
                        tempDB.get("country").toString(), tempDB.get("address").toString(),
                        tempDB.get("fb").toString(), tempDB.get("web").toString(),
                        tempDB.get("bloggerintro").toString(), tempDB.get("opentime").toString(),
                        tempDB.get("tag_note").toString(), tempDB.get("description").toString(),
                        tempDB.get("pic_url").toString(),
                        tempDB.get("score") as Int, tempDB.get("status") as Int, 0f, false,
                        tripplan.tripplanID.toLong(),
                        item, 0
                    )
                    tripplandbPpmodeldao.insert(tripPlan_ppmodel)
                    if (debugmode)
                        Log.i("TripPlanning_Fragment", "download_MultiPP - insert to LocalDB: index: $tripPlan_ppmodel")
                    /*
                     val tripPlan_PPModel = TripPlan_PPModel (
            add_pp.id,
            add_pp.cloudID,
            add_pp.name,
            add_pp.phone,
            add_pp.country,
            add_pp.addr,
            add_pp.fb,
            add_pp.web,
            add_pp.blogInfo,
            add_pp.opentime,
            add_pp.tag_note,
            add_pp.descrip,
            add_pp.pic_url,
            add_pp.score,
            add_pp.status,
            add_pp.distance,
            insertID,
            item,
            0
                     */
                    //cloudresp_MultiPP.add(ppmodel)
                }
                //PlanningHelper().add_pp_intoKEYDAY(
                //    context,
                //    useremail!!,
                //    tripPlanList!!.id,
                //    item,
                //    cloudresp_MultiPP,
                //    0
                //)
                recyclerViewLoad(1)

            } else {
                //Toast.makeText(context, "【無】線上資料", Toast.LENGTH_LONG)
                //    .show()
            }
            cloudPPReady = 1
        }
        cloudHdler!!.execute("POST", CloudHelperURL, cloud_cmd.toString())
    }

    fun addPP_toCloud(context: Context, useremail: String, tripplan: TripListModel, item: String, pp_id: String) {
        //py = {'CMD' : 'TRIPPLAN_ADD_PP' ,'email' : 'qfeel0215@gmail.com', 'Planname' : 'test_tripplan', 'PlanID' : '', 'saveitem' : 'DAY1', 'pp_id' : '5' }
        val planname = tripplan.planname
        val planID = tripplan.tripplanID
        if (tripPlanDB == null) tripPlanDB = TripPlanDAO(context)
        if (useremail.isEmpty() || !useremail.contains("@")) {
            Toast.makeText(context, R.string.un_login_tripplan_cloudservice_zh, Toast.LENGTH_LONG).show()
            return
        }

        val cloud_cmd = "CMD=//TODO&email=$useremail&Planname=${URLEncoder.encode(planname, "UTF-8")}&PlanID=$planID&saveitem=$item&pp_id=$pp_id"
        if (debugmode)
            Log.i("TripPlanning_Fragment", " addPP_toCloud - Cloud Commend : $cloud_cmd")
        cloudHdler = Cloud_Helper(context) {
            if (it == null) {
                Toast.makeText(context, "網路不穩定，請稍後再試", Toast.LENGTH_LONG).show()
                return@Cloud_Helper
            }

            if (debugmode)
                Log.i("TripPlanning_Fragment", " addPP_toCloud - Cloud Update response : $it")
            val cloud_db = JSONObject(it)
            if (!cloud_db.isNull("status")) {
                val status = cloud_db.get("status") as Boolean
                if (status) {
                    if (!cloud_db.isNull("content")) {
                        val content = cloud_db.get("content") as String
                        tripplan.cloudContent = content
                        tripPlanDB!!.update(tripplan)
                    }
                }
            }
        }
        cloudHdler!!.execute("POST", TripPlanCloudHelperURL, cloud_cmd.toString())
    }

    fun delPP_toCloud(context: Context, useremail:String, tripplan: TripListModel, item: String, pp_id: String) {
        //py = {'CMD' : 'TRIPPLAN_DEL_PP' ,'email' : 'qfeel0215@gmail.com', 'Planname' : 'test_tripplan', 'PlanID' : '', 'delitem' : 'DAY1', 'pp_id' : '2' }
        val planname = tripplan.planname
        val planID = tripplan.tripplanID
        if (tripPlanDB == null) tripPlanDB = TripPlanDAO(context)

        if (useremail.isEmpty() || !useremail.contains("@")) {
            Toast.makeText(context, R.string.un_login_tripplan_cloudservice_zh, Toast.LENGTH_LONG).show()
            return
        }
        val cloud_cmd = "CMD=//TODO&email=$useremail&Planname=${URLEncoder.encode(planname, "UTF-8")}&PlanID=$planID&delitem=$item&pp_id=$pp_id"

        cloudHdler = Cloud_Helper(context) {
            if (it == null) {
                Toast.makeText(context, "網路不穩定，請稍後再試", Toast.LENGTH_LONG).show()
                return@Cloud_Helper
            }

            if (debugmode)
                Log.i("TripPlanning_Fragment", " delPP_toCloud - Cloud Update response : $it")
            val cloud_db = JSONObject(it)
            if (!cloud_db.isNull("status")) {
                val status = cloud_db.get("status") as Boolean
                if (status) {
                    if (!cloud_db.isNull("content")) {
                        val content = cloud_db.get("content") as String
                        tripplan.cloudContent = content
                        tripPlanDB!!.update(tripplan)
                    }
                }
            }
        }
        cloudHdler!!.execute("POST", TripPlanCloudHelperURL, cloud_cmd.toString())
    }

    fun movePP_onCloud(context: Context, useremail:String, tripplan: TripListModel, from: String, to: String, pp_id: String) {
        //py = {'CMD' : 'TRIPPLAN_MOVE_PP' ,'email' : 'qfeel0215@gmail.com', 'Planname' : 'test_tripplan', 'PlanID' : '', 'fromitem' : 'UNDEF', 'toitem' : 'DAY1', 'pp_id' : '1005' }
        val planname = tripplan.planname//planname
        val planID = tripplan.tripplanID
        if (tripPlanDB == null) tripPlanDB = TripPlanDAO(context)
        if (useremail.isEmpty() || !useremail.contains("@")) {
            Toast.makeText(context, R.string.un_login_tripplan_cloudservice_zh, Toast.LENGTH_LONG).show()
            return
        }
        val cloud_cmd = "CMD=//TODO&email=$useremail&Planname=${URLEncoder.encode(planname, "UTF-8")}&PlanID=$planID&fromitem=$from&toitem=$to&pp_id=$pp_id"
        if (debugmode)
            Log.i("TripPlanning_Fragment", " movePP_onCloud - Cloud Send commend : $cloud_cmd")
        cloudHdler = Cloud_Helper(context) {
            if (it == null) {
                Toast.makeText(context, "網路不穩定，請稍後再試", Toast.LENGTH_LONG).show()
                return@Cloud_Helper
            }

            if (debugmode)
                Log.i("TripPlanning_Fragment", " movePP_onCloud - Cloud Update response : $it")
            val cloud_db = JSONObject(it)
            if (!cloud_db.isNull("status")) {
                val status = cloud_db.get("status") as Boolean
                if (status) {
                    if (!cloud_db.isNull("content")) {
                        val content = cloud_db.get("content") as String
                        tripplan.cloudContent = content
                        tripPlanDB!!.update(tripplan)
                    }
                }
            }

        }
        cloudHdler!!.execute("POST", TripPlanCloudHelperURL, cloud_cmd.toString())
    }

    fun addDay_onCloud(context: Context, useremail: String, tripplan: TripListModel, dayAdd: String) {
        //py = {'CMD' : 'TRIPPLAN_ADD_DAY' ,'email' : 'qfeel0215@gmail.com', 'Planname' : 'test_tripplan', 'PlanID' : '', 'DayAdd' : 'DAY2'}
        val planname = tripplan.planname//planname
        val planID = tripplan.tripplanID
        //if(set_frag == null) {
        //    Toast.makeText(context, R.string.un_login_tripplan_zh, Toast.LENGTH_LONG).show()
        //    return
        //}
        //val useremail:String? = set_frag!!.prefs!!.getString(set_frag!!.user_email_pref, " ")
        if (useremail.isEmpty() || !useremail.contains("@")) {
            Toast.makeText(context, R.string.un_login_tripplan_cloudservice_zh, Toast.LENGTH_LONG).show()
            return
        }
        val cloud_cmd = "CMD=//TODO&email=$useremail&Planname=${URLEncoder.encode(planname, "UTF-8")}&PlanID=$planID&DayAdd=$dayAdd"

        cloudHdler = Cloud_Helper(context) {
            if (it == null) {
                Toast.makeText(context, "網路不穩定，請稍後再試", Toast.LENGTH_LONG).show()
                return@Cloud_Helper
            }

            if (debugmode)
                Log.i("TripPlanning_Fragment", " addDay_onCloud - Cloud Update response : $it")
        }
        cloudHdler!!.execute("POST", TripPlanCloudHelperURL, cloud_cmd.toString())
    }

    fun delDay_onCloud(context: Context, useremail: String, tripplan: TripListModel, dayDel: String) {
        //py = {'CMD' : 'TRIPPLAN_DEL_DAY' ,'email' : 'qfeel0215@gmail.com', 'Planname' : 'test_tripplan', 'PlanID' : '', 'DayDel' : 'DAY2'}
        val planname = tripplan.planname//planname
        val planID = tripplan.tripplanID

        if (useremail.isEmpty() || !useremail.contains("@")) {
            Toast.makeText(context, R.string.un_login_tripplan_cloudservice_zh, Toast.LENGTH_LONG).show()
            return
        }
        val cloud_cmd = "CMD=//TODO&email=$useremail&Planname=${URLEncoder.encode(planname, "UTF-8")}&PlanID=$planID&DayDel=$dayDel"

        cloudHdler = Cloud_Helper(context) {
            if (it == null) {
                Toast.makeText(context, "網路不穩定，請稍後再試", Toast.LENGTH_LONG).show()
                return@Cloud_Helper
            }

            if (debugmode)
                Log.i("TripPlanning_Fragment", " delDay_onCloud - Cloud Update response : $it")
        }
        cloudHdler!!.execute("POST", TripPlanCloudHelperURL, cloud_cmd.toString())
    }

    fun deleteUser_onCloud(context: Context, authoremail: String, tripplan: TripListModel, useremail: String) {
        //#py = {'CMD' : 'REMOVE_USER_TRIPPLAN' ,'authoremail' : 'brianshih112@gmail.com', 'useremail' : 'tyng0317@gmail.com', 'Planname' : 'test_tripplan', 'PlanID' : ''}
        val planname = tripplan.planname//planname
        val planID = tripplan.tripplanID
        //if(set_frag == null) {
        //    Toast.makeText(context, R.string.un_login_tripplan_zh, Toast.LENGTH_LONG).show()
        //    return
        //}
        //val authoremail:String? = set_frag!!.prefs!!.getString(set_frag!!.user_email_pref, " ")
        if (authoremail.isEmpty()) {
            Toast.makeText(context, R.string.un_login_tripplan_cloudservice_zh, Toast.LENGTH_LONG).show()
            return
        }
        val cloud_cmd = "CMD=//TODO&authoremail=$authoremail&useremail=$useremail&Planname=${URLEncoder.encode(planname, "UTF-8")}&PlanID=$planID"

        cloudHdler = Cloud_Helper(context) {
            if (it == null) {
                Toast.makeText(context, "網路不穩定，請稍後再試", Toast.LENGTH_LONG).show()
                return@Cloud_Helper
            }

            if (debugmode)
                Log.i("TripPlanning_Fragment", " delDay_onCloud - Cloud Update response : $it")
            if (authoremail.equals(useremail)) {
                if (tripPlanDB == null) tripPlanDB = TripPlanDAO(context)
                tripPlanDB!!.delete(tripplan.id)
            }
        }
        cloudHdler!!.execute("POST", TripPlanCloudHelperURL, cloud_cmd.toString())
    }

    fun inviteUser_onCloud(context: Context, authoremail: String, tripplan: TripListModel, useremail: String) {
        //#py = {'CMD' : 'INVITE_USER_TRIPPLAN' ,'authoremail' : 'brianshih112@gmail.com', 'useremail' : 'test001@gmail.com', 'Planname' : 'test_tripplan', 'PlanID' : ''}
        val planname = tripplan.planname//planname
        val planID = tripplan.tripplanID
        //if(set_frag == null) {
        //    Toast.makeText(context, R.string.un_login_tripplan_zh, Toast.LENGTH_LONG).show()
        //    return
        //}

        //val authoremail:String? = set_frag!!.prefs!!.getString(set_frag!!.user_email_pref, " ")
        if (authoremail.isEmpty()) {
            Toast.makeText(context, R.string.un_login_tripplan_cloudservice_zh, Toast.LENGTH_LONG).show()
            return
        }
        val cloud_cmd = "CMD=//TODO&authoremail=$authoremail&useremail=$useremail&Planname=${URLEncoder.encode(planname, "UTF-8")}&PlanID=$planID"

        cloudHdler = Cloud_Helper(context) {
            if (it == null) {
                Toast.makeText(context, "網路不穩定，請稍後再試", Toast.LENGTH_LONG).show()
                return@Cloud_Helper
            }

            if (debugmode)
                Log.i("TripPlanning_Fragment", " delDay_onCloud - Cloud Update response : $it")
        }
        cloudHdler!!.execute("POST", TripPlanCloudHelperURL, cloud_cmd.toString())
    }
}