package tw.breadcrumbs.makeda.TripPlanning

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import kotlinx.android.synthetic.main.planning_days_rc_item.view.*
import kotlinx.android.synthetic.main.planning_pp_rc_item.view.*
import kotlinx.android.synthetic.main.trip_plann_rc_item.view.*
import org.json.JSONArray
import org.json.JSONObject
import tw.breadcrumbs.makeda.DBModules.TripPlanDAO
import tw.breadcrumbs.makeda.DBModules.TripPlanDB_PPModelDAO
import tw.breadcrumbs.makeda.R
import tw.breadcrumbs.makeda.dataModel.PPModel
import tw.breadcrumbs.makeda.dataModel.TripListModel
import java.util.*


class PlanningListDaysRC ( var dayItems: MutableList<String>, val clickListener: (String, Int) -> Unit) : androidx.recyclerview.widget.RecyclerView.Adapter<PlanningListDaysRC.ViewHolder>() {
    private val debugmode = false
    var context: Context? = null
    var usingTripPlan: TripListModel? = null
    var useremail: String? = ""
    var tripPlanDB: TripPlanDAO? = null
    var tripPlanDB_PPModelDAO: TripPlanDB_PPModelDAO? = null
    var editmode:Boolean = false
    var selIndex: Int = 0
    var mode = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.planning_days_rc_item, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTripPlanListModel(position, dayItems[position], clickListener)
    }
    fun setEditMode(editMode : Boolean) {
        editmode = editMode
    }

    fun set_userEmail( user: String ){
        useremail = user
    }

    fun getDaysList() : MutableList<String> {
        return dayItems
    }

    fun getDayIndex () : Int {
        return selIndex
    }

    fun setDayIndex(index : Int) {
        selIndex = index
    }

    fun set_TripPlan(tlm : TripListModel) {
        usingTripPlan = tlm
    }

    // 返回數目
    override fun getItemCount(): Int {
        return dayItems.size
    }

    fun set_TripPlanDB(db: TripPlanDAO) {
        tripPlanDB = db
    }

    fun delDayInCloudContent(delDay : String, dayIndex : Int) {
        usingTripPlan = tripPlanDB!!.get(usingTripPlan!!.id)
        if (!(usingTripPlan!!.cloudContent.isEmpty() || usingTripPlan!!.cloudContent.equals(""))) {
            val cloudContent = JSONObject(usingTripPlan!!.cloudContent)
            if (!cloudContent.isNull(delDay)) {
                val delItems = cloudContent.get(delDay) as JSONArray
                if (cloudContent.isNull("UNDEF")) {
                    cloudContent.put("UNDEF", delItems)
                } else {
                    var undefItems = cloudContent.get("UNDEF") as JSONArray
                    for (i in 0..delItems.length()-1) {
                        undefItems.put(delItems.get(i))
                    }

                    cloudContent.remove(delDay)
                    cloudContent.put("UNDEF", undefItems)
                }

                if (dayIndex < dayItems.count()) {
                    for (i in dayIndex..dayItems.count()) {
                        if (debugmode) Log.i("PlanningListDaysRC", "remove DAY ${i + 1} put into $i")
                        if (!cloudContent.isNull("DAY ${i + 1}")) {
                            val moveItems = cloudContent.get("DAY ${i + 1}") as JSONArray
                            var toItems : JSONArray? = null
                            if (cloudContent.isNull("DAY $i")) {
                                toItems = JSONArray()//cloudContent.get("DAY ${i}") as JSONArray
                            } else {
                                toItems = cloudContent.get("DAY ${i}") as JSONArray
                            }
                            for (i in 0..moveItems.length() - 1) {
                                toItems.put(moveItems.get(i))
                            }
                            cloudContent.put("DAY ${i}", toItems)
                            cloudContent.remove("DAY ${i + 1}")
                        }
                    }
                }
            } else {
                cloudContent.remove(dayItems[dayItems.count() - 1])
            }

            usingTripPlan!!.cloudContent = cloudContent.toString()
            tripPlanDB!!.update(usingTripPlan!!)
            if (debugmode) Log.i("PlanningListDaysRC", "delDayInCloudContent - remove day : ${dayItems[dayItems.count() - 1]}")
            if (debugmode) Log.i("PlanningListDaysRC", "delDayInCloudContent - final cloud Content : $cloudContent")
        }
    }

    // view
    inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        fun bindTripPlanListModel(position: Int, dayItem: String, clickListener: (String,Int) -> Unit) {
            itemView.let{
                if (editmode) {
                    itemView.day_del.visibility = View.VISIBLE
                } else {
                    itemView.day_del.visibility = View.INVISIBLE
                }
                if (debugmode) Log.i("PlanningListDaysRC", "select index : $selIndex / position : $position")
                if (selIndex == position) {
                    if (debugmode) Log.i("PlanningListDaysRC", "clicked index : $selIndex")
                    //itemView.dayTitle.setTextColor(Color.BLACK)
                    itemView.planning_days_ln.setBackgroundColor(Color.rgb(30, 203, 255))
                    itemView.dayTitle.setTextColor(Color.rgb(255, 255, 255))
                } else {
                    if (debugmode) Log.i("PlanningListDaysRC", "re-init index: $selIndex")
                    itemView.planning_days_ln.setBackgroundColor(Color.rgb(255, 255, 255))
                    itemView.dayTitle.setTextColor(Color.rgb(30, 203, 255))
                    //itemView.dayTitle.setTextColor(Color.BLUE)
                }
                dayItem.let{
                    itemView.dayTitle.text = dayItem.toString()
                    //itemView.titleName.text = triplistmodel.planname
                    //itemView.author.text = triplistmodel.author
                }
                itemView.setOnClickListener{
                    clickListener(dayItem, 0)
                    //itemView.dayTitle.setTextColor(R.color.colorBlack)
                }

                itemView.day_del.setOnClickListener{
                    if (mode == 1) {
                        Toast.makeText(context, "無權限編輯", Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }
                    if (!editmode) return@setOnClickListener
                    //var dayItemIndex = 0
                    if (dayItems.count() == 1) return@setOnClickListener

                    if (tripPlanDB_PPModelDAO == null) tripPlanDB_PPModelDAO = TripPlanDB_PPModelDAO(context!!)
                    var readID : Long= 0
                    var cloudMode = 0
                    if (usingTripPlan!!.tripplanID.equals("") || usingTripPlan!!.tripplanID.isEmpty()) {
                        readID = usingTripPlan!!.id
                    } else {
                        readID = usingTripPlan!!.tripplanID.toLong()
                        cloudMode = 1
                    }
                    val dayXPPList = tripPlanDB_PPModelDAO!!.getItemX(readID, dayItem)
                    dayXPPList!!.forEach {
                        it.setTripPlanItem = "UNDEF"
                        tripPlanDB_PPModelDAO!!.update(it)
                    }

                    var dayItemIndex = -1

                    for ( i in 0..dayItems.count() - 1) {
                        val tempItem = dayItems.get(i)
                        if (tempItem.equals(dayItem)) {
                            dayItemIndex = i
                        }
                        if (dayItemIndex >= 0 && (i + 1) < dayItems.count()) {
                            dayItemIndex = i
                            val movePPList = tripPlanDB_PPModelDAO!!.getItemX(readID, dayItems.get(i + 1))
                            movePPList?.forEach {
                                it.setTripPlanItem = dayItems.get(i)
                                tripPlanDB_PPModelDAO!!.update(it)
                            }
                        }
                    }
                    //tripPlanDB_PPModelDAO!!.close()
                    if (debugmode) Log.i("PlanningListDaysRC", "itemView.day_del.setOnClickListener - del Day = $dayItem ,dayItemIndex = $dayItemIndex")
                    /*if (dayItemIndex < dayItems.count()) {
                        for (c in (dayItemIndex + 1)..dayItems.count() - 1) {
                            if (dayItemIndex - 1 < 0) continue
                            val movePPList = tripPlanDB_PPModelDAO!!.getItemX(readID, dayItems.get(c - 1))
                            movePPList?.forEach {
                                it.setTripPlanItem = "DAY ${c - 2}"
                                tripPlanDB_PPModelDAO!!.update(it)
                            }
                        }
                    }*/

                    if (cloudMode == 1) {
                        val tpf = TripPlanning_Fragment()
                        tpf.delDay_onCloud(context!!, useremail!!, usingTripPlan!!, dayItem)
                        if (debugmode) Log.i(
                            "PlanningListDaysRC",
                            " update DB : ${usingTripPlan!!.id}"
                        )
                    }

                    delDayInCloudContent(dayItem, dayItemIndex)
                    val lastDay = dayItems[dayItems.count() - 1]
                    dayItems.remove(lastDay)
                    if (debugmode) Log.i("PlanningListDaysRC", "itemView.day_del.setOnClickListener - final cloud Content : ${usingTripPlan!!.cloudContent}")
                    clickListener(dayItem, 1)
/*
                    val cloudContent = JSONObject(usingTripPlan!!.cloudContent)
                    val loc_contentJson = JSONObject(usingTripPlan!!.contentJsonFormat)
                    //var jsonArray: JSONArray? = null
                    if (!loc_contentJson.isNull(dayItem) && !cloudContent.isNull(dayItem)) {
                        val cl_tripPlanPPid = cloudContent.get(dayItem)
                        val tripPlanPPList = loc_contentJson.get(dayItem)
                        if (tripPlanPPList.toString().isNotEmpty() && cl_tripPlanPPid.toString().isNotEmpty()) {
                            val cl_del_pps_array: JSONArray = JSONArray(cl_tripPlanPPid.toString())
                            var cl_undef_pps_array:JSONArray? = JSONArray()
                            val del_pps_array: JSONArray = JSONArray(tripPlanPPList.toString())
                            var undef_pps_array:JSONArray? = JSONArray()
                            if (!loc_contentJson.isNull("UNDEF") && !cloudContent.isNull("UNDEF")) {
                                val cl_UndefPPList_org = cloudContent.get("UNDEF")
                                if (cl_UndefPPList_org is JSONArray) {
                                    cl_undef_pps_array = cl_UndefPPList_org as JSONArray
                                } else if (cl_UndefPPList_org is JSONObject) {
                                    val jsonObj = JSONObject(cl_UndefPPList_org.toString())
                                    cl_undef_pps_array!!.put(cl_del_pps_array.length(), jsonObj)
                                }

                                val UndefPPList_org = loc_contentJson.get("UNDEF")
                                if (UndefPPList_org is JSONArray) {
                                    undef_pps_array = UndefPPList_org as JSONArray
                                } else if (UndefPPList_org is JSONObject) {
                                    val jsonObj = JSONObject(UndefPPList_org.toString())
                                    undef_pps_array!!.put(undef_pps_array.length(), jsonObj)
                                }
                            }

                            for (i in 0 until cl_del_pps_array.length()) {
                                val single = cl_del_pps_array.get(i).toString()
                                cl_undef_pps_array!!.put(cl_undef_pps_array.length(), single)
                            }

                            for (i in 0 until del_pps_array.length()) {
                                undef_pps_array!!.put(undef_pps_array.length(), del_pps_array.get(i))
                            }
                            //undef_pps_array!!.o//put(undef_pps_array.length(), del_pps_array)
                            cloudContent.put("UNDEF", cl_undef_pps_array)
                            loc_contentJson.put("UNDEF", undef_pps_array)
                        }
                    }

                    var dayIndex = 1
                    var fillUP = 0
                    var rmItem : String = ""
                    dayItems.forEach{
                        if (fillUP == 0) {
                            if (it == dayItem && dayIndex == dayItems.count() && dayItems.count() > 1) {
                                //remove last day
                                if (!loc_contentJson.isNull(dayItem) && !cloudContent.isNull(dayItem)) {
                                    cloudContent.remove(dayItem)
                                    loc_contentJson.remove(dayItem)
                                }
                            } else {
                                rmItem = dayItem
                                fillUP = 1
                            }
                        } else {
                            if (rmItem.isNotEmpty()) {
                                if (!loc_contentJson.isNull(it) && !cloudContent.isNull(it)) {
                                    cloudContent.put(rmItem, cloudContent.get(it))
                                    loc_contentJson.put(rmItem, loc_contentJson.get(it))
                                }
                                rmItem = it
                            }
                        }
                        dayIndex += 1

                    }
                    if (dayItems.count() > 1) {
                        val lastDay = dayItems[dayItems.count() - 1]
                        dayItems.remove(lastDay)
                        cloudContent.remove(lastDay)
                        loc_contentJson.remove(lastDay)
                    }
                    usingTripPlan!!.cloudContent = cloudContent.toString()
                    usingTripPlan!!.contentJsonFormat = loc_contentJson.toString()
                    val tpf = TripPlanning_Fragment()
                    tpf.delDay_onCloud(context!!, useremail!!, usingTripPlan!!, dayItem)
                    if (debugmode) Log.i("PlanningListDaysRC", " update DB : ${usingTripPlan!!.id}")

                    if (tripPlanDB!!.update(usingTripPlan!!)) {
                        if (debugmode) Log.i("PlanningListDaysRC" , "day_del.setOnClickListener: update tripPlanDB success")
                    }
                    clickListener(dayItem, 1)
                    //jsonArray.put(pp_json.toString())//.join(pp_json.toString())


 */
                }
            }
        }
    }
}