package tw.breadcrumbs.makeda

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.dash_pplist_item.view.descriptionTextView
import kotlinx.android.synthetic.main.dash_pplist_item.view.head_name
import kotlinx.android.synthetic.main.locaterecently_rc_item.view.*
import tw.breadcrumbs.makeda.DBModules.ItemDAO
import tw.breadcrumbs.makeda.dataModel.TripPlan_PPModel
import tw.breadcrumbs.makeda.dateModel.LRModel

class LocateRecentlyListRC(var feedModelItems: MutableList<LRModel>, val clickListener: (TripPlan_PPModel) -> Unit) : androidx.recyclerview.widget.RecyclerView.Adapter<LocateRecentlyListRC.ViewHolder>() {
    private val debugmode = false
    var context : Context? = null
    var onLineIcon: Drawable? = null
    var newIcon: Drawable? = null
    var localIcon: Drawable? =null
    var item_dao : ItemDAO? = null
    var useremail: String? = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.locaterecently_rc_item, parent, false)
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
        fun bindPPModel(item: LRModel, clickListener: (TripPlan_PPModel) -> Unit) {
            itemView.head_name.text = item.pp.name
            itemView.descriptionTextView.text = item.pp.tag_note
            itemView.gotodate.text = "到訪時間 : " + item.date

            itemView.setOnClickListener{
                clickListener(item.pp)
            }
        }
    }
}