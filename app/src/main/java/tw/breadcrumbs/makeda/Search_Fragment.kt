package tw.breadcrumbs.makeda

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import kotlinx.android.synthetic.main.search_fragment.*
import org.json.JSONObject
import tw.breadcrumbs.makeda.CloudObj.Cloud_Helper
import tw.breadcrumbs.makeda.DBModules.ItemDAO
import tw.breadcrumbs.makeda.dataModel.PPModel

class Search_Fragment : androidx.fragment.app.Fragment() {
    private val debugmode = false
    var item_dao : ItemDAO? = null
    var dash_frag:Dash_Fragment? = null
    var downFromCloud:MutableList<PPModel>? = mutableListOf()
    var localList: MutableList<PPModel>? = mutableListOf()
    var resultList: MutableList<PPModel>? = mutableListOf()
    //private var dashCalHelper: calcuteDistanceHelper? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.search_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        item_dao = ItemDAO(this@Search_Fragment.context!!)
        SetupView()
    }

    fun SetupView() {
        back_button_setup()
        searchActionSetup()
        dash_frag = Dash_Fragment()//(activity as MainActivity).dash_frag as Dash_Fragment
    }

    fun back_button_setup() {
        searchBackButton.setOnClickListener {
            //val dash:Dash_Fragment = (activity as MainActivity).dash_frag as Dash_Fragment
            (activity as MainActivity).onBackPressed()
        }
    }

    fun searchAction() {
        val getText = search_in.text.toString()
        resultList = mutableListOf()
        localList = mutableListOf()
        downFromCloud = mutableListOf()

        localList = item_dao!!.find(getText) as MutableList<PPModel>
        if (!localList!!.isEmpty()) {
            resultList = localList
        }
        //Log.i("Search_Fragment", "ppList[0] : ${ppList.get(0).name}")
        //'CMD' : 'FIND_PP_KEYWORDS', 'keywords':u'老宅,食,麵包屑&三合院'
        val cloud_cmd = "CMD=//TODO&keywords=$getText"
        Cloud_Helper(this@Search_Fragment.context!!) {
            if (it == null) {
                //println("connection error")
                return@Cloud_Helper
            }
            val cloud_db = JSONObject(it)
            if (!cloud_db.isNull("count")) {
                val cloud_count = cloud_db.get("count") as Int
                for (i in 1..cloud_count) {
                    if (debugmode) Log.i("Search_Fragment", "search Index : $i")
                    val tempDB = cloud_db.get(i.toString()) as JSONObject
                    val score = tempDB.get("score").toString()
                    val status = tempDB.get("status").toString()
                    val cloudID = tempDB.get("cloudID").toString()
                    val ppmodel = PPModel(
                        0, cloudID.toLong(),
                        tempDB.get("name").toString(), tempDB.get("phone").toString(),
                        tempDB.get("country").toString(), tempDB.get("address").toString(),
                        tempDB.get("fb").toString(), tempDB.get("web").toString(),
                        tempDB.get("bloggerintro").toString(), tempDB.get("opentime").toString(),
                        tempDB.get("tag_note").toString(), tempDB.get("description").toString(),
                        tempDB.get("pic_url").toString(),
                        score.toInt(), status.toInt()
                        //tempDB.get("score") as Int, tempDB.get("status") as Int
                    )
                    if (debugmode) Log.i("Dash_Fragment", " insert to cloud_PPModelList: index: $i / ${tempDB.get("name").toString()}")
                    downFromCloud!!.add(ppmodel)

                }
                if (downFromCloud!!.isNotEmpty()) {
                    for (temp in downFromCloud!!) {
                        var sameInList = 0
                        for (g in resultList!!) {
                            if (g.name == temp.name) {
                                sameInList = 1
                            }
                        }
                        if (sameInList == 0) {
                            resultList!!.add(temp)
                        }
                    }
                }
                if (!resultList!!.isEmpty()) {
                    if (dash_frag == null) {
                        dash_frag = Dash_Fragment()
                    }
                    dash_frag!!.recycleViewInit(
                        this@Search_Fragment.context!!,
                        SearchListAction(resultList!!),
                        searchRecyclerView, resultList!!
                    )
                }
                Toast.makeText(this@Search_Fragment.context, "資料搜尋 完成", Toast.LENGTH_LONG).show()

                //cloud_download_ready = 1
                //updatePPList()
            } else {
                Toast.makeText(this@Search_Fragment.context, "【無】線上資料", Toast.LENGTH_LONG)
                    .show()
            }
        }.execute("POST", dash_frag!!.CloudHelperURL, cloud_cmd.toString())
    }

    fun searchActionSetup() {
        search_in.setOnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE){
                searchAction()
                true
            } else {
                false
            }
        }

        searchActionButton.setOnClickListener{
            Toast.makeText(this@Search_Fragment.context, "啟動資料搜尋中,請稍候", Toast.LENGTH_LONG)
                .show()
            searchAction()
        }
    }

    inner class SearchListAction(val ppModelItems: List<PPModel>):ListAction() {

        override fun pp_onClick(partItem: PPModel) {
            //Log.i("DashListAction :", "pp_onClicked : " + partItem)
            var ppModel : PPModel? = null

            val review_f = Review_Fragment()
            partItem.let { ppModel =  partItem}
            review_f.pp_set(ppModel!!)

            (activity as MainActivity).switchFragment(this@Search_Fragment, review_f, 2)
        }

        override fun onSwiped(position: Int) {
            //Log.i("DashListAction :", "onClicked : $position") //ppModelItems[position])
            var cloud = false
            if (resultList!![position].id.toInt() == 0) cloud = true
            if (cloud) {
                val ret = dash_frag!!.pp_insert(resultList!![position])
                if (ret == null) {
                    Toast.makeText(this@Search_Fragment.context, "存入手冊 " + resultList!![position].name + " 失敗",
                        Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@Search_Fragment.context, "存入手冊 " + resultList!![position].name + " 成功",
                        Toast.LENGTH_LONG).show()
                    resultList!!.get(position).id = ret.id
                    dash_frag!!.dash_list_rc!!.notifyDataSetChanged()
                    //updatePPList()
                }
            } else {
                if (dash_frag!!.pp_delete(resultList!![position].id.toLong())) {
                    Toast.makeText(this@Search_Fragment.context, "刪除 " + resultList!![position].name + " 完成",
                        Toast.LENGTH_LONG).show()
                    resultList!![position].id = 0
                    dash_frag!!.dash_list_rc!!.notifyDataSetChanged()
                    //displayList!!.removeAt(position)
                    //global_pp_list!![position].id = 0
                    //global_pp_list!!.removeAt(position)
                    //dash_list_rc!!.notifyItemRemoved(position);
                    //dash_list_rc!!.notifyItemRangeChanged(position, dash_list_rc!!.getItemCount())
                    //updatePPList()
                } else {
                    Toast.makeText(this@Search_Fragment.context, "刪除 " + resultList!![position].name + " 失敗",
                        Toast.LENGTH_LONG).show()
                }
            }
        }

    }
}