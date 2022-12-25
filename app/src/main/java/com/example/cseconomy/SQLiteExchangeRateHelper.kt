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
import java.math.RoundingMode
import java.text.DecimalFormat

class SQLiteExchangeRateHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "cs_economy.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_EXCHANGE_RATES = "ExchangeRate"
        private const val ER_ID = "id"
        private const val ER_BASE = "base"
        private const val ER_CURRENCY_TO = "currency_to"
        private const val ER_EXCHANGE_RATE = "exchange_rate"
    }

    override fun onCreate(p0: SQLiteDatabase?) {
        val createTableExchangeRates = ("CREATE TABLE " + TABLE_EXCHANGE_RATES
                + "("
                    + ER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + ER_BASE + " TEXT,"
                    + ER_CURRENCY_TO + " TEXT,"
                    + ER_EXCHANGE_RATE + " REAL"
                + ")")

        p0?.execSQL(createTableExchangeRates)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        p0!!.execSQL("DROP TABLE IF EXISTS $TABLE_EXCHANGE_RATES")
        onCreate(p0)
    }

    //prida novy zaznam
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

    //update daneho kurzu podla meny
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

    //kontrola ci existuje dany kurz v databaze
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

    //premaze tabulku kurzov
    fun deleteAllExchangeRates(): Int {
        val db = this.writableDatabase

        val success = db.delete(TABLE_EXCHANGE_RATES, "", null)
        db.close()
        return success
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

    //vrati vsetky kurzy
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
}