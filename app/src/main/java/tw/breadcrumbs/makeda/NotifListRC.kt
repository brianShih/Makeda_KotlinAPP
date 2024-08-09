package tw.breadcrumbs.makeda

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.TypedArrayUtils.getString
import kotlinx.android.synthetic.main.dash_pplist_item.view.*
import kotlinx.android.synthetic.main.dash_pplist_item.view.descriptionTextView
import kotlinx.android.synthetic.main.dash_pplist_item.view.head_name
import kotlinx.android.synthetic.main.notif_rc_item.view.*
import tw.breadcrumbs.makeda.DBModules.ItemDAO
import tw.breadcrumbs.makeda.DBModules.TripPlanDAO
import tw.breadcrumbs.makeda.TripPlanning.PlanningHelper
import tw.breadcrumbs.makeda.TripPlanning.TripPlanning_Fragment
import tw.breadcrumbs.makeda.dataModel.TripPlan_PPModel
import tw.breadcrumbs.makeda.dataModel.PPModel

class NotifListRC (var feedModelItems: MutableList<TripPlan_PPModel>, val clickListener: (TripPlan_PPModel) -> Unit) : androidx.recyclerview.widget.RecyclerView.Adapter<NotifListRC.ViewHolder>() {
    private val debugmode = false
    var context : Context? = null
    var onLineIcon: Drawable? = null
    var newIcon: Drawable? = null
    var localIcon: Drawable? =null
    var item_dao : ItemDAO? = null
    var useremail: String? = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.notif_rc_item, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindPPModel( feedModelItems[position], clickListener)
    }

    // 返回數目
    override fun getItemCount(): Int {
        return feedModelItems.size
    }

    // view
    inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        //@SuppressLint("SetTextI18n")
        fun bindPPModel(ppModel: TripPlan_PPModel, clickListener: (TripPlan_PPModel) -> Unit) {
            itemView.head_name.text = ppModel.name
            itemView.descriptionTextView.text = ppModel.tag_note
            if (ppModel.calDistanceDone) {
                if (ppModel.distance <= 0.03) {
                    itemView.near_notif.text = " HERE!! "
                    itemView.near_notif.setTextColor(Color.BLUE)//textColors =
                    //itemView.near_notif.textColors = color
                } else {
                    itemView.near_notif.text = "距離 " + ppModel.distance
                    itemView.near_notif.setTextColor(Color.BLACK)
                }
            } else {
                itemView.near_notif.setTextColor(Color.BLACK)//textColors =
            }

            itemView.setOnClickListener{
                clickListener(ppModel)
            }
        }
    }
}
