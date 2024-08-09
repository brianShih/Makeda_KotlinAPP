package tw.breadcrumbs.makeda.TripPlanning

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.opengl.Visibility
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.invite_user_layout.*
import kotlinx.android.synthetic.main.planning_helper.*
import kotlinx.android.synthetic.main.planning_helper.planning_invite_option
import org.json.JSONArray
import org.json.JSONObject
import tw.breadcrumbs.makeda.DBModules.TripPlanDAO
import tw.breadcrumbs.makeda.dataModel.PPModel
import tw.breadcrumbs.makeda.dataModel.TripListModel
import tw.breadcrumbs.makeda.*
import tw.breadcrumbs.makeda.CloudObj.Cloud_Helper
import tw.breadcrumbs.makeda.DBModules.OpenTripPlanDB_PPModelDAO
import tw.breadcrumbs.makeda.DBModules.TripPlanDB_PPModelDAO
import tw.breadcrumbs.makeda.dataModel.TripPlan_PPModel
import java.io.File.separator
import java.net.URLEncoder

//import java.lang.reflect.Array


class PlanningHelper : androidx.fragment.app.Fragment() {
    private val debugmode = false
    var editMode:Boolean = false
    var day_index: Int = 0
    val daysLimit = 50
    var input_tripListPPList : MutableList<TripPlan_PPModel>? = null
    var pH_List: TripListModel? = null
    var ppList : MutableList<TripPlan_PPModel> ? = null
    var daysList : MutableList<String> ? = null
    var undefList : MutableList<TripPlan_PPModel> ? = null
    var planningUndeflistrc : PlanningUndefListRC? = null
    var planningDayListrc : PlanningListDaysRC? = null
    var planninglistrc : PlanningListRC? = null
    var planningContents:JSONObject? = null
    var tripPlanDB:TripPlanDAO? = null
    var tripPlanDB_PPModelDAO: TripPlanDB_PPModelDAO? = null
    var open_tripplanDB_PPModelDAO: OpenTripPlanDB_PPModelDAO? = null
    var set_frag: Setting_Fragment? = null
    var useremail: String? = ""
    private var undefListCalHelper: calcuteDistanceHelper? = null
    private var dayPPListCalHelper: calcuteDistanceHelper? = null
    var usingMode = 0

    override fun onStop() {
        super.onStop()
        //if (tripPlanDB_PPModelDAO != null)
        //    tripPlanDB_PPModelDAO!!.close()
        //if (tripPlanDB != null)
        //    tripPlanDB!!.close()
    }

    override fun onResume() {
        super.onResume()
        if (pH_List == null) {
            val dash_f = (activity as MainActivity).dash_frag as Dash_Fragment
            (activity as MainActivity).switchFragment(this@PlanningHelper, dash_f, 2)
        }
    }

