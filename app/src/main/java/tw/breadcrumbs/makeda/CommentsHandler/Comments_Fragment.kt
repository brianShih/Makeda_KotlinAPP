package tw.breadcrumbs.makeda.CommentsHandler

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import kotlinx.android.synthetic.main.comments_fragment.*
import org.json.JSONObject
import tw.breadcrumbs.makeda.CloudObj.Cloud_Helper
import tw.breadcrumbs.makeda.Dash_Fragment
import tw.breadcrumbs.makeda.MainActivity
import tw.breadcrumbs.makeda.R
import tw.breadcrumbs.makeda.Setting_Fragment
import tw.breadcrumbs.makeda.dataModel.CmtModel
import tw.breadcrumbs.makeda.dataModel.PPModel
import java.net.URLEncoder


class Comments_Fragment : androidx.fragment.app.Fragment() {
    private val CloudHelperURL = "//TODO"
    var cmtsDonwloading = false
    private var cmtsBackBtn: ImageButton? = null
    private var cmt_content: EditText? = null
    private var cmtAddBtn: Button? = null
    private var cmtListRC : CmtsListRC? = null
    var cloudComments : MutableList<CmtModel>? = mutableListOf()//CmtModel
    var firstLayerComments: MutableList<CmtModel>? = mutableListOf()//CmtModel
    var set_frag: Setting_Fragment? = null
    private var user_name :String? = null
    private var user_email :String? = null
    private var ppModel: PPModel? = null
    private var cloudCmdDWNHelper: Cloud_Helper? = null
    private var cloudCmdUPDHelper: Cloud_Helper? = null
    private var cloudCmdADDHelper: Cloud_Helper? = null
    private var cloudCmdDELHelper: Cloud_Helper? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.comments_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (ppModel == null) {
            val dash_f = (activity as MainActivity).dash_frag as Dash_Fragment
            (activity as MainActivity).switchFragment(this@Comments_Fragment, dash_f, 2)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

    }

    fun setPPModel(pp: PPModel) {
        ppModel = pp
    }

    fun getCmtsDownloadStatus() : Boolean {
        return cmtsDonwloading
    }

    fun getCmtsCount() : Int {
        cloudComments!!.let {
            return cloudComments!!.count()
        }
    }

    override fun onStart() {
        super.onStart()
        set_frag = (activity as MainActivity).setting_frag as Setting_Fragment
        if (set_frag!!.local_setting_isReady()){
            user_name = set_frag!!.prefs!!.getString(set_frag!!.user_name_pref, "") as String
            user_email = set_frag!!.prefs!!.getString(set_frag!!.user_email_pref, "") as String
        }
        setup_view()

    }

    fun delCmt(position: Int, cmtsModel : List<CmtModel>) {
        if (user_email!!.isEmpty() && user_name!!.isEmpty()) {
            Toast.makeText(this@Comments_Fragment.context, "請先登入", Toast.LENGTH_LONG).show()
            return
        }
        val email = user_email
        val cmtModel = cmtsModel[position]
        val cloud_cmd = "//TODO"
        Log.i("Comments_Fragment","delCmt : delete commentID = ${cmtsModel[position].commentID}")
        Log.i("Comments_Fragment","delCmt : cloud_cmd $cloud_cmd")
        cloudCmdDELHelper = Cloud_Helper(this@Comments_Fragment.context!!) {
            if (it == null) {
                return@Cloud_Helper
            }
            val cloud_db = JSONObject(it)
            if (!cloud_db.isNull("status")) {
                if (!cloud_db.isNull("count")) {
                    cloudComments = mutableListOf()
                    val cloud_count = cloud_db.get("count") as Int
                    for (i in 1..cloud_count) {
                        val tempDB = cloud_db.get(i.toString()) as JSONObject
                        val cmtModel = CmtModel(
                            tempDB.get("commentID") as Int,
                            tempDB.get("comment_post_ID") as Int,
                            tempDB.get("comment_parent_ID") as Int,
                            tempDB.get("comment_author").toString(),
                            tempDB.get("comment_author_email").toString(),
                            tempDB.get("comment_content").toString(),
                            tempDB.get("comment_date").toString()
                        )
                        Log.i(
                            "Comments_Fragment",
                            " insert to cloudComments: index: $i / ${tempDB.get("comment_author_email").toString()}"
                        )
                        cloudComments!!.add(cmtModel)
                    }
                    for (t in cloudComments!!) {
                        if (t.comment_parent_ID == 0) {
                            var same = 0
                            for (f in firstLayerComments!!) {
                                if (f.comment_author_email == t.comment_author_email) {
                                    same = 1
                                }
                            }
                            if (same == 0) {
                                firstLayerComments!!.add(t)
                            }
                        }
                    }
                    recyclerViewLoad()
                }
            }
        }
        cloudCmdDELHelper!!.execute("POST", CloudHelperURL, cloud_cmd)
    }

