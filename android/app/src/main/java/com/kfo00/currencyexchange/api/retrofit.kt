package com.kfo00.currencyexchange.api


import com.kfo00.currencyexchange.api.model.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

object ExchangeService {
    private const val API_URL: String = "http://192.168.1.28:5000"
    fun exchangeApi():Exchange {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        // Create an instance of our Exchange API interface.
        return retrofit.create(Exchange::class.java);
    }
    interface Exchange {
        @GET("/exchangeRate")
        fun getExchangeRates(): Call<ExchangeRates>

        @POST("/transaction")
        fun addTransaction(@Body transaction: Transaction,
                           @Header("Authorization") authorization: String?): Call<Any>

        @GET("/userTransactions")
        fun getTransactions(@Header("Authorization") authorization: String): Call<List<Transaction>>

        @POST("/user")
        fun addUser(@Body user: User): Call<User>

        @POST("/authentication")
        fun authenticate(@Body user:User): Call<Token>

        @GET("/graph2")
        fun getCoordinates():Call<List<Coordinate>>

        @GET("/users")
        fun getUsers(@Header("Authorization") authorization:String):Call<List<String>>

        @POST("/userTransaction/{username}")
        fun addUserTransaction(@Body transaction:Transaction,
                               @Header("Authorization")authorization: String,
                                @Path("username") username:String?): Call<Any>

        @GET("/statistics")
        fun getStats():Call<Stat>

        @GET("/listings")
        fun getListings():Call<List<Listing>>

        @POST("/listing")
        fun addListing(@Body listing:Listing,
                       @Header("Authorization")authorization:String):Call<Any>

        @POST("/acceptListing")
        fun acceptListing(@Header("Authorization")authorization:String,
                            @Body listing: Listing):Call<Any>


    }




}