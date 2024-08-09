package tw.breadcrumbs.makeda.DBModules

/**
 * Created by brian on 2019/10/11.
 */
import java.util.ArrayList

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import tw.breadcrumbs.makeda.dataModel.TripPlan_PPModel

class OpenTripPlanDB_PPModelDAO (context: Context) {

    // 資料庫物件
    //private val db: SQLiteDatabase
    private val db: SQLiteDatabase? = DBHelper.getDatabase(context)

    // 讀取所有記事資料
    val all: List<TripPlan_PPModel>
        get() {
            val result = ArrayList<TripPlan_PPModel>()
            val cursor = db!!.query(
                TABLE_NAME, null, null, null, null, null, null, null)

            while (cursor.moveToNext()) {
                result.add(getRecord(cursor))
            }

            cursor.close()
            return result
        }

    fun cleanall() {
        //db!!.execSQL("DROP TABLE IF EXISTS " + OpenTripPlanDB_PPModelDAO.TABLE_NAME)
        db!!.execSQL("delete from "+ TABLE_NAME);
        //val where = "$TRIPPLANID=$tripPlanID"
        // 刪除指定編號資料並回傳刪除是否成功
        //return db!!.delete()//delete(TABLE_NAME, where, null) > 0
    }


    // 取得資料數量
    val count: Int
        get() {
            var result = 0
            val cursor = db!!.rawQuery("SELECT COUNT(*) FROM $TABLE_NAME", null)

            if (cursor.moveToNext()) {
                result = cursor.getInt(0)
            }

            return result
        }

    init {
        //db = DBHelper.getDatabase(context)
    }

    // 關閉資料庫，一般的應用都不需要修改
    fun close() {
        db!!.close()
    }

    // 新增參數指定的物件
    // 新增參數指定的物件
    fun insert(item: TripPlan_PPModel): TripPlan_PPModel? {

        // 建立準備新增資料的ContentValues物件
        val cv = ContentValues()
        cv.put(CLOUDID, item.cloudID)
        cv.put(NAME_COLUMN, item.name)
        cv.put(PHONE_COLUMN, item.phone)
        cv.put(COUNTRY_COLUMN, item.country)
        cv.put(ADDRESS_COLUMN, item.addr)
        cv.put(FB_COLUMN, item.fb)
        cv.put(WEBSITE_COLUMN, item.web)
        cv.put(BLOGINFO_COLUMN, item.blogInfo)
        cv.put(OPENTIME_COLUMN, item.opentime)
        cv.put(TAG_NOTE_COLUM, item.tag_note)
        cv.put(DESCRIPTION_COLUM, item.descrip)
        cv.put(PIC_URL_COLUM, item.pic_url)
        cv.put(SCORE_COLUM, item.score)
        cv.put(STATUS_COLUM, item.status)
        cv.put(TRIPPLANID, item.tripPlanID)
        cv.put(SETTRIPPLANITEM, item.setTripPlanItem)
        cv.put(STATUSINTRIPPLAN, item.statusInTripPlan)

        // 新增一筆資料並取得編號
        // 第一個參數是表格名稱
        // 第二個參數是沒有指定欄位值的預設值
        // 第三個參數是包裝新增資料的ContentValues物件
        val id = db!!.insert(TABLE_NAME, null, cv)

        // 設定編號
        item.id = id
        // 回傳結果
        return item
    }

