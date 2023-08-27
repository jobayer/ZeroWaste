package com.bracu.zerowaste.data.models

data class User(
    var uid: String = "",
    var rfid: String = "",
    var email: String = "",
    var name: String = "",
    var points: Int = 0
)