package tw.breadcrumbs.makeda

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.opentripplan_fragment.*
import org.json.JSONObject
import tw.breadcrumbs.makeda.CloudObj.Cloud_Helper
import tw.breadcrumbs.makeda.DBModules.OpenTripPlanDB_PPModelDAO
import tw.breadcrumbs.makeda.TripPlanning.OpenTripPlanRC
import tw.breadcrumbs.makeda.TripPlanning.PlanningHelper
import tw.breadcrumbs.makeda.TripPlanning.TripPlanning_Fragment
import tw.breadcrumbs.makeda.dataModel.TripListModel
import tw.breadcrumbs.makeda.dataModel.TripPlan_PPModel

//import tw.breadcrumbs.makeda.dataModel.TripPlan_PPModel

class OpenTripplan_Fragment : androidx.fragment.app.Fragment() {
    private val CloudHelperURL = "//TODO"
    private val TripPlanCloudHelperURL = "//TODO"
    private val debugmode = false
    var TripList : MutableList<TripListModel>? = mutableListOf()
    var TripListPPList : MutableList<TripPlan_PPModel>? = mutableListOf()
    private var cloudHdler : Cloud_Helper? = null
    //var firstLayerTripList: MutableList<TripListModel>? = mutableListOf()//CmtModel
    private var openTripPlanRC : OpenTripPlanRC? = null


    override fun onStop() {
        super.onStop()
        //val opentripplanDAO = OpenTripPlanDB_PPModelDAO(context!!)
        //opentripplanDAO.cleanall()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //(activity as MainActivity).setNavigationID(3)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.opentripplan_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupView()
        backButtonSetup()
    }

    fun setupView () {
        DownloadTripPlans()
        recycelViewLoad()
    }

    fun backButtonSetup() {
        Otp_BackButton.setOnClickListener{
            if (cloudHdler != null ) cloudHdler!!.cancel(false)
            (activity as MainActivity).onBackPressed()
        }
    }

    fun recycelViewLoad() {
        TripList = mutableListOf()
        if (this@OpenTripplan_Fragment.context == null) return
        // init recycler view
        opentripplanRecyclerView?.let {
            opentripplanRecyclerView.layoutManager =
                androidx.recyclerview.widget.LinearLayoutManager(this@OpenTripplan_Fragment.context!!)
            openTripPlanRC = OpenTripPlanRC(
                triplistModel = TripList!!,
                clickListener = { partItem: TripListModel, opt: Int ->
                    click(
                        partItem,
                        opt
                    )
                })
            openTripPlanRC!!.tripplan_ppList = TripListPPList
            opentripplanRecyclerView.adapter = openTripPlanRC
        }
    }

    private fun click(partItem : TripListModel, opt: Int) {
        if (opt == 0) {
            if (debugmode) Toast.makeText(context, " click : ${partItem.planname}", Toast.LENGTH_LONG)
                .show()
            if (cloudHdler != null)
                cloudHdler!!.cancel(true)
            val pH = PlanningHelper()
            val tripPlan = partItem
            pH.usingMode = 1
            pH.input_tripListPPList = null
            pH.input_tripListPPList = TripListPPList
            pH.setTripListModel(tripPlan)
            (activity as MainActivity).switchFragment(this@OpenTripplan_Fragment, pH, 3)
        }
    }

