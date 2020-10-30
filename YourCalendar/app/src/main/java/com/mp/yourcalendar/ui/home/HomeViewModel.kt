package com.mp.yourcalendar.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text: MutableLiveData<String> = MutableLiveData<String>().apply {
        value = "This is home fragment"
    }
    val text: LiveData<String> = _text
}