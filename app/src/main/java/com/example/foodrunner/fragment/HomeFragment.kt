package com.example.foodrunner.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.foodrunner.adapter.HomeRecyclerAdapter
import com.example.foodrunner.R
import com.example.foodrunner.model.HomeRestaurant
import com.example.foodrunner.util.ConnectionManager
import org.json.JSONException
import kotlinx.android.synthetic.main.sort_radio_button.view.*
import java.util.*
import kotlin.Comparator
import kotlin.collections.HashMap

class HomeFragment(val contextParam: Context) : Fragment() {

    lateinit var recyclerHome: RecyclerView
    lateinit var layoutManager: LinearLayoutManager
    lateinit var recyclerAdapter: HomeRecyclerAdapter
    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar
    lateinit var radioButtonView: View

    lateinit var etSearch: EditText
    lateinit var cantFind: RelativeLayout

    var homeRestaurantInfoList = arrayListOf<HomeRestaurant>()

    //sort according to ratings
    var ratingComparator = Comparator<HomeRestaurant> { rest1, rest2 ->

        if (rest1.restaurantRating.compareTo(rest2.restaurantRating, true) == 0) {
            rest1.restaurantName.compareTo(rest2.restaurantName, true)
        } else {
            rest1.restaurantRating.compareTo(rest2.restaurantRating, true)
        }
    }

    //sort according to cost(decreasing)
    var costComparator = Comparator<HomeRestaurant> { rest1, rest2 ->

        rest1.restaurantPrice.compareTo(rest2.restaurantPrice, true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

//        Enable/show filter menu
        setHasOptionsMenu(true)

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        layoutManager = LinearLayoutManager(activity)
        recyclerHome = view.findViewById(R.id.recyclerHome)
        progressLayout = view.findViewById(R.id.progressLayout)
        progressBar = view.findViewById(R.id.progressBar)

        etSearch = view.findViewById(R.id.etSearch)
        cantFind = view.findViewById(R.id.cantFind)

// Search Bar Start
        fun filterFun(strTyped: String) {
            val filteredList = arrayListOf<HomeRestaurant>()

            for (item in homeRestaurantInfoList) {
                if (item.restaurantName.toLowerCase(Locale.ROOT)
                        .contains(strTyped.toLowerCase(Locale.ROOT))
                ) {
                    filteredList.add(item)
                }
            }

            if (filteredList.size == 0) {
                cantFind.visibility = View.VISIBLE
            } else {
                cantFind.visibility = View.INVISIBLE
            }

            recyclerAdapter.filterList(filteredList)

        }

        etSearch.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(strTyped: Editable?) {
                filterFun(strTyped.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })// Searchbar End


        return view

    }

    fun fetchData(){

        if(ConnectionManager().checkConnectivity(activity as Context)){

            progressLayout.visibility = View.VISIBLE

            val queue = Volley.newRequestQueue(activity as Context)

            val url = "http://13.235.250.119/v2/restaurants/fetch_result/"

            val jsonObjectRequest = object : JsonObjectRequest(Method.GET, url, null, Response.Listener {

                try {
                    progressLayout.visibility = View.GONE

                    val res = it.getJSONObject("data")
                    val success = res.getBoolean("success")
                    if(success){
                        val data = res.getJSONArray("data")
                        for( i in 0 until data.length()){
                            val restaurantJsonObject = data.getJSONObject(i)
                            val restaurantObject = HomeRestaurant(
                                restaurantJsonObject.getString("id"),
                                restaurantJsonObject.getString("name"),
                                restaurantJsonObject.getString("rating"),
                                "₹" + restaurantJsonObject.getString("cost_for_one") + "/person",
                                restaurantJsonObject.getString("image_url")
                            )
                            homeRestaurantInfoList.add(restaurantObject)
                        }
                        recyclerAdapter = HomeRecyclerAdapter(activity as Context, homeRestaurantInfoList)

                        recyclerHome.adapter = recyclerAdapter
                        recyclerHome.layoutManager = layoutManager
//                        recyclerHome.addItemDecoration(DividerItemDecoration(recyclerHome.context, (layoutManager as LinearLayoutManager).orientation))
                    } else {
                        Toast.makeText(
                            activity as Context,
                            "Some Error Occurred",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: JSONException){
                    Toast.makeText(
                        activity as Context,
                        "Some unexpected error occurred!!!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }, Response.ErrorListener {
                if(activity != null){
                    println("this is the error $it")
                    Toast.makeText(
                        activity as Context,
                        "Volley error $it occurred!!!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String,String> ()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "64dd3ad5877c86"
                    return headers
                }
            }
            queue.add(jsonObjectRequest)

        } else {
            val dialog = AlertDialog.Builder(activity as Context)
            dialog.setTitle("ERROR")
            dialog.setMessage("Internet Connection Not Found")
            dialog.setPositiveButton("Open Setting"){text, listener ->
                val settingIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingIntent)
                activity?.finish()
            }
            dialog.setNegativeButton("Exit"){text, listener ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            dialog.setCancelable(false)
            dialog.create()
            dialog.show()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_sort, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.sort -> {
                radioButtonView = View.inflate(
                    contextParam,
                    R.layout.sort_radio_button,
                    null
                )
                AlertDialog.Builder(activity as Context)
                    .setTitle("Sort By?")
                    .setView(radioButtonView)
                    .setPositiveButton("OK") { _, _ ->
                        if (radioButtonView.radio_high_to_low.isChecked) {
                            Collections.sort(homeRestaurantInfoList, costComparator)
                            homeRestaurantInfoList.reverse()
                            recyclerAdapter.notifyDataSetChanged()
                        }
                        if (radioButtonView.radio_low_to_high.isChecked) {
                            Collections.sort(homeRestaurantInfoList, costComparator)
                            recyclerAdapter.notifyDataSetChanged()
                        }
                        if (radioButtonView.radio_rating.isChecked) {
                            Collections.sort(homeRestaurantInfoList, ratingComparator)
                            homeRestaurantInfoList.reverse()
                            recyclerAdapter.notifyDataSetChanged()
                        }
                    }
                    .setNegativeButton("Cancel") { _, _ ->

                    }
                    .create()
                    .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {

        if (ConnectionManager().checkConnectivity(activity as Context)) {
            if (homeRestaurantInfoList.isEmpty())
                fetchData()
        } else {

            val alterDialog = androidx.appcompat.app.AlertDialog.Builder(activity as Context)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Check Internet Connection!")
            alterDialog.setPositiveButton("Open Settings") { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_SETTINGS)
                startActivity(settingsIntent)
            }
            alterDialog.setNegativeButton("Exit") { _, _ ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            alterDialog.setCancelable(false)
            alterDialog.create()
            alterDialog.show()
        }
        super.onResume()
    }

}