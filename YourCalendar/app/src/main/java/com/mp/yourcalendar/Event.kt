package com.mp.yourcalendar

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Event (
        var eventName: String = "",
        var eventStartDate: String = "",
        var eventStartTime: String = "",
        var eventEndDate: String = "",
        var eventEndTime: String = "",
        var eventDescription: String? = "",
        var eventType: Int = 0,
        var eventRepeat: Int = 0,
        var eventLocName: String? = "",
        var eventLocLatLng: String? = "",
        val eventNotificationList: MutableList<EventNotification> = mutableListOf(),
        var eventKey: String? = ""
) : Parcelable {
    /*constructor() : this("", "", "", "",
            "", "", 0, 0,
            "", "", emptyList<EventNotification>() as MutableList<EventNotification>)*/
}