    fun download_MultiPP(context: Context, tripplan: TripListModel, item: String, multiPPid: ArrayList<String>) {
        val opentripplanDAO = OpenTripPlanDB_PPModelDAO(context)
        if (debugmode) Log.i("OpenTripplan_Fragment", " id list - $multiPPid")
        var cloud_cmd = "CMD=//TODO&id_array="
        for ( i in 0.. multiPPid.count() - 1) {
            cloud_cmd += multiPPid[i]
            if (i < multiPPid.count() - 1) cloud_cmd += ","
        }
        if (debugmode) Log.i("OpenTripplan_Fragment", "cloud command : $cloud_cmd")

        cloudHdler = Cloud_Helper(context) {
            if (it == null) {
                Toast.makeText(context, "網路不穩定，請稍後再試", Toast.LENGTH_LONG).show()
                return@Cloud_Helper
            }
            if (debugmode)
                Log.i("OpenTripplan_Fragment", "download_MultiPP - Cloud response : $it")
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
                    //val old = opentripplanDAO.get_useCloudID(tripPlan_ppmodel.cloudID, tripPlan_ppmodel.tripPlanID)
                    //if (old == null)
                    //    opentripplanDAO.insert(tripPlan_ppmodel)
                    //else
                    //    opentripplanDAO.update(tripPlan_ppmodel)
                    var same = 0
                    var sameIndex = 0
                    var rmIndex = 0
                    TripListPPList?.forEach {
                        if (it.tripPlanID.equals(tripPlan_ppmodel.tripPlanID) && it.cloudID.equals(tripPlan_ppmodel.cloudID)) {
                            same = 1
                            rmIndex = sameIndex
                            return@forEach
                        }
                        sameIndex ++
                    }
                    if (same == 0) TripListPPList?.add(tripPlan_ppmodel)
                    else {
                        TripListPPList!!.removeAt(rmIndex)
                        TripListPPList!!.add(tripPlan_ppmodel)
                    }
                    if (debugmode)
                        Log.i("OpenTripplan_Fragment", "download_MultiPP - insert to LocalDB: index: $tripPlan_ppmodel")
                }
            } else {
                //Toast.makeText(context, "【無】線上資料", Toast.LENGTH_LONG)
                //    .show()
            }
        }
        cloudHdler!!.execute("POST", CloudHelperURL, cloud_cmd.toString())
    }

    fun DownloadTripPlans() {
        //val cloud_tripplanAll: ArrayList<TripListModel>? = arrayListOf()

        val cloud_cmd = "CMD=//TODO"
        cloudHdler = Cloud_Helper(this@OpenTripplan_Fragment.context!!) {
            if (it == null) {
                Toast.makeText(this@OpenTripplan_Fragment.context, "網路不穩定，請稍後再試", Toast.LENGTH_LONG).show()
                return@Cloud_Helper
            }

            if (debugmode) Log.i("OpenTripplan_Fragment", " DownloadTripPlans - Cloud Update response : $it")
            val cloud_db = JSONObject(it)
            if (!cloud_db.isNull("count")) {

                val cloud_count = cloud_db.get("count") as Int
                for (i in 1..cloud_count) {
                    if (cloud_db.isNull(i.toString())) continue
                    val tempDB = cloud_db.get(i.toString()) as JSONObject

                    val cloud_tripplan = TripListModel(
                        0, tempDB.get("tripplanName").toString(),
                        tempDB.get("author").toString(),
                        tempDB.get("grouplist").toString(),
                        "",
                        tempDB.get("contentJsonFormat").toString(),
                        tempDB.get("tripplanID").toString(),
                        tempDB.get("log").toString(), 0
                    )
                    //cloud_tripplanAll!!.add(cloud_tripplan)
                    var same = 0
                    TripList?.forEach {
                        if (it.tripplanID.equals(cloud_tripplan.tripplanID)) {
                            same = 1
                        }
                    }
                    if (same == 0) {
                        TripList?.add(cloud_tripplan)
                        val items = TripPlanning_Fragment().getCloudContentItems(cloud_tripplan)
                        items!!.forEach{ item->
                            val downArr : ArrayList<String> = arrayListOf()
                            val arr = TripPlanning_Fragment().getPPModelFromItem(cloud_tripplan, item)
                            download_MultiPP(this@OpenTripplan_Fragment.context!!, cloud_tripplan, item, arr!!)
                        }
                    }
                }
            }

            openTripPlanRC?.notifyDataSetChanged()
        }
        cloudHdler!!.execute("POST", TripPlanCloudHelperURL, cloud_cmd.toString())
    }

}