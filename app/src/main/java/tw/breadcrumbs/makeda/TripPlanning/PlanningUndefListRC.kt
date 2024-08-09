package tw.breadcrumbs.makeda.TripPlanning

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import kotlinx.android.synthetic.main.planning_pp_rc_item.view.*
import kotlinx.android.synthetic.main.planning_rc_item.view.*
import kotlinx.android.synthetic.main.trip_plann_rc_item.view.*
import tw.breadcrumbs.makeda.DBModules.TripPlanDAO
import tw.breadcrumbs.makeda.R
import tw.breadcrumbs.makeda.dataModel.PPModel
import tw.breadcrumbs.makeda.dataModel.TripListModel
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import tw.breadcrumbs.makeda.DBModules.TripPlanDB_PPModelDAO
import tw.breadcrumbs.makeda.Setting_Fragment
import tw.breadcrumbs.makeda.dataModel.TripPlan_PPModel


class PlanningUndefListRC (var feedModelItems: MutableList<TripPlan_PPModel>, val clickListener: (String, TripPlan_PPModel, Int) -> Unit) : androidx.recyclerview.widget.RecyclerView.Adapter<PlanningUndefListRC.ViewHolder>() {
    private val debugmode = false
    var usingTripPlan: TripListModel? = null
    var tripPlanDB: TripPlanDAO? = null
    var tripPlanDB_PPModelDAO: TripPlanDB_PPModelDAO? = null
    var input_tripListPPList: MutableList<TripPlan_PPModel>? = null
    var context: Context? = null
    var days : ArrayList<String>? = null
    var useremail: String? = ""
    var mode = 0
    //var set_frag: Setting_Fragment? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.planning_pp_rc_item, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTripPlanListModel( feedModelItems[position], clickListener)
    }

    // 返回數目
    override fun getItemCount(): Int {
        return feedModelItems.size
    }

    fun set_TripPlan(tlm : TripListModel) {
        usingTripPlan = tlm
    }

    fun set_userEamil(email: String) {
        useremail = email
    }

    fun setDaysList( dayitems: MutableList<String> ) {
        days = ArrayList(dayitems.size)
        dayitems.forEach{
            days!!.add(it)
        }
    }

    fun set_TripPlanDB(db: TripPlanDAO) {
        tripPlanDB = db
    }

    fun delpp(pp: TripPlan_PPModel) {
        if (context != null) {
            usingTripPlan = tripPlanDB!!.get(usingTripPlan!!.id)
            if (tripPlanDB_PPModelDAO == null) tripPlanDB_PPModelDAO = TripPlanDB_PPModelDAO(context!!)
            var readID : Long= 0
            var cloudMode = 0
            if (usingTripPlan!!.tripplanID.equals("") || usingTripPlan!!.tripplanID.isEmpty()) {
                readID = usingTripPlan!!.id
            } else {
                readID = usingTripPlan!!.tripplanID.toLong()
                cloudMode = 1
            }
            tripPlanDB_PPModelDAO!!.deleteUsingCloudID(pp.cloudID, readID)
            //tripPlanDB_PPModelDAO!!.close()
            if (cloudMode == 1) {
                val tpf = TripPlanning_Fragment()
                tpf.delPP_toCloud(
                    context!!,
                    useremail!!,
                    usingTripPlan!!,
                    "UNDEF",
                    pp.cloudID.toString()
                )
            }

            //val tpf = TripPlanning_Fragment()
            //tpf.delPP_toCloud(context!!, useremail!!, usingTripPlan!!, "UNDEF", pp.cloudID.toString())
            /*
            val planningContents = JSONObject(usingTripPlan!!.contentJsonFormat)
            val cloudContents = JSONObject(usingTripPlan!!.cloudContent)
            //var undefArray_t: JSONArray? = JSONArray()
            var cl_new_undefArray: JSONArray? = JSONArray()
            var new_undefArray: JSONArray? = JSONArray()
            if (!planningContents.isNull("UNDEF") && !cloudContents.isNull("UNDEF")) {
                val cl_undefStrArray = cloudContents.get("UNDEF")
                var cl_undefArray: JSONArray? = null
                if (cl_undefStrArray is JSONArray && cl_undefStrArray.toString().isNotEmpty())
                    cl_undefArray = JSONArray(cl_undefStrArray.toString())
                else cl_undefArray = JSONArray()
                for (idx in 0 until cl_undefArray.length()) {
                    val ppmodel_id_str = cl_undefArray.get(idx).toString()
                    if (ppmodel_id_str != pp.cloudID.toString()) {
                        cl_new_undefArray!!.put(ppmodel_id_str)
                    }
                }

                val undefStrArray = planningContents.get("UNDEF")// as String
                var undefArray: JSONArray? = null
                if (undefStrArray is JSONArray && undefStrArray.toString().isNotEmpty())
                    undefArray = JSONArray(undefStrArray.toString())
                else undefArray = JSONArray()

                for (idx in 0 until undefArray.length()) {
                    val ppmodel_str = undefArray.get(idx).toString()
                    val ppmodel = JSONObject(ppmodel_str)
                    if (ppmodel.get("name").toString() != pp.name.toString()) {
                        new_undefArray!!.put(ppmodel)
                    }
                }
                cloudContents.remove("UNDEF")
                cloudContents.put("UNDEF", cl_new_undefArray)//undefArray_t

                planningContents.remove("UNDEF")
                planningContents.put("UNDEF", new_undefArray)//undefArray_t
                usingTripPlan!!.contentJsonFormat = planningContents.toString()
                usingTripPlan!!.cloudContent = cloudContents.toString()
                val tpf = TripPlanning_Fragment()
                tpf.delPP_toCloud(context!!, useremail!!, usingTripPlan!!, "UNDEF", pp.cloudID.toString())

                if (tripPlanDB!!.update(usingTripPlan!!)) {
                    if(debugmode) Log.i("PlanningUndefListRC", "delpp: SUCCESS: remove ${pp.name}")
                }
            }

             */
        }
    }

    //@RequiresApi(Build.VERSION_CODES.KITKAT)
    fun movePP_alert(pp: TripPlan_PPModel) {
        if (context != null) {
            val builder = AlertDialog.Builder(context)
            val array = arrayOfNulls<String>(days!!.size)
            val dialog: AlertDialog = builder.setTitle(R.string.tripPlan_moveTo_zh)
                .setSingleChoiceItems(days!!.toArray(array),-1) { dialog, which ->
                    usingTripPlan = tripPlanDB!!.get(usingTripPlan!!.id)
                    if (debugmode) Log.i("PlanningListRC","movePP_alert usingTripPlan = $usingTripPlan" )
                    var readID : Long = 0
                    var cloudMode = 0
                    if (!(usingTripPlan!!.tripplanID.equals("") || usingTripPlan!!.tripplanID.isEmpty())) {
                        cloudMode = 1
                        readID = usingTripPlan!!.tripplanID.toLong()
                    } else {
                        readID = usingTripPlan!!.id
                    }
                    if (tripPlanDB_PPModelDAO == null) tripPlanDB_PPModelDAO = TripPlanDB_PPModelDAO(context!!)
                    val tripPlanDB_PP = tripPlanDB_PPModelDAO!!.get_useCloudID(pp.cloudID, readID)
                    if (debugmode) Log.i("PlanningListRC","movePP_alert selected : readID = $readID , tripPlanDB_PP = $tripPlanDB_PP" )
                    if (tripPlanDB_PP != null) {
                        tripPlanDB_PP.setTripPlanItem = days!![which]
                        tripPlanDB_PPModelDAO!!.update(tripPlanDB_PP)
                        clickListener(tripPlanDB_PP.setTripPlanItem, pp, 1)
                        if (cloudMode == 1) {
                            //fun movePP_onCloud(context: Context, useremail:String, tripplan: TripListModel, from: String, to: String, pp_id: String) {
                            val tpf = TripPlanning_Fragment()
                            tpf.movePP_onCloud(context!!, useremail!!, usingTripPlan!!, "UNDEF", tripPlanDB_PP.setTripPlanItem, pp.cloudID.toString())
                        }
                    } else {
                        if (debugmode) {
                            val tripPlanDB_PPList = tripPlanDB_PPModelDAO!!.findTripPlan(readID)//get_useCloudID(pp.cloudID, readID)
                            Log.i("PlanningListRC","movePP_alert selected : readID = $readID , tripPlanDB_PP = $tripPlanDB_PPList" )
                        }
                    }
                    //tripPlanDB_PPModelDAO!!.close()
                    /*
                    //var temp : JSONObject? = null//JSONObject()
                    // move out
                    val planningContents = JSONObject(usingTripPlan!!.contentJsonFormat)
                    val cloudContents = JSONObject(usingTripPlan!!.cloudContent)
                    var cl_undefArray: JSONArray? = JSONArray()
                    var cl_dayPPArray: JSONArray? = JSONArray()
                    var undefArray_t: JSONArray? = JSONArray()
                    var dayPPArray: JSONArray? = JSONArray()

                    if (!planningContents.isNull("UNDEF") && !cloudContents.isNull("UNDEF")) {
                        val undefCl_StrArray = cloudContents.get("UNDEF")
                        var undefCl_Array: JSONArray? = null
                        val undefStrArray = planningContents.get("UNDEF")// as String
                        var undefArray: JSONArray? = null
                        if (undefCl_StrArray is JSONArray && undefCl_StrArray.toString().isNotEmpty())
                            undefCl_Array = JSONArray(undefCl_StrArray.toString())
                        else
                            undefCl_Array = JSONArray(undefStrArray.toString())

                        val cl_dayPPstr = cloudContents.get(days!![which])
                        if (cl_dayPPstr is JSONArray && cl_dayPPstr.toString().isNotEmpty()) {
                            cl_dayPPArray = JSONArray(cl_dayPPstr.toString())
                        }

                        for (idx in 0..(undefCl_Array.length() - 1)) {
                            val ppmodel_ID_str = undefCl_Array.get(idx).toString()

                            if (ppmodel_ID_str == pp.cloudID.toString()) {
                                cl_dayPPArray!!.put(ppmodel_ID_str)
                            } else {
                                cl_undefArray!!.put(ppmodel_ID_str)
                            }
                        }

                        if (undefStrArray is JSONArray && undefStrArray.toString().isNotEmpty())
                            undefArray = JSONArray(undefStrArray.toString())
                        else undefArray = JSONArray()
                        var dayPPstr : Any? = ""
                        if (planningContents.isNull(days!![which])) {
                            dayPPstr = ""
                        } else dayPPstr = planningContents.get(days!![which])
                        if (dayPPstr is JSONArray && dayPPstr.toString().isNotEmpty()) {
                            dayPPArray = JSONArray(dayPPstr.toString())
                        }

                        for (idx in 0..(undefArray.length() - 1)) {
                            val ppmodel_str = undefArray.get(idx).toString()
                            val ppmodel = JSONObject(ppmodel_str)

                            if (ppmodel.get("name").toString() == pp.name.toString()) {
                                dayPPArray!!.put(ppmodel)
                            } else {
                                undefArray_t!!.put(ppmodel)
                            }
                        }

                        //cloudContents.remove("UDNEF")
                        cloudContents.put("UNDEF", cl_undefArray)
                        cloudContents.put(days!![which], cl_dayPPArray)

                        //planningContents.remove("UNDEF")
                        planningContents.put("UNDEF", undefArray_t)//undefArray_t
                        planningContents.put(days!![which], dayPPArray)
                        val tpf = TripPlanning_Fragment()
                        tpf.movePP_onCloud(context!!, useremail!!, usingTripPlan!!, "UNDEF", days!![which], pp.cloudID.toString())
                    }
                    usingTripPlan!!.cloudContent = cloudContents.toString()
                    usingTripPlan!!.contentJsonFormat = planningContents.toString()
                    if (tripPlanDB!!.update(usingTripPlan!!)) {
                        if (debugmode) {
                            Log.i(
                                "PlanningUndefListRC",
                                "movePP_alert: SUCCESS: add ${days!![which]}"
                            )
                            Log.i(
                                "PlanningUndefListRC",
                                "cloudContent : ${usingTripPlan!!.cloudContent}"
                            )
                            Log.i(
                                "PlanningUndefListRC",
                                "contentJsonFormat : ${usingTripPlan!!.contentJsonFormat}"
                            )
                        }
                    }

                     */
                    clickListener(days!![which], pp, 1)
                    dialog.dismiss()
                }
                .create()

            dialog.show()
        }
    }

    fun save_to_tripPlanning(pp: TripPlan_PPModel) {
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
                    val ppModel = PPModel(
                        pp.id,
                        pp.cloudID,
                        pp.name,
                        pp.phone,
                        pp.country,
                        pp.addr,
                        pp.fb,
                        pp.web,
                        pp.blogInfo,
                        pp.opentime,
                        pp.tag_note,
                        pp.descrip,
                        pp.pic_url,
                        pp.score,
                        pp.status,
                        pp.distance,
                        pp.calDistanceDone
                    )
                    ph.add_pp_into(context!!, tripPlanModel!!.id, ppModel, "UNDEF")
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

    // view
    inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

        fun bindTripPlanListModel(ppmodel: TripPlan_PPModel, clickListener: (String, TripPlan_PPModel, Int) -> Unit) {
            ppmodel.let{
                itemView.pp_titleName.text = ppmodel.name
                itemView.pp_subTitle.text = ppmodel.tag_note
                //itemView.author.text = triplistmodel.author
                if (ppmodel.calDistanceDone) {
                    val distance = ppmodel.distance
                    itemView.pp_distance.text = "離您約 $distance KM"
                } else {
                    itemView.pp_distance.text = "請按右上方排序，計算距離"
                }
                itemView.pp_tripplan_nav!!.setOnClickListener {
                    //(activity as MainActivity).mapNavi(ppmodel.addr)
                    clickListener("", ppmodel, 2)
                }

                itemView.pp_tripplan_call!!.setOnClickListener {
                    //(activity as MainActivity).callPhone(ppmodel.phone)
                    clickListener("", ppmodel, 3)
                }
                if (mode == 0) {
                    itemView.pp_tripplan_del!!.visibility = View.VISIBLE
                    itemView.pp_tripplan_del!!.setOnClickListener {
                        if (mode == 1) {
                            Toast.makeText(context, "無權限編輯", Toast.LENGTH_LONG).show()
                            return@setOnClickListener
                        }
                        delpp(ppmodel)
                        clickListener("", ppmodel, 4)
                    }
                } else {
                    itemView.pp_tripplan_del!!.visibility = View.INVISIBLE
                }
            }

            itemView.setOnClickListener{

                clickListener("", ppmodel, 0)
            }

            itemView.setOnLongClickListener(View.OnLongClickListener { v ->
                if (mode == 1) {
                    //Toast.makeText(context, "無權限編輯", Toast.LENGTH_LONG).show()
                    save_to_tripPlanning(ppmodel)
                    return@OnLongClickListener true
                }
                //Toast.makeText(v.context, "Position is $position", Toast.LENGTH_SHORT).show()
                movePP_alert(ppmodel)
                false
            })
        }
    }
}