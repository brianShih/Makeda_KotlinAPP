package tw.breadcrumbs.makeda.TripPlanning

import android.annotation.SuppressLint
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import kotlinx.android.synthetic.main.opentripplan_rc_item.view.*
import kotlinx.android.synthetic.main.trip_plann_rc_item.view.*
import org.json.JSONArray
import org.json.JSONObject
import tw.breadcrumbs.makeda.DBModules.OpenTripPlanDB_PPModelDAO
import tw.breadcrumbs.makeda.DBModules.TripPlanDAO
import tw.breadcrumbs.makeda.DBModules.TripPlanDB_PPModelDAO
import tw.breadcrumbs.makeda.R
import tw.breadcrumbs.makeda.dataModel.TripListModel
import tw.breadcrumbs.makeda.dataModel.TripPlan_PPModel
import kotlin.collections.ArrayList


class OpenTripPlanRC (val triplistModel:List<TripListModel>, val clickListener: (TripListModel, Int) -> Unit) : androidx.recyclerview.widget.RecyclerView.Adapter<OpenTripPlanRC.ViewHolder>() {
    var context: Context? = null
    var tripPlanDB: TripPlanDAO? = null
    var opentripplanDAO: OpenTripPlanDB_PPModelDAO? = null
    var tripplan_ppList: MutableList<TripPlan_PPModel>? = null
    var cities:ArrayList<String> = arrayListOf()
    var countries: ArrayList<String> = arrayListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.opentripplan_rc_item, parent, false)
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
        //val tripPlan = tripPlanDB!!.get(triplistmodel.id)
        val readID = triplistmodel.tripplanID.toLong()
        //if (triplistmodel.tripplanID.isEmpty() || triplistmodel.tripplanID.equals("")) {
        //    readID = triplistmodel.id
        //} else {
        //    readID = triplistmodel.tripplanID.toLong()
        //}
        //if (opentripplanDAO == null) opentripplanDAO = OpenTripPlanDB_PPModelDAO(context!!)
        //val tripPlanDB_PPlist = opentripplanDAO!!.findTripPlan(readID)
        val temp : MutableList<TripPlan_PPModel> = mutableListOf()
        tripplan_ppList?.forEach {
            if (it.tripPlanID.equals(readID)) temp.add(it)
        }
        temp.forEach {
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

    // view
    inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bindTripPlanListModel(triplistmodel: TripListModel, clickListener: (TripListModel, Int) -> Unit) {
            triplistmodel.let{
                countries = arrayListOf()
                cities = arrayListOf()
                getCityFromTripplan(triplistmodel)
                //pre_sync()
                itemView.opl_titleName.text = triplistmodel.planname//titleName.text = triplistmodel.planname
                itemView.opl_author.text = triplistmodel.author+" 創建"
                if (countries.isEmpty() && cities.isEmpty()) {
                    itemView.opl_subTitle.text = "開始計劃旅程"//subTitle.text = "開始計劃旅程"
                } else {
                    itemView.opl_subTitle.text = "行程計畫涵蓋了 - 區域/國家 : " + countries + " / 城市 : " + cities //subTitle.text = "行程計畫涵蓋了 - 區域/國家 : " + countries + " / 城市 : " + cities
                }
                itemView.setOnClickListener{
                    clickListener(triplistmodel, 0)
                }
            }
        }
    }
}