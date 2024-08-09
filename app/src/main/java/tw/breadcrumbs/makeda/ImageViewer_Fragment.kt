package tw.breadcrumbs.makeda

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.imageviewer_layout.*

class ImageViewer_Fragment : androidx.fragment.app.Fragment() {
    var imageUrlList :  MutableList<String>? = mutableListOf()
    var imageListRC: ImageViewerListRC? = null
    var list_index = 0;

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.imageviewer_layout, container, false)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        closeButtonSetup()
        if (imageUrlList!!.count() > 0) {
            //Picasso.get().load(imageUrlList!![list_index]).into(imageviewer)
            ImageRCSetup()
        }
    }

    fun ImageRCSetup() {
        if (imageUrlList != null) {
            imageRecyclerView.layoutManager =
                androidx.recyclerview.widget.LinearLayoutManager(context)
            imageListRC = ImageViewerListRC({ partItem: String -> imageOnClick() })
            imageListRC!!.setSelectIndex(imageUrlList!!, list_index)
            imageRecyclerView.adapter = imageListRC
            val itemTouchHelperCallback =
                object :
                    ItemTouchHelper.SimpleCallback(
                        0,
                        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                    ) {
                    override fun onMove(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                    ): Boolean {

                        return false
                    }

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        Log.i("ImageViewer_Fragment", " direction : $direction")
                        if (direction == ItemTouchHelper.RIGHT) {
                            if (imageListRC != null) {
                                if (list_index < imageUrlList!!.count() - 1)
                                    list_index += 1
                                else if (list_index == imageUrlList!!.count() - 1) {
                                    list_index = 0
                                }
                                imageListRC!!.setSelectIndex(imageUrlList!!, list_index)
                                imageListRC!!.notifyDataSetChanged()
                            }
                        } else if (direction == ItemTouchHelper.LEFT) {
                            if (imageListRC != null) {
                                if (list_index > 0)
                                    list_index -= 1
                                else if (list_index == 0) {
                                    list_index = imageUrlList!!.count() - 1
                                }
                                imageListRC!!.setSelectIndex(imageUrlList!!, list_index)
                                imageListRC!!.notifyDataSetChanged()
                            }
                        }
                    }

                }

            val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
            itemTouchHelper.attachToRecyclerView(imageRecyclerView)
        } else {
            val dash_f = (activity as MainActivity).dash_frag as Dash_Fragment
            (activity as MainActivity).switchFragment(this@ImageViewer_Fragment, dash_f, 2)
        }
    }

    fun imageOnClick() {

    }

    fun closeButtonSetup() {
        viewer_close_btn.setOnClickListener{
            (activity as MainActivity).onBackPressed()
        }
    }

    fun set_imageUrlList( list:MutableList<String>, selIndex : Int) {
        imageUrlList = list
        list_index = selIndex
    }

}