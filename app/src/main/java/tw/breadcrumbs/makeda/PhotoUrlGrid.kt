package tw.breadcrumbs.makeda

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.squareup.picasso.Picasso
import tw.breadcrumbs.makeda.R.layout.pp_photos_item


class PhotoUrlGrid(private val context: Context, private val urllist: MutableList<String>) : BaseAdapter() {
    private val debugmode = false
    private var mInflater: LayoutInflater? = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var selected:MutableList<String>? = mutableListOf()
    var listAction : ListAction? = null

    override fun getCount(): Int {
        return urllist.size
    }

    override fun getItem(position: Int): Any? {
        return urllist.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun setClickListion(listion : ListAction) {
        listAction = listion
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val grid = this.mInflater!!.inflate(pp_photos_item, null)
        val holder = ViewHolder()
        holder.imageView = grid.findViewById(R.id.photo_view) as ImageView
        if (debugmode)
            Log.i("PhotoUrlGrid", "getView  - url list = $urllist")
        Picasso.get().load(urllist[position]).into(holder.imageView)
        holder.imageView!!.setOnClickListener{
            val imF = ImageViewer_Fragment()
            imF.set_imageUrlList(urllist, position)
            listAction!!.openImageView(imF)
            //(activity as MainActivity).switchFragment(context!!, imF, 2)
        }

        return grid
    }

    class ViewHolder {
        var imageView: ImageView? = null
        //var mInflater: LayoutInflater? = null
    }
}