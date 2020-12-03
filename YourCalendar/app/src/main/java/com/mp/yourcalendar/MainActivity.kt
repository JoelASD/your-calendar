package com.mp.yourcalendar

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.mp.yourcalendar.ui.home.HomeFragment
import com.mp.yourcalendar.ui.home.activityViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var ref: DatabaseReference

    // Instance of activityViewModel owned by this activity
    private val viewModel by viewModels<activityViewModel>()

    companion object {
        var eventList: MutableList<Event> = mutableListOf()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Initializing navView and drawerMenu + controller
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        val navHeader: View = navView.getHeaderView(0)
        val userEmailText: TextView = navHeader.findViewById(R.id.authEmailTextView)
        userEmailText.text = Firebase.auth.currentUser?.email

        // Navigator for fragments
        appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_home, R.id.nav_new_event, R.id.eventDetailFragment), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Initialize FirebaseAuth instance
        auth = FirebaseAuth.getInstance()

        // Set database reference
        ref = FirebaseDatabase.getInstance().getReference("users").child(auth.uid.toString())
        // Listener for users data, runs at activity created and when data is changed
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newList: MutableList<Event> = mutableListOf()
                for (e in snapshot.children) {
                    val event = e.getValue<Event>()
                    newList.add(event!!)
                }
                //databaseLoaded(newList)
                Log.d("DATABASE", "Users data was accessed")
                // Update eventList in activityViewModel
                viewModel.setEventList(newList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Database Error", "Error in DB ValueEventListener. ${error.toException()}")
            }
        })

        //Look here! :D  (true at the end! )
        // Logout using drawer
        navView.menu.findItem(R.id.nav_logout).setOnMenuItemClickListener {
            logout()
            true
        }

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

    // Navigate fragments from drawer menu
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

    // When valueEventListener is triggered and data in activityViewModel is updated with this
    /*fun databaseLoaded(list: MutableList<Event>) {
        Log.d("LOADED", "MAIN ACTIVITY: DATABASE LOADED, ${list.size}")
        viewModel.setEventList(list)
    }*/
}