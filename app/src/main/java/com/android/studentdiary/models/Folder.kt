package com.android.studentdiary.models

import com.google.firebase.firestore.PropertyName

data class Folder (
    var author_uid: String? = null,
    var name: String? = null,
    val canBeDeleted: Boolean = false,
    var group: Boolean = false, //Это маркер, пусть пока побудет здесь, потом его можно будет убрать.
    val id: String = "", //Нужен для того, чтобы добраться до папки/группы с этой записью
    var names: ArrayList<String>? = null, //Костыльный вариант, но ничего не поделаешь
    var uids: ArrayList<String>? = null


    //val users: ArrayList<Map<String,String?>>? = null //arrayListOf(mapOf("name" to "", "uid" to "")) Это лучший вариант
    //Map<String, ArrayList<String>>? = null //mapOf("name" to arrayListOf(), "uid" to arrayListOf()) Ну это другой вариант

)