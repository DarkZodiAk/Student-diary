package com.android.studentdiary.models

data class Task(
    var name: String = "",
    var text: String = "",
    val author_uid: String? = null,
    var folder_id: String? = null,   //var вместо val, ибо как это все дело перемещать
    var id: String? = null,          //var вместо val, ибо как это все дело перемещать
    var task: Boolean = true,
    var checked: Boolean? = null,
    var deadline: String? = null
)