    fun addCmt(commentContent:String) {
        if (user_email!!.isEmpty() || user_name!!.isEmpty()) {
            Toast.makeText(this@Comments_Fragment.context,
                R.string.un_login_alert_message_zh, Toast.LENGTH_LONG).show()
            return
        }
        val email = user_email
        val reply_id = 0
        //py = {'CMD' : 'ADD_PP_COM', 'email':'qfeel0215@gmail.com','name' : u'麵包屑&三合院', 'reply_id':'0','comment':'測試測試01'}
        val cloud_cmd = "CMD=//TODO&email=$email&" +
                "name=${URLEncoder.encode(ppModel!!.name, "UTF-8")}" +
                "&reply_id=$reply_id&comment=$commentContent"
        Log.i("Comments_Fragment","addCmt : cloud_cmd $cloud_cmd")
        cloudCmdADDHelper = Cloud_Helper(this@Comments_Fragment.context!!) {
            if (it == null) {
                Toast.makeText(
                    this@Comments_Fragment.context,
                    R.string.cmt_add_failure_zh,
                    Toast.LENGTH_LONG
                ).show()
                return@Cloud_Helper
            }
            val cloud_db = JSONObject(it)
            if (!cloud_db.isNull("status")) {
                if (cloud_db.get("status") as Boolean == false) {
                    val msg = cloud_db.get("message") as String
                    if (msg == "User already Had comment on this PP") {
                        Toast.makeText(this@Comments_Fragment.context, "您已評論過喔！", Toast.LENGTH_LONG).show()
                        return@Cloud_Helper
                    }
                }
                cmt_content!!.setText("")
                Toast.makeText(
                    this@Comments_Fragment.context,
                    R.string.cmt_add_success_zh,
                    Toast.LENGTH_LONG
                ).show()
                if (!cloud_db.isNull("count")) {
                    cloudComments = mutableListOf()
                    val cloud_count = cloud_db.get("count") as Int
                    for (i in 1..cloud_count) {
                        val tempDB = cloud_db.get(i.toString()) as JSONObject
                        val cmtModel = CmtModel(
                            tempDB.get("commentID") as Int,
                            tempDB.get("comment_post_ID") as Int,
                            tempDB.get("comment_parent_ID") as Int,
                            tempDB.get("comment_author").toString(),
                            tempDB.get("comment_author_email").toString(),
                            tempDB.get("comment_content").toString(),
                            tempDB.get("comment_date").toString()
                        )
                        //Log.i("Comments_Fragment", " insert to cloudComments: index: $i / ${tempDB.get("comment_author_email").toString()}")
                        cloudComments!!.add(cmtModel)
                    }
                    for (t in cloudComments!!) {
                        if (t.comment_parent_ID == 0) {
                            var same = 0
                            for (f in firstLayerComments!!) {
                                if (f.comment_author_email == t.comment_author_email) {
                                    same = 1
                                }
                            }
                            if (same == 0) {
                                firstLayerComments!!.add(t)
                            }
                        }
                    }
                    recyclerViewLoad()
                    Toast.makeText(
                        this@Comments_Fragment.context,
                        R.string.cmt_add_OK_get_exp_zh,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        cloudCmdADDHelper!!.execute("POST", CloudHelperURL, cloud_cmd)
    }

    fun downloadCmts(context: Context, internal : Boolean) {
        cloudComments = mutableListOf()
        firstLayerComments = mutableListOf()
        val cloud_cmd = "CMD=//TODO&name=${URLEncoder.encode(ppModel!!.name, "UTF-8")}"
        Log.i("Comments_Fragment", " Download comments from Cloud")
        cloudCmdDWNHelper = Cloud_Helper(context) {
            if (it == null) {
                return@Cloud_Helper
            }

            val cloud_db = JSONObject(it)
            if (!cloud_db.isNull("count")) {
                val cloud_count = cloud_db.get("count") as Int
                for (i in 1..cloud_count) {
                    val tempDB = cloud_db.get(i.toString()) as JSONObject
                    val cmtModel = CmtModel(
                        tempDB.get("commentID") as Int,
                        tempDB.get("comment_post_ID") as Int,
                        tempDB.get("comment_parent_ID") as Int,
                        tempDB.get("comment_author").toString(),
                        tempDB.get("comment_author_email").toString(),
                        tempDB.get("comment_content").toString(),
                        tempDB.get("comment_date").toString()
                    )
                    Log.i(
                        "Comments_Fragment",
                        " insert to cloudComments: index: $i / ${tempDB.get("comment_author_email").toString()}"
                    )
                    cloudComments!!.add(cmtModel)
                }
                for (t in cloudComments!!) {
                    if (t.comment_parent_ID == 0) {
                        var same = 0
                        for (f in firstLayerComments!!) {
                            if (f.comment_author_email == t.comment_author_email) {
                                same = 1
                            }
                        }
                        if (same == 0) {
                            firstLayerComments!!.add(t)
                        }
                    }
                }
                if (internal) {
                    recyclerViewLoad()
                }
            }
            cmtsDonwloading = false
        }
        cmtsDonwloading = true
        cloudCmdDWNHelper!!.execute("POST", CloudHelperURL, cloud_cmd)
    }

    fun updateCmts() {
        downloadCmts(this@Comments_Fragment.context!!, true)

        val locThread = Thread(Runnable {
            while (true) {
                if (cloudComments!!.count() > 0) break
                Thread.sleep((2 * 1000).toLong())
            }
            commentsRecyclerView?.post {
                if (cloudComments!!.count() > 0) {
                    for (t in cloudComments!!) {
                        if (t.comment_parent_ID == 0) {
                            var same = 0
                            for (f in firstLayerComments!!) {
                                if (f.comment_author_email == t.comment_author_email) {
                                    same = 1
                                }
                            }
                            if (same == 0) {
                                firstLayerComments!!.add(t)
                            }
                        }
                    }
                    recyclerViewLoad()
                }
            }
        })
        locThread.start()
    }

    fun recyclerViewLoad() {
        commentsRecyclerView.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this@Comments_Fragment.context!!)
        cmtListRC = CmtsListRC(
            firstLayerComments!!,
            { partItem: CmtModel -> cmt_onClick(partItem) })
        if (!(user_email!!.isEmpty() && user_name!!.isEmpty())) {
            cmtListRC!!.setUserData(user_email!!, user_name!!)
        }
        val action = CmtsCloudAction(firstLayerComments!!)
        cmtListRC!!.setCommentsCloudAction(action)
        commentsRecyclerView.adapter = cmtListRC
    }

    fun setup_view() {
        cmt_content = view!!.findViewById(R.id.comment_content)

        cmtAddBtn = view!!.findViewById(R.id.comment_add)
        cmtAddBtn!!.setOnClickListener {
            if (user_email!!.isEmpty() && user_name!!.isEmpty()){
                Toast.makeText(this@Comments_Fragment.context, "請先登入", Toast.LENGTH_LONG).show()
            } else {
                addCmt(cmt_content!!.text.toString())
                updateCmts()
            }
        }
        cmtsBackBtn = view!!.findViewById(R.id.comments_backButton)
        cmtsBackBtn!!.setOnClickListener {
            if (cloudCmdADDHelper != null) {
                cloudCmdADDHelper!!.cancel(true)
            }
            if (cloudCmdUPDHelper != null) {
                cloudCmdUPDHelper!!.cancel(true)
            }
            if (cloudCmdDWNHelper != null) {
                cloudCmdDWNHelper!!.cancel(true)
            }
            cloudCmdADDHelper = null
            cloudCmdUPDHelper = null
            cloudCmdDWNHelper = null
            (activity as MainActivity).onBackPressed()
        }

        //selectImgBtn.setOnClickListener() {
        //    val intent = Intent()
        //    intent.type = "image/*"
        //    intent.action = Intent.ACTION_GET_CONTENT
        //    startActivityForResult(Intent.createChooser(intent, "Select Image1 From Gallery"), 1)
        //}


        updateCmts()
    }


    fun cmt_onClick(partItem : CmtModel) {

    }

    inner class CmtsCloudAction(val comments: List<CmtModel>) {

        fun updateCmt(cmtModel : CmtModel) {
            //py = {'CMD' : 'EDIT_PP_COM', 'email':'qfeel0215@gmail.com','name' : u'麵包屑&三合院', 'id':'25','comment':'回應-測試測試01 + 更新測試'}
            if (user_email!!.isEmpty() && user_name!!.isEmpty()) {
                Toast.makeText(this@Comments_Fragment.context,
                    R.string.un_login_alert_message_zh, Toast.LENGTH_LONG).show()
                return
            }
            val email = user_email
            //val cmtModel = cmtsModel[position]
            val cloud_cmd = "CMD=//TODO&" +
                    "name=${URLEncoder.encode(ppModel!!.name, "UTF-8")}" +
                    "&id=${cmtModel.commentID}" +
                    "&email=$email" +
                    "&comment=${cmtModel.comment_content}"
            cloudCmdUPDHelper = Cloud_Helper(this@Comments_Fragment.context!!) {
                if (it == null) {
                    Toast.makeText(
                        this@Comments_Fragment.context,
                        R.string.cmt_update_failure_zh,
                        Toast.LENGTH_LONG
                    ).show()
                    return@Cloud_Helper
                }
                val cloud_db = JSONObject(it)
                if (!cloud_db.isNull("status")) {
                    if (cloud_db.get("status") as Boolean) {
                        updateCmts()
                    } else {
                        Toast.makeText(
                            this@Comments_Fragment.context,
                            R.string.cmt_update_failure_zh,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                Toast.makeText(
                    this@Comments_Fragment.context,
                    R.string.cmt_update_success_zh,
                    Toast.LENGTH_LONG
                ).show()
            }
            cloudCmdUPDHelper!!.execute("POST", CloudHelperURL, cloud_cmd)
        }

        fun editAction(position: Int) {
            //updateCmt(position, comments)
        }

        //fun deleteAction(position: Int) {
        //    delCmt(position, comments)
        //}

    }
}