package com.example.cseconomy

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.media.MediaCodec
import android.provider.ContactsContract
import java.lang.Exception
import java.lang.reflect.Executable

class SQLiteHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

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

        val createTableExchangeRates = ("CREATE TABLE " + TABLE_EXCHANGE_RATES
                + "("
                    + ER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + ER_BASE + " TEXT,"
                    + ER_CURRENCY_TO + " TEXT,"
                    + ER_EXCHANGE_RATE + " REAL"
                + ")")

        val createTableFavItems = ("CREATE TABLE " + TABLE_FAV_ITEMS
                + "("
                + FAV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + FAV_ITEM_ID + " INTEGER"
                + ")")

        p0?.execSQL(createTableItems)
        p0?.execSQL(createTableExchangeRates)
        p0?.execSQL(createTableFavItems)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        p0!!.execSQL("DROP TABLE IF EXISTS $TABLE_ITEMS")
        p0!!.execSQL("DROP TABLE IF EXISTS $TABLE_EXCHANGE_RATES")
        p0!!.execSQL("DROP TABLE IF EXISTS $TABLE_FAV_ITEMS")
        onCreate(p0)
    }

    //Items
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

    fun updateItemByName(item: ItemModel): Int {
        val db = this.writableDatabase

        val contentValues = ContentValues()

        //contentValues.put(ITEM_ID, item.item_id)
        contentValues.put(ITEM_NAME, item.item_name)
        contentValues.put(ITEM_MARKETABLE, item.item_marketable)
        contentValues.put(ITEM_ICON_URL, item.item_icon_url)
        contentValues.put(ITEM_LAST_PRICE, item.item_last_price)

        val success = db.update(TABLE_ITEMS,contentValues,"name='" + item.item_name + "'", null)
        db.close()
        return success
    }

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

        if(cursor.moveToFirst())
            do {
                result = true
            } while (cursor.moveToNext())

            return result
    }

    fun deleteAllItems(): Int {
        val db = this.writableDatabase

        val success = db.delete(TABLE_ITEMS, "", null)
        db.close()
        return success
    }

    fun getAllItems(): ArrayList<ItemModel> {
        val itemList: ArrayList<ItemModel> = ArrayList()
        val selectQuery = "SELECT * FROM $TABLE_ITEMS"
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

        if(cursor.moveToFirst())
            do {
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                marketable = cursor.getString(cursor.getColumnIndexOrThrow("marketable")).toBoolean()
                icon_url = cursor.getString(cursor.getColumnIndexOrThrow("icon_url"))
                last_price = cursor.getString(cursor.getColumnIndexOrThrow("last_price")).toFloat()

                val item = ItemModel(item_id = id, item_name = name, item_marketable = marketable, item_icon_url = icon_url, item_last_price = last_price)
                itemList.add(item)
            } while (cursor.moveToNext())

        return itemList
    }

    //Exchange rates
    fun insertExchangeRate(rate: ExchangeRateModel): Long {
        val db = this.writableDatabase

        val contentValues = ContentValues()

        contentValues.put(ER_BASE, rate.base)
        contentValues.put(ER_CURRENCY_TO, rate.currency_to)
        contentValues.put(ER_EXCHANGE_RATE, rate.exchange_rate)

        val success = db.insert(TABLE_EXCHANGE_RATES,null, contentValues)
        db.close()
        return success
    }

    fun updateExchangeRateByCurrencyTo(rate: ExchangeRateModel): Int {
        val db = this.writableDatabase

        val contentValues = ContentValues()

        contentValues.put(ER_BASE, rate.base)
        contentValues.put(ER_CURRENCY_TO, rate.currency_to)
        contentValues.put(ER_EXCHANGE_RATE, rate.exchange_rate)

        val success = db.update(TABLE_EXCHANGE_RATES,contentValues,"currency_to='"+ rate.currency_to + "'", null)
        db.close()
        return success
    }

    fun checkExchangeRateExists(currencyTo: String?): Boolean {
        var result = false
        val db = this.readableDatabase
        val checkQuery = "SELECT * FROM $TABLE_EXCHANGE_RATES WHERE currency_to='$currencyTo'"

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

    fun deleteAllExchangeRates(): Int {
        val db = this.writableDatabase

        val success = db.delete(TABLE_EXCHANGE_RATES, "", null)
        db.close()
        return success
    }

    fun getAllExchangeRates(): ArrayList<ExchangeRateModel> {
        val exchangeRatesList: ArrayList<ExchangeRateModel> = ArrayList()
        val selectQuery = "SELECT * FROM $TABLE_EXCHANGE_RATES"
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
        var base: String
        var currency_to: String
        var exchange_rate: Float

        if(cursor.moveToFirst())
            do {
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                base = cursor.getString(cursor.getColumnIndexOrThrow("base"))
                currency_to = cursor.getString(cursor.getColumnIndexOrThrow("currency_to"))
                exchange_rate = cursor.getString(cursor.getColumnIndexOrThrow("exchange_rate")).toFloat()

                val rate = ExchangeRateModel(id = id, base = base, currency_to = currency_to, exchange_rate = exchange_rate)
                exchangeRatesList.add(rate)
            } while (cursor.moveToNext())

        return exchangeRatesList
    }

    //Fav items
    fun insertFavItem(itemId: Int): Long {
        val db = this.writableDatabase

        val contentValues = ContentValues()

        contentValues.put(FAV_ITEM_ID, itemId)

        val success = db.insert(TABLE_FAV_ITEMS,null, contentValues)
        db.close()
        return success
    }

    fun deleteFavItem(Id: Int): Int {
        val db = this.writableDatabase

        val success = db.delete(TABLE_FAV_ITEMS, "item_id=$Id", null)
        db.close()
        return success
    }

    fun deleteAllFavItems(): Int {
        val db = this.writableDatabase

        val success = db.delete(TABLE_FAV_ITEMS, "", null)
        db.close()
        return success
    }

    fun getAllFavItems(): ArrayList<ItemModel> {
        val itemList: ArrayList<ItemModel> = ArrayList()
        val selectQuery = "SELECT a.* FROM $TABLE_ITEMS a INNER JOIN $TABLE_FAV_ITEMS b ON a.id = b.item_id"
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

        if(cursor.moveToFirst())
            do {
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                marketable = cursor.getString(cursor.getColumnIndexOrThrow("marketable")).toBoolean()
                icon_url = cursor.getString(cursor.getColumnIndexOrThrow("icon_url"))
                last_price = cursor.getString(cursor.getColumnIndexOrThrow("last_price")).toFloat()

                val item = ItemModel(item_id = id, item_name = name, item_marketable = marketable, item_icon_url = icon_url, item_last_price = last_price)
                itemList.add(item)
            } while (cursor.moveToNext())

        return itemList
    }

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

}