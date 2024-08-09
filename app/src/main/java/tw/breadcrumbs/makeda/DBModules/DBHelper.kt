package tw.breadcrumbs.makeda.DBModules

/**
 * Created by brian on 2017/11/23.
 */

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context, name: String,
               factory: CursorFactory?, version: Int)
    : SQLiteOpenHelper(context, name, factory, version) {

    override fun onCreate(db: SQLiteDatabase) {
        // 建立應用程式需要的表格
        db.execSQL(ItemDAO.CREATE_TABLE)
        db.execSQL(TripPlanDAO.CREATE_TABLE)
        db.execSQL(TripPlanDB_PPModelDAO.CREATE_TABLE)
        db.execSQL(OpenTripPlanDB_PPModelDAO.CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // 刪除原有的表格
        db.execSQL("DROP TABLE IF EXISTS " + ItemDAO.TABLE_NAME)
        db.execSQL("DROP TABLE IF EXISTS " + TripPlanDAO.TABLE_NAME)
        db.execSQL("DROP TABLE IF EXISTS " + TripPlanDB_PPModelDAO.TABLE_NAME)
        db.execSQL("DROP TABLE IF EXISTS " + OpenTripPlanDB_PPModelDAO.TABLE_NAME)
        // 呼叫onCreate建立新版的表格
        onCreate(db)
    }

    companion object {

        // 資料庫名稱
        val DATABASE_NAME = "makeda_local.db"
        // 資料庫版本，資料結構改變的時候要更改這個數字，通常是加一
        val VERSION = 7

        // 需要資料庫的元件呼叫這個函式，這個函式在一般的應用都不需要修改
        fun getDatabase(context: Context): SQLiteDatabase {
            return DBHelper(
                context,
                DATABASE_NAME,
                null,
                VERSION
            ).writableDatabase
        }

    }
}
