package com.example.cseconomy

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.MenuItem
import android.view.View
import android.view.contentcapture.ContentCaptureCondition
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.set
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputLayout

class SettingsActivity : AppCompatActivity() {

    lateinit var toggle : ActionBarDrawerToggle

    private lateinit var sqliteHelper: SQLiteHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setTitle("Settings")

        sqliteHelper = SQLiteHelper(this)

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
                R.id.nav_searchItems ->
                {
                    Toast.makeText(applicationContext, "Search items", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, SearchItemActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_settings -> Toast.makeText(applicationContext, "Settings", Toast.LENGTH_SHORT).show()
            }
            true
        }

        setDropdownItems()
    }

    fun setDropdownItems() {
        val sharedPreferences = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val currentUserCurrency = sharedPreferences.getString("user_currency","EUR")

        //nastavit aby sa zobrazoval text aktualnej meny...
        val textInputLayout = findViewById<TextInputLayout>(R.id.textInputLayout)
        textInputLayout.editText?.setText(currentUserCurrency)

        val currencies = arrayOf("EUR","USD","CAD","JPY","GBP","NZD","CZK","HUF")
        val arrayAdapter = ArrayAdapter(this,R.layout.dropdown_item, currencies)
        val autoComplete = findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView)
        autoComplete.setAdapter(arrayAdapter)

        autoComplete.setOnItemClickListener { _, _, position, _ ->
            val value = arrayAdapter.getItem(position)
            changeCurrencySettings(value)
            Toast.makeText(applicationContext, "Currency changed to: $value", Toast.LENGTH_SHORT).show()
        }
    }

    //zmena meny uzivatela, ulozi sa do shared preferences
    fun changeCurrencySettings(value:String?) {
        val sharedPreferences = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        var editor = sharedPreferences.edit()
        editor.putString("user_currency", value)
        editor.commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)){
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}