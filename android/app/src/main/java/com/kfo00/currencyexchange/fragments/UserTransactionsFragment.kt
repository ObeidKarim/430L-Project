package com.kfo00.currencyexchange.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.kfo00.currencyexchange.R
import com.kfo00.currencyexchange.api.Authentication
import com.kfo00.currencyexchange.api.ExchangeService
import com.kfo00.currencyexchange.api.model.Listing
import com.kfo00.currencyexchange.api.model.Transaction
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UserTransactions.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserTransactionsFragment : Fragment() {


    class UserTransactionAdapter(
        private val inflater: LayoutInflater,
        private val dataSource: List<Listing>
    ) : BaseAdapter() {


        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view: View = inflater.inflate(R.layout.item_listing, parent, false)
            view.findViewById<TextView>(R.id.listID).text =
                dataSource[position].listing_id.toString()
            view.findViewById<TextView>(R.id.usdAmount).text =
                dataSource[position].usd_amount.toString()
            view.findViewById<TextView>(R.id.rate).text = dataSource[position].rate.toString()
            view.findViewById<TextView>(R.id.usdToLbp).text = if (dataSource[position].usd_to_lbp!!) "Sell" else "Buy"
            view.findViewById<TextView>(R.id.userId).text = dataSource[position].user_name


            return view
        }

        override fun getItem(position: Int): Any {
            return dataSource[position]
        }

        override fun getItemId(position: Int): Long {
            return dataSource[position].listing_id?.toLong() ?: 0
        }

        override fun getCount(): Int {
            return dataSource.size
        }
    }


    private var listview: ListView? = null
    private var listings: ArrayList<Listing>? = ArrayList()
    private var adapter: UserTransactionAdapter? = null

    private var fab: FloatingActionButton? = null
    private var listingsDialog: View? = null
    private var acceptListingDialog: View? = null
    private var acceptButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fetchListings()
    }

    override fun onResume() {
        super.onResume()
        fetchListings()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_user_transactions, container, false)
        listview = view.findViewById(R.id.listview)
        adapter = UserTransactionAdapter(layoutInflater, listings!!)
        listview?.adapter = adapter

        fab = view.findViewById(R.id.fab)
        fab?.setOnClickListener { _ ->
            showDialog()
        }

        acceptButton = view.findViewById(R.id.button_accept)
        acceptButton?.setOnClickListener{_ ->
            showAcceptListingDialog()
        }
        return view
    }

    private fun fetchListings() {
        ExchangeService.exchangeApi().getListings().enqueue(object : Callback<List<Listing>> {
            override fun onFailure(call: Call<List<Listing>>, t: Throwable) {
                return
            }

            override fun onResponse(
                call: Call<List<Listing>>, response: Response<List<Listing>>
            ) {
                listings?.clear()
                listings?.addAll(response.body()!!)
                adapter?.notifyDataSetChanged()
            }
        })

    }

    private fun showDialog() {
        listingsDialog = LayoutInflater.from(activity)
            .inflate(R.layout.dialog_listing, null, false)


        MaterialAlertDialogBuilder(requireActivity()).setView(listingsDialog)
            .setTitle("Add Listing")
            .setMessage("Enter listing details")
            .setPositiveButton("Add") { dialog, _ ->
                val usdAmount =
                    listingsDialog?.findViewById<TextInputLayout>(R.id.txtInptUsdAmount)?.editText?.text?.toString()
                        ?.toFloat()
                val rate =
                    listingsDialog?.findViewById<TextInputLayout>(R.id.txtInptRate)?.editText?.text?.toString()
                        ?.toFloat()
                val radioGroup =
                    listingsDialog?.findViewById<RadioGroup>(R.id.rdGrpTransactionType)


                val selectedButtonId = radioGroup?.getCheckedRadioButtonId()
                var usd_to_lbp = (selectedButtonId == R.id.rdBtnSellUsd)

                val list = Listing()
                list.rate = rate
                list.usd_amount = usdAmount
                list.usd_to_lbp = usd_to_lbp

                addListing(list)


                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }


    private fun addListing(listing: Listing) {

        if (Authentication.getToken() != null) {
            ExchangeService.exchangeApi().addListing(
                listing,
                "Bearer ${Authentication.getToken()}"
            )
                .enqueue(object : Callback<Any> {
                    override fun onResponse(
                        call: Call<Any>, response:
                        Response<Any>
                    ) {
                        if (response.isSuccessful()){
                            Snackbar.make(
                                fab as View, "Listing added!",
                                Snackbar.LENGTH_LONG
                            )
                                .show()
                            fetchListings()}
                        else {
                            Snackbar.make(
                                fab as View, "Could not add listing.",
                                Snackbar.LENGTH_LONG
                            )
                                .show()
                        }

                    }

                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        Snackbar.make(
                            fab as View, "Could not add listing.",
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                    }
                })
        }
    }

    private fun showAcceptListingDialog() {

        acceptListingDialog = LayoutInflater.from(activity)
            .inflate(R.layout.dialog_accept_listing, null, false)


        MaterialAlertDialogBuilder(requireActivity()).setView(acceptListingDialog)
            .setTitle("Accept Listing")
            .setMessage("Enter the ID of the listing you want to accept")
            .setPositiveButton("Add") { dialog, _ ->
                val listing_id =
                    acceptListingDialog?.findViewById<TextInputLayout>(R.id.txtInptListingID)?.editText?.text?.toString()
                        ?.toInt()

                acceptListing(listing_id!!)

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun acceptListing(listing_id: Int) {

        if (Authentication.getToken() != null) {
            var listingWithID : Listing = Listing()
            listingWithID.listing_id = listing_id
            ExchangeService.exchangeApi().acceptListing("Bearer ${Authentication.getToken()}",listingWithID)
                .enqueue(object : Callback<Any> {
                    override fun onResponse(call: Call<Any>, response: Response<Any>
                    ) {
                        if (response.isSuccessful()){
                        Snackbar.make(
                            acceptButton as View, "Listing accepted!",
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                        fetchListings()}
                        else{
                            Snackbar.make(
                                acceptButton as View, "Could not accept listing.",
                                Snackbar.LENGTH_LONG
                            )
                                .show()

                        }

                    }

                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        Snackbar.make(
                            acceptButton as View, "Could not accept listing.",
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                    }
                })
        }
    }

}