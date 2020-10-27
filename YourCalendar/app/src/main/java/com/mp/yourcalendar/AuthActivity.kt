package com.mp.yourcalendar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        if(savedInstanceState == null){
            supportFragmentManager.beginTransaction()
                .add(R.id.authFragmentFrame, LogInFragment())
                .commit()
        }
    }
}