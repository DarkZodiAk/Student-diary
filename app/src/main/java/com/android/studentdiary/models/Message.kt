package com.android.studentdiary.models

import com.google.firebase.Timestamp

data class Message(
    val from: String? = null,
    val text: String? = null,
    val date: Timestamp? = null
)