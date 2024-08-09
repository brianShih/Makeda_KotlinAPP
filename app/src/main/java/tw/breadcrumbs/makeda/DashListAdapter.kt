package tw.breadcrumbs.makeda

import android.graphics.Canvas
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import android.util.Log
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable


class DashListAdapter : ItemTouchHelper.Callback() {
    private val debugmode = false
    private var buttonsActions: ListAction? = null
    var deleteIcon: Drawable? = null
    var saveIcon: Drawable? = null
    fun swipeController(buttonsActions: ListAction) {
        this.buttonsActions = buttonsActions
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        var swipe = 0
        recyclerView.let {
            if (recyclerView.layoutManager is androidx.recyclerview.widget.LinearLayoutManager) {
                swipe = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            }
        }
        return makeFlag(ACTION_STATE_SWIPE, swipe)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {

        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        if (debugmode)
            Log.i("DashListAdapter", " onSwiped : direction:$direction")
        buttonsActions!!.onSwiped(viewHolder.adapterPosition)
    }
    override fun onChildDraw(
        c: Canvas,
        recyclerView: androidx.recyclerview.widget.RecyclerView,
        viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (viewHolder.adapterPosition < 0) return
        if (debugmode)
            Log.i("DashListAdapter", " onChildDraw : ")
        val adapter = recyclerView.adapter as DashListRC
        val pplist = adapter.getItems()
        val index = viewHolder.adapterPosition
        val optionIcon: Drawable?
        val colorDrawableBackground: ColorDrawable?
        if (pplist[index].id.toInt() == 0) {
            optionIcon = saveIcon
            colorDrawableBackground = ColorDrawable(Color.rgb(30, 203, 255))
        } else {
            optionIcon = deleteIcon
            colorDrawableBackground = ColorDrawable(Color.rgb(255, 90, 90))
        }
        val iconMarginVertical = (viewHolder.itemView.height - optionIcon!!.intrinsicHeight) / 2
        val itemView = viewHolder.itemView

        if (dX > 0) {
            colorDrawableBackground.setBounds(itemView.left, itemView.top, dX.toInt(), itemView.bottom)
            optionIcon.setBounds(itemView.left + iconMarginVertical, itemView.top + iconMarginVertical,
                itemView.left + iconMarginVertical + optionIcon.intrinsicWidth, itemView.bottom - iconMarginVertical)
        } else {
            colorDrawableBackground.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
            optionIcon.setBounds(itemView.right - iconMarginVertical - optionIcon.intrinsicWidth, itemView.top + iconMarginVertical,
                itemView.right - iconMarginVertical, itemView.bottom - iconMarginVertical)
            optionIcon.level = 0
        }

        colorDrawableBackground.draw(c)

        c.save()

        if (dX > 0)
            c.clipRect(itemView.left, itemView.top, dX.toInt(), itemView.bottom)
        else
            c.clipRect(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)

        optionIcon.draw(c)

        c.restore()

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

}