package com.mp.yourcalendar.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.mp.yourcalendar.Event

class HomeViewModel : ViewModel() {

    private lateinit var ref: DatabaseReference
    private var eventList: MutableList<Event> = mutableListOf()
    private lateinit var auth: FirebaseAuth


    /*
    *
    * Not on use currently
    *
    *
    *
     */

    fun getEvents(): MutableList<Event> {
        auth = FirebaseAuth.getInstance()
        ref = FirebaseDatabase.getInstance().getReference("users").child(auth.uid.toString())

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (e in snapshot.children) {
                        //val event = e.getValue(Event::class.java)
                        val event = e.getValue<Event>()
                        //Log.d("EVENT", "$eve")
                        eventList.add(event!!)
                    }
                    //Log.d("******", "${snapshot.childrenCount}")
                    //Log.d("******", "${eventList.size}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return eventList
    }
}