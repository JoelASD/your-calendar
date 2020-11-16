package com.mp.yourcalendar

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class EventNotification (
        var date: String = "",
        var time: String = ""
) : Parcelable {}