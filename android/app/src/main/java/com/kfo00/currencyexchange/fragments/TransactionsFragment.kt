package com.kfo00.currencyexchange.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import com.kfo00.currencyexchange.R
import com.kfo00.currencyexchange.api.Authentication
import com.kfo00.currencyexchange.api.ExchangeService
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
 * Use the [TransactionsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TransactionsFragment : Fragment() {

    class TransactionAdapter(
        private val inflater: LayoutInflater,
        private val dataSource: List<Transaction>
    ) : BaseAdapter() {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view: View = inflater.inflate(R.layout.item_transaction, parent, false)
            view.findViewById<TextView>(R.id.transID).text = dataSource[position].id.toString()
            view.findViewById<TextView>(R.id.lbpAmount).text = dataSource[position].lbpAmount.toString()
            view.findViewById<TextView>(R.id.usdAmount).text = dataSource[position].usdAmount.toString()
            view.findViewById<TextView>(R.id.transDate).text = dataSource[position].addedDate
            view.findViewById<TextView>(R.id.usdToLbp).text = dataSource[position].usdToLbp.toString()
            view.findViewById<TextView>(R.id.userId).text = dataSource[position].userName
            return view
        }

        override fun getItem(position: Int): Any {
            return dataSource[position]
        }

        override fun getItemId(position: Int): Long {
            return dataSource[position].id?.toLong() ?: 0
        }

        override fun getCount(): Int {
            return dataSource.size
        }
    }

    private var listview: ListView? = null
    private var transactions: ArrayList<Transaction>? = ArrayList()
    private var adapter: TransactionAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    fetchTransactions()
    }

    override fun onResume() {
        super.onResume()
        fetchTransactions()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_transactions, container, false)
        listview = view.findViewById(R.id.listview)
        adapter = TransactionAdapter(layoutInflater, transactions!!)
        listview?.adapter = adapter
        return view
    }

    private fun fetchTransactions() {
        if (Authentication.getToken() != null) {
            ExchangeService.exchangeApi().getTransactions("Bearer ${Authentication.getToken()}")
                .enqueue(object : Callback<List<Transaction>> {
                    override fun onFailure(call: Call<List<Transaction>>, t: Throwable) {
                        return
                    }
                    override fun onResponse(call: Call<List<Transaction>>, response: Response<List<Transaction>>
                    ) {
                        transactions?.clear()
                        transactions?.addAll(response.body()!!)
                        adapter?.notifyDataSetChanged()
                    }
                })
        }
    }


}