    override fun onStart() {
        super.onStart()
        if (pH_List == null) {
            val dash_f = (activity as MainActivity).dash_frag as Dash_Fragment
            (activity as MainActivity).switchFragment(this@PlanningHelper, dash_f, 2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.planning_helper, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (pH_List != null) {
            tripPlanDB = TripPlanDAO(this@PlanningHelper.context!!)
            if (usingMode == 0)
                tripPlanDB_PPModelDAO = TripPlanDB_PPModelDAO(this@PlanningHelper.context!!)
            //else
            //    open_tripplanDB_PPModelDAO = OpenTripPlanDB_PPModelDAO(this@PlanningHelper.context!!)
            back_button_setup()
            pH_List.let {
                if (it?.planname.isNullOrEmpty()) {
                    val dash_f = (activity as MainActivity).dash_frag as Dash_Fragment
                    (activity as MainActivity).switchFragment(this@PlanningHelper, dash_f, 2)
                }
                planning_name?.text = it?.planname
            }
            edit_button_setup()
            //pre_sync()
            if (set_frag == null) {
                set_frag = (activity as MainActivity).setting_frag as Setting_Fragment
                if (set_frag!!.local_setting_isReady())
                    useremail = set_frag!!.prefs!!.getString(set_frag!!.user_email_pref, " ")
            }

            DaysRecyclerViewLoad(editMode, day_index)
            UndefineRecyclerViewLoad(editMode)
            if (daysList!!.count() == 0) {
                RecyclerViewLoad("DAY 1", editMode)
            } else {
                RecyclerViewLoad(daysList!!.get(day_index), editMode)
            }

            add_day_button_setup()
            planning_sort_option.setOnClickListener {
                Toast.makeText(context, R.string.tripPlan_sort_zh, Toast.LENGTH_LONG).show()
                sortStart()
            }
            invite_button_setup()
        } else {
            val dash_f = (activity as MainActivity).dash_frag as Dash_Fragment
            (activity as MainActivity).switchFragment(this@PlanningHelper, dash_f, 2)
        }
    }

    fun setTripListModel(list : TripListModel) {
        pH_List = list
    }

    fun set_usingMode(mode : Int) {
        usingMode = mode
    }

    fun add_pp_intoKEYDAY(context: Context, loc_usermail: String, listID:Long, item:String, add_multiPP: MutableList<PPModel>, upMode: Int) {
        /*
        if (debugmode) Log.i("PlanningHelper", "add_pp_intoKEYDAY - item = $item, add_multiPP = $add_multiPP")
        if (add_multiPP.count() <= 0) return
        if (tripPlanDB == null) {
            tripPlanDB = TripPlanDAO(context)
        }
        val tripPlanList = tripPlanDB!!.get(listID)
        var jsonArray:JSONArray? = null

        if (tripPlanList!!.contentJsonFormat.isEmpty()) {
            planningContents = JSONObject()
            jsonArray = JSONArray()
            planningContents!!.put(item, jsonArray)
        } else {
            planningContents = JSONObject(tripPlanList.contentJsonFormat)

            if (planningContents!!.isNull(item)) {
                jsonArray = JSONArray()
            } else {
                val tripPlanningUndefList = planningContents!!.get(item)
                if (tripPlanningUndefList is JSONArray && tripPlanningUndefList.toString().isNotEmpty())
                    jsonArray =  JSONArray(tripPlanningUndefList.toString())
                else jsonArray = JSONArray()
            }
        }
        add_multiPP.forEach{
            if (debugmode) Log.i("PlanningHelper", "add_pp_intoKEYDAY . add_multiPP add PPModel = $it")
            val pp_json = JSONObject()
            pp_json.put("id", it.id)
            pp_json.put("cloudID", it.cloudID)
            pp_json.put("name", it.name)
            pp_json.put("phone", it.phone)
            pp_json.put("country", it.country)
            pp_json.put("addr", it.addr)
            pp_json.put("fb", it.fb)
            pp_json.put("web", it.web)
            pp_json.put("blogInfo", it.blogInfo)
            pp_json.put("opentime", it.opentime)
            pp_json.put("tag_note", it.tag_note)
            pp_json.put("descrip", it.descrip)
            pp_json.put("distance", 0)
            var same = 0
            for (i in 0..jsonArray.length() - 1) {
                val pp = jsonArray.get(i) as JSONObject
                if (pp.get("cloudID").toString() == it.cloudID.toString() )
                    same = 1
            }
            if (same == 0)
                jsonArray.put(pp_json)
        }
        if (planningContents!!.isNull("DAY 1")) {
            val temp: JSONArray = JSONArray()
            planningContents!!.put("DAY 1", temp)
        }
        planningContents!!.put(item, jsonArray)
        tripPlanList.contentJsonFormat = planningContents.toString()

        if (upMode == 1) {
            val tpf = TripPlanning_Fragment()
            add_multiPP.forEach {

                tpf.addPP_toCloud(
                    this@PlanningHelper.context!!,
                    loc_usermail,
                    tripPlanList,
                    item,
                    it.cloudID.toString()
                )
            }
        }

        if(debugmode) Log.i("PlanningHelper", " add $item pp into content: ${tripPlanList.contentJsonFormat}")
        //Toast.makeText(context, "Add Undefine PP into [${tripPlanList.planname}] list", Toast.LENGTH_LONG).show()
        tripPlanDB!!.update(tripPlanList)
        */
    }

    fun add_pp_into(context: Context, listID:Long, add_pp: PPModel, item: String) {
        if (usingMode == 1) {
            Toast.makeText(context, "無權限編輯", Toast.LENGTH_LONG).show()
            return
        }
        if (tripPlanDB_PPModelDAO == null) {
            tripPlanDB_PPModelDAO = TripPlanDB_PPModelDAO(context)
        }
        if (tripPlanDB == null) {
            tripPlanDB = TripPlanDAO(context)
        }
        var insertID : Long = 0
        val tripPlanList = tripPlanDB!!.get(listID)
        //if (tripPlanList!!.tripplanID.toInt() == 0) {
        if (tripPlanList!!.tripplanID.equals("")) {
            insertID = tripPlanList.id
        } else {
            insertID = tripPlanList.tripplanID.toLong()
        }
        //val tripPlan_PPModel = tripPlanDB_PPModelDAO!!.findTripPlan(tripPlanList!!.tripplanID.toLong())
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
            add_pp.calDistanceDone,
            insertID,
            item,
            0
        )
        tripPlanDB_PPModelDAO!!.insert(tripPlan_PPModel)
        //tripPlanDB_PPModelDAO!!.close()

        /*
        if (tripPlanDB == null) {
            tripPlanDB = TripPlanDAO(context)
        }
        val tripPlanList = tripPlanDB!!.get(listID)
        var cloudContent: JSONObject? = null
        var localContent: JSONObject? = null

        val pp_json = JSONObject()
        pp_json.put("id", add_pp.id)
        pp_json.put("cloudID", add_pp.cloudID)
        pp_json.put("name", add_pp.name)
        pp_json.put("phone", add_pp.phone)
        pp_json.put("country", add_pp.country)
        pp_json.put("addr", add_pp.addr)
        pp_json.put("fb", add_pp.fb)
        pp_json.put("web", add_pp.web)
        pp_json.put("blogInfo", add_pp.blogInfo)
        pp_json.put("opentime", add_pp.opentime)
        pp_json.put("tag_note", add_pp.tag_note)
        pp_json.put("descrip", add_pp.descrip)
        pp_json.put("distance", 0)

        var jsonArray: JSONArray? = null
        var cloudJsonArr: JSONArray? = null
        if (tripPlanList!!.cloudContent.isEmpty()) {
            cloudContent = JSONObject()
            cloudJsonArr = JSONArray()
            cloudContent.put("DAY 1", JSONArray())
        } else {
            cloudContent = JSONObject(tripPlanList.cloudContent)
            cloudJsonArr = JSONArray()
            if (!cloudContent.isNull("UNDEF")) {
                val tripPlanCL_UndefList = cloudContent.get("UNDEF")
                if (tripPlanCL_UndefList is JSONArray && tripPlanCL_UndefList.toString().isNotEmpty())
                    cloudJsonArr =  JSONArray(tripPlanCL_UndefList.toString())
                else cloudJsonArr = JSONArray()
            }
        }

        cloudJsonArr.put(add_pp.cloudID.toString())
        cloudContent.put("UNDEF", cloudJsonArr)

        if (tripPlanList.contentJsonFormat.isEmpty()) {
            daysList = mutableListOf()
            daysList!!.add("DAY 1")
            localContent = JSONObject()
            jsonArray = JSONArray()
            localContent.put("DAY 1", JSONArray())
        } else {
            localContent = JSONObject(tripPlanList.contentJsonFormat)
            if (localContent.isNull("UNDEF")) {
                jsonArray = JSONArray()
            } else {
                val tripPlanningUndefList = localContent.get("UNDEF")
                if (tripPlanningUndefList is JSONArray && tripPlanningUndefList.toString().isNotEmpty())
                    jsonArray =  JSONArray(tripPlanningUndefList.toString())
                else jsonArray = JSONArray()
            }
        }
        jsonArray.put(pp_json)//.join(pp_json.toString())
        localContent.put("UNDEF", jsonArray)
        tripPlanList.cloudContent = cloudContent.toString()
        tripPlanList.contentJsonFormat = localContent.toString()
        if(debugmode) Log.i("PlanningHelper", "add_pp_into - tripPlanList : $tripPlanList, pp cloudID: ${add_pp.cloudID.toString()}")
        //val cloudIDstr = add_pp.cloudID.toString()
        //val item = "UNDEF"
        //pH_List = tripPlanList
        //
        if(debugmode) Log.i("PlanningHelper", "add_pp_into - add Undefine pp into content: ${tripPlanList.contentJsonFormat}")
        //Toast.makeText(context, "Add Undefine PP into [${pH_List!!.planname}] list", Toast.LENGTH_LONG).show()
        tripPlanDB!!.update(tripPlanList)
         */
    }

    private fun edit_button_setup() {
        if (usingMode == 0) {
            planning_edit_option.visibility = View.VISIBLE
            planning_edit_option.setOnClickListener {
                if (usingMode == 1) {
                    Toast.makeText(context, "無權限編輯", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                var image: Drawable? = null
                editMode = !editMode
                if (editMode) {
                    //planning_edit_option.setImageDrawable()
                    image = ContextCompat.getDrawable(context!!, R.drawable.ok_icon)
                    planningDayListrc!!.set_TripPlan(pH_List!!)
                    planningUndeflistrc!!.set_TripPlan(pH_List!!)
                } else {
                    image = ContextCompat.getDrawable(context!!, R.drawable.edite_icon)
                }
                planning_edit_option.setImageDrawable(image)
                planningDayListrc!!.setEditMode(editMode)
                planningDayListrc!!.notifyDataSetChanged()
                //DaysRecyclerViewLoad(editMode, day_index)
            }
        } else {
            planning_edit_option.visibility = View.INVISIBLE
        }
    }

    private fun add_day_button_setup() {
        if (usingMode == 0) {
            add_day.visibility = View.VISIBLE
            add_day.setOnClickListener {
                if (usingMode == 1) {
                    Toast.makeText(context, "無權限編輯", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                val tripPlan = tripPlanDB!!.get(pH_List!!.id)
                val cnt = daysList!!.count()
                val dayStr: String = "DAY ${cnt + 1}"
                daysList!!.add(dayStr)
                addDayOption(this@PlanningHelper.context!!, tripPlan!!, dayStr)
                if (!(tripPlan.tripplanID.equals("") || tripPlan.tripplanID.isEmpty())) {
                    val tpf = TripPlanning_Fragment()
                    tpf.addDay_onCloud(this@PlanningHelper.context!!, useremail!!, tripPlan, dayStr)
                }
                planningUndeflistrc!!.setDaysList(daysList!!)
                planningUndeflistrc!!.notifyDataSetChanged()
                planninglistrc!!.setDaysList(daysList!!)
                planninglistrc!!.notifyDataSetChanged()
                planningDayListrc!!.notifyDataSetChanged()
            }
        } else {
            add_day.visibility = View.INVISIBLE
        }
    }

    fun back_button_setup() {
        planningBackButton.setOnClickListener {
            (activity as MainActivity).onBackPressed()
        }
    }

    fun invite_button_setup() {
        if (usingMode == 0) {
            planning_invite_option.visibility = View.VISIBLE
            planning_invite_option.setOnClickListener {
                if (usingMode == 1) {
                    Toast.makeText(context, "無權限編輯", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                val orgGroupListSel: ArrayList<String> = arrayListOf()
                val orgGroupList: ArrayList<String> =
                    arrayListOf()//"brianshih112@gmail.com", "coolnet119@gmail.com")
                val tripPlanList = tripPlanDB!!.findName(pH_List!!.planname)
                if (tripPlanList!!.grouplist.isNotEmpty()) {
                    val org = tripPlanList.grouplist.split(",").map { it }
                    org.forEach {
                        orgGroupList.add(it)
                    }
                }

                val array = arrayOfNulls<String>(orgGroupList.size)
                val dailogView = LayoutInflater.from(this@PlanningHelper.context)
                    .inflate(R.layout.invite_user_layout, null)
                val inviteBtn = dailogView.findViewById(R.id.invite_button) as Button
                val inviteText = dailogView.findViewById(R.id.invite_editeText) as EditText
                val delBtn = dailogView.findViewById(R.id.removeInviteUser) as ImageButton

                if (debugmode) Log.i(
                    "PlanningHelper",
                    "invite_button_setup - groupList : $orgGroupList"
                )

                val dialog: AlertDialog =
                    AlertDialog.Builder(this@PlanningHelper.context, R.style.AlertDialogStyle)
                        .setTitle(R.string.trip_groupListTitle_zh)
                        .setView(dailogView)
                        .setMultiChoiceItems(
                            orgGroupList.toArray(array),
                            null
                        ) { dialog, which, isChecked ->
                            if (isChecked) {
                                orgGroupListSel.add(orgGroupList[which])
                            } else {
                                orgGroupListSel.remove(orgGroupList[which])
                            }
                        }
                        .create()

                if (orgGroupList.count() == 0) {
                    delBtn.visibility = View.INVISIBLE
                    delBtn.isEnabled = false
                }
                dialog.show()
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)

                inviteBtn.setOnClickListener {
                    val userEmail = inviteText.text.toString()
                    orgGroupList.add(userEmail)
                    var cnt = 0
                    var groupList_str = ""
                    orgGroupList.forEach {
                        if (cnt > 0) groupList_str += ","
                        groupList_str += it
                        cnt += 1
                    }
                    //val groupList_str = orgGroupList.joinToString(",", "", "")
                    tripPlanList.grouplist = groupList_str
                    tripPlanDB!!.update(tripPlanList)
                    val tpf = TripPlanning_Fragment()
                    tpf.inviteUser_onCloud(
                        this@PlanningHelper.context!!,
                        useremail!!,
                        tripPlanList,
                        userEmail
                    )
                    dialog.dismiss()
                }
                delBtn.setOnClickListener {
                    if (orgGroupListSel.count() > 0) {
                        orgGroupListSel.forEach {
                            orgGroupList.remove(it)
                            val tpf = TripPlanning_Fragment()
                            tpf.deleteUser_onCloud(
                                this@PlanningHelper.context!!,
                                useremail!!,
                                tripPlanList,
                                it
                            )
                        }
                        if (orgGroupList.count() > 0) {
                            var cnt = 0
                            var groupList_str = ""
                            orgGroupList.forEach {
                                if (cnt > 0) groupList_str += ","
                                groupList_str += it
                                cnt += 1
                            }
                            tripPlanList.grouplist = groupList_str
                            //orgGroupList.joinToString { "," }//joined(separator:",");
                        } else {
                            tripPlanList.grouplist = ""
                        }
                        tripPlanDB!!.update(tripPlanList)
                    }
                    dialog.dismiss()
                }
            }
        } else {
            planning_invite_option.visibility = View.INVISIBLE
        }
    }

    fun alertDialogBuildInit(list: ArrayList<String>, dailogView: View, listener: DialogInterface.OnMultiChoiceClickListener): AlertDialog {
        val array = arrayOfNulls<String>(list.size)
        val build = AlertDialog.Builder(this@PlanningHelper.context, R.style.AlertDialogStyle)
            .setTitle(R.string.trip_groupListTitle_zh)
            .setView(dailogView)
            .setMultiChoiceItems(list.toArray(array), null, listener)
            .create()
        return build
    }

    fun addDayOption(context: Context, in_tripplan: TripListModel, dayString: String) {
        if (usingMode == 1) {
            Toast.makeText(context, "無權限編輯", Toast.LENGTH_LONG).show()
            return
        }
        if (tripPlanDB == null) tripPlanDB = TripPlanDAO(context)
        var cloudContent : JSONObject? = null
        val tripPlan = tripPlanDB!!.get(in_tripplan.id)

        if (tripPlan!!.cloudContent.isNotEmpty()) {
            cloudContent = JSONObject(tripPlan.cloudContent)
        } else {
            cloudContent = JSONObject()
            cloudContent.put("DAY 1", JSONArray())
        }

        val jsonArray = JSONArray()
        cloudContent.put(dayString, jsonArray)
        tripPlan.cloudContent = cloudContent.toString()
        if (tripPlanDB!!.update(tripPlan)) {
            if(debugmode) Log.i("PlanningHelper" , "add_day_button_setup: add ${dayString} into tripPlanDB success")
            if(debugmode) Log.i("PlanningHelper" , "add_day_button_setup: cloud Content = ${tripPlan.cloudContent}")
        }
    }

    fun setdayIndex(index:Int) {
        day_index = index
    }

    fun setdayslist(days_list:MutableList<String>) {
        daysList = mutableListOf()
        daysList = days_list
    }

    fun DaysRecyclerViewLoad(editMode:Boolean, selIndex: Int) {
        sortFinish()
        var tripPlan : TripListModel? = null
        daysList = mutableListOf()
        if (usingMode == 0) {
            if (tripPlanDB_PPModelDAO == null) {
                tripPlanDB_PPModelDAO = TripPlanDB_PPModelDAO(this@PlanningHelper.context!!)
            }
        } //else {
            //if (open_tripplanDB_PPModelDAO == null) open_tripplanDB_PPModelDAO = OpenTripPlanDB_PPModelDAO(this@PlanningHelper.context!!)
        //}

        if (tripPlanDB == null) {
            tripPlanDB = TripPlanDAO(this@PlanningHelper.context!!)
        }
        if (usingMode == 0) {
            var readID: Long = 0
            tripPlan = tripPlanDB!!.get(pH_List!!.id)
            if (tripPlan!!.tripplanID.equals("")) {
                readID = tripPlan.id
            } else {
                readID = tripPlan.tripplanID.toLong()
            }
        } else {
            tripPlan = pH_List
        }
        if (!(tripPlan!!.cloudContent.equals("") || tripPlan.cloudContent.isEmpty())) {
            val dayJsonObj = JSONObject(tripPlan.cloudContent)
            var nullCnt = 0
            for (i in 1..daysLimit) {
                val dayHead = "DAY $i"
                if (dayJsonObj.isNull(dayHead)) {
                    if (nullCnt > 5) break
                    nullCnt ++
                } else {
                    if (nullCnt > 2) {
                        for (c in (i - nullCnt)..i) {
                            val str = "DAY $c"
                            daysList!!.add(str)
                        }
                    }
                    daysList!!.add(dayHead)
                }

            }
        }
        /*
        val dayArr= tripPlanDB_PPModelDAO!!.getItems(readID)
        //var nullCnt = 0
        if (dayArr!!.count() > 0) {
            dayArr.forEach {
                if (!it.equals("UNDEF")) {
                    var same = 0
                    daysList!!.forEach { dayItem ->
                        if (dayItem.equals(it)) same = 1
                    }
                    if (same == 0) {
                        daysList!!.add(it)
                    }
                }
            }
        }
         */
        if (daysList!!.count() == 0) daysList!!.add("DAY 1")

/*
        if (tripPlan.cloudContent.isEmpty()) {
            val dayJsonObj = JSONObject()
            dayJsonObj.put("DAY 1", JSONArray())
            tripPlan.cloudContent = dayJsonObj.toString()
            tripPlanDB!!.update(tripPlan)
        } else {
            val dayJsonObj = JSONObject(tripPlan.cloudContent)
            for (t in 1..(daysList!!.count()+10)) {
                var same = 0
                if (!dayJsonObj.isNull("DAY $t")) {
                    daysList!!.forEach {
                        if (it.equals("DAY $t")) same = 1
                    }
                    if (same == 0) {
                        daysList!!.add("DAY $t")
                    }
                }
            }
        } */


/*
        //pH_List = tripPlanDB!!.findName("")
        if (!pH_List!!.cloudContent.isEmpty()) {
            planningContents = JSONObject(pH_List!!.cloudContent)
            var nullCnt = 0
            for (i in 1..daysLimit) {
                if (planningContents!!.isNull("DAY ${i}")) {
                    if (i == 0) {
                        daysList!!.add("DAY 1")
                    }
                    if (nullCnt > 5) break
                    nullCnt ++
                    continue
                } else {
                    for ( j in (i - nullCnt) .. (i - 1) ) {
                        daysList!!.add("DAY ${j}")
                    }
                }
                if(debugmode) Log.i("PlanningHelper", "DaysRecyclerViewLoad - DAY $i added")
                daysList!!.add("DAY ${i}")
            }
        } else {
            // db content is null, init...
            daysList!!.add("DAY 1")
            val jsonArray = JSONArray()
            planningContents = JSONObject()
            planningContents!!.put("DAY 1", jsonArray)
            pH_List!!.contentJsonFormat = planningContents.toString()
            tripPlanDB!!.update(pH_List!!)
        }
         */
        //if (daysList!!.count() == 0) daysList!!.add("DAY 1")
        if (tripplanDayTitleRC != null) {
            tripplanDayTitleRC.layoutManager =
                androidx.recyclerview.widget.LinearLayoutManager(
                    this@PlanningHelper.context!!,
                    androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL,
                    false
                )
            planningDayListrc = PlanningListDaysRC(
                dayItems = daysList!!,
                clickListener = { partItem: String, mode: Int ->
                    dayClick(
                        partItem, mode
                    )
                })
            var index = selIndex
            if (daysList!!.count() in 1..selIndex) {
                index = daysList!!.count() - 1
            }
            day_index = index
            planningDayListrc!!.setDayIndex(day_index)
            planningDayListrc!!.setEditMode(editMode)
            planningDayListrc!!.set_TripPlanDB(tripPlanDB!!)
            planningDayListrc!!.set_TripPlan(tripPlan)
            planningDayListrc!!.set_userEmail(useremail!!)
            tripplanDayTitleRC.adapter = planningDayListrc
            if(debugmode) Log.i("PlanningHelper", "DaysRecyclerViewLoad - daysList = $daysList, day_index = $day_index , selIndex = $selIndex")
            if(debugmode) Log.i("PlanningHelper", "DaysRecyclerViewLoad - cloudContent = ${tripPlan.cloudContent}")
            splite_line1.text = daysList!![day_index]
        }
    }

    fun RecyclerViewLoad(day: String, editMode:Boolean) {
        var readID: Long = 0
        var ppModel: PPModel? = null
        var tripPlanModel : TripListModel? = null
        var tripPlanPPList : List<TripPlan_PPModel>? = null

        sortFinish()
        if(debugmode) Log.i("PlanningHelper", "RecyclerViewLoad : day : $day View Load.")
        // update pH_List

        if (tripPlanDB_PPModelDAO == null) {
            tripPlanDB_PPModelDAO = TripPlanDB_PPModelDAO(this@PlanningHelper.context!!)
        }

        if (tripPlanDB == null) {
            tripPlanDB = TripPlanDAO(this@PlanningHelper.context!!)
        }
        ppList = mutableListOf()
        if (usingMode == 0) {
            tripPlanModel = tripPlanDB!!.get(pH_List!!.id)//findName(pH_List!!.planname)
            //if (tripPlanModel!!.tripplanID.toInt() == 0) {
            if (tripPlanModel!!.tripplanID.equals("")) {
                readID = tripPlanModel.id
            } else {
                readID = tripPlanModel.tripplanID.toLong()
            }
            tripPlanPPList = tripPlanDB_PPModelDAO!!.getItemX(readID, day)
        } else {
            tripPlanModel = pH_List
            readID = pH_List!!.tripplanID.toLong()
            //tripPlanPPList = open_tripplanDB_PPModelDAO!!.getItemX(readID, day)
            val temp: MutableList<TripPlan_PPModel> = mutableListOf()
            input_tripListPPList?.forEach {
                if (it.setTripPlanItem.equals(day) && it.tripPlanID.equals(readID)) {
                    temp.add(it)
                }
            }
            tripPlanPPList = temp
        }

        tripPlanPPList!!.forEach {  fromList ->
            var same = 0
            ppList?.forEach {
                if (it.cloudID.equals(fromList.cloudID)) same = 1
            }
            if (same == 0) {
                ppList!!.add(fromList)
            }
        }
        //tripPlanDB_PPModelDAO!!.close()
        /*
        var localContents: JSONObject? = null


        if (tripPlanModel!!.contentJsonFormat.isNotEmpty() && tripPlanModel.contentJsonFormat.length > 0)
        {
            localContents = JSONObject(tripPlanModel.contentJsonFormat)
            if (!localContents.isNull(day)) {
                if(debugmode) Log.i("PlanningHelper", "RecyclerViewLoad : day : $day Init...")
                if(debugmode) Log.i("PlanningHelper", "RecyclerViewLoad : day : $day ,localContents : ${localContents.get(day)}")
                val daypplistStr = localContents.get(day)
                if (daypplistStr is JSONObject) {
                    val pp : JSONObject= daypplistStr as JSONObject
                    val ppID = pp.get("id") as Int
                    val cloudID = pp.get("cloudID") as Int
                    ppModel = PPModel(
                        ppID.toLong(), cloudID.toLong(), pp.get("name").toString(),
                        pp.get("phone").toString(), pp.get("country").toString(), pp.get("addr").toString(),
                        pp.get("fb").toString(), pp.get("web").toString(), pp.get("blogInfo").toString(),
                        pp.get("opentime").toString(), pp.get("tag_note").toString(), pp.get("descrip").toString(),
                        pp.get("distance").toString()
                    )
                    if (debugmode) Log.i("PlanningHelper", "RecyclerViewLoad [${ppModel.name}] JSONObject insert to ppList")
                    ppList!!.add(ppModel)

                } else if (daypplistStr is JSONArray){
                    val ppArray = JSONArray(daypplistStr.toString())
                    for (i in 0..ppArray.length()-1) {
                        val ppStr = ppArray.get(i).toString()
                        val pp = JSONObject(ppStr)
                        val ppID = pp.get("id") as Int
                        val cloudID = pp.get("cloudID") as Int
                        ppModel = PPModel(
                            ppID.toLong(), cloudID.toLong(), pp.get("name").toString(),
                            pp.get("phone").toString(), pp.get("country").toString(), pp.get("addr").toString(),
                            pp.get("fb").toString(), pp.get("web").toString(), pp.get("blogInfo").toString(),
                            pp.get("opentime").toString(), pp.get("tag_note").toString(), pp.get("descrip").toString(),
                            pp.get("distance").toString()
                        )
                        if (debugmode) Log.i("PlanningHelper", "RecyclerViewLoad [${ppModel.name}]JSONArray insert to ppList")
                        ppList!!.add(ppModel)
                    }
                }

            } else {
                Log.i ("PlanningHelper", "planningContents!!.isNull(day): pH_List!!.contentJsonFormat = ${tripPlanModel.contentJsonFormat}")
            }
        }

         */
        if (tripplanhelperRecyclerView != null) {
            tripplanhelperRecyclerView.layoutManager = object : androidx.recyclerview.widget.LinearLayoutManager(context) {
                override fun canScrollVertically() = false
            }

            planninglistrc = PlanningListRC(
                feedModelItems = ppList!!,
                clickListener = { item: String, partItem: TripPlan_PPModel, mode: Int ->
                    rv_click(
                        item, partItem, mode
                    )
                })
            planninglistrc!!.mode = usingMode
            planninglistrc!!.set_userEamil(useremail!!)
            planninglistrc!!.setDaysList(daysList!!)
            planninglistrc!!.setDayIndex(day_index)
            planninglistrc!!.set_TripPlanDB(tripPlanDB!!)
            planninglistrc!!.set_TripPlan(tripPlanModel!!)
            tripplanhelperRecyclerView.adapter = planninglistrc
        }
    }

    fun UndefineRecyclerViewLoad(editMode:Boolean) {
        sortFinish()
        var tripPlanPPList: List<TripPlan_PPModel>? = null
        var tripPlanModel : TripListModel? = null
        // undefine list init...
        undefList = mutableListOf()
        if (usingMode == 0) {
            if (tripPlanDB_PPModelDAO == null) {
                tripPlanDB_PPModelDAO = TripPlanDB_PPModelDAO(this@PlanningHelper.context!!)
            }
        }// else {
            //open_tripplanDB_PPModelDAO = OpenTripPlanDB_PPModelDAO(this@PlanningHelper.context!!)
        //}
        if (tripPlanDB == null) {
            tripPlanDB = TripPlanDAO(this@PlanningHelper.context!!)
        }
        var readID : Long = 0
        if (usingMode == 0)
            tripPlanModel = tripPlanDB!!.get(pH_List!!.id)
        else
            tripPlanModel = pH_List
        //if (tripPlanModel!!.tripplanID.toInt() == 0) {
        if (tripPlanModel!!.tripplanID.equals("")) {
            readID = tripPlanModel.id
        } else {
            readID = tripPlanModel.tripplanID.toLong()
        }
        if (usingMode == 0) {
            tripPlanPPList = tripPlanDB_PPModelDAO!!.getItemX(readID, "UNDEF")
        } else {
            //tripPlanPPList = open_tripplanDB_PPModelDAO!!.getItemX(readID, "UNDEF")
            val temp : MutableList<TripPlan_PPModel> = mutableListOf()
            input_tripListPPList?.forEach{
                if (it.setTripPlanItem.equals("UNDEF") && it.tripPlanID.equals(readID))
                    temp.add(it)
            }
            tripPlanPPList = temp
        }
        tripPlanPPList!!.forEach { fromList ->
            var same = 0
            undefList?.forEach {
                if (it.cloudID.equals(fromList.cloudID)) same = 1
            }
            if (same == 0)
                undefList!!.add(fromList)
        }
        //tripPlanDB_PPModelDAO!!.close()
        /*
                pH_List = tripPlanDB!!.findName(pH_List!!.planname)
        if (!pH_List!!.contentJsonFormat.isEmpty()) {
            planningContents = JSONObject(pH_List!!.contentJsonFormat)
        }
        if (!pH_List!!.contentJsonFormat.isEmpty() && !planningContents!!.isNull("UNDEF")) {
            val tripPlanningUndefList = planningContents!!.get("UNDEF")
            var jsonArray : JSONArray? = null
            if (tripPlanningUndefList is JSONArray && tripPlanningUndefList.toString().isNotEmpty())
                jsonArray = JSONArray(tripPlanningUndefList.toString())
            else jsonArray = JSONArray()
            if(debugmode) Log.i("PlanningHelper", "Undefine list = $tripPlanningUndefList")
            for (i in 0 .. jsonArray.length()) {
                if (jsonArray.isNull(i)) {
                    break
                }
                val ppStr = jsonArray.get(i).toString()
                val pp = JSONObject(ppStr)
                val ppID = pp.get("id") as Int
                val cloudID = pp.get("cloudID") as Int
                val ppModel = PPModel(ppID.toLong(), cloudID.toLong(),pp.get("name").toString(),
                    pp.get("phone").toString(), pp.get("country").toString(), pp.get("addr").toString(),
                    pp.get("fb").toString(), pp.get("web").toString(), pp.get("blogInfo").toString(),
                    pp.get("opentime").toString(), pp.get("tag_note").toString(), pp.get("descrip").toString(),
                    pp.get("distance").toString())
                if(debugmode) Log.i("PlanningHelper", "add pp [${ppModel.name}] into list")
                undefList!!.add(ppModel)
            }
        } */
        if (tripplanhelperUndefRC != null) {
            tripplanhelperUndefRC.layoutManager = object : androidx.recyclerview.widget.LinearLayoutManager(context) {
                //解决RecyclerView嵌套RecyclerView滑动卡顿的问题
                //如果你的RecyclerView是水平滑动的话可以重写canScrollHorizontally方法
                override fun canScrollVertically() = false
            }
                    //LinearLayoutManager(this@PlanningHelper.context!!)

            planningUndeflistrc = PlanningUndefListRC(
                feedModelItems = undefList!!,
                clickListener = { day: String, partItem: TripPlan_PPModel, mode: Int ->
                    undef_click(
                        day, partItem, mode
                    )
                })
            planningUndeflistrc!!.mode = usingMode
            planningUndeflistrc!!.set_TripPlan(pH_List!!)
            planningUndeflistrc!!.setDaysList(daysList!!)
            planningUndeflistrc!!.set_TripPlanDB(tripPlanDB!!)
            planningUndeflistrc!!.set_userEamil(useremail!!)//set_settingFrag(set_frag!!)
            tripplanhelperUndefRC.adapter = planningUndeflistrc

            //tripplanhelperRecyclerView.isNestedScrollingEnabled = false
            //tripplanhelperRecyclerView.setHasFixedSize(true)
            //tripplanhelperRecyclerView.isFocusable = false
        }
    }

    fun sortStart() {
        //private var undefListCalHelper: calcuteDistanceHelper? = null
        //private var dayPPListCalHelper: calcuteDistanceHelper? = null
        if (undefListCalHelper == null) {
            undefListCalHelper = calcuteDistanceHelper(activity as MainActivity,
                this@PlanningHelper.context!!) {
                //Log.i("Dash_Fragment", "calDistanceTaskInit sort feeback!!")
                if (it != null) {
                    undefList = it
                    undefList!!.sortBy {
                        it.distance
                    }
                    //it.sortBy { it.distance }
                    //global_pp_list!!.sortBy { it.distance }
                    //displayList!!.forEach( )
                    //global_pp_list = it
                    planningUndeflistrc!!.notifyDataSetChanged()
                    //recycleViewInit()
                }
            }
            undefListCalHelper!!.execute(undefList)
        }

        if (dayPPListCalHelper == null) {
            dayPPListCalHelper = calcuteDistanceHelper(activity as MainActivity,
                this@PlanningHelper.context!!) {
                //Log.i("Dash_Fragment", "calDistanceTaskInit sort feeback!!")
                if (it != null) {
                    ppList = it
                    ppList!!.sortBy { it.distance }
                    planninglistrc!!.notifyDataSetChanged()
                }
            }
            dayPPListCalHelper!!.execute(ppList)
        }
    }

    fun sortFinish() {
        if (undefListCalHelper != null) {
            //if (dashCalHelper!!.status == AsyncTask.Status.RUNNING) {
            undefListCalHelper!!.cancel(true)
            undefListCalHelper = null
        }

        if (dayPPListCalHelper != null) {
            //if (dashCalHelper!!.status == AsyncTask.Status.RUNNING) {
            dayPPListCalHelper!!.cancel(true)
            dayPPListCalHelper = null
        }
    }

    fun dayClick(item: String, mode: Int) {
        var sel = 0
        daysList!!.forEach{
            if (it == item) {
                day_index = sel
                return@forEach
            } else sel += 1
        }
        if (day_index >= daysList!!.count()) day_index = daysList!!.count() - 1

        pH_List = planningDayListrc!!.usingTripPlan
        daysList = planningDayListrc!!.getDaysList()
        planningDayListrc!!.setDayIndex(day_index)
        planninglistrc!!.setDayIndex(day_index)
        planninglistrc!!.notifyDataSetChanged()
        planningDayListrc!!.notifyDataSetChanged()
        //planningUndeflistrc!!.notifyDataSetChanged()
        UndefineRecyclerViewLoad(editMode)
        if (debugmode) Log.i("PlanningHelper", "dayClick - Click $item / daysList[$day_index] = ${daysList!!.get(day_index)}")
        //DaysRecyclerViewLoad(editMode, day_index)
        if (day_index >= daysList!!.size) {
            day_index = daysList!!.size - 1
        }
        splite_line1.text = daysList!![day_index]
        RecyclerViewLoad(item, false)
    }

    fun rv_click(item: String, partItem: TripPlan_PPModel, mode: Int) {
        if (mode == 0) {
            var ppModel : PPModel? = null

            val review_f = Review_Fragment()
            partItem.let {
                ppModel = PPModel(
                    partItem.id,
                    partItem.cloudID,
                    partItem.name,
                    partItem.phone,
                    partItem.country,
                    partItem.addr,
                    partItem.fb,
                    partItem.web,
                    partItem.blogInfo,
                    partItem.opentime,
                    partItem.tag_note,
                    partItem.descrip,
                    partItem.pic_url,
                    partItem.score,
                    partItem.status,
                    partItem.distance,
                    partItem.calDistanceDone
                )
                //partItem}
            }
            review_f.pp_set(ppModel!!)
            //calDistanceTaskStop()
            (activity as MainActivity).switchFragment(this@PlanningHelper, review_f, 2)
        } else if (mode == 2) {
            (activity as MainActivity).mapNavi(partItem.addr)
        } else if (mode == 3) {
            (activity as MainActivity).callPhone(partItem.phone)
        } else if (mode == 4) {

        }
        var idx = 0
        if (item.isNotEmpty()) {
            idx = 0
            daysList!!.forEach {
                if (daysList!!.get(idx) == item) return@forEach
                idx += 1
            }
        }
        if (debugmode) Log.i("PlanningHelper", "rv_click: Item : $item / index : $idx")
        UndefineRecyclerViewLoad(editMode)
        RecyclerViewLoad(daysList!!.get(idx), editMode)
        DaysRecyclerViewLoad(editMode, idx)
    }

    fun undef_click(day: String, partItem: TripPlan_PPModel, mode: Int) {
        if (mode == 0) {
            var ppModel : PPModel? = null

            val review_f = Review_Fragment()
            partItem.let { ppModel =  PPModel(
                partItem.id,
                partItem.cloudID,
                partItem.name,
                partItem.phone,
                partItem.country,
                partItem.addr,
                partItem.fb,
                partItem.web,
                partItem.blogInfo,
                partItem.opentime,
                partItem.tag_note,
                partItem.descrip,
                partItem.pic_url,
                partItem.score,
                partItem.status,
                partItem.distance,
                partItem.calDistanceDone
            )}
            review_f.pp_set(ppModel!!)
            //calDistanceTaskStop()
            (activity as MainActivity).switchFragment(this@PlanningHelper, review_f, 2)
        } else if (mode == 1) {

        } else if (mode == 2) {
            (activity as MainActivity).mapNavi(partItem.addr)
        } else if (mode == 3) {
            (activity as MainActivity).callPhone(partItem.phone)
        } else if (mode == 4) {

        }

        //var idx = 0
        //daysList!!.forEach{
        //    if (daysList!!.get(idx) == day) return@forEach
        //    idx += 1
        //}
        //if (idx >= daysList!!.count()) idx = day_index
        if (debugmode) Log.i("PlanningHelper", "undef_click: Day : $day / index : $day_index")
        DaysRecyclerViewLoad(editMode, day_index)
        UndefineRecyclerViewLoad(editMode)
        RecyclerViewLoad(daysList!!.get(day_index), editMode)
    }
}