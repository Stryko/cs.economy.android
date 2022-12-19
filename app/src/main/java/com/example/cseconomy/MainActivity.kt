package com.example.cseconomy

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.gson.GsonBuilder
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var sqlManager: SQLiteHelperItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        Handler().postDelayed({
            val intent = Intent(this@MainActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)

        sqlManager = SQLiteHelperItem(this)

        //LoadItemsFromApi()

        //LoadExchangeRatesFromApi()

        //getInventoryItems()

        val items = sqlManager.getAllItems()
        val exchangeRates = sqlManager.getAllExchangeRates()
    }

    //vrati list predmetov v inventari uzivatela, 730 kod hry csgo
    //aj na vlastnej api je nastavene zavolanie si vysledku inventaru z hry podle id uzivatela alebo podla uzivatelskeho mena
    fun getInventoryItems() {
        val apiCallUrl = "https://steamcommunity.com/profiles/76561198078598973/inventory/json/730/2"

        val request = Request.Builder().url(apiCallUrl).build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val body = response?.body?.string()?.replace("\"rgCurrency\":[],","")
                println(body)

                val gson = GsonBuilder().create()

                val results = gson.fromJson(body, InventoryData::class.java)
            }

            override fun onFailure(call: Call, e: IOException) {
                println("api call failed")
            }
        })
    }

    //vrati z api vsetky predmety pri nacitani aplikacie a updatuje si svoju databazu internu
    fun LoadItemsFromApi() {
        val apiCallUrl = "http://csgoeconomy-api.somee.com/Items/3"

        val request = Request.Builder().url(apiCallUrl).build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback{
            override fun onResponse(call: Call, response: Response) {
                val body = "{\"items\":" + response?.body?.string() + "}"
                println(body)

                val gson = GsonBuilder().create()

                val results = gson.fromJson(body, CSGOData::class.java)
                UpdateItemsDatabase(results)
            }

            override fun onFailure(call: Call, e: IOException) {
                println("api call failed: " + e.toString())
            }
        })
    }

    //update kurzov
    fun LoadExchangeRatesFromApi() {
        val apiCallUrl = "http://csgoeconomy-api.somee.com/ExchangeRates"

        val request = Request.Builder().url(apiCallUrl).build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback{
            override fun onResponse(call: Call, response: Response) {
                val body = "{\"rates\":" + response?.body?.string() + "}"
                println(body)

                val gson = GsonBuilder().create()

                val results = gson.fromJson(body, ExchangeRatesData::class.java)
                UpdateExchangeRatesDatabase(results)
            }

            override fun onFailure(call: Call, e: IOException) {
                println("api call failed: " + e.toString())
            }
        })
    }

    //ulozenie nacitanych predmetov do databazy
    fun UpdateItemsDatabase(itemsApi: CSGOData) {
        itemsApi.items.forEach {
            val item = ItemModel(
            item_name = it.name,
            item_marketable = it.marketable,
            item_icon_url = it.iconUrl,
            item_last_price = it.lastPrice)

            if (sqlManager.checkItemExists(it.name))
                sqlManager.updateItemByName(item)
            else
                sqlManager.insertItem(item)
        }
    }

    //ulozenie nacitanych kurzov z eur na ostatne meny
    fun UpdateExchangeRatesDatabase(exchangeRates: ExchangeRatesData) {
        exchangeRates.rates.forEach {
            val rate = ExchangeRateModel(
                base = it.base,
                currency_to = it.currencyTo,
                exchange_rate = it.exchangeRate1)

            if (sqlManager.checkExchangeRateExists(it.currencyTo))
                sqlManager.updateExchangeRateByCurrencyTo(rate)
            else
                sqlManager.insertExchangeRate(rate)
        }
    }
}