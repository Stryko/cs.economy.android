package com.example.cseconomy

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputLayout

class SearchItemActivity : AppCompatActivity() {

    lateinit var toggle : ActionBarDrawerToggle

    private var adapter:ItemAdapter? = null
    private lateinit var sqliteItemHelper: SQLiteItemHelper
    private lateinit var sqliteFavItemHelper: SQLiteFavItemHelper

    private var filterWeaponWearG : String? = ""
    private var filterItemNameG : String? = ""

    private lateinit var recyclerView: RecyclerView
    private lateinit var drawerLayout : DrawerLayout
    private lateinit var navigationView : NavigationView
    private lateinit var filterItemName : EditText
    private lateinit var filterButton : Button
    private lateinit var filtersWrapper : LinearLayout
    private lateinit var textInputLayout : TextInputLayout
    private lateinit var autoCompleteWeaponWear : AutoCompleteTextView
    private lateinit var filterSwitch : Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_item)

        setTitle("Browse Items")

        initElements()

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navigationView.setNavigationItemSelectedListener {
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

        initRecycledView()

        sqliteItemHelper = SQLiteItemHelper(this)
        sqliteFavItemHelper = SQLiteFavItemHelper(this)

        //pridanie do oblubenych
        adapter?.setOnClickAddFavItem {
            addItemToFavourite(it.item_id)
        }

        setWeaponWearItems()

        setFilterItemName()

        setFilterButton()

        setFilterToogle()

        getItems()
    }

    fun initElements() {
        recyclerView = findViewById(R.id.recyclerViewItems)
        drawerLayout = findViewById(R.id.mainDrawerLayout)
        navigationView = findViewById(R.id.navView)
        filterItemName = findViewById(R.id.filterItemName)
        filterButton = findViewById(R.id.btnFilter)
        filtersWrapper = findViewById(R.id.filtersLayout)
        textInputLayout = findViewById(R.id.dropDownWeaponWear)
        autoCompleteWeaponWear = findViewById(R.id.autoCompleteWeaponWear)
        filterSwitch = findViewById(R.id.swShowFilters)
    }

    //nastavenie filtru textu - filtruje podla nazvu predmetu
    fun setFilterItemName() {
        val filterItemNameSavedText = getFilterItemNameSavedText()
        filterItemName.setText(filterItemNameSavedText)
        filterItemNameG = filterItemNameSavedText

        filterItemName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus)
            {
                filterItemNameG = filterItemName.text.toString()
                getItems()
                saveFilterItemName()
            }
        }
    }

    //vrati hodnotu textu
    fun getFilterItemNameSavedText() : String? {
        val sharedPreferences = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        return sharedPreferences.getString("filter_item_name","")
    }

    //ulozenie filtru nazvu predmetu
    fun saveFilterItemName() {
        val sharedPreferences = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        var editor = sharedPreferences.edit()
        editor.putString("filter_item_name", filterItemNameG)
        editor.commit()
    }

    //nastavenie tlacitka filtrovania - schova filtre
    fun setFilterButton() {
        filterButton.setOnClickListener {
            filterItemNameG = filterItemName.text.toString()
            getItems()
            hideFiters()
            saveFilterItemName()
        }
    }

    //skrytie filtrov
    fun hideFiters() {
        filtersWrapper.visibility = View.GONE
        filterSwitch.isChecked = false
    }

    //nastavenie prepinania medzi viditelnostou filtrov
    fun setFilterToogle() {
        filtersWrapper.visibility = View.GONE

        filterSwitch.setOnCheckedChangeListener() { _, b ->
            if (b) filtersWrapper.visibility = View.VISIBLE
            else filtersWrapper.visibility = View.GONE
        }
    }

    //nastavenie fitru opotrebenia zbrane
    fun setWeaponWearItems() {
        val sharedPreferences = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val filterWeaponWear = sharedPreferences.getString("filter_weapon_wear","Any")

        textInputLayout.editText?.setText(filterWeaponWear)

        filterWeaponWearG = filterWeaponWear

        val weaponWearTypes = arrayOf("Any","Factory new","Minimal wear","Field-Tested","Battle-Scarred","Well-Worn")
        val arrayAdapter = ArrayAdapter(this,R.layout.dropdown_item, weaponWearTypes)
        autoCompleteWeaponWear.setAdapter(arrayAdapter)

        autoCompleteWeaponWear.setOnItemClickListener { _, _, position, _ ->
            val value = arrayAdapter.getItem(position)
            changeFilterWeaponWear(value)
            getItems()
        }
    }

    //zmena filtru opotrebenia zbrani, funguje iba na zbrane su aj predmety, ktore nie su zbrane
    //ulozi filter do shared preferences
    fun changeFilterWeaponWear(value:String?) {
        filterWeaponWearG = value

        val sharedPreferences = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        var editor = sharedPreferences.edit()
        editor.putString("filter_weapon_wear", value)
        editor.commit()
    }

    //pridanie predmetu do oblubenych
    private fun addItemToFavourite(id:Int) {
        if (sqliteFavItemHelper.itemIsFavourite(id))
        {
            Toast.makeText(this,"Item is already in favourite", Toast.LENGTH_SHORT).show()
            return;
        }

        val status = sqliteFavItemHelper.insertFavItem(id)

        if (status > -1)
            Toast.makeText(this,"Item added to favourite", Toast.LENGTH_SHORT).show()
        else
            Toast.makeText(this,"Failed adding item to favourite", Toast.LENGTH_SHORT).show()
    }

    //vrati vsetky predmety a zobrazi ich do rec. view pomocou adapteru
    private fun getItems() {
        val itemsList = sqliteItemHelper.getAllItems(this, filterWeaponWearG, filterItemNameG, 20)

        adapter?.addItems(itemsList)
    }

    //nastavenie adaptera pre rec. view
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