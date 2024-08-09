package tw.breadcrumbs.makeda.CommentsHandler

import android.graphics.Canvas
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import android.util.Log
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.InputType
import android.text.method.ArrowKeyMovementMethod
import android.view.View
import android.widget.Button


class CmtsListAdapter : ItemTouchHelper.Callback() {
    private var buttonsActions: Comments_Fragment.CmtsCloudAction? = null
    var updateBtn: Button? =null
    var deleteIcon: Drawable? = null
    var editIcon: Drawable? = null

    fun swipeController(buttonsActions: Comments_Fragment.CmtsCloudAction) {
        this.buttonsActions = buttonsActions
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        var swipe = 0
        recyclerView.let {
            if (it.layoutManager is androidx.recyclerview.widget.LinearLayoutManager) {
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
        Log.i("CmtsListAdapter", " onSwiped : direction:$direction")
        //buttonsActions!!.deleteAction(viewHolder!!.adapterPosition)
        if (updateBtn != null) {
            updateBtn!!.visibility = View.VISIBLE
            updateBtn!!.isFocusable = true
            updateBtn!!.isClickable = true
            updateBtn!!.isCursorVisible = true
            updateBtn!!.isFocusableInTouchMode = true
            //isEnabled = true
            updateBtn!!.setTextIsSelectable(true)
            updateBtn!!.movementMethod = ArrowKeyMovementMethod.getInstance()
            updateBtn!!.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE//TYPE_CLASS_TEXT
            updateBtn!!.setOnClickListener{
                buttonsActions!!.editAction(viewHolder.adapterPosition)
            }
        }

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
        Log.i("CmtsListAdapter", " onChildDraw : ")
        //val adapter = recyclerView.adapter as CmtsListRC
        //val pplist = adapter.getItems()
        //val index = viewHolder.adapterPosition
        val optionIcon: Drawable?
        val colorDrawableBackground: ColorDrawable?
        optionIcon = editIcon
        colorDrawableBackground = ColorDrawable(Color.rgb(30, 203, 255))
        //optionIcon = deleteIcon
        //colorDrawableBackground = ColorDrawable(Color.rgb(255, 90, 90))
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