package com.hasnain.usermoduleupdated.models

data class TimeSlot(
    val time: String = "",
    var time_booked: String = "NB",
    val time_id: String = ""
) {
    override fun toString(): String {
        return time
    }
}