    // 修改參數指定的物件
    fun update(item: TripPlan_PPModel): Boolean {
        // 建立準備修改資料的ContentValues物件
        val cv = ContentValues()
        // 加入ContentValues物件包裝的修改資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
        // 其它表格欄位名稱
        cv.put(CLOUDID, item.cloudID)
        cv.put(NAME_COLUMN, item.name)
        cv.put(PHONE_COLUMN, item.phone)
        cv.put(COUNTRY_COLUMN, item.country)
        cv.put(ADDRESS_COLUMN, item.addr)
        cv.put(FB_COLUMN, item.fb)
        cv.put(WEBSITE_COLUMN, item.web)
        cv.put(BLOGINFO_COLUMN, item.blogInfo)
        cv.put(OPENTIME_COLUMN, item.opentime)
        cv.put(TAG_NOTE_COLUM, item.tag_note)
        cv.put(DESCRIPTION_COLUM, item.descrip)
        cv.put(PIC_URL_COLUM, item.pic_url)
        cv.put(SCORE_COLUM, item.score)
        cv.put(STATUS_COLUM, item.status)
        cv.put(TRIPPLANID, item.tripPlanID)
        cv.put(SETTRIPPLANITEM, item.setTripPlanItem)
        cv.put(STATUSINTRIPPLAN, item.statusInTripPlan)

        // 設定修改資料的條件為編號
        // 格式為「欄位名稱＝資料」
        val where = KEY_ID + "=" + item.id

        // 執行修改資料並回傳修改的資料數量是否成功
        return db!!.update(TABLE_NAME, cv, where, null) > 0
    }

    // 刪除參數指定編號的資料
    fun delete(id: Long): Boolean {
        // 設定條件為編號，格式為「欄位名稱=資料」
        val where = "$KEY_ID=$id"
        // 刪除指定編號資料並回傳刪除是否成功
        return db!!.delete(TABLE_NAME, where, null) > 0
    }

    fun deleteUsingCloudID(cloudId: Long, tripPlanID: Long): Boolean {
        // 設定條件為編號，格式為「欄位名稱=資料」
        val where = "$CLOUDID=$cloudId AND $TRIPPLANID=$tripPlanID"
        // 刪除指定編號資料並回傳刪除是否成功
        return db!!.delete(TABLE_NAME, where, null) > 0
    }

    fun deleteTripPlanPPList(tripPlanID: Long): Boolean {
        // 設定條件為編號，格式為「欄位名稱=資料」
        val where = "$TRIPPLANID=$tripPlanID"
        // 刪除指定編號資料並回傳刪除是否成功
        return db!!.delete(TABLE_NAME, where, null) > 0
    }

    fun getItems(tripPlanID: Long) : List<String>? {
        val ret : ArrayList<String>? = arrayListOf()
        val result = db!!.rawQuery("SELECT * FROM " + TABLE_NAME +
                " WHERE tripPlanID =" + "'" + tripPlanID+"'", null)
        while (result.moveToNext()) {
            val tripPP = getRecord(result)
            var same = 0
            ret!!.forEach {
                if (it == tripPP.setTripPlanItem) same = 1
            }
            if (same == 0) {
                ret.add(tripPP.setTripPlanItem)
            }
        }

        result.close()

        return ret
    }

    fun getItemX(tripPlanID: Long, item: String) : List<TripPlan_PPModel>? {
        val ret : ArrayList<TripPlan_PPModel>? = arrayListOf()
        val result = db!!.rawQuery("SELECT * FROM " + TABLE_NAME +
                " WHERE tripPlanID =" + "'" + tripPlanID +"' AND setTripPlanItem ='" + item + "'", null)
        while (result.moveToNext()) {
            ret!!.add(getRecord(result))
        }

        result.close()

        return ret
    }

    fun findTripPlan(tripplanID: Long) : List<TripPlan_PPModel>? {
        var item: ArrayList<TripPlan_PPModel>? = arrayListOf()
        val result = db!!.rawQuery("SELECT * FROM " + TABLE_NAME +
                " WHERE tripPlanID = '" + tripplanID +"'", null)
        // 如果有查詢結果
        while (result.moveToNext()) {
            // 讀取包裝一筆資料的物件
            //item = getRecord(result)
            item!!.add(getRecord(result))
        }
        result.close()

        return item
    }

    fun get_useCloudID(cloudID : Long, tripPlanID: Long) : TripPlan_PPModel? {
        var item: TripPlan_PPModel? = null
        val where = "$CLOUDID=$cloudID AND $TRIPPLANID=$tripPlanID"
        // 執行查詢
        val result = db!!.query(
            TABLE_NAME, null, where, null, null, null, null, null)

        // 如果有查詢結果
        if (result.moveToFirst()) {
            // 讀取包裝一筆資料的物件
            item = getRecord(result)
        }

        // 關閉Cursor物件
        result.close()
        // 回傳結果
        return item
    }

