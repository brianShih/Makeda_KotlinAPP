package tw.breadcrumbs.makeda.CommentsHandler

import androidx.recyclerview.widget.RecyclerView
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import kotlinx.android.synthetic.main.cmts_rc_item.view.*
import tw.breadcrumbs.makeda.DBModules.ItemDAO
import tw.breadcrumbs.makeda.R
import tw.breadcrumbs.makeda.dataModel.CmtModel


class CmtsListRC (val cmtsModel:List<CmtModel>, val clickListener: (CmtModel) -> Unit) : androidx.recyclerview.widget.RecyclerView.Adapter<CmtsListRC.ViewHolder>() {
    //var onLineIcon: Drawable? = null
    //var newIcon: Drawable? = null
    //var localIcon: Drawable? =null
    var item_dao : ItemDAO? = null
    var user_email:String? = null
    var user_name:String? = null
    private var cmtsActions: Comments_Fragment.CmtsCloudAction? = null
    //var updateFun : Comments_Fragment.updateCmt? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cmts_rc_item, parent, false)
        Log.i("CmtsListRC", "onCreateViewHolder")
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindCmtModel( cmtsModel[position], clickListener)
    }

    fun setCommentsCloudAction(Actions: Comments_Fragment.CmtsCloudAction){
        cmtsActions = Actions
    }

    fun setUserData(email:String, name:String) {
        user_email = email
        user_name = name
    }

    fun setItemDAO(dao: ItemDAO) {
        item_dao = dao
    }

    fun getItems(): List<CmtModel> {
        return cmtsModel
    }

    // 返回數目
    override fun getItemCount(): Int {
        return cmtsModel.size
    }

    // view
    inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

        private fun enableEditText(editText: EditText) {
            Log.i("CmtsListRC : ", "enableEditText working")
            with(editText) {
                isFocusable = true
                isClickable = true
                isCursorVisible = true
                //isFocusableInTouchMode = true
                //isEnabled = true
                setTextIsSelectable(true)
                //movementMethod = ArrowKeyMovementMethod.getInstance()
                //inputType = type//InputType.TYPE_CLASS_TEXT
            }
        }

        private fun disableEditText(editText: EditText) {
            with(editText) {
                isFocusable = false
                isClickable = false
                isCursorVisible = false
                //view!!.clearFocus()
                //movementMethod = null //ArrowKeyMovementMethod.getInstance()
                //val imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                //imm.hideSoftInputFromWindow(editText.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        }

        fun bindCmtModel(cmtModel: CmtModel, clickListener: (CmtModel) -> Unit) {
            cmtModel.let {
                Log.i("CmtsListRC", "bindCmtModel : ${it.comment_author_email}")
                itemView.commenter.text = it.comment_author + " | " + it.comment_date
                itemView.comment_content.setText(it.comment_content)
                disableEditText(itemView.comment_content)
                if (user_email != cmtModel.comment_author_email) {
                    itemView.cmt_update_btn.visibility = View.GONE
                } else {
                    itemView.cmt_update_btn.visibility = View.VISIBLE
                    enableEditText(itemView.comment_content)//, InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                    itemView.cmt_update_btn.setOnClickListener{
                        cmtModel.comment_content = itemView.comment_content.text.toString()
                        cmtsActions!!.updateCmt(cmtModel)
                    }
                }

                itemView.setOnClickListener{
                    clickListener(cmtModel)
                }
            }
        }
    }
}