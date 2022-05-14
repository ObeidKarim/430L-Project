package com.kfo00.currencyexchange.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.highsoft.highcharts.common.hichartsclasses.*
import com.highsoft.highcharts.core.HIChartView
import com.kfo00.currencyexchange.R
import com.kfo00.currencyexchange.api.Authentication
import com.kfo00.currencyexchange.api.ExchangeService
import com.kfo00.currencyexchange.api.model.ExchangeRates
import com.kfo00.currencyexchange.api.model.Transaction
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList



class ExchangeFragment : Fragment() {

    private var buyUsdTextView: TextView? = null
    private var sellUsdTextView: TextView? = null
    private var fab: FloatingActionButton? = null
    private var transactionDialog: View? = null

//    var usersLayout: TextInputLayout? = null
//    var usersView: AutoCompleteTextView? = null

    //    calculator params
    private var buyRate:Float? = null
    private var sellRate:Float? = null

    private var fromLabel: TextView? = null
    private var toLabel: TextView? = null
    private var buyAmount: TextView? = null
    private var sellAmount: TextView? = null
    private var fromUSD: Boolean = true
    private var buttonToggle: ImageButton? = null
    private var buttonCalculate: Button? = null





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fetchRates()
    }

    override fun onResume() {
        super.onResume()
        fetchRates()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view: View = inflater.inflate(R.layout.fragment_exchange, container, false);
        buyUsdTextView = view.findViewById(R.id.txtBuyUsdRate)
        sellUsdTextView = view.findViewById(R.id.txtSellUsdRate)

//        calculator
        fromLabel = view.findViewById(R.id.fromLabel)
        toLabel = view.findViewById(R.id.toLabel)
        buyAmount = view.findViewById(R.id.buyResult)
        sellAmount = view.findViewById(R.id.sellResult)
        buttonToggle = view.findViewById(R.id.button_toggle)
        buttonCalculate = view.findViewById(R.id.button_calculate)

        buttonToggle?.setOnClickListener{ _ ->
            fromUSD = !fromUSD
            swapText()
        }

        buttonCalculate?.setOnClickListener{_ ->
            val amountToCalculate = view.findViewById<TextInputLayout>(R.id.amountInput)?.editText?.text.toString().toFloat()
            if (fromUSD){
                buyAmount?.text = "Buy: " + (amountToCalculate * buyRate!!) + " LBP"
                sellAmount?.text = "Sell: " + (amountToCalculate * sellRate!!) + " LBP"
            }
            else{
                buyAmount?.text = "Buy: " + (amountToCalculate / buyRate!!) + " USD"
                sellAmount?.text = "Buy: " + (amountToCalculate / sellRate!!) + " USD"
            }
        }

        fab = view.findViewById(R.id.fab)
        fab?.setOnClickListener { _ ->
            showDialog()
        }

        return  view
    }


    private fun fetchRates(){
        ExchangeService.exchangeApi().getExchangeRates().enqueue(object :
            Callback<ExchangeRates> {
            override fun onResponse(call: Call<ExchangeRates>, response:
            Response<ExchangeRates>
            ) {
                val responseBody: ExchangeRates? = response.body();
                buyRate = responseBody?.lbpToUsd
                sellRate = responseBody?.usdToLbp
                buyUsdTextView?.text = buyRate.toString() + " LBP"
                sellUsdTextView?.text = sellRate.toString() + " LBP"
            }
            override fun onFailure(call: Call<ExchangeRates>, t: Throwable) {
                return;

            }
        })
    }

    private fun addTransaction(transaction: Transaction) {

        ExchangeService.exchangeApi().addTransaction(transaction,
            if (Authentication.getToken() != null) "Bearer ${Authentication.getToken()}" else null)
            .enqueue(object : Callback<Any> {
                override fun onResponse(call: Call<Any>, response:
                Response<Any>) {
                    Snackbar.make(fab as View, "Transaction added!",
                        Snackbar.LENGTH_LONG)
                        .show()
                fetchRates()
                }
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    Snackbar.make(fab as View, "Could not add transaction.",
                        Snackbar.LENGTH_LONG)
                        .show()
                }
            })
    }

    private fun addUserTransaction(transaction: Transaction, username: String) {
        if (Authentication.getToken() != null){
            ExchangeService.exchangeApi().addUserTransaction(transaction,
                "Bearer ${Authentication.getToken()}", username)
                .enqueue(object : Callback<Any> {
                    override fun onResponse(call: Call<Any>, response:
                    Response<Any>) {
                        Snackbar.make(fab as View, "Transaction added!",
                            Snackbar.LENGTH_LONG)
                            .show()
                fetchRates()
                    }
                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        Snackbar.make(fab as View, "Could not add transaction.",
                            Snackbar.LENGTH_LONG)
                            .show()
                    }
                })
        }
    }

    private fun swapText(){
        if (fromUSD){
            fromLabel?.text = "From USD"
            toLabel?.text = "To LBP"
        }
        else {
            fromLabel?.text = "From LBP"
            toLabel?.text = "To USD"

        }
    }

    private fun showDialog() {
        transactionDialog = LayoutInflater.from(activity)
            .inflate(R.layout.dialog_transaction, null, false)

        val usersView = transactionDialog?.findViewById<TextView>(R.id.usersDropdown)
        val usersLayout = transactionDialog?.findViewById<TextInputLayout>(R.id.dropdownLayout)
        if (Authentication.getToken() != null) {
            usersLayout?.visibility = View.VISIBLE
//            setDropDown(usersView)
        }
        else {
            usersLayout?.visibility = View.GONE
        }


        MaterialAlertDialogBuilder(requireActivity()).setView(transactionDialog)
            .setTitle("Add Transaction")
            .setMessage("Enter transaction details")
            .setPositiveButton("Add") { dialog, _ ->
                val usdAmount = transactionDialog?.findViewById<TextInputLayout>(R.id.txtInptUsdAmount)?.editText?.text?.toString()?.toFloat()
                val lbpAmount = transactionDialog?.findViewById<TextInputLayout>(R.id.txtInptLbpAmount)?.editText?.text?.toString()?.toFloat()
                val radioGroup = transactionDialog?.findViewById<RadioGroup>(R.id.rdGrpTransactionType)


                val selectedButtonId = radioGroup?.getCheckedRadioButtonId()
                var usd_to_lbp = (selectedButtonId == R.id.rdBtnSellUsd)

                val trans = Transaction()
                trans.lbpAmount = lbpAmount
                trans.usdAmount = usdAmount
                trans.usdToLbp = usd_to_lbp

                if ((Authentication.getToken() != null) && (usersLayout?.editText?.text != null )){
                    addUserTransaction(trans, usersLayout?.editText?.text.toString())}
                else addTransaction(trans)


                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    //to set dropdown with usernames
//    private fun setDropDown(usersView : AutoCompleteTextView?){
//
//        var usernames: List<String>? = null
//
//        ExchangeService.exchangeApi().getUsers("Bearer ${Authentication.getToken()}").enqueue(object :
//            Callback<List<String>> {
//            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
//
//                usernames = response.body()!!
//                Log.d("TAG", usernames.toString())
//
//
//            }
//            override fun onFailure(call: Call<List<String>>, t: Throwable) {
//                return;
//
//            }
//        })
//
//        if (usernames != null){
//            val arrayAdapter = ArrayAdapter(transactionDialog!!.context,R.layout.user_dropdown_item, usernames!!)
//            usersView?.setAdapter(arrayAdapter)
//        }
//    }

}