package com.example.cseconomy

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.lang.Exception
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.roundToInt

class SQLiteFavItemHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "cs_economy.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_ITEM = "Item"
        private const val TABLE_EXCHANGE_RATES = "ExchangeRate"

        private const val TABLE_FAV_ITEMS = "FavItem"
        private const val FAV_ID = "id"
        private const val FAV_ITEM_ID = "item_id"
    }

    override fun onCreate(p0: SQLiteDatabase?) {
        val createTableFavItems = ("CREATE TABLE " + TABLE_FAV_ITEMS
                + "("
                + FAV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + FAV_ITEM_ID + " INTEGER"
                + ")")

        p0?.execSQL(createTableFavItems)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        p0!!.execSQL("DROP TABLE IF EXISTS $TABLE_FAV_ITEMS")
        onCreate(p0)
    }

    //pridanie noveho zaznamu
    fun insertFavItem(itemId: Int): Long {
        val db = this.writableDatabase

        val contentValues = ContentValues()

        contentValues.put(FAV_ITEM_ID, itemId)

        val success = db.insert(TABLE_FAV_ITEMS,null, contentValues)
        db.close()
        return success
    }

    //vymazanie predmetu podla id
    fun deleteFavItem(Id: Int): Int {
        val db = this.writableDatabase

        val success = db.delete(TABLE_FAV_ITEMS, "item_id=$Id", null)
        db.close()
        return success
    }

    //premazanie tabulky
    fun deleteAllFavItems(): Int {
        val db = this.writableDatabase

        val success = db.delete(TABLE_FAV_ITEMS, "", null)
        db.close()
        return success
    }

    //vrati vsetky oblubene predmety
    fun getAllFavItems(context: Context): ArrayList<ItemModel> {
        val itemList: ArrayList<ItemModel> = ArrayList()
        val selectQuery = "SELECT a.* FROM $TABLE_ITEM a INNER JOIN $TABLE_FAV_ITEMS b ON a.id = b.item_id"
        val db = this.readableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var id: Int
        var name: String
        var marketable: Boolean
        var icon_url: String
        var last_price: Float

        val sharedPreferences = context.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val userCurrency = sharedPreferences.getString("user_currency","EUR")
        val currencyExchangeRate = getExchangeRateForCurrency(userCurrency)

        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING

        if(cursor.moveToFirst())
            do {
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                marketable = cursor.getString(cursor.getColumnIndexOrThrow("marketable")).toBoolean()
                icon_url = cursor.getString(cursor.getColumnIndexOrThrow("icon_url"))
                last_price = (((cursor.getString(cursor.getColumnIndexOrThrow("last_price")).toFloat() * currencyExchangeRate) * 100.0).roundToInt() / 100.0).toFloat()

                val item = ItemModel(item_id = id, item_name = name, item_marketable = marketable, item_icon_url = icon_url, item_last_price = last_price, currency = userCurrency)
                itemList.add(item)
            } while (cursor.moveToNext())

        return itemList
    }

    //kontrola ci je predmet medzi oblubenymi
    fun itemIsFavourite(itemId: Int): Boolean {
        var result = false
        val db = this.readableDatabase
        val checkQuery = "SELECT * FROM $TABLE_FAV_ITEMS WHERE item_id='$itemId'"

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(checkQuery, null)
        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(checkQuery)
            return result
        }

        if(cursor.moveToFirst())
            do {
                result = true
            } while (cursor.moveToNext())

        return result
    }

    //vrati kurz pre danu menu
    fun getExchangeRateForCurrency(currency:String?) : Float {
        if (currency == "EUR")
            return 1f

        var exchange_rate = 0f
        val selectQuery = "SELECT exchange_rate FROM $TABLE_EXCHANGE_RATES WHERE currency_to='$currency'"

        val db = this.readableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(selectQuery)
            return exchange_rate
        }

        if(cursor.moveToFirst())
            do {
                exchange_rate = cursor.getString(cursor.getColumnIndexOrThrow("exchange_rate")).toFloat()
            } while (cursor.moveToNext())

        return exchange_rate
    }
}