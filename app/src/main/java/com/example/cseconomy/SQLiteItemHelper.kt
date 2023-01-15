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

class SQLiteItemHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "cs_economy.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_ITEMS = "Item"
        private const val ITEM_ID = "id"
        private const val ITEM_NAME = "name"
        private const val ITEM_MARKETABLE = "marketable"
        private const val ITEM_ICON_URL = "icon_url"
        private const val ITEM_LAST_PRICE = "last_price"

        private const val TABLE_EXCHANGE_RATES = "ExchangeRate"
        private const val ER_ID = "id"
        private const val ER_BASE = "base"
        private const val ER_CURRENCY_TO = "currency_to"
        private const val ER_EXCHANGE_RATE = "exchange_rate"

        private const val TABLE_FAV_ITEMS = "FavItem"
        private const val FAV_ID = "id"
        private const val FAV_ITEM_ID = "item_id"
    }

    override fun onCreate(p0: SQLiteDatabase?) {
        val createTableItems = ("CREATE TABLE " + TABLE_ITEMS
                + "("
                    + ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + ITEM_NAME + " TEXT,"
                    + ITEM_MARKETABLE + " TEXT,"
                    + ITEM_ICON_URL + " TEXT,"
                    + ITEM_LAST_PRICE + " REAL"
                + ")")

        val createTableFavItems = ("CREATE TABLE " + TABLE_FAV_ITEMS
                + "("
                + FAV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + FAV_ITEM_ID + " INTEGER"
                + ")")

        val createTableExchangeRates = ("CREATE TABLE " + TABLE_EXCHANGE_RATES
                + "("
                + ER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ER_BASE + " TEXT,"
                + ER_CURRENCY_TO + " TEXT,"
                + ER_EXCHANGE_RATE + " REAL"
                + ")")

        p0?.execSQL(createTableExchangeRates)
        p0?.execSQL(createTableFavItems)
        p0?.execSQL(createTableItems)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        p0!!.execSQL("DROP TABLE IF EXISTS $TABLE_ITEMS")
        onCreate(p0)
    }

    //pridanie noveho zaznamu
    fun insertItem(item: ItemModel): Long {
        val db = this.writableDatabase

        val contentValues = ContentValues()

        contentValues.put(ITEM_NAME, item.item_name)
        contentValues.put(ITEM_MARKETABLE, item.item_marketable)
        contentValues.put(ITEM_ICON_URL, item.item_icon_url)
        contentValues.put(ITEM_LAST_PRICE, item.item_last_price)

        val success = db.insert(TABLE_ITEMS,null,contentValues)
        db.close()
        return success
    }

    //update predmetu podla id predmetu
    fun updateItemById(item: ItemModel): Int {
        val db = this.writableDatabase

        val contentValues = ContentValues()

        contentValues.put(ITEM_ID, item.item_id)
        contentValues.put(ITEM_NAME, item.item_name)
        contentValues.put(ITEM_MARKETABLE, item.item_marketable)
        contentValues.put(ITEM_ICON_URL, item.item_icon_url)
        contentValues.put(ITEM_LAST_PRICE, item.item_last_price)

        val success = db.update(TABLE_ITEMS,contentValues,"id="+ item.item_id, null)
        db.close()
        return success
    }

    //update predmetu podla nazvu
    fun updateItemByName(item: ItemModel): Int {
        val db = this.writableDatabase

        val contentValues = ContentValues()

        //contentValues.put(ITEM_ID, item.item_id)
        contentValues.put(ITEM_NAME, item.item_name)
        contentValues.put(ITEM_MARKETABLE, item.item_marketable)
        contentValues.put(ITEM_ICON_URL, item.item_icon_url)
        contentValues.put(ITEM_LAST_PRICE, item.item_last_price)

        val success = db.update(TABLE_ITEMS,contentValues,"name='"
                + (item.item_name?.replace("'","''") ?: "") + "'", null)
        db.close()
        return success
    }

    //kontrola ci existuje v databaze predmet s danym nazvom
    fun checkItemExists(itemName: String?): Boolean {
        var result = false
        val db = this.readableDatabase
        val checkQuery = "SELECT * FROM $TABLE_ITEMS WHERE name='$itemName'"

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(checkQuery, null)
        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(checkQuery)
            return result
        }

        if (cursor.moveToFirst()) {
            do {
                result = true
            } while (cursor.moveToNext())
        }

        return result
    }

    //premazanie tabulky
    fun deleteAllItems(): Int {
        val db = this.writableDatabase

        val success = db.delete(TABLE_ITEMS, "", null)
        db.close()
        return success
    }

    //vrati vsetky predmety podla filtrov
    fun getAllItems(context: Context, filterWeaponWear:String?, filterItemName:String?, limit:Int): ArrayList<ItemModel> {
        val itemList: ArrayList<ItemModel> = ArrayList()

        var filterWeaponWearBD = filterWeaponWear
        if(filterWeaponWear == "Any")
            filterWeaponWearBD = ""

        val selectQuery = "SELECT * FROM $TABLE_ITEMS WHERE name LIKE '%$filterWeaponWearBD%' AND name LIKE '%$filterItemName%' LIMIT $limit ORDER BY $ITEM_NAME"
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