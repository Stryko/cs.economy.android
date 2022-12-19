package com.example.cseconomy

import com.google.gson.annotations.SerializedName

data class CSGOData (
    @SerializedName("items") var items : List<CSGOItem> = arrayListOf()
)

data class CSGOItem (
    @SerializedName("id") var id         : String?  = null,
    @SerializedName("dt") var dt         : String?  = null,
    @SerializedName("name") var name       : String?  = null,
    @SerializedName("marketable") var marketable : Boolean? = null,
    @SerializedName("iconUrl") var iconUrl    : String?  = null,
    @SerializedName("lastPrice") var lastPrice  : Float?     = null
)