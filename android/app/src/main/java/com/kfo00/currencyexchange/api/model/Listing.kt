package com.kfo00.currencyexchange.api.model

import com.google.gson.annotations.SerializedName

class Listing {


    @SerializedName("listing_id")
    var listing_id :Int? = null

    @SerializedName("usd_amount")
    var usd_amount: Float? = null

    @SerializedName("rate")
    var rate: Float? = null

    @SerializedName("usd_to_lbp")
    var usd_to_lbp: Boolean?  = null

    @SerializedName("user_id")
    var user_name : String? = null


}