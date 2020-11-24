package com.mp.yourcalendar.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.mp.yourcalendar.Event

/*
*
*   I chose to run realtime database's valueEventListener in mainActivity rather than in homeFragment.
*   so it doesn't run every time user enters homeFragment again (if no data was changed).
*
*   This viewModel is set up so that MainActivity owns it and HomeFragment retrieves it from memory.
*   When valueEventListener in MainActivity catches change in database, it updates this eventList with setEventList()
*   and observer in HomeFragment triggers.
*
 */

class activityViewModel(application: Application): AndroidViewModel(application) {
    val eventList = MutableLiveData<MutableList<Event>>()

    fun setEventList(list: MutableList<Event>){
        eventList.value = list
        Log.d("VIEWMODEL", "eventlist updated ${eventList.value}")
    }
}