package com.mp.yourcalendar

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize FirebaseAuth instance
        auth = FirebaseAuth.getInstance()

        //TODO: add logout button to its actual place
        mainLogOutButton.setOnClickListener {
            //logout
            auth.signOut()
            //go to login
            startAuthActivity()
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

    fun startAuthActivity(){
        var intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
    }
}