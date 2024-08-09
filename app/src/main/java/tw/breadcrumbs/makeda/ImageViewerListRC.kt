package tw.breadcrumbs.makeda

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.image_url_item.view.*

class ImageViewerListRC (val clickListener: (String) -> Unit) : androidx.recyclerview.widget.RecyclerView.Adapter<ImageViewerListRC.ViewHolder>() {
    var context : Context? = null
    var imageUrlList : MutableList<String>? = mutableListOf()
    var selIndex = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_url_item, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindPPModel(clickListener)
    }

    // 返回數目
    override fun getItemCount(): Int {
        return 1
    }

    fun setSelectIndex(urlList: MutableList<String>, index : Int) {
        imageUrlList = urlList
        selIndex = index
    }

    inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        fun bindPPModel(clickListener: (String) -> Unit) {
            Picasso
                .get()
                .load(imageUrlList!![selIndex])
                .into(itemView.imageviewer)//.centerCrop()
        }

    }
}