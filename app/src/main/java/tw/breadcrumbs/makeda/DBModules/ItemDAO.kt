package tw.breadcrumbs.makeda.DBModules

/**
 * Created by brian on 2017/11/23.
 */
import java.util.ArrayList

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import tw.breadcrumbs.makeda.dataModel.PPModel

class ItemDAO (context: Context) {
    private val debugmode = false
    // 資料庫物件
    //private val db: SQLiteDatabase
    private val db: SQLiteDatabase = DBHelper.getDatabase(context)

    // 讀取所有記事資料
    val all: List<PPModel>
        get() {
            val result = ArrayList<PPModel>()
            val cursor = db.query(
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
            val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_NAME", null)

            if (cursor.moveToNext()) {
                result = cursor.getInt(0)
            }

            return result
        }

    init {
        //db = DBHelper.ge`
    }

    // 關閉資料庫，一般的應用都不需要修改
    fun close() {
        db.close()
    }

    // 新增參數指定的物件
    // 新增參數指定的物件
    fun insert(item: PPModel): PPModel? {
        if (item.name.isEmpty()) {
            return null
        }
        val exist: PPModel? = findName(item.name)
        if (exist != null) {

            return null
        }
        val calDone = if (item.calDistanceDone) { 1 }
        else { 0 }
        // 建立準備新增資料的ContentValues物件
        val cv = ContentValues()

        // 加入ContentValues物件包裝的新增資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
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
        cv.put(DISTANCE_COLUM, item.distance)
        cv.put(DISTANCE_CAL_DONE_COLUM, calDone)

        // 新增一筆資料並取得編號
        // 第一個參數是表格名稱
        // 第二個參數是沒有指定欄位值的預設值
        // 第三個參數是包裝新增資料的ContentValues物件
        val id = db.insert(TABLE_NAME, null, cv)

        // 設定編號
        item.id = id
        // 回傳結果
        return item
    }

    // 修改參數指定的物件
    fun update(item: PPModel): Boolean {
        val calDone = if (item.calDistanceDone) { 1 }
        else { 0 }
        // 建立準備修改資料的ContentValues物件
        val cv = ContentValues()
        // 加入ContentValues物件包裝的修改資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
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
        cv.put(DISTANCE_COLUM, item.distance)
        cv.put(DISTANCE_CAL_DONE_COLUM, calDone)
        // 設定修改資料的條件為編號
        // 格式為「欄位名稱＝資料」
        val where = KEY_ID + "=" + item.id

        // 執行修改資料並回傳修改的資料數量是否成功
        return db.update(TABLE_NAME, cv, where, null) > 0
    }

    // 刪除參數指定編號的資料
    fun delete(id: Long): Boolean {
        // 設定條件為編號，格式為「欄位名稱=資料」
        val where = "$KEY_ID=$id"
        // 刪除指定編號資料並回傳刪除是否成功
        return db.delete(TABLE_NAME, where, null) > 0
    }

    // 讀取
    fun getSelect(country: String, city: String): List<PPModel> {
        val result = ArrayList<PPModel>()
        if (debugmode) Log.i("ITEMDAO: ", "get input : $country,  $city")
        val cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME +
                " WHERE country=" + "'" + country + " | "+ city +"'", null)

        while (cursor.moveToNext()) {
            result.add(getRecord(cursor))
        }

        cursor.close()
        return result
    }

    fun find(searchText:String) : List<PPModel> {
        val item: ArrayList<PPModel> = arrayListOf()
        val result = db.rawQuery("SELECT * FROM " + TABLE_NAME +
                " WHERE name LIKE" + "'%" + searchText +"%'", null)

        while (result.moveToNext()) {
            item.add(getRecord(result))
        }
        result.close()
        return item
    }

    fun findName(name: String) : PPModel? {
        var item: PPModel? = null
        //val where = "$NAME_COLUMN=$name"
        val result = db.rawQuery("SELECT * FROM " + TABLE_NAME +
                " WHERE name LIKE" + "'" + name +"'", null)
        // 如果有查詢結果
        if (result.moveToFirst()) {
            // 讀取包裝一筆資料的物件
            item = getRecord(result)
        }
        result.close()
        return item
    }

    // 取得指定編號的資料物件
    operator fun get(id: Long): PPModel? {
        // 準備回傳結果用的物件
        var item: PPModel? = null
        // 使用編號為查詢條件
        val where = "$KEY_ID=$id"
        // 執行查詢
        val result = db.query(
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
    fun getRecord(cursor: Cursor): PPModel {
        val calDone = cursor.getInt(16) > 0
        // 回傳結果
        return PPModel(
            cursor.getLong(0), //id
            cursor.getLong(1), //cloudID
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
            cursor.getInt(14),
            cursor.getFloat(15),
            calDone
        )
    }

    /* 建立範例資料
    fun sample() {
        //val item = PPModel( 1, "麵包屑&三合院", "0975474378",
        //        "台灣 | 彰化縣", "花壇鄉金墩村金福街111號", "https://www.breadcrumbs.tw",
        //        "https://www.facebook.com/breadcrumbs.tw", " ", "14:00-19:30 | 星期一二休",
        //    "#花壇鄉 #三合院 #食 #咖啡 #蛋糕 #下午茶 #甜點", "在老家中的三合院老宅，製作樸實的美味",
        //    " ", 0, 0)
        var item: PPModel = PPModel(
            1,0,"麵包屑&三合院", "0975474378",
            "台灣 | 彰化縣", "花壇鄉金墩村金福街111號", "https://www.breadcrumbs.tw",
            "https://www.facebook.com/breadcrumbs.tw", " ", "14:00-19:30 | 星期一二休",
            "#花壇鄉 #三合院 #食 #咖啡 #蛋糕 #下午茶 #甜點", "在老家中的三合院老宅，製作樸實的美味",
            " ", 0, 0
        )

        insert(item)

    }*/

    companion object {
        // 表格名稱
        val TABLE_NAME = "makeda_pps"
        // 編號表格欄位名稱，固定不變
        val KEY_ID = "_id"
        /*constructor(id: Long, datetime: Long, name: String, phone: String, country: String, addr: String,
                    fb: String, website: String, blogInfo: String, opentime: String, desp: String, tag_note: String,
                    pic_url:String, score:Int, status:Int)*/
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
        val DISTANCE_COLUM = "distance"
        val DISTANCE_CAL_DONE_COLUM = "calDistanceDone"

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
                DISTANCE_COLUM + " REAL DEFAULT 0, " +
                DISTANCE_CAL_DONE_COLUM + " INTEGER DEFAULT 0)"
    }
}
