package com.android.studentdiary

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.android.studentdiary.databinding.ActivityMainBinding
import com.android.studentdiary.models.Folder
import com.android.studentdiary.models.Message
import com.android.studentdiary.models.Task
import com.android.studentdiary.models.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    lateinit var binding: ActivityMainBinding
    private var listeners: List<ListenerRegistration>? = null
    private var listenUser: ListenerRegistration? = null

    //TODO("Подумать над тем, нужно ли создавать структуру для сообщений, подобную этим двум? Пока работаем без структуры.")
    val taskModel: MutableMap<String,MutableMap<String,Task>> = mutableMapOf()  //MutableMap<String,ArrayList<Task>>
    val folderModel: MutableMap<String,Folder> = mutableMapOf()
    var user = User()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid
        val db = Firebase.firestore
        db.firestoreSettings = firestoreSettings{isPersistenceEnabled = true}

        db.collection("users").document("$uid").get()
            .addOnSuccessListener {
                user = it.toObject(User::class.java)!!//.result.toObject(User::class.java)!!
                binding.textView.text = user.name + "\n" + user.email
                Log.d("Firestore_MainLog", "$user")
                loadTasksFromFolders(db)
                listeners = listenForGroupChanges(db)

                listenUser = listenForUserInfo(db, uid!!)
            }


        binding.bSignOut.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LauncherActivity::class.java))
            if(listeners!=null)
                listeners!![0].remove()
                listeners!![1].remove()
            listenUser!!.remove()
        }

        /*binding.task.setOnClickListener {
            createTask("Task1", "Something", auth.currentUser!!.uid, true, folder_id = "ILMozSpZnsDzc5qSLcxS", db = db)
        }*/
    }


    //Создать пустую папку или группу. Это регулируется флагом isGroup в функции. Записей здесь нет
    private fun createFolder(name: String,
                             db: FirebaseFirestore,
                             usrinf: FirebaseUser?,
                             user: User,
                             isGroup: Boolean = false){
        val ref = db.collection("folders").document()
        val folder = Folder(usrinf?.uid, name, canBeDeleted = true, group = isGroup, id = ref.id,
            names = arrayListOf(user.name), uids = arrayListOf(usrinf!!.uid))
        ref.set(folder)
            .addOnSuccessListener { Log.d("Firestore_Log", "Id of added folder: ${ref.id}") }
            .addOnFailureListener { Log.d("Firestore_Log", "Couldn't add folder") }
        user.folder_ids.add(ref.id)

        val usRef = db.collection("users").document(usrinf.uid)
        if (isGroup)
            usRef.update("group_ids",FieldValue.arrayUnion(ref.id))
        else
            usRef.update("folder_ids",FieldValue.arrayUnion(ref.id))

        folderModel[ref.id] = folder
        taskModel[ref.id] = mutableMapOf()
    }

    //Удалить папку/группу
    private fun deleteFolder(folder_id: String, db: FirebaseFirestore){
        db.collection("folders").document(folder_id).delete()
            .addOnFailureListener { Log.d("Fire_Error", "Error deleting folder/group, $it") }
        if(!folderModel[folder_id]!!.group){
            folderModel.remove(folder_id)
        }
    }


    //Изменить свойства папки/группы. На данный момент у группы можно передать права админа другому человеку.
    private fun updateFolder(folder_id: String, name: String, author_uid: String, db: FirebaseFirestore){
        db.collection("folders").document(folder_id)
            .update(mapOf("author_uid" to author_uid,"name" to name))
        if(!folderModel[folder_id]!!.group){
            folderModel[folder_id]!!.name = name
        }
    }


    //Вспомогательная функция добавления юзера в группу
    private fun addUserToGroup(db: FirebaseFirestore, group_id: String, usrinf: FirebaseUser?){
        db.collection("folders").document(group_id)
            .update("uids",FieldValue.arrayUnion(usrinf!!.uid),
                "names", FieldValue.arrayUnion(user.name))

    }


    //Войти в группу. TODO("Возможен вариант, когда делаешь запрос по ID, а админ принимает. Можно отдельную коллекцию сделать (folder_req)")
    private fun enterInGroup(db: FirebaseFirestore, group_id: String, name: String, usrinf: FirebaseUser?){
        db.collection("folders").whereEqualTo("id",group_id).whereEqualTo("name", name)
            .get().addOnSuccessListener {
                if (!it.isEmpty){
                    for(doc in it){
                        addUserToGroup(db, group_id, usrinf)
                    }
                }
            }.addOnFailureListener { Log.d("GroupEntrance","Exception: $it") }
    }




    //TODO("Исключить пользователя из группы")
    private fun kickUserFromGroup(){}


    //Позволяет превратить папку в группу с одним пользователем.
    private fun moveFolderToGroup(folder_id: String, author_uid: String, db: FirebaseFirestore){
        db.collection("folders").document(folder_id)
            .update(mapOf("group" to true, "names" to arrayListOf(user.name), "uids" to arrayListOf(author_uid)))

        folderModel[folder_id]!!.group = true
        folderModel[folder_id]!!.names = arrayListOf(user.name)
        folderModel[folder_id]!!.uids = arrayListOf(author_uid)

        user.folder_ids.remove(folder_id)
        user.group_ids.add(folder_id)

        db.collection("users").document(author_uid)
            .update(mapOf("folder_ids" to user.folder_ids, "group_ids" to user.group_ids))
    }


    //Функция для создания записи
    private fun createTask(name: String,
                           text: String,
                           uid: String,
                           isTask: Boolean,
                           deadline: String? = null,
                           folder_id: String,
                           db: FirebaseFirestore){
        val ref = db.collection("folders").document(folder_id)
            .collection("tasks").document()

        val task = Task(name, text, uid, task = isTask, id = ref.id, folder_id = folder_id)
        if(task.task){
            task.deadline = deadline
            task.checked = false
        }
        ref.set(task)
        if(!folderModel[folder_id]!!.group){
            taskModel[folder_id]!![ref.id] = task
        }
    }


    //Удалить запись
    private fun deleteTask(folder_id: String, id: String, db: FirebaseFirestore) {
        db.collection("folders").document(folder_id)
            .collection("tasks").document(id).delete()
            .addOnFailureListener { Log.d("Fire_Error", "Error deleting task, $it") }
        //TODO("Пересмотреть эту штуку")
        if (!folderModel[folder_id]!!.group){
            taskModel[folder_id]!!.remove(id)
        }
    }


    //Обновить запись
    private fun updateTask(folder_id: String,
                           id: String,
                           name: String,
                           text: String,
                           isTask: Boolean,
                           deadline: String? = null,
                           checked: Boolean? = null,
                           db: FirebaseFirestore,
                           usrinf: FirebaseUser?){

        db.collection("folders").document(folder_id)
            .collection("tasks").document(id)
            .update(mapOf("name" to name, "text" to text, "task" to isTask, "deadline" to deadline, "checked" to checked))
            .addOnFailureListener { Log.d("Fire_Error", "Error updating task, $it") }

        //TODO("Пересмотреть позже")
        if(!folderModel[folder_id]!!.group){
            val task = Task(name, text, usrinf?.uid, folder_id, id, isTask, checked, deadline)
            taskModel[folder_id]!![id] = task
        }
    }


    //Переместить запись из одной папки/группы в другую
    private fun moveTask(fold_id: String, fnew_id: String, id: String, db: FirebaseFirestore, task: Task){
        deleteTask(fold_id, id, db)
        val ref = db.collection("folders").document(fnew_id)
            .collection("tasks").document()
        task.id = ref.id
        task.folder_id = fnew_id
        ref.set(task)
        if(!folderModel[fnew_id]!!.group){
            taskModel[fnew_id]!![ref.id] = task
        }
    }


    //Отправка сообщения
    private fun sendMessage(db: FirebaseFirestore, folder_id: String, text: String){
        val ref = db.collection("folders").document(folder_id)
            .collection("messages").document()
        val msg = Message(user.name, text, Timestamp(Date()))
        ref.set(msg)
    }


    //Загрузка сообщений группы и прослушивание на счет новых
    private fun loadMessages(db: FirebaseFirestore, folder_id: String){
        val TAG = "Message_Listen"
        val ref = db.collection("folders").document(folder_id).collection("messages")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                for (doc in snapshot!!){
                    val msg = doc.toObject(Message::class.java)
                }
            }
    }


    private fun listenForUserInfo(db: FirebaseFirestore, uid: String): ListenerRegistration {
        val TAG = "Fr_Listen"
        val userListener = db.collection("users").document(uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }
                if(snapshot!=null && snapshot.exists()) {
                    val snap_user = snapshot.toObject(User::class.java)
                    if (snap_user!=user)
                        //TODO("Разобраться в том, что здесь будет происходить")
                        Log.d(TAG, "Current data: $snap_user")
                }
            }
        //TODO("Мы остановились на обновлении данных пользователя")
        return userListener
    }

    //Загружает все данные о записях из папок пользователя и групп, где он состоит. Должен использоваться 1 раз.
    private fun loadTasksFromFolders(db: FirebaseFirestore){

        for (id in user.folder_ids) {
            if (taskModel[id] == null) {
                taskModel[id] = mutableMapOf()
            }

            db.collection("folders").document(id).get()
                .addOnSuccessListener {
                    if (it != null) {
                        val folder = it.toObject<Folder>()
                        Log.d("Fr_LoadAll", "Loaded folder: $folder")
                        folderModel[id] = folder!!
                    }
                }
                .addOnFailureListener { Log.d("Fr_LoadAll", "We got exception here: $it") }

            db.collection("folders").document(id)
                .collection("tasks").get()
                .addOnSuccessListener {
                    for (document in it) {
                        val task = document.toObject(Task::class.java)
                        taskModel[id]!![task.id!!] = task
                        Log.d("Fr_LoadAll","Loaded task $task from $id folder/group")
                    }
                }
        }
    }

    private fun listenForGroupChanges(db: FirebaseFirestore): List<ListenerRegistration> {
        val TAG = "Fr_Listen"

        for (id in user.group_ids) {
            if (taskModel[id] == null) {
                taskModel[id] = mutableMapOf()
            }
        }

        val queryFolder = db.collection("folders").whereEqualTo("group",true) //Это нужно было для предыдущего варика
        //.whereArrayContains("uids",usrinf!!.uid)
        val listenerFolder = queryFolder.addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            for (dc in snapshots!!.documentChanges){
                when (dc.type){
                    DocumentChange.Type.ADDED -> {
                        val folder = dc.document.toObject<Folder>()
                        folderModel[folder.id] = folder
                        Log.d(TAG, "Added group: $folder")
                    }

                    DocumentChange.Type.MODIFIED ->{
                        val folder = dc.document.toObject<Folder>()
                        folderModel[folder.id]!!.name = folder.name
                        folderModel[folder.id]!!.author_uid = folder.author_uid
                        Log.d(TAG, "Modified group: $folder")
                    }

                    DocumentChange.Type.REMOVED -> {
                        val id = dc.document.toObject<Folder>().id
                        folderModel.remove(id)
                        Log.d(TAG, "Removed group with id: $id")
                    }
                }
                //Log.d("Fire_Snap_Listen","${doc.toObject(Task::class.java)}")
            }
        }

        val queryTask = db.collectionGroup("tasks").whereIn("folder_id", user.group_ids)

        val listenerTask = queryTask.addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }
            for (dc in snapshots!!.documentChanges) {
                when (dc.type) {
                    DocumentChange.Type.ADDED -> {
                        val task = dc.document.toObject<Task>()
                        taskModel[task.folder_id]!![task.id!!] = task
                        Log.d(TAG, "Added task: $task")
                    }

                    DocumentChange.Type.MODIFIED -> {
                        val task = dc.document.toObject<Task>()
                        taskModel[task.folder_id]!![task.id!!] = task
                        Log.d(TAG, "Modified task: $task")
                    }

                    DocumentChange.Type.REMOVED -> {
                        val task = dc.document.toObject<Task>()
                        taskModel[task.folder_id]!!.remove(task.id)
                        Log.d(TAG, "Removed task: $task")
                    }
                }
            }
        }

        return listOf(listenerTask,listenerFolder)
    }



    //private fun deleteTask(){}




    //private fun changeName(){ Когда-нибудь потом}
}