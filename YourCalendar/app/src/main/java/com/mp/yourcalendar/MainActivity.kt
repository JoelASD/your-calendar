package com.mp.yourcalendar

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Initializing navView and drawerMenu + controller
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Navigator for fragments
        appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_home, R.id.nav_new_event, R.id.nav_logout), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Initialize FirebaseAuth instance
        auth = FirebaseAuth.getInstance()



        /*//TODO: Not working properly?
        // Listen to logout button/ action
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId){
                R.id.nav_logout -> {
                    logout()
                    startAuthActivity()
                    true
                }
                else -> false
            }
        }*/

    }

    override fun onStart() {
        super.onStart()

        // Check if user is logged in
        val user = auth.currentUser
        if(user != null){
            // User is logged in. Stay on MainActivity
        } else {
            // No user logged in, go to AuthActivity - login
            startAuthActivity()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    // Go to login/registering
    fun startAuthActivity(){
        var intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
    }

    // Sign out current auth user
    fun logout(){
        auth.signOut()
        startAuthActivity()
    }
}