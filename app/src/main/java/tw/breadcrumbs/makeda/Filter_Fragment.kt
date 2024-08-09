package tw.breadcrumbs.makeda

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.filter_fragment.*
import android.widget.GridView
import tw.breadcrumbs.makeda.dataModel.PPModel


class Filter_Fragment : androidx.fragment.app.Fragment() {
    private val debugmode = false
    val areaUnit = arrayOf( "市", "村", "町", "區", "鄉", "鎮")
    val no_include = arrayOf("樂園", "夜市", "超市", "園區", "市場", "社區", "沖繩總鎮守", "近市區", "遊樂區", "新村", "鄉公所", "遊憩區", "度假村", "露營區", "渡假村", "市集")
    val mainTags = arrayOf("#食", "#衣", "#住", "#行", "#景", "#購物")
    private var dash:Dash_Fragment? = null
    private var grid : GridView? = null
    private var pps_list:List<PPModel>? = listOf()
    private var allTagsList: MutableList<String>? = mutableListOf()
    private var in_tags:MutableList<String>? = mutableListOf()
    private var pps_selected:MutableList<Int>? = mutableListOf()
    private var gFilterAdapter: FilterCustomGrid? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.filter_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setup_view()
    }

    fun pp_set(in_pps_list:List<PPModel>) {
        pps_list = in_pps_list
    }


    fun setup_view() {
        dash = (activity as MainActivity).dash_frag as Dash_Fragment
        if ( pps_list == null ) {
            val dash_f = (activity as MainActivity).dash_frag as Dash_Fragment
            (activity as MainActivity).switchFragment(this@Filter_Fragment, dash_f, 2)
        }
        back_button_setup()
        parsing_tags()
        setup_area_toggleButton()
        setup_mainTags_toggleButton()
        setup_subTags_toggleButton()
        gFilterAdapter = FilterCustomGrid(this@Filter_Fragment.context!!, allTagsList!!)
        toggleBTNInit(gFilterAdapter!!, R.id.filter_cons_content_layout1, allTagsList!!)
        val selClicked = gFilterAdapter!!.get_selectList()
        val f_tags = dash!!.getfilterTags()
        var index = 0
        allTagsList!!.forEach{
            f_tags.forEach { f_tag->
                if (f_tag == it) selClicked.set(index, 1)
            }
            index += 1
        }
        gFilterAdapter!!.set_selectList(selClicked)
    }

    fun parsing_tags() {
        if (pps_list == null) return
        var index = 0
        for (i in pps_list!!) {
            val tags = i.tag_note.split(" ")

            for (t in tags) {
                var same = 0
                //for (s_tag in in_tags!!) {
                in_tags?.forEach {
                    if (it.equals(t)) same = 1
                }
                    //if (t == s_tag) same = 1
                //}
                if (same == 0 && t.isNotEmpty()) {
                    in_tags!!.add(t)//[index] = t//.add(t)
                    pps_selected!!.add(0)
                    //dym_in_tags!!.add(t)
                    index += 1
                }
            }
        }
        in_tags.let {
            if (debugmode)
                Log.i("Filter_Fragment", "in_tags : $in_tags")
        }
    }

    fun setup_subTags_toggleButton() {
        var temp = in_tags!!.filter {
            var same = 0
            val size = allTagsList!!.size - 1
            for(t in 0..size) {
                if (it == allTagsList!!.get(t)) {
                    same = 1
                    //Log.i("Filter_Fragment","Area_list aready has - city unit : $it!!")
                }
            }
            same == 0
        }

        temp.let {
            val endIdx = temp.size - 1
            for (c in 0..endIdx) {
                var same = 0
                //for (m in sub_tags_list!!) {
                for (m in allTagsList!!) {
                    if (it.get(c) == m) {
                        same = 1
                    }
                }
                if (same == 0) {
                    allTagsList!!.add(it.get(c))
                }
            }
        }
    }

    fun setup_mainTags_toggleButton() {
        var temp = in_tags!!.filter {
            var same = 0
            val size = allTagsList!!.size - 1
            for(t in 0..size) {
                if (it == allTagsList!!.get(t)) {
                    same = 1
                    if (debugmode)
                        Log.i("Filter_Fragment","temp - city unit : $it!!")
                }
            }
            same == 0
        }
        if (debugmode)
            Log.i("Filter_Fragment"," setup_mainTags_toggleButton : reduce temp : $temp")

        temp.let {
            val endIdx = temp.size - 1
            for (c in 0..endIdx) {
                for (main_tag in mainTags) {
                    if (it!!.get(c) == main_tag) {
                        var same = 0
                        //for (m in main_tags_list!!) {
                        for (m in allTagsList!!) {
                            if (it[c] == m) {
                                same = 1
                            }
                        }
                        if (same == 0) {
                            //main_tags_list!!.add(it.get(c))
                            allTagsList!!.add(it.get(c))
                        }
                    }
                }
            }
        }
    }

    fun setup_area_toggleButton() {
        if (debugmode)
            Log.i("Filter_Fragment","in_tags = ${in_tags!!}")
        val endIdx = in_tags!!.size - 1
        for (i in 0..endIdx) {
            if (debugmode)
                Log.i("Filter_Fragment"," i = $i, content = ${in_tags!!.get(i)}")
            var noinclude = 0
            var is_unit = 0
            for (u in areaUnit) {
                if (in_tags!!.get(i).contains(u)) {
                    for (n in no_include) {
                        if (in_tags!!.get(i).contains(n)) {
                            noinclude = 1
                        }
                    }
                    if (noinclude == 0) {
                        is_unit = 1
                    }
                }
            }
            if (is_unit == 1) {
                var same = 0
                //val arealist_EndIdx = area_list!!.size - 1
                //for (g in area_list!!) {
                for (g in allTagsList!!) {
                    g.let { cache_area ->
                        if (in_tags!!.get(i) == cache_area) {
                            same = 1
                        }
                    }
                }
                if (same == 0) {
                    //Log.i("Filter_Fragment", " area list add = ${in_tags!!.get(i)}")
                    //area_list!!.add(in_tags!!.get(i))
                    allTagsList!!.add(in_tags!!.get(i))
                }
            }
        }
    }

    fun update_pps_selected(list: MutableList<String>, select: MutableList<Int>) {
        if (list.size != select.size) {
            if (debugmode) {
                Log.i("Filter_Fragment", "input size not equi")
                Log.i("Filter_Fragment", "input size : list - ${list.size} , select - ${select.size}")
            }
        } else {
            var index = 0

            for (i in list) {
                if (select[index] == 1) {
                    var row_index = 0
                    for (p in in_tags!!) {
                        if (list[index].equals(in_tags!![row_index])) {
                            pps_selected!!.set(row_index, 1)//add(row_index, 1)//[row_index] = 1

                        }
                        row_index += 1
                    }
                }
                index += 1
            }
        }
    }

    fun parsing_selected_tags () :MutableList<String> {
        val size = pps_selected!!.size - 1
        var tags_selected :MutableList<String> = mutableListOf()
        for (i in 0..size){
            if (pps_selected!![i] == 1) {
                tags_selected.add(in_tags!![i])
            }
        }
        if (debugmode) Log.i("Filter_Fragment", "in_tags : $in_tags")
        if (debugmode) Log.i("Filter_Fragment", "pps_selected : $pps_selected")
        return tags_selected
    }

    fun back_button_setup() {
        filter_backButton.setOnClickListener {
            val list = gFilterAdapter!!.get_selectList()
            update_pps_selected(allTagsList!!, list)

            val tags_sel = parsing_selected_tags()
            if (debugmode) Log.i("Filter_Fragment", " tags_sel : $tags_sel")
            dash!!.setfilterTags(tags_sel)
            (activity as MainActivity).onBackPressed()
        }
    }

    fun toggleBTNInit(adapter:FilterCustomGrid, layoutID:Int, list:MutableList<String>) {
        adapter.selectListInit()
        grid = view!!.findViewById(layoutID)
        grid!!.setAdapter(adapter)
    }
}




