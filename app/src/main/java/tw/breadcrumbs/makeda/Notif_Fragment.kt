package tw.breadcrumbs.makeda

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.notif_fragment.*
import org.json.JSONObject
import org.w3c.dom.Text
import java.io.IOException
import android.R.attr.bitmap
import android.content.Context
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import tw.breadcrumbs.makeda.CloudObj.Cloud_Helper
import tw.breadcrumbs.makeda.DBModules.ItemDAO
import tw.breadcrumbs.makeda.LocRecently.LocateRecently_Fragment
import tw.breadcrumbs.makeda.dataModel.PPModel
import tw.breadcrumbs.makeda.dataModel.TripPlan_PPModel
import tw.breadcrumbs.makeda.dateModel.LRModel


class Notif_Fragment : androidx.fragment.app.Fragment() {
    private val debugmode = false
    val CloudHelperURL = "//TODO"
    val UserTraceURL = "//TODO"
    var country: String = " - "
    var city: String = " - "
    var town: String = " - "
    var old_locText = ""
    var useremail : String? = ""
    private var samePlaceCount = 0
    var locateText : TextView? = null
    var notif_list_rc: NotifListRC? = null
    var pps_downloading = false
    var pps_distance_caling = false
    private var last_loc: Location? = null
    private var yourHaveGoTRIPPPModel: MutableList<TripPlan_PPModel>? = mutableListOf()
    private var currTripModel:TripPlan_PPModel? = null
    var near_PPModelList: MutableList<TripPlan_PPModel>? = mutableListOf()
    private var nearListCalHelper: calcuteDistanceHelper? = null
    var locThread : Thread? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.notif_fragment, container, false)
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as MainActivity).setNavigationID(3)
        if (yourHaveGoTRIPPPModel!!.count() > 0 && notif_you_hered != null) {
            recycleViewInit(this@Notif_Fragment.context!!, notif_you_hered, yourHaveGoTRIPPPModel!!)
        }
        recentlyV_setup()
    }

    override fun onStart() {
        super.onStart()
        if (view != null)
            locateText = view!!.findViewById(R.id.locate_text) as TextView

        recentlyPP_Task()
    }

    fun recentlyPP_Task() {
        if (locThread != null) return
        locThread = Thread(Runnable {
            if (debugmode) Log.i("Notif_Fragment", " - onStart / locThread working...")
            while (true) {
                if (activity != null) {
                    //locateText!!.text = " --    |    --"
                    val location = (activity as MainActivity).getLastLocation()
                    if (location != null) {
                        if (last_loc == null) {
                            last_loc = location
                        }
                        //val toLastLocDis = last_loc!!.distanceTo(location)
                        val disValue = (last_loc!!.distanceTo(location)).toInt()
                        val toLastLocDis = disValue.toFloat() / 1000f
                        if (toLastLocDis > 0.08 && !pps_distance_caling) {
                            last_loc = location
                            locateText?.post(Runnable {
                                if (notif_you_hered != null && notif_rc_loading != null) {
                                    notif_you_hered.visibility = View.GONE
                                    notif_rc_loading.visibility = View.VISIBLE
                                }

                                yourHaveGoTRIPPPModel = mutableListOf()
                                if (debugmode) Log.i(
                                    "Notif_Fragment",
                                    "$toLastLocDis --- reset yourHaveGoTRIPPPModel "
                                )
                            })
                        }

                        val newText = updateLocationText(location)
                        if (newText != null) {
                            val old = old_locText
                            if (!old.equals(newText) || near_PPModelList!!.count() == 0) {
                                if (debugmode) Log.i(
                                    "Notif_Fragment",
                                    " download... $old / $newText"
                                )
                                old_locText = newText
                                samePlaceCount = 0
                                currTripModel = null
                                downloadCurrLocPPs()
                            }
                            if (debugmode) Log.i(
                                "Notif_Fragment",
                                "samePlaceCount = $samePlaceCount / ${near_PPModelList!!.count()} | ${yourHaveGoTRIPPPModel!!.count()} --- $pps_downloading -- $pps_distance_caling"
                            )
                            if (yourHaveGoTRIPPPModel!!.count() == 0 && !pps_downloading && !pps_distance_caling) {
                                sortStart()
                            }

                            locateText?.post(Runnable {
                                locateText!!.text = newText
                            })
                        }
                        if (yourHaveGoTRIPPPModel!!.count() > 0) {
                            yourHaveGoTRIPPPModel!!.forEach {
                                if (it.distance <= 0.03) {
                                    if (currTripModel != null) {
                                        if (currTripModel!!.cloudID.equals(it.cloudID)) {
                                            //same tripmodel
                                            samePlaceCount++ // once 2 seconds
                                            if (samePlaceCount > 60) { // 2 mins - 60
                                                if (debugmode) Log.i(
                                                    "Notif_Fragment",
                                                    "prepare -- notif_sendLocToCloud --"
                                                )
                                                samePlaceCount = 0
                                                //todo something
                                                notif_sendLocToCloud(context!!, it)
                                            }
                                        }
                                    } else {
                                        samePlaceCount = 0
                                        currTripModel = it
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (debugmode) Log.i("Notif_Fragment", " Waiting Activity Ready...")
                }
                if (pps_downloading || pps_distance_caling) {
                    Thread.sleep((5 * 1000).toLong())
                } else {
                    Thread.sleep((2 * 1000).toLong())
                }
            }
        })
        locThread!!.start()
    }

    fun recentlyV_setup() {
        notif_recently_locs.setOnClickListener {
            val locateRecent_F = LocateRecently_Fragment()
            (activity as MainActivity).switchFragment(this@Notif_Fragment, locateRecent_F, 3)
        }
    }

    fun recycleViewInit(context: Context, recycleView : androidx.recyclerview.widget.RecyclerView, list : MutableList<TripPlan_PPModel>) {
        if (useremail.equals("not fill") || useremail.isNullOrEmpty() || !useremail!!.contains("@")) {
            val prefs = context.getSharedPreferences("MAKEDA_USER_SET",
                Context.MODE_PRIVATE
            )
            useremail = prefs!!.getString(Setting_Fragment().user_email_pref, " ")
        }
        if (debugmode) Log.i("Notif_Fragment", "recycleViewInit useremail = $useremail")
        recycleView.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(context)
        notif_list_rc = NotifListRC(list, { partItem : TripPlan_PPModel -> notifRC_onClick(partItem) })
        //notif_list_rc!!.set_useremail(useremail!!)
        //notif_list_rc!!.onLineIcon = ContextCompat.getDrawable(context, R.drawable.online_icon)
        //notif_list_rc!!.newIcon = ContextCompat.getDrawable(context, R.drawable.new_icon)
        //notif_list_rc!!.localIcon = ContextCompat.getDrawable(context, R.drawable.book_icon)
        //val adapter = NotifListAdapter()
        //adapter.deleteIcon = ContextCompat.getDrawable(context, R.drawable.del_icon)
        //adapter.saveIcon = ContextCompat.getDrawable(context, R.drawable.plus_icon)
        recycleView.adapter = notif_list_rc
        //val actions = listaction

        //adapter.swipeController(actions)
        //val itemTouchhelper = ItemTouchHelper(adapter)
        //itemTouchhelper.attachToRecyclerView(recycleView)
        recycleView.visibility = View.VISIBLE
        notif_rc_loading.visibility = View.GONE
    }

    fun updateLocationText(loc: Location) : String? {
        var ret: String? = null
        try {
            val geoCoder = Geocoder(this@Notif_Fragment.context)
            val placemark = geoCoder.getFromLocation(loc.latitude, loc.longitude, 1)
            //Log.i("Notif_Fragment", "placemark:$placemark")
            if (placemark[0].subAdminArea == null) {
                city = placemark[0].adminArea.toString()
            } else if (placemark[0].adminArea == null) {
                city = placemark[0].subAdminArea.toString()
            } else if (placemark[0].adminArea == null && placemark[0].subAdminArea == null) {

            }
            if (placemark[0].locality != null) {
                town = placemark[0].locality.toString()
            }
            country = placemark[0].countryName.toString()
            ret = "$country    |    $city    |    $town"
        } catch (e: IOException) {
            if (debugmode)
                Log.i("Notif_Fragment", " error : $e")
        }
        return ret
    }

    fun sortStart() {
        //private var undefListCalHelper: calcuteDistanceHelper? = null
        //private var dayPPListCalHelper: calcuteDistanceHelper? = null

        if (pps_distance_caling == false) {
            pps_distance_caling = true
            nearListCalHelper = calcuteDistanceHelper(activity as MainActivity,
                this@Notif_Fragment.context!!) {
                if (it != null) {
                    near_PPModelList = it
                    near_PPModelList!!.sortBy { nearIt ->
                        nearIt.distance
                    }
                    it.forEach {
                        if (it.distance < 0.1) { //90mm
                            yourHaveGoTRIPPPModel!!.add(it)
                        }
                    }
                    pps_distance_caling = false
                    if (this@Notif_Fragment.context != null && notif_you_hered != null) {
                        recycleViewInit(
                            this@Notif_Fragment.context!!,
                            notif_you_hered,
                            yourHaveGoTRIPPPModel!!
                        )
                    }
                }
            }
            nearListCalHelper!!.execute(near_PPModelList)
        }

    }

    fun downloadCurrLocPPs() {
        val cloud_cmd = "CMD=//TODO&Country=$country&City=$city&Town=$town"
        if (debugmode)
            Log.i("Notif_Fragment", " cloud_cmd : $cloud_cmd")
        pps_downloading = true
        Cloud_Helper(this@Notif_Fragment.context!!) {
            if (it == null) {
                Toast.makeText(this@Notif_Fragment.context, R.string.network_not_stable_zh, Toast.LENGTH_LONG).show()
                return@Cloud_Helper
            }
            if (debugmode)
                Log.i("Notif_Fragment", " response : $it")

            val cloud_db = JSONObject(it)
            if (!cloud_db.isNull("count")) {
                val cloud_count = cloud_db.get("count") as Int
                near_PPModelList = mutableListOf()
                for (i in 1..cloud_count) {
                    val tempDB = cloud_db.get(i.toString()) as JSONObject
                    val cloudID = tempDB.get("cloudID").toString()
                    var ppmodel = TripPlan_PPModel(
                        0, cloudID.toLong(),
                        tempDB.get("name").toString(),
                        tempDB.get("phone").toString(),
                        tempDB.get("country").toString(),
                        tempDB.get("address").toString(),
                        tempDB.get("fb").toString(),
                        tempDB.get("web").toString(),
                        tempDB.get("bloggerintro").toString(),
                        tempDB.get("opentime").toString(),
                        tempDB.get("tag_note").toString(),
                        tempDB.get("description").toString(),
                        tempDB.get("pic_url").toString(),
                        tempDB.get("score") as Int,
                        tempDB.get("status") as Int,
                        0f,
                        false,
                        0,
                    "",
                    0
                    )
                    if (debugmode)
                        Log.i("Notif_Fragment", " insert to cloud_PPModelList: index: $i / ${tempDB.get("name").toString()}")
                    near_PPModelList!!.add(ppmodel)

                }
                pps_downloading = false
            } else {
                Toast.makeText(this@Notif_Fragment.context, "$country | $city 【無】線上資料", Toast.LENGTH_LONG)
                    .show()
            }
        }.execute("POST", CloudHelperURL, cloud_cmd.toString())
    }
    
    fun notif_sendLocToCloud(context: Context, send :TripPlan_PPModel) {
        if (useremail.equals("not fill") || useremail.isNullOrEmpty() || !useremail!!.contains("@")) {
            val prefs = context.getSharedPreferences("MAKEDA_USER_SET",
                Context.MODE_PRIVATE
            )
            useremail = prefs!!.getString(Setting_Fragment().user_email_pref, " ")
        }

        val cloud_cmd = "CMD=//TODO&email=$useremail&pp_id=${send.cloudID}"
        if (debugmode)
            Log.i("Notif_Fragment", " cloud_cmd : $cloud_cmd")

        Cloud_Helper(this@Notif_Fragment.context!!) {
            if (it == null) {
                Toast.makeText(this@Notif_Fragment.context, R.string.network_not_stable_zh, Toast.LENGTH_LONG).show()
                return@Cloud_Helper
            }
            if (debugmode)
                Log.i("Notif_Fragment", " response : $it")
            val cloud_db = JSONObject(it)
            if (!cloud_db.isNull("status")) {
                val status = cloud_db.get("status") as Boolean
                if (status && !cloud_db.isNull("loc_record")) {
                    val cloudIDarr : ArrayList<String> = arrayListOf()
                    val loc_record = cloud_db.get("loc_record") as String
                    val records = JSONObject(loc_record)
                    if (debugmode) Log.i("Notif_Fragment", " records : $records")
                    if (!records.isNull("new")) {
                        val new = records.get("new") as Int
                        if (debugmode) Log.i("Notif_Fragment", " new ? = > $new")
                        if (new == 1) {
                            val str = send.name + " - " + getString(R.string.user_trace_new_pp_zh)
                            Toast.makeText(this@Notif_Fragment.context, str, Toast.LENGTH_LONG).show()
                        }
                    }
                }

            }
        }.execute("POST", UserTraceURL, cloud_cmd.toString())
    }

    fun notifRC_onClick(setItem :TripPlan_PPModel) {
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
        )}
        review_f.pp_set(ppModel!!)
        (activity as MainActivity).switchFragment(this@Notif_Fragment, review_f, 3)
    }
}