package com.example.cseconomy

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView

class HomeActivity : AppCompatActivity() {

    lateinit var toggle : ActionBarDrawerToggle

    private var adapter:FavItemAdapter? = null
    private lateinit var sqliteFavItemHelper: SQLiteFavItemHelper

    private lateinit var recyclerView: RecyclerView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        setTitle("Home")

        initElements()

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navigationView.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.nav_home -> Toast.makeText(applicationContext, "Home", Toast.LENGTH_SHORT).show()
                R.id.nav_searchItems -> {
                    val intent = Intent(this, SearchItemActivity::class.java)
                    startActivity(intent)
                    Toast.makeText(applicationContext, "Search items", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_settings ->
                {
                    Toast.makeText(applicationContext, "Settings", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                }
            }
            true
        }

        initRecycledView()

        sqliteFavItemHelper = SQLiteFavItemHelper(this)

        getItems()

        //pridanie do oblubenych
        adapter?.setOnClickRemoveFavItem {
            removeItemFromFavourite(it.item_id)
        }
    }

    fun initElements() {
        recyclerView = findViewById(R.id.recyclerViewFavItems)
        drawerLayout = findViewById(R.id.mainDrawerLayout)
        navigationView = findViewById(R.id.navView)
    }

    //vrati vsetky oblubene predmety
    private fun getItems() {
        val itemsList = sqliteFavItemHelper.getAllFavItems(this)

        adapter?.addItems(itemsList)
    }

    private fun initRecycledView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FavItemAdapter()
        recyclerView.adapter = adapter
    }

    //odstrani predmet z oblubenych a obnovi predmety
    private fun removeItemFromFavourite(id:Int) {
        val status = sqliteFavItemHelper.deleteFavItem(id)

        if (status > -1) {
            Toast.makeText(this,"Item removed from favourite", Toast.LENGTH_SHORT).show()
            getItems()
        } else {
            Toast.makeText(this,"Failed removing item", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)){
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}