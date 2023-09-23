package com.example.foodrunner.adapter

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.foodrunner.R
import com.example.foodrunner.activity.RestaurantMenuActivity
import com.example.foodrunner.database.RestaurantDatabase
import com.example.foodrunner.database.RestaurantEntity
import com.example.foodrunner.model.HomeRestaurant
import com.squareup.picasso.Picasso

class HomeRecyclerAdapter(val context: Context, var itemList: ArrayList<HomeRestaurant>): RecyclerView.Adapter<HomeRecyclerAdapter.HomeViewHolder>() {

    class HomeViewHolder(view : View): RecyclerView.ViewHolder(view) {

        val txtRestaurantName : TextView = view.findViewById(R.id.txtRestaurantName)
        val txtRestaurantPrice : TextView = view.findViewById(R.id.txtRestaurantPrice)
        val txtRestaurantRating : TextView = view.findViewById(R.id.txtRestaurantRating)
        val txtRestaurantHeart : TextView = view.findViewById(R.id.txtRestaurantFavorite)
        val txtRestaurantImage : ImageView = view.findViewById(R.id.imgRestaurantImage)
        val llContent : LinearLayout = view.findViewById(R.id.llContent)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_home_single_row, parent, false)
        return HomeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {

        val restaurant = itemList[position]

//        val restaurantEntity = RestaurantEntity(restaurant.restaurantId, restaurant.restaurantName)

        holder.txtRestaurantName.text = restaurant.restaurantName
        holder.txtRestaurantPrice.text = restaurant.restaurantPrice
        holder.txtRestaurantRating.text = restaurant.restaurantRating

        Picasso.get().load(restaurant.restaurantImage).error(R.drawable.book_app_icon_web).into(holder.txtRestaurantImage)


        holder.llContent.setOnClickListener {
            Toast.makeText(context , "Clicked on ${holder.txtRestaurantName.text}", Toast.LENGTH_SHORT).show()
            val intent = Intent(context, RestaurantMenuActivity::class.java)
            intent.putExtra("restaurantId", restaurant.restaurantId)
                .putExtra("restaurantName", restaurant.restaurantName)
            context.startActivity(intent)
        }

        val restaurantEntity = RestaurantEntity(
            restaurantId = restaurant.restaurantId,
            restaurantName = restaurant.restaurantName,

        )


        //Adding and removing from favourites using database
        holder.txtRestaurantHeart.setOnClickListener {
            if (!DBAsyncTask(context, restaurantEntity, 1).execute().get()) {
                val result = DBAsyncTask(context, restaurantEntity, 2).execute().get()

                if (result) {
                    Toast.makeText(context, "${restaurant.restaurantName} added to Favorites", Toast.LENGTH_SHORT).show()
                    holder.txtRestaurantHeart.tag = "liked"
                    holder.txtRestaurantHeart.background = context.resources.getDrawable(R.drawable.ic_fav_fill)

                } else {
                    Toast.makeText(context, "Some error occurred", Toast.LENGTH_SHORT).show()
                }

            } else {
                val result = DBAsyncTask(context, restaurantEntity, 3).execute().get()
                if (result) {

                    Toast.makeText(context, "${restaurant.restaurantName} removed from Favorites", Toast.LENGTH_SHORT).show()
                    holder.txtRestaurantHeart.tag = "unliked"
                    holder.txtRestaurantHeart.background = context.resources.getDrawable(R.drawable.ic_fav_outline)

                } else {
                    Toast.makeText(context, "Some error occurred", Toast.LENGTH_SHORT).show()
                }
            }
        }


        //        ClickListener On Restaurants

//        holder.llContent.setOnClickListener {
//
//            println(holder.txtRestaurantName.tag.toString())
//            val intent = Intent(context, RestaurantMenuActivity::class.java)
//            intent.putExtra("restaurantId", restaurant.txtRestaurantName.tag)
//            intent.putExtra("restaurantName", restaurant.txtRestaurantName)
//            context.startActivity(intent)
//
//        }


        val checkFav = DBAsyncTask(context, restaurantEntity, 1).execute()
        val isFav = checkFav.get()
        if (isFav) {
            holder.txtRestaurantHeart.tag = "liked"
            holder.txtRestaurantHeart.background = context.resources.getDrawable(R.drawable.ic_fav_fill)

        } else {
            holder.txtRestaurantHeart.tag = "unliked"
            holder.txtRestaurantHeart.background = context.resources.getDrawable(R.drawable.ic_fav_outline)
        }
    }


//    Searchbar
    fun filterList(filteredList: ArrayList<HomeRestaurant>) {
        itemList = filteredList
        notifyDataSetChanged()
    }

    class DBAsyncTask(val context: Context, val restaurantEntity: RestaurantEntity, val mode: Int) :
        AsyncTask<Void, Void, Boolean>() {

        val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "restaurant-db").build()

        override fun doInBackground(vararg p0: Void?): Boolean {

            /*
            * Mode 1->check if restaurant is in favourites
            * Mode 2->Save the restaurant into DB as favourites
            * Mode 3-> Remove the favourite restaurant*/
            when (mode) {
                1 -> {
                    val restaurant: RestaurantEntity? = db.restaurantDao()
                        .getRestaurantById(restaurantEntity.restaurantId)
                    db.close()
                    return restaurant != null
                }
                2 -> {
                    db.restaurantDao().insertRestaurant(restaurantEntity)
                    db.close()
                    return true
                }
                3 -> {
                    db.restaurantDao().deleteRestaurant(restaurantEntity)
                    db.close()
                    return true
                }
                else -> return false

            }
        }
    }
}