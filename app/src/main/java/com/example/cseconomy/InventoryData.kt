package com.example.cseconomy

import com.google.gson.annotations.SerializedName

data class InventoryData (
    @SerializedName("success" ) var success : Boolean,
    @SerializedName("rgInventory") var rgInventory : Map<String, InventoryItem>,
    //@SerializedName("rgInventory") var rgCurrency : List<String>,
    @SerializedName("rgDescription") var rgDescription : Map<String, InventoryItemDescription>,
    @SerializedName("more") var more : Boolean,
    @SerializedName("more_start") var more_start : Boolean
)

data class InventoryItem (
    @SerializedName("id") var id : Long,
    @SerializedName("classid") var classid : Long,
    @SerializedName("instanceid") var instanceid : String,
    @SerializedName("amount") var amount : String,
    @SerializedName("hide_in_china") var hide_in_china : Int,
    @SerializedName("pos") var pos : Int
)

data class InventoryItemDescription (
    @SerializedName("appid") var appid : String,
    @SerializedName("classid") var classid : Int,
    @SerializedName("instanceid") var instanceid : Long,
    @SerializedName("icon_url") var icon_url : String,
    @SerializedName("icon_url_large") var icon_url_large : String,
    @SerializedName("icon_drag_url") var icon_drag_url : String,
    @SerializedName("name") var name : String,
    @SerializedName("market_hash_name") var market_hash_name : String,
    @SerializedName("market_name") var market_name : String,
    @SerializedName("name_color") var name_color : String,
    @SerializedName("background_color") var background_color : String,
    @SerializedName("type") var type : String,
    @SerializedName("tradable") var tradable : Boolean,
    @SerializedName("marketable") var marketable : Boolean,
    @SerializedName("commodity") var commodity : Boolean,
    @SerializedName("market_tradable_restriction") var market_tradable_restriction : String,
)