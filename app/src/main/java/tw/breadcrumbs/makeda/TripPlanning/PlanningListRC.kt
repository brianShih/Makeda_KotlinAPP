package tw.breadcrumbs.makeda.TripPlanning

import android.app.AlertDialog
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import kotlinx.android.synthetic.main.planning_pp_rc_item.view.*
import kotlinx.android.synthetic.main.trip_plann_rc_item.view.*
import org.json.JSONArray
import org.json.JSONObject
import tw.breadcrumbs.makeda.DBModules.TripPlanDAO
import tw.breadcrumbs.makeda.DBModules.TripPlanDB_PPModelDAO
import tw.breadcrumbs.makeda.MainActivity
import tw.breadcrumbs.makeda.R
import tw.breadcrumbs.makeda.Setting_Fragment
import tw.breadcrumbs.makeda.dataModel.PPModel
import tw.breadcrumbs.makeda.dataModel.TripListModel
import tw.breadcrumbs.makeda.dataModel.TripPlan_PPModel


class PlanningListRC (var feedModelItems: MutableList<TripPlan_PPModel>, val clickListener: (String, TripPlan_PPModel, Int) -> Unit) : androidx.recyclerview.widget.RecyclerView.Adapter<PlanningListRC.ViewHolder>() {
    private val debugmode = false
    var useremail : String? = ""
    var usingTripPlan: TripListModel? = null
    var input_tripListPPList: MutableList<TripPlan_PPModel>? = null
    var tripPlanDB: TripPlanDAO? = null
    var tripPlanDB_PPModelDAO: TripPlanDB_PPModelDAO? = null
    var days: ArrayList<String>? = null
    var selitem : ArrayList<String>? = null
    var context: Context? = null
    var selIndex: Int = 0
    var mode = 0

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

    fun setDayIndex(index : Int) {
        selIndex = index
    }

    fun set_TripPlanDB(db: TripPlanDAO) {
        tripPlanDB = db
    }

