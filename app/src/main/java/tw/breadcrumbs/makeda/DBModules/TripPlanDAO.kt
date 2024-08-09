package tw.breadcrumbs.makeda.DBModules

/**
 * Created by brian on 2019/10/11.
 */
import java.util.ArrayList

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import tw.breadcrumbs.makeda.dataModel.TripListModel

class TripPlanDAO (context: Context) {

    // 資料庫物件
    //private val db: SQLiteDatabase
    private val db: SQLiteDatabase? = DBHelper.getDatabase(context)

    // 讀取所有記事資料
    val all: List<TripListModel>
        get() {
            val result = ArrayList<TripListModel>()
            val cursor = db!!.query(
                TABLE_NAME, null, null, null, null, null, null, null)

            while (cursor.moveToNext()) {
                result.add(getRecord(cursor))
            }

            cursor.close()
            return result
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
    fun insert(item: TripListModel): TripListModel? {

        val exist: TripListModel? = findName(item.planname)
        if (exist != null) {

            return null
        }
        // 建立準備新增資料的ContentValues物件
        val cv = ContentValues()

        // 加入ContentValues物件包裝的新增資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
        cv.put(PLANNAME_COLUMN, item.planname)
        cv.put(AUTHOR_COLUMN, item.author)
        cv.put(GROUP_COLUMN, item.grouplist)
        cv.put(CONTENTJSONFORMAT_COLUMN, item.contentJsonFormat)
        cv.put(CLOUD_CONTENT, item.cloudContent)
        cv.put(TRIPPLAN_CLOUD_ID, item.tripplanID)
        cv.put(LOG_COLUMN, item.log)
        cv.put(UPDATED, item.updated)

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
    fun update(item: TripListModel): Boolean {
        // 建立準備修改資料的ContentValues物件
        val cv = ContentValues()
        // 加入ContentValues物件包裝的修改資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
        // 其它表格欄位名稱
        cv.put(PLANNAME_COLUMN, item.planname)
        cv.put(AUTHOR_COLUMN, item.author)
        cv.put(GROUP_COLUMN, item.grouplist)
        cv.put(CONTENTJSONFORMAT_COLUMN, item.contentJsonFormat)
        cv.put(CLOUD_CONTENT, item.cloudContent)
        cv.put(TRIPPLAN_CLOUD_ID, item.tripplanID)
        cv.put(LOG_COLUMN, item.log)
        cv.put(UPDATED, item.updated)
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

    fun find(searchText:String) : List<TripListModel> {
        val item: ArrayList<TripListModel> = arrayListOf()
        val result = db!!.rawQuery("SELECT * FROM " + TABLE_NAME +
                " WHERE planname LIKE" + "'%" + searchText +"%'", null)

        while (result.moveToNext()) {
            item.add(getRecord(result))
        }
        result.close()
        return item
    }

    fun findName(planname: String) : TripListModel? {
        var item: TripListModel? = null
        //val where = "$PLANNAME_COLUMN=$planname"
        val result = db!!.rawQuery("SELECT * FROM " + TABLE_NAME +
                " WHERE planname =" + "'" + planname +"'", null)
        // 如果有查詢結果
        if (result.moveToFirst()) {
            // 讀取包裝一筆資料的物件
            item = getRecord(result)
        }
        result.close()
        return item
    }

    fun get_useTripPlanID(tripplanID: Long): TripListModel? {
        var item: TripListModel? = null
        // 使用編號為查詢條件
        val where = "$TRIPPLAN_CLOUD_ID=$tripplanID"
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
    operator fun get(id: Long): TripListModel? {
        // 準備回傳結果用的物件
        var item: TripListModel? = null
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

    fun updateTripplanID(planname: String, tripplanID : String) :Boolean? {
        val exist: TripListModel = findName(planname) ?: return false
        val cv = ContentValues()

        // 加入ContentValues物件包裝的新增資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
        cv.put(PLANNAME_COLUMN, exist.planname)
        cv.put(AUTHOR_COLUMN, exist.author)
        cv.put(GROUP_COLUMN, exist.grouplist)
        cv.put(CONTENTJSONFORMAT_COLUMN, exist.contentJsonFormat)
        cv.put(CLOUD_CONTENT, exist.cloudContent)
        cv.put(TRIPPLAN_CLOUD_ID, tripplanID)
        cv.put(LOG_COLUMN, exist.log)
        cv.put(UPDATED, exist.updated)

        // 新增一筆資料並取得編號
        // 第一個參數是表格名稱
        // 第二個參數是沒有指定欄位值的預設值
        // 第三個參數是包裝新增資料的ContentValues物件
        val id = db!!.insert(TABLE_NAME, null, cv)
        if (id < 0) return false

        return true
    }

    // 把Cursor目前的資料包裝為物件
    fun getRecord(cursor: Cursor): TripListModel {
        // 回傳結果
        return TripListModel(
            cursor.getLong(0), //id
            cursor.getString(1), // planname
            cursor.getString(2), // author
            cursor.getString(3), // grouplist
            cursor.getString(4), // content
            cursor.getString(5), // content
            cursor.getString(6), // tripplanID
            cursor.getString(7), // log
            cursor.getInt(8) //update
        ) // contentJsonFormat)
    }

    companion object {
        // 表格名稱
        val TABLE_NAME = "makeda_triplists"
        // 編號表格欄位名稱，固定不變
        val KEY_ID = "_id"

        // 其它表格欄位名稱
        val PLANNAME_COLUMN = "planname"
        val AUTHOR_COLUMN = "author"
        val GROUP_COLUMN = "grouplist"
        val CONTENTJSONFORMAT_COLUMN = "contentJsonFormat"
        val CLOUD_CONTENT = "cloudContent"
        val TRIPPLAN_CLOUD_ID = "tripplanID"
        val LOG_COLUMN = "log"
        val UPDATED = "updated"

        // 使用上面宣告的變數建立表格的SQL指令
        val CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PLANNAME_COLUMN + " TEXT NOT NULL, " +
                AUTHOR_COLUMN + " TEXT NOT NULL, " +
                GROUP_COLUMN + " TEXT NOT NULL, " +
                CONTENTJSONFORMAT_COLUMN + " TEXT NOT NULL, " +
                CLOUD_CONTENT + " TEXT NOT NULL, " +
                TRIPPLAN_CLOUD_ID + " TEXT NOT NULL, " +
                LOG_COLUMN + " TEXT NOT NULL," +
                UPDATED + " INTEGER)"
    }
}



