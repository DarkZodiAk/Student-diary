package com.android.studentdiary.models

data class User(
    var name: String = "",
    val email: String?=null,
    val imageUrl: String?=null,
    var settings: String="",
    val folder_ids: ArrayList<String> = arrayListOf(),
    val group_ids: ArrayList<String> = arrayListOf())