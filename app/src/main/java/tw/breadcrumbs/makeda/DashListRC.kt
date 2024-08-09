package tw.breadcrumbs.makeda

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.Toast
import kotlinx.android.synthetic.main.dash_pplist_item.view.*
import org.json.JSONArray
import org.json.JSONObject
import tw.breadcrumbs.makeda.DBModules.ItemDAO
import tw.breadcrumbs.makeda.DBModules.TripPlanDAO
import tw.breadcrumbs.makeda.TripPlanning.PlanningHelper
import tw.breadcrumbs.makeda.TripPlanning.TripPlanning_Fragment
import tw.breadcrumbs.makeda.dataModel.PPModel


class DashListRC (var feedModelItems: MutableList<PPModel>, val clickListener: (PPModel) -> Unit) : androidx.recyclerview.widget.RecyclerView.Adapter<DashListRC.ViewHolder>() {
    private val debugmode = false
    var context : Context? = null
    var onLineIcon: Drawable? = null
    var newIcon: Drawable? = null
    var localIcon: Drawable? =null
    var item_dao : ItemDAO? = null
    var useremail: String? = ""
    private var photo_url_List: MutableList<String>? = mutableListOf()
    private var photo_grid_Adapter: PhotoUrlGrid? = null
    var pt_height = 0
    var imageHdlr = 0
    var listAction : ListAction? = null
    //var photos_item_rc : DashPhotoListRC? = null
    //private var photo_grid : GridView? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.dash_pplist_item, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindPPModel( feedModelItems[position], clickListener)
    }

    fun set_useremail( user: String ) {
        useremail = user
    }

    fun setImageDwnHdlr( imgHdlrIn : Int) {
        imageHdlr = imgHdlrIn
    }

    fun setItemDAO(dao: ItemDAO) {
        item_dao = dao
    }

    fun getItems(): List<PPModel> {
        return feedModelItems
    }

    // 返回數目
    override fun getItemCount(): Int {
        return feedModelItems.size
    }

    fun save_to_tripPlanning(pp: PPModel) {
        val tpf = TripPlanning_Fragment()
        val tripPlanList = tpf.getAllTripList(context!!)
        if (tripPlanList.count() == 0) {
            Toast.makeText(context, R.string.trip_zero_alert_zh, Toast.LENGTH_LONG).show()
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
                    ph.add_pp_into(context!!, tripPlanModel!!.id, pp, "UNDEF")
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

    fun setListion(listion: ListAction) {
        listAction = listion
    }

    // view
    inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bindPPModel(ppModel: PPModel, clickListener: (PPModel) -> Unit) {
            ppModel.let {
                if (ppModel.id.toInt() == 0 && ppModel.status == 0) {
                    itemView.onlineStatus.setImageDrawable(onLineIcon)
                }
                else if (ppModel.id.toInt() == 0 && ppModel.status == 1) {
                    itemView.onlineStatus.setImageDrawable(newIcon)
                } else {
                    itemView.onlineStatus.setImageDrawable(localIcon)
                }


                itemView.head_name.text = ppModel.name
                itemView.descriptionTextView.text = ppModel.tag_note
                photo_url_List = mutableListOf()
                //https://wwww.breadcrumbs.tw
                if (imageHdlr == 1 && ppModel.pic_url.length > 15) {
                    if (debugmode)
                        Log.i(
                            "DashListRC",
                            "pic url = ${ppModel.pic_url} / length = ${ppModel.pic_url.length}"
                        )
                    //photo_grid = itemView.findViewById(R.id.photo_grid)
                    if (ppModel.pic_url.isNotEmpty() && ppModel.pic_url.length > 15) {
                        itemView.photo_grid.isEnabled = true
                        itemView.photo_grid.visibility = View.VISIBLE
                        if (ppModel.pic_url.contains("[") && ppModel.pic_url.contains("]")) {
                            val tempJsonArr = JSONArray(ppModel.pic_url)
                            for (i in 0..(tempJsonArr.length() - 1)) {
                                photo_url_List!!.add(tempJsonArr.get(i).toString())
                            }
                            photo_grid_Adapter = PhotoUrlGrid(context!!, photo_url_List!!)
                            photo_grid_Adapter!!.setClickListion(listAction!!)
                            itemView.photo_grid.setAdapter(photo_grid_Adapter)
                            val params = itemView.photo_grid!!.layoutParams
                            if (params.height == 0 && pt_height != 0) {
                                if (debugmode) Log.i(
                                    "DashListRC",
                                    " photo grid height should not 0"
                                )
                                params.height = pt_height
                                itemView.photo_grid.layoutParams = params
                            }
                        }
                        //params.height = 0
                        //itemView.photo_grid.layoutParams = params
                    } else {
                        //photo_grid.display =
                        if (debugmode) Log.i("DashListRC", " photo grid hide")
                        itemView.photo_grid!!.isEnabled = false
                        itemView.photo_grid!!.visibility = View.INVISIBLE
                        val params = itemView.photo_grid!!.layoutParams
                        if (params.height != 0)
                            pt_height = params.height
                        params.height = 0
                        itemView.photo_grid.layoutParams = params
                    }
                } else {
                    itemView.photo_grid!!.isEnabled = false
                    itemView.photo_grid!!.visibility = View.INVISIBLE
                    val params = itemView.photo_grid!!.layoutParams
                    if (params.height != 0)
                        pt_height = params.height
                    params.height = 0
                    itemView.photo_grid.layoutParams = params
                }
                itemView.setOnClickListener{
                    clickListener(ppModel)
                }

                itemView.setOnLongClickListener {
                    save_to_tripPlanning(ppModel)
                    true
                }

            }
        }


    }
}