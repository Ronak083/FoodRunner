package com.example.foodrunner.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.foodrunner.R
import com.example.foodrunner.fragment.*
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    lateinit var coordinatorLayout: CoordinatorLayout
    lateinit var frameLayout: FrameLayout
    lateinit var toolbar: Toolbar
    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationView: NavigationView
    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    lateinit var sharedPreferences: SharedPreferences
    lateinit var txtUser: TextView
    lateinit var txtNumber: TextView

    var previousMenuItem: MenuItem?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences(
            getString(R.string.shared_preferences),
            Context.MODE_PRIVATE
        )

        coordinatorLayout = findViewById(R.id.coordinatorLayout)
        frameLayout = findViewById(R.id.frameLayout)
        toolbar=findViewById(R.id.toolbar)
        drawerLayout=findViewById(R.id.drawerLayout)
        navigationView=findViewById(R.id.navigationView)

        val headerView = navigationView.getHeaderView(0)
        txtUser = headerView.findViewById(R.id.txtUser)
        txtNumber = headerView.findViewById(R.id.txtNumber)

        navigationView.menu.getItem(0).isCheckable = true
        navigationView.menu.getItem(0).isChecked = true

        setUpToolbar()
        openHome()

        txtUser.text = sharedPreferences.getString("name", "UserName")
        txtNumber.text = "+91 ${sharedPreferences.getString("mobile_number", "9999999999")}"

//Hamburger icon setup for navigation drawer
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout,
            R.string.nav_open,
            R.string.nav_close
        )
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()


        navigationView.setNavigationItemSelectedListener {
            if (previousMenuItem != null){
                previousMenuItem?.isChecked = false
            }
            it.isCheckable = true
            it.isChecked = true
            previousMenuItem =it

            when(it.itemId){
                R.id.home -> {
                    openHome()
                    drawerLayout.closeDrawers()
                    Toast.makeText(this, "Clicked on Home", Toast.LENGTH_SHORT).show()
                }

                R.id.profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, ProfileFragment(this))
                        .commit()
                    supportActionBar?.title = "My Profile"
                    drawerLayout.closeDrawers()

                    Toast.makeText(this, "Clicked on Profile", Toast.LENGTH_SHORT).show()
                }

                R.id.favourites -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, FavoriteRestaurantFragment(this))
                        .commit()
                    supportActionBar?.title = "Favourites HomeRestaurant"
                    drawerLayout.closeDrawers()

                    Toast.makeText(this, "Clicked on Favourite HomeRestaurant", Toast.LENGTH_SHORT).show()
                }

                R.id.orderHistory -> {
                    val intent = Intent(this, OrderHistoryActivity::class.java)
                    drawerLayout.closeDrawers()
                    Toast.makeText(this@MainActivity, "Order History", Toast.LENGTH_SHORT).show()
                    startActivity(intent)
                }

                R.id.faqs -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, FAQsFragment())
                        .commit()
                    supportActionBar?.title = "FAQs"
                    drawerLayout.closeDrawers()

                    Toast.makeText(this, "Clicked on FAQs", Toast.LENGTH_SHORT).show()
                }

                R.id.developer -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, DeveloperFragment())
                        .commit()
                    supportActionBar?.title = "About Developer"
                    drawerLayout.closeDrawers()

                    Toast.makeText(this, "Clicked on About Developer", Toast.LENGTH_SHORT).show()
                }

//              Logout Code
                R.id.logout -> {
                    drawerLayout.closeDrawers()

                    val alterDialog = androidx.appcompat.app.AlertDialog.Builder(this)
                    alterDialog.setMessage("Do you wish to log out?")
                    alterDialog.setPositiveButton("Yes") { _, _ ->
                        sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()
                        ActivityCompat.finishAffinity(this)
                    }
                    alterDialog.setNegativeButton("No") { _, _ ->

                    }
                    alterDialog.create()
                    alterDialog.show()
                }


            }
            return@setNavigationItemSelectedListener true
        }

    }

    fun setUpToolbar(){
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Toolbar Title"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    fun openHome(){
        val fragment= HomeFragment(this)
        val transaction=supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayout, fragment)
        transaction.commit()
        supportActionBar?.title="All Restaurant"
        navigationView.setCheckedItem(R.id.home)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id=item.itemId
        if (id == android.R.id.home){
            drawerLayout.openDrawer(GravityCompat.START)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        when (supportFragmentManager.findFragmentById(R.id.frameLayout)) {
            !is HomeFragment -> {
                navigationView.menu.getItem(0).isChecked = true
                openHome()
            }
            else -> super.onBackPressed()
        }
    }
    override fun onResume() {
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        )
        super.onResume()
    }

}