    fun delpp(pp: TripPlan_PPModel, day: String) {
        if (context != null) {
            var cloudMode = 0
            usingTripPlan = tripPlanDB!!.get(usingTripPlan!!.id)
            if (tripPlanDB_PPModelDAO == null) tripPlanDB_PPModelDAO =
                TripPlanDB_PPModelDAO(context!!)
            var readID: Long = 0
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
                    day,
                    pp.cloudID.toString()
                )
            }
            //if (tripPlanDB!!.update(usingTripPlan!!)) {
            //    if (debugmode) Log.i("PlanningListRC", "delpp: SUCCESS: remove ${pp.name}")
            //}
            /*
            val planningContents = JSONObject(usingTripPlan!!.contentJsonFormat)
            val cloudContents = JSONObject(usingTripPlan!!.cloudContent)
            var cl_new_dayppArray: JSONArray? = JSONArray()
            var new_dayppArray: JSONArray? = JSONArray()
            if (!planningContents.isNull(day) && !cloudContents.isNull(day)) {
                val cl_undefStrArray = cloudContents.get(day)// as String
                var cl_undefArray: JSONArray? = null
                if (cl_undefStrArray is JSONArray && cl_undefStrArray.toString().isNotEmpty())
                    cl_undefArray = JSONArray(cl_undefStrArray.toString())
                else cl_undefArray = JSONArray()

                for (idx in 0 until cl_undefArray.length()) {
                    val ppmodel_id_str = cl_undefArray.get(idx).toString()
                    if (ppmodel_id_str != pp.cloudID.toString()) {
                        cl_new_dayppArray!!.put(ppmodel_id_str)
                    }
                }


                val undefStrArray = planningContents.get(day)// as String
                var undefArray: JSONArray? = null
                if (undefStrArray is JSONArray && undefStrArray.toString().isNotEmpty())
                    undefArray = JSONArray(undefStrArray.toString())
                else undefArray = JSONArray()

                for (idx in 0 until undefArray.length()) {
                    val ppmodel_str = undefArray.get(idx).toString()
                    val ppmodel = JSONObject(ppmodel_str)
                    if (ppmodel.get("name").toString() != pp.name.toString()) {
                        new_dayppArray!!.put(ppmodel)
                    }
                }

                cloudContents.remove(day)
                cloudContents.put(day, cl_new_dayppArray)

                planningContents.remove(day)
                planningContents.put(day, new_dayppArray)//undefArray_t
                usingTripPlan!!.contentJsonFormat = planningContents.toString()
                usingTripPlan!!.cloudContent = cloudContents.toString()
                val tpf = TripPlanning_Fragment()
                tpf.delPP_toCloud(context!!, useremail!!, usingTripPlan!!, day, pp.cloudID.toString())
                if (tripPlanDB!!.update(usingTripPlan!!)) {
                    if (debugmode) Log.i("PlanningListRC", "delpp: SUCCESS: remove ${pp.name}")
                }
            }

             */
        }
    }

    fun movePP_alert(pp: TripPlan_PPModel) {
        if (debugmode) Log.i("PlanningListRC","movePP_alert start" )
        selitem = ArrayList(days!!.size+1)
        val builder = AlertDialog.Builder(context)
        days!!.forEach {
            selitem!!.add(it)
        }
        selitem!!.add("UNDEFINE")
        val array = arrayOfNulls<String>(selitem!!.size)
        val dialog: AlertDialog = builder.setTitle(R.string.tripPlan_moveTo_zh)
            .setSingleChoiceItems(selitem!!.toArray(array),-1) { dialog, which ->
                if (debugmode) Log.i("PlanningListRC","movePP_alert selected : ${selitem!![selIndex]} to ${selitem!![which]}" )
                usingTripPlan = tripPlanDB!!.get(usingTripPlan!!.id)
                if (debugmode) Log.i("PlanningListRC","movePP_alert usingTripPlan = $usingTripPlan" )
                var readID : Long = 0
                var selItem = ""
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
                //tripPlanDB_PPModelDAO!!.close()
                if (tripPlanDB_PP != null) {
                    if (selitem!![which].equals("UNDEFINE")) {
                        tripPlanDB_PP.setTripPlanItem = "UNDEF"
                        selItem = selitem!![selIndex]
                    } else {
                        tripPlanDB_PP.setTripPlanItem = selitem!![which]
                        selItem = selitem!![which]
                    }
                    tripPlanDB_PPModelDAO!!.update(tripPlanDB_PP)
                    //tripPlanDB_PPModelDAO!!.close()
                    if (cloudMode == 1) {
                        val tpf = TripPlanning_Fragment()
                        tpf.movePP_onCloud(
                            context!!,
                            useremail!!,
                            usingTripPlan!!,
                            selitem!![selIndex],
                            tripPlanDB_PP.setTripPlanItem,
                            pp.cloudID.toString()
                        )
                    }
                    selIndex = which
                    clickListener(selItem, pp, 1)
                } else {
                    if (debugmode) {
                        val tripPlanDB_PPList = tripPlanDB_PPModelDAO!!.findTripPlan(readID)//get_useCloudID(pp.cloudID, readID)
                        Log.i("PlanningListRC","movePP_alert selected : readID = $readID , tripPlanDB_PP = $tripPlanDB_PPList" )
                    }
                }
                //if (!(usingTripPlan!!.cloudContent.isEmpty() || usingTripPlan!!.cloudContent.equals(""))) {
                //    val cloudContents = JSONObject(usingTripPlan!!.cloudContent)
                //}


                /*
                val planningContents = JSONObject(usingTripPlan!!.contentJsonFormat)
                val cloudContents = JSONObject(usingTripPlan!!.cloudContent)
                var to_Undef_OR_Day = 0
                val selPPArray: JSONArray? = JSONArray()
                val cl_selPPArray: JSONArray? = JSONArray()
                var undefArray_t: JSONArray? = JSONArray()
                var cl_undefArray_t: JSONArray? = JSONArray()
                var to_dayPPArray: JSONArray? = JSONArray()
                var cl_to_dayPPArray: JSONArray? = JSONArray()
                if (selitem!![which].toString() == "UNDEFINE") {
                    to_Undef_OR_Day = 0
                } else to_Undef_OR_Day = 1

                if (!planningContents.isNull(selitem!![selIndex]) && !cloudContents.isNull(selitem!![selIndex])) {
                    //var undefArray: JSONArray? = null
                    val cl_seldayStrArray = cloudContents.get(selitem!![selIndex]) // as String
                    var cl_seldayArray :JSONArray? = null//JSONArray()
                    if ( cl_seldayStrArray.toString().isNotEmpty()) {
                        if (cl_seldayStrArray is JSONArray) {
                            cl_seldayArray = JSONArray(cl_seldayStrArray.toString())
                        } else if (cl_seldayStrArray is JSONObject) {
                            val jsonObj = JSONObject(cl_seldayStrArray.toString())
                            cl_seldayArray = JSONArray()
                            cl_seldayArray.put(jsonObj)
                        }
                    } else cl_seldayArray = JSONArray()

                    val seldayStrArray = planningContents.get(selitem!![selIndex]) // as String
                    var seldayArray :JSONArray? = null//JSONArray()
                    if ( seldayStrArray.toString().isNotEmpty()) {
                        if (seldayStrArray is JSONArray) {
                            seldayArray = JSONArray(seldayStrArray.toString())
                        } else if (seldayStrArray is JSONObject) {
                            val jsonObj = JSONObject(seldayStrArray.toString())
                            seldayArray = JSONArray()
                            seldayArray.put(jsonObj)
                        }
                    } else seldayArray = JSONArray()
                    if (debugmode) Log.i("PlanningListRC", "movePP_alert: old seldayStrArray = $seldayStrArray")
                    if (debugmode) Log.i("PlanningListRC", "movePP_alert: old seldayArray = ${seldayArray.toString()}")
                        //val seldayArray = JSONArray(seldayStrArray)

                    if (to_Undef_OR_Day == 0) {
                        //get undefine array from jsonobj
                        if (!planningContents.isNull("UNDEF") && !cloudContents.isNull("UNDEF")) {
                            val cl_undefStrArray = cloudContents.get("UNDEF")
                            if ( cl_undefStrArray.toString().isNotEmpty()) {
                                if (cl_undefStrArray is JSONArray) {
                                    undefArray_t = JSONArray(cl_undefStrArray.toString())
                                } else if (cl_undefStrArray is JSONObject) {
                                    val jsonObj = JSONObject(cl_undefStrArray.toString())
                                    cl_undefArray_t = JSONArray()
                                    cl_undefArray_t.put(jsonObj)
                                }
                            } else cl_undefArray_t = JSONArray()

                            val undefStrArray = planningContents.get("UNDEF")
                            if ( undefStrArray.toString().isNotEmpty()) {
                                if (undefStrArray is JSONArray) {
                                    undefArray_t = JSONArray(undefStrArray.toString())
                                } else if (undefStrArray is JSONObject) {
                                    val jsonObj = JSONObject(undefStrArray.toString())
                                    undefArray_t = JSONArray()
                                    undefArray_t.put(jsonObj)
                                }
                            } else undefArray_t = JSONArray()
                        } else {
                            cl_undefArray_t = JSONArray()
                            undefArray_t = JSONArray()
                        }
                        if (debugmode) Log.i("PlanningListRC", "movePP_alert: old undefArray_t = ${undefArray_t.toString()}")
                    } else {
                        if (!planningContents.isNull(selitem!![which]) && !cloudContents.isNull(selitem!![which])) {
                            //get click day item array from jsonobj
                            val cl_dayPPstr = cloudContents.get(selitem!![which])
                            if ( cl_dayPPstr.toString().isNotEmpty()) {
                                if (cl_dayPPstr is JSONArray) {
                                    cl_to_dayPPArray = JSONArray(cl_dayPPstr.toString())
                                } else if (cl_dayPPstr is JSONObject) {
                                    val jsonObj = JSONObject(cl_dayPPstr.toString())
                                    cl_to_dayPPArray = JSONArray()
                                    cl_to_dayPPArray.put(jsonObj)
                                }
                            } else cl_to_dayPPArray = JSONArray()

                            val dayPPstr = planningContents.get(selitem!![which])
                            if ( dayPPstr.toString().isNotEmpty()) {
                                if (dayPPstr is JSONArray) {
                                    to_dayPPArray = JSONArray(dayPPstr.toString())
                                } else if (dayPPstr is JSONObject) {
                                    val jsonObj = JSONObject(dayPPstr.toString())
                                    to_dayPPArray = JSONArray()
                                    to_dayPPArray.put(jsonObj)
                                }
                            } else to_dayPPArray = JSONArray()

                        } else {
                            to_dayPPArray = JSONArray()
                        }
                        if (debugmode) Log.i("PlanningListRC", "movePP_alert: old to_dayPPArray = ${to_dayPPArray.toString()}")
                    }

                    for (idx in 0 until cl_seldayArray!!.length()) {
                        val ppmodel_id_str = cl_seldayArray.get(idx).toString()
                        //val ppmodel = JSONObject(ppmodel_str)

                        if (ppmodel_id_str == pp.cloudID.toString()) {
                            if (to_Undef_OR_Day == 1) {
                                if (debugmode) Log.i("PlanningListRC", "movePP_alert: put ppmodel to cl_to_dayPPArray: move ${ppmodel_id_str} to ${cl_to_dayPPArray.toString()}")
                                cl_to_dayPPArray!!.put(ppmodel_id_str)
                            }
                            else {
                                cl_undefArray_t!!.put(ppmodel_id_str)
                            }
                        } else {
                            cl_selPPArray!!.put(ppmodel_id_str)
                        }
                    }

                    for (idx in 0 until seldayArray!!.length()) {
                        val ppmodel_str = seldayArray.get(idx).toString()
                        val ppmodel = JSONObject(ppmodel_str)

                        if (ppmodel.get("name").toString() == pp.name) {
                            if (to_Undef_OR_Day == 1) {
                                if (debugmode) Log.i("PlanningListRC", "movePP_alert: put ppmodel to to_dayPPArray: move ${ppmodel} to ${to_dayPPArray.toString()}")
                                to_dayPPArray!!.put(ppmodel)
                            }
                            else {
                                undefArray_t!!.put(ppmodel)
                            }
                        } else {
                            selPPArray!!.put(ppmodel)
                        }
                    }

                } else {
                    if (debugmode) Log.i("PlanningListRC", "movePP_alert: selitem is NULL")
                }
                if (debugmode) {
                    Log.i(
                        "PlanningListRC",
                        "movePP_alert: new selPPArray = ${selPPArray.toString()}"
                    )
                    Log.i(
                        "PlanningListRC",
                        "movePP_alert: new undefArray_t = ${undefArray_t.toString()}"
                    )
                    Log.i(
                        "PlanningListRC",
                        "movePP_alert: new to_dayPPArray = ${to_dayPPArray.toString()}"
                    )
                }
                cloudContents.remove(selitem!![selIndex])
                cloudContents.put(selitem!![selIndex], cl_selPPArray)

                planningContents.remove(selitem!![selIndex])
                planningContents.put(selitem!![selIndex], selPPArray)

                if (to_Undef_OR_Day == 1) {
                    cloudContents.put(selitem!![which], cl_to_dayPPArray)
                    planningContents.put(selitem!![which], to_dayPPArray)
                    val tpf = TripPlanning_Fragment()
                    tpf.movePP_onCloud(context!!, useremail!!, usingTripPlan!!, selitem!![selIndex], selitem!![which], pp.cloudID.toString())
                } else {
                    cloudContents.put("UNDEF", cl_undefArray_t)
                    planningContents.put("UNDEF", undefArray_t)//undefArray_t

                    val tpf = TripPlanning_Fragment()
                    tpf.movePP_onCloud(context!!, useremail!!, usingTripPlan!!, selitem!![selIndex], "UNDEF", pp.cloudID.toString())
                }

                usingTripPlan!!.contentJsonFormat = planningContents.toString()
                usingTripPlan!!.cloudContent = cloudContents.toString()
                if (tripPlanDB!!.update(usingTripPlan!!)) {
                    if(debugmode) Log.i("PlanningListRC", "movePP_alert: SUCCESS: move ${selitem!![which]} / ${to_dayPPArray.toString()}")
                }
                if (to_Undef_OR_Day == 1) {
                    clickListener(days!![which], pp, 1)
                    selIndex = which
                } else {
                    clickListener(days!![selIndex], pp, 1)
                }

*/
                dialog.dismiss()
            }
            .create()

        dialog.show()
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
                    itemView.pp_tripplan_del!!.setOnClickListener{
                        if (mode == 1) {
                            Toast.makeText(context, "無權限編輯", Toast.LENGTH_LONG).show()
                            return@setOnClickListener
                        }
                        delpp(ppmodel, days!![selIndex])
                        clickListener(days!![selIndex], ppmodel, 4)
                    }
                } else {
                    itemView.pp_tripplan_del!!.visibility = View.INVISIBLE
                }
            }

            itemView.setOnClickListener{
                clickListener("", ppmodel, 0)
            }

            itemView.setOnLongClickListener { v ->
                if (mode == 1) {
                    //Toast.makeText(context, "無權限編輯", Toast.LENGTH_LONG).show()
                    //return@setOnLongClickListener true
                    save_to_tripPlanning(ppmodel)
                    return@setOnLongClickListener true
                }
                movePP_alert(ppmodel)
                true
            }
        }
    }
}