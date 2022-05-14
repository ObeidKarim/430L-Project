package com.kfo00.currencyexchange.api.model

import com.google.gson.annotations.SerializedName



class Transaction(){

    @SerializedName("id")
    var id: Int? = null

    @SerializedName("usd_amount")
    var usdAmount: Float? = null

    @SerializedName("lbp_amount")
    var lbpAmount: Float? = null

    @SerializedName("usd_to_lbp")
    var usdToLbp: Boolean? = null

    @SerializedName("added_date")
    var addedDate: String? = null

    @SerializedName("user_name")
    var userName: String? = null



}