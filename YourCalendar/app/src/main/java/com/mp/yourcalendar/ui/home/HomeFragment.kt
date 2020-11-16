package com.mp.yourcalendar.ui.home

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mp.yourcalendar.R
import java.util.*
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.mp.yourcalendar.Event
import kotlinx.android.synthetic.main.event_list.*


class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    //var eventList: MutableList<Event> = mutableListOf()

    companion object {
        var eventList: MutableList<Event> = mutableListOf()
    }

    private lateinit var ref: DatabaseReference
    //private var eventList: MutableList<Event> = mutableListOf()
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.home_fragment, container, false)

        getEvents()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("XXXXXX", "${eventList.size}")
        //setupRecyclerView(eventList)
    }

    fun setupRecyclerView(events: MutableList<Event>){
        recyclerView.layoutManager = LinearLayoutManager(this.context)
        recyclerView.adapter = EventsAdapter(this)
    }

    fun getEvents() {
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
                    Log.d("******", "${snapshot.childrenCount}")
                    Log.d("******", "${eventList.size}")
                    setupRecyclerView(eventList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    /*override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        // TODO: Use the ViewModel
    }*/

}