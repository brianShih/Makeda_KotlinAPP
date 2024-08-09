package tw.breadcrumbs.makeda.TripPlanning

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import kotlinx.android.synthetic.main.trip_plann_rc_item.view.*
import org.json.JSONArray
import org.json.JSONObject
import tw.breadcrumbs.makeda.DBModules.TripPlanDAO
import tw.breadcrumbs.makeda.DBModules.TripPlanDB_PPModelDAO
import tw.breadcrumbs.makeda.R
import tw.breadcrumbs.makeda.dataModel.TripListModel
import kotlin.collections.ArrayList


class TripPlanListRC (val triplistModel:List<TripListModel>, val clickListener: (TripListModel, Int) -> Unit) : androidx.recyclerview.widget.RecyclerView.Adapter<TripPlanListRC.ViewHolder>() {
    var debugmode = false
    var context: Context? = null
    var tripPlanDB: TripPlanDAO? = null
    var tripPlanDB_PPModelDAO: TripPlanDB_PPModelDAO? = null
    var cities:ArrayList<String> = arrayListOf()
    var countries: ArrayList<String> = arrayListOf()
    var useremail: String? = ""
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.trip_plann_rc_item, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTripPlanListModel( triplistModel[position], clickListener)
    }

    // 返回數目
    override fun getItemCount(): Int {
        return triplistModel.size
    }

    fun set_TripPlanDB(db: TripPlanDAO) {
        tripPlanDB = db
    }

    fun getCityFromTripplan(triplistmodel: TripListModel) {
        val tripPlan = tripPlanDB!!.get(triplistmodel.id)
        if (debugmode) Log.i(
            "TripPlanListRC",
            " tripPlan = $tripPlan"
        )
        tripPlan?.let{
            var readID : Long = 0
            if (it.tripplanID.isEmpty() || it.tripplanID.equals("")) {
                readID = it.id
            } else {
                readID = it.tripplanID.toLong()
            }
            if (tripPlanDB_PPModelDAO == null) tripPlanDB_PPModelDAO = TripPlanDB_PPModelDAO(context!!)
            val tripPlanDB_PPlist = tripPlanDB_PPModelDAO!!.findTripPlan(readID)
            tripPlanDB_PPlist!!.forEach {
                val area_arr = it.country.split(" | ")
                var same = 0
                countries.forEach {
                    if (area_arr.get(0) == it) same = 1
                }
                if (same == 0) countries.add(area_arr.get(0))
                same = 0
                cities.forEach {
                    if (area_arr.get(1) == it) same = 1
                }
                if (same == 0) cities.add(area_arr.get(1))
            }
        }
    }

    // view
    inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bindTripPlanListModel(triplistmodel: TripListModel, clickListener: (TripListModel, Int) -> Unit) {
            triplistmodel.let{
                countries = arrayListOf()
                cities = arrayListOf()
                getCityFromTripplan(it)
                //pre_sync()
                itemView.titleName.text = triplistmodel.planname
                if (countries.isEmpty() && cities.isEmpty()) {
                    itemView.subTitle.text = "開始計劃旅程"
                } else {
                    itemView.subTitle.text = "行程計畫涵蓋了 - 區域/國家 : " + countries + " / 城市 : " + cities
                }
            }
            TripPlanning_Fragment().cloud_checkTripPlan(context!!, useremail!!, triplistmodel)
            val switch = itemView!!.findViewById(R.id.tripplan_switch) as ImageButton
            if (triplistmodel.updated == 0) {
                switch.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.non_cloud_icon))//setImageIcon(R.drawable.online_icon)
            } else {
                switch.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.online_icon))
            }
            switch.setOnClickListener{
                if (triplistmodel.updated == 0) {
                    triplistmodel.updated = 1
                    switch.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.online_icon))
                } else {
                    triplistmodel.updated = 0
                    switch.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.non_cloud_icon))
                }
                tripPlanDB!!.update(triplistmodel)
                TripPlanning_Fragment().cloud_switchTripPlan(context!!, useremail!!, triplistmodel)
                clickListener(triplistmodel, 0)
            }

            val del = itemView.findViewById(R.id.tripplan_del) as ImageButton
            del.setOnClickListener {
                val items = TripPlanning_Fragment().getCloudContentItems(triplistmodel)
                items!!.forEach {
                    val cloudIDList = TripPlanning_Fragment().getPPModelFromItem(triplistmodel, it)//getCloudContentItems(triplistmodel)
                    cloudIDList?.forEach {
                        if (tripPlanDB_PPModelDAO == null) tripPlanDB_PPModelDAO = TripPlanDB_PPModelDAO(context!!)
                        tripPlanDB_PPModelDAO?.deleteUsingCloudID(it.toLong(), triplistmodel.tripplanID.toLong())
                    }
                }

                tripPlanDB!!.delete(triplistmodel.id)
                clickListener(triplistmodel, 1)
            }

            itemView.setOnClickListener{
                clickListener(triplistmodel, 0)
            }
        }
    }
}