    // 取得指定編號的資料物件
    operator fun get(id: Long): TripPlan_PPModel? {
        // 準備回傳結果用的物件
        var item: TripPlan_PPModel? = null
        // 使用編號為查詢條件
        val where = "$KEY_ID=$id"
        // 執行查詢
        val result = db!!.query(
            TABLE_NAME, null, where, null, null, null, null, null)

        // 如果有查詢結果
        if (result.moveToFirst()) {
            // 讀取包裝一筆資料的物件
            item = getRecord(result)
        }

        // 關閉Cursor物件
        result.close()
        // 回傳結果
        return item
    }

    // 把Cursor目前的資料包裝為物件
    fun getRecord(cursor: Cursor): TripPlan_PPModel {
        // 回傳結果
        return TripPlan_PPModel(
            cursor.getLong(0), //id
            cursor.getLong(1), //id
            cursor.getString(2), // name
            cursor.getString(3), // phone
            cursor.getString(4), // country
            cursor.getString(5), // addr
            cursor.getString(6), // fb
            cursor.getString(7), // web
            cursor.getString(8), // bloginfo
            cursor.getString(9), // opentime
            cursor.getString(10), // tag_note
            cursor.getString(11), // descrip
            cursor.getString(12), // pic_url
            cursor.getInt(13), // score
            cursor.getInt(14), // status
            cursor.getFloat(15),
            false,
            cursor.getLong(17), // tripplan ID
            cursor.getString(18), // setTripPlanItem
            cursor.getInt(19) //statusInTripPlan
        ) // contentJsonFormat)
    }

    companion object {
        // 表格名稱
        val TABLE_NAME = "makeda_open_tripplandb_ppmodel"
        // 編號表格欄位名稱，固定不變
        val KEY_ID = "_id"

        // 其它表格欄位名稱
        val CLOUDID = "cloudID"
        val NAME_COLUMN = "name"
        val PHONE_COLUMN = "phone"
        val COUNTRY_COLUMN = "country"
        val ADDRESS_COLUMN = "addr"
        val FB_COLUMN = "fb"
        val WEBSITE_COLUMN = "web"
        val BLOGINFO_COLUMN = "blogInfo"
        val OPENTIME_COLUMN = "opentime"
        val DESCRIPTION_COLUM = "descrip"
        val TAG_NOTE_COLUM = "tag_note"
        val PIC_URL_COLUM = "pic_url"
        val SCORE_COLUM = "score"
        val STATUS_COLUM = "status"
        val TRIPPLANID = "tripPlanID"
        val SETTRIPPLANITEM = "setTripPlanItem"
        val STATUSINTRIPPLAN = "statusInTripPlan"

        // 使用上面宣告的變數建立表格的SQL指令
        val CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CLOUDID + " INTEGER DEFAULT 0, " +
                NAME_COLUMN + " TEXT NOT NULL, " +
                PHONE_COLUMN + " TEXT NOT NULL, " +
                COUNTRY_COLUMN + " TEXT NOT NULL, " +
                ADDRESS_COLUMN + " TEXT NOT NULL, " +
                FB_COLUMN + " TEXT NOT NULL, " +
                WEBSITE_COLUMN + " TEXT NOT NULL, " +
                BLOGINFO_COLUMN + " TEXT NOT NULL, " +
                OPENTIME_COLUMN + " TEXT NOT NULL, " +
                TAG_NOTE_COLUM + " TEXT NOT NULL, " +
                DESCRIPTION_COLUM + " TEXT NOT NULL, " +
                PIC_URL_COLUM + " TEXT NOT NULL, " +
                SCORE_COLUM + " INTEGER NOT NULL, " +
                STATUS_COLUM + " INTEGER NOT NULL, " +
                TRIPPLANID + " INTEGER DEFAULT 0, " +
                SETTRIPPLANITEM + " TEXT NOT NULL, " +
                STATUSINTRIPPLAN + " INTEGER DEFAULT 0)"
    }
}



