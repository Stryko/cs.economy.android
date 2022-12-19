package com.example.cseconomy

import com.google.gson.annotations.SerializedName

data class ExchangeRatesData (
    @SerializedName("rates") var rates : List<ExchangeRateItem> = arrayListOf()
)

data class ExchangeRateItem (
    @SerializedName("id") var id         : String?  = null,
    @SerializedName("dt") var dt         : String?  = null,
    @SerializedName("base") var base       : String?  = null,
    @SerializedName("currencyTo") var currencyTo : String? = null,
    @SerializedName("exchangeRate1") var exchangeRate1  : Float? = null
)