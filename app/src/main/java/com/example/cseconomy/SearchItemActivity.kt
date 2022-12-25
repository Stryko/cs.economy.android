package com.example.cseconomy

import android.content.Context
import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputLayout

class SearchItemActivity : AppCompatActivity() {

    lateinit var toggle : ActionBarDrawerToggle

    private lateinit var recyclerView: RecyclerView

    private var adapter:ItemAdapter? = null
    private lateinit var sqliteHelper: SQLiteHelper

    private var filterWeaponWearG : String? = ""
    private var filterItemNameG : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_item)

        setTitle("Browse Items")

        val drawerLayout : DrawerLayout = findViewById(R.id.mainDrawerLayout)
        val navView : NavigationView = findViewById(R.id.navView)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.nav_home ->
                {
                    Toast.makeText(applicationContext, "Home", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_searchItems -> Toast.makeText(applicationContext, "Search items", Toast.LENGTH_SHORT).show()
                R.id.nav_settings ->
                {
                    Toast.makeText(applicationContext, "Settings", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                }
            }
            true
        }

        initView()
        initRecycledView()

        setWeaponWearItems()

        sqliteHelper = SQLiteHelper(this)

        getItems()

        //pridanie do oblubenych
        adapter?.setOnClickAddFavItem {
            addItemToFavourite(it.item_id)
        }

        setFilterItemName()

        setFilterButton()

        setFilterToogle()
    }

    fun setFilterItemName() {
        val filterItemName = findViewById<EditText>(R.id.filterItemName)
        filterItemName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus)
            {
                filterItemNameG = filterItemName.text.toString()
                getItems()
            }
        }
    }

    fun setFilterButton() {
        val filterItemName = findViewById<EditText>(R.id.filterItemName)
        val filterButton = findViewById<Button>(R.id.btnFilter)
        filterButton.setOnClickListener {
            filterItemNameG = filterItemName.text.toString()
            getItems()
            hideFiters()
        }
    }

    fun hideFiters() {
        val filtersWrapper = findViewById<LinearLayout>(R.id.filtersLayout)
        filtersWrapper.visibility = View.GONE
    }

    fun setFilterToogle() {
        val filtersWrapper = findViewById<LinearLayout>(R.id.filtersLayout)
        filtersWrapper.visibility = View.GONE

        val filterSwitch = findViewById<Switch>(R.id.swShowFilters)
        filterSwitch.setOnCheckedChangeListener() { _, b ->
            if (b) filtersWrapper.visibility = View.VISIBLE
            else filtersWrapper.visibility = View.GONE
        }
    }

    fun setWeaponWearItems() {
        val sharedPreferences = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val filterWeaponWear = sharedPreferences.getString("filter_weapon_wear","Any")

        val textInputLayout = findViewById<TextInputLayout>(R.id.dropDownWeaponWear)
        textInputLayout.editText?.setText(filterWeaponWear)

        filterWeaponWearG = filterWeaponWear

        val weaponWearTypes = arrayOf("Any","Factory new","Minimal wear","Field-Tested","Battle-Scarred","Well-Worn")
        val arrayAdapter = ArrayAdapter(this,R.layout.dropdown_item, weaponWearTypes)
        val autoComplete = findViewById<AutoCompleteTextView>(R.id.autoCompleteWeaponWear)
        autoComplete.setAdapter(arrayAdapter)

        autoComplete.setOnItemClickListener { _, _, position, _ ->
            val value = arrayAdapter.getItem(position)
            changeFilterWeaponWear(value)
            getItems()
        }
    }

    //zmena filtru weapon wear
    fun changeFilterWeaponWear(value:String?) {
        filterWeaponWearG = value

        val sharedPreferences = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        var editor = sharedPreferences.edit()
        editor.putString("filter_weapon_wear", value)
        editor.commit()
    }

    private fun addItemToFavourite(id:Int) {
        if (sqliteHelper.itemIsFavourite(id))
        {
            Toast.makeText(this,"Item is already in favourite", Toast.LENGTH_SHORT).show()
            return;
        }

        val status = sqliteHelper.insertFavItem(id)

        if (status > -1) {
            Toast.makeText(this,"Item added to favourite", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this,"Failed adding item to favourite", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getItems() {
        val itemsList = sqliteHelper.getAllItems(this, filterWeaponWearG, filterItemNameG)

        adapter?.addItems(itemsList)
    }

    private fun initView() {
        recyclerView = findViewById(R.id.recyclerViewItems)
    }

    private fun initRecycledView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ItemAdapter()
        recyclerView.adapter = adapter
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)){
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}