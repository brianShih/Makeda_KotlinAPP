package tw.breadcrumbs.makeda

import android.content.ContentValues
import android.widget.TextView
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.widget.BaseAdapter

import android.content.Context

import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.*
import android.view.LayoutInflater
import android.widget.ToggleButton
import androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior.setTag
import android.widget.AdapterView
import kotlinx.android.synthetic.main.grid_single.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import tw.breadcrumbs.makeda.R.layout.grid_single


class FilterCustomGrid(private val context: Context, private val text: MutableList<String>) : BaseAdapter() {
    private val debugmode = false
    private var selected:MutableList<Int>? = mutableListOf()

    fun selectListInit() {//(setin : MutableList<Int>) {
        //val size = text.size - 1


        if (selected!!.count() < text.count()) {
            val index = selected!!.count()
            val outrange = text.count() - selected!!.count()
            if (debugmode) {
                Log.i("FilterCustomGrid", " selected count : ${selected!!.count()}")
                Log.i("FilterCustomGrid", " text count : ${text.count()}")
                Log.i("FilterCustomGrid", " outrange : $outrange")
            }
            for ( i in 1..outrange) {
                selected!!.add(index + (i - 1), 0)
            }
        }
        //selected = setin
    }

    fun set_selectList(setL : MutableList<Int>) {
        selected = setL
    }

    fun get_selectList() : MutableList<Int> {
        return selected!!
    }

    override fun getCount(): Int {
        return text.size
    }

    override fun getItem(position: Int): Any? {
        return text.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        // Context 動態放入mainActivity
        if (debugmode)
            Log.i("FilterCustomGrid", "position : $position , covertView : $convertView")
        val holder: ViewHolder? = ViewHolder()
        holder!!.mInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val grid = holder.mInflater!!.inflate(grid_single, null)
        holder.textView = grid.findViewById(R.id.grid_text) as TextView
        grid.setTag(holder)
        holder.textView!!.setText(text.get(position))
        if (selected!!.get(position) == 1) {
            //holder.textView!!.setBackgroundColor(Color.rgb(30, 203, 255))
            holder.textView!!.background = ContextCompat.getDrawable(context, R.drawable.radius_turnon_bg)
        } else {
            //holder.textView!!.setBackgroundColor(Color.rgb(237, 237, 237))
            holder.textView!!.background = ContextCompat.getDrawable(context, R.drawable.radius_turnoff_bg)
        }

        holder.textView!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (debugmode)
                    Log.i(ContentValues.TAG, "grid onItemClickListener : 你選取了 ${text.get(position)} / index = $position")

                if (selected!!.get(position) == 1) {
                    selected!!.set(position, 0)
                    //holder!!.textView!!.setBackgroundColor(Color.rgb(237, 237, 237))//.setBackgroundColor(Color.BLUE)
                    holder.textView!!.background = ContextCompat.getDrawable(context, R.drawable.radius_turnoff_bg)
                } else {
                    selected!!.set(position, 1)
                    //holder!!.textView!!.setBackgroundColor(Color.rgb(30, 203, 255))
                    holder.textView!!.background = ContextCompat.getDrawable(context, R.drawable.radius_turnon_bg)
                }
                if (debugmode)
                    Log.i(ContentValues.TAG, "grid onItemClickListener : Update ${text.get(position)} / index = $position")
            }
        })
        return grid
    }
    class ViewHolder {
        var textView: TextView? = null
        var mInflater: LayoutInflater? = null
    }
}