package com.example.cseconomy

data class ItemModel(
    var item_id: Int = 0,
    var item_name: String? = "",
    var item_marketable: Boolean? = false,
    var item_icon_url: String? = "",
    var item_last_price: Float? = 0f
)