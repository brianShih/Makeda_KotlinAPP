package tw.breadcrumbs.makeda.LocRecently

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.locate_recently.*
import org.json.JSONObject
import tw.breadcrumbs.makeda.*
import tw.breadcrumbs.makeda.CloudObj.Cloud_Helper
import tw.breadcrumbs.makeda.dataModel.PPModel
import tw.breadcrumbs.makeda.dataModel.TripPlan_PPModel
import tw.breadcrumbs.makeda.dateModel.LRModel

class LocateRecently_Fragment : androidx.fragment.app.Fragment() {
    private val debugmode = false
    val UserTraceURL = "xxxx"
    private val CloudHelperURL = "xxxx"
    var useremail : String? = ""
    var lr_list_rc: LocateRecentlyListRC? = null
    private var recentlyList: MutableList<LRModel>? = mutableListOf()
    var cloudIDarr : ArrayList<String> = arrayListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.locate_recently, container, false)
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as MainActivity).setNavigationID(3)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        locaterecentlyRC.visibility = View.GONE
        locaterecently_rc_loading.visibility = View.VISIBLE

        backBtn_setup()
        recentlyDownload()
        //recycleViewInit(context!!, locaterecentlyRC, recentlyList!!)
    }

    fun backBtn_setup() {
        backButton.setOnClickListener{
            (activity as MainActivity).onBackPressed()
        }
    }

    fun recentlyDownload() {
        if (useremail.equals("not fill") || useremail.isNullOrEmpty() || !useremail!!.contains("@")) {
            val prefs = context!!.getSharedPreferences("MAKEDA_USER_SET",
                Context.MODE_PRIVATE
            )
            useremail = prefs!!.getString(Setting_Fragment().user_email_pref, " ")
        }
        val cloud_cmd = "CMD=//TODO&email=$useremail"
        if (debugmode)
            Log.i("LocateRecently_Fragment", " cloud_cmd : $cloud_cmd")
        recentlyList = mutableListOf()
        Cloud_Helper(context!!) {
            if (it == null) {
                Toast.makeText(context, R.string.network_not_stable_zh, Toast.LENGTH_LONG).show()
                return@Cloud_Helper
            }
            if (debugmode)
                Log.i("LocateRecently_Fragment", " response : $it")

            val cloud_db = JSONObject(it)
            if (!cloud_db.isNull("status")) {
                val status = cloud_db.get("status") as Boolean
                if (status && !cloud_db.isNull("loc_record")) {
                    cloudIDarr = arrayListOf()
                    val loc_record = cloud_db.get("loc_record") as String
                    val records = JSONObject(loc_record)
                    if (debugmode) Log.i("LocateRecently_Fragment", " records : $records")

                    val keysStr = records.keys();
                    while (keysStr.hasNext()) {
                        val key: String = keysStr.next()
                        if (debugmode) Log.i("LocateRecently_Fragment", " key : $key")
                        val lrModel = LRModel(
                            pp = TripPlan_PPModel(0, key.toLong(), "", "",""
                                ,"", "", "", "", "","", ""
                                , "", 0, 0, 0f, false, 0
                                , "", 0)
                            , date = records.get(key.toString()) as String
                        )
                        recentlyList!!.add(lrModel)
                        cloudIDarr.add(key)
                    }
                }

                // finally
                if (context != null)
                    download_MultiPP(context!!, cloudIDarr)
            }

        }.execute("POST", UserTraceURL, cloud_cmd.toString())
    }

    fun download_MultiPP(context: Context, multiPPid: ArrayList<String>) {

        if (debugmode) Log.i("TripPlanning_Fragment", " id list - $multiPPid")
        var cloud_cmd = "CMD=//TODO&id_array="
        for ( i in 0.. multiPPid.count() - 1) {
            cloud_cmd += multiPPid[i]
            if (i < multiPPid.count() - 1) cloud_cmd += ","
        }
        if (debugmode) Log.i("TripPlanning_Fragment", "cloud command : $cloud_cmd")

        Cloud_Helper(context) {
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
                        0,
                        "",
                        0
                    )
                    recentlyList!!.forEach {
                        if (it.pp.cloudID.equals(cloudID.toLong())) {
                            it.pp = tripPlan_ppmodel
                        }
                    }
                    if (debugmode)
                        Log.i("TripPlanning_Fragment", "download_MultiPP - insert to LocalDB: index: $tripPlan_ppmodel")
                }
                if (locaterecentlyRC != null)
                    recycleViewInit(context, locaterecentlyRC, recentlyList!!)
            } else {
                //Toast.makeText(context, "【無】線上資料", Toast.LENGTH_LONG)
                //    .show()
            }
        }.execute("POST", CloudHelperURL, cloud_cmd.toString())
    }

    fun recycleViewInit(context: Context, recycleView : androidx.recyclerview.widget.RecyclerView, list : MutableList<LRModel>) {
        if (useremail.equals("not fill") || useremail.isNullOrEmpty() || !useremail!!.contains("@")) {
            val prefs = context.getSharedPreferences("MAKEDA_USER_SET",
                Context.MODE_PRIVATE
            )
            useremail = prefs!!.getString(Setting_Fragment().user_email_pref, " ")
        }
        if (debugmode) Log.i("LocateRecently_Fragment", "recycleViewInit useremail = $useremail")
        recycleView.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(context)
        lr_list_rc = LocateRecentlyListRC(
            list,
            { partItem: TripPlan_PPModel -> locateRecRC_onClick(partItem) })
        recycleView.adapter = lr_list_rc
        locaterecentlyRC.visibility = View.VISIBLE
        locaterecently_rc_loading.visibility = View.GONE
    }

    fun locateRecRC_onClick(setItem :TripPlan_PPModel) {
        val review_f = Review_Fragment()
        var ppModel : PPModel? = null
        setItem.let { ppModel =  PPModel(
            setItem.id,
            setItem.cloudID,
            setItem.name,
            setItem.phone,
            setItem.country,
            setItem.addr,
            setItem.fb,
            setItem.web,
            setItem.blogInfo,
            setItem.opentime,
            setItem.tag_note,
            setItem.descrip,
            setItem.pic_url,
            setItem.score,
            setItem.status,
            setItem.distance,
            setItem.calDistanceDone
        )
        }
        review_f.pp_set(ppModel!!)
        (activity as MainActivity).switchFragment(this@LocateRecently_Fragment, review_f, 3)
    }
}