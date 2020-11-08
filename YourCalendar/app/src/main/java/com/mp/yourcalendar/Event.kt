package com.mp.yourcalendar

data class Event (
    var eventName: String,
    var eventStartDate: String,
    var eventStartTime: String,
    var eventEndDate: String,
    var eventEndTime: String,
    var eventDescription: String?,
    var eventType: Int,
    var eventRepeat: Int,
    var eventLocName: String?,
    //val eventNotificationList: MutableList<EventNotification> //var eventNotificationList: MutableList<EventNotification>
    //var eventNotification: EventNotification?
)