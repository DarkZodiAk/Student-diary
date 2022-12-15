package com.example.myapplication

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.FragmentAddTaskBinding


class FragmentAddTask: Fragment(), TaskAdapter.Listener
{
    private lateinit var binding: FragmentAddTaskBinding

    private lateinit var rcView: RecyclerView
    private lateinit var btAdd: Button
    private val adapter = TaskAdapter(this) /* Создание адаптера */

    lateinit var editLauncher: ActivityResultLauncher<Intent> /* ActivityResultLauncher - класс */

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_task, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* Сюда возвращается результат */
        editLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK)
                adapter.addTask(it.data?.getSerializableExtra("task") as Task) /* getSerializableExtra - передача целого класса Task */
        }

        /* кнопка */
        rcView = view.findViewById(R.id.rcView)
        btAdd = view.findViewById(R.id.btAdd)
        rcView.layoutManager = LinearLayoutManager(context)
        rcView.adapter = adapter
        btAdd.setOnClickListener {
            editLauncher.launch(Intent(this@FragmentAddTask.context, EditActivity::class.java))
            /*val task = Task("Задача")
            adapter.addTask(task)*/
        }
        /* ?Кнопка */






        /*companion object {
            @JvmStatic
            fun newInstance() = FragmentAddTask() /* создание образца(инстанции) */
        }*/


        /*private fun init() {
        binding.apply {
            /* Форма rcView */
            rcView.layoutManager = LinearLayoutManager(context)
            rcView.adapter = adapter
            btAdd.setOnClickListener {
                val task = Task("Задача")
                adapter.addTask(task)
            }
        }
    }*/


    }

    override fun onClick(task: Task)
        {
            startActivity(Intent(this@FragmentAddTask.context,ContentActivity::class.java).apply {
                putExtra("item", task)
            })



        }

}