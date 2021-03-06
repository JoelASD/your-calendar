package com.mp.yourcalendar

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
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

        // Creates notification channel if it hasn't been created yet
        createNotifChannel()

        // Set database reference
        ref = FirebaseDatabase.getInstance().getReference("users").child(auth.uid.toString())
        // Listener for users data, runs at activity created and when data is changed
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newEventList: MutableList<Event> = mutableListOf()
                val newKeyList: MutableList<String> = mutableListOf()
                for (e in snapshot.children) {
                    newKeyList.add(e.key!!)
                    var event = e.getValue<Event>()
                    event?.eventKey = e.key
                    newEventList.add(event!!)
                }
                // Update eventList in activityViewModel
                viewModel.setEventList(newEventList, newKeyList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Database Error", "Error in DB ValueEventListener. ${error.toException()}")
            }
        })

        // Logout using drawer
        navView.menu.findItem(R.id.nav_logout).setOnMenuItemClickListener {
            logout()
            true
        }
        navView.menu.findItem(R.id.nav_weekly_view).setOnMenuItemClickListener {
            viewModel.changeCalendarView(2)
            drawer_layout.closeDrawers()
            true
        }
        navView.menu.findItem(R.id.nav_monthly_view).setOnMenuItemClickListener {
            viewModel.changeCalendarView(1)
            drawer_layout.closeDrawers()
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
    private fun startAuthActivity(){
        var intent = Intent(this, AuthActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    // Sign out current auth user
    private fun logout(){
        auth.signOut()
        startAuthActivity()
    }

    private fun createNotifChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Event notification"
            val desc = "Shows notifications for events set by user"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("1", name, importance).apply {
                description = desc
            }
            //val notificationManager: NotificationManager = this.context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationManager: NotificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}