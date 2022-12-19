package com.example.cseconomy

data class ExchangeRateModel(
    var id: Int = 0,
    var base: String? = "",
    var currency_to: String? = "",
    var exchange_rate: Float? = 0f
)