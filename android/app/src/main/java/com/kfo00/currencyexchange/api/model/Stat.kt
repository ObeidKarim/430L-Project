package com.kfo00.currencyexchange.api.model

import com.google.gson.annotations.SerializedName


class Stat {

    @SerializedName("volume")
    var volume: Float? = null

    @SerializedName("numberOfTransactions")
    var numberOfTransactions: Float? = null

    @SerializedName("max_usd_to_lbp")
    var max_uTol: Float? = null

    @SerializedName("median_usd_to_lbp")
    var median_uTol: Float? = null

    @SerializedName("stdev_usd_to_lbp")
    var stdev_uTol: Float? = null

    @SerializedName("mode_usd_to_lbp")
    var mode_uTol: Float? = null

    @SerializedName("variance_usd_to_lbp")
    var variance_uTol: Float? = null


    @SerializedName("max_lbp_to_usd")
    var max_lToU: Float? = null

    @SerializedName("median_lbp_to_usd")
    var median_lToU: Float? = null

    @SerializedName("stdev_lbp_to_usd")
    var stdev_lToU: Float? = null

    @SerializedName("mode_lbp_to_usd")
    var mode_lToU: Float? = null

    @SerializedName("variance_lbp_to_usd")
    var variance_lToU: Float? = null



}