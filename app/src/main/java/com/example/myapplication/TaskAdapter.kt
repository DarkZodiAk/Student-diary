package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.TaskItemBinding

class TaskAdapter(val listener: Listener): RecyclerView.Adapter<TaskAdapter.TaskHolder>() {    /* ViewHolder - класс, который содержит ссылки на все view(textView,imageView) в одном элементе */
    val taskList = ArrayList<Task>()


    /* Создание ViewHolder */
    /* Здесь заполняется View */
    class TaskHolder(item: View): RecyclerView.ViewHolder(item) {    /* View - это класс */    /* TaskHolder наследуется от ViewHolder */

        var binding = TaskItemBinding.bind(item) /* мы уже наполнили все в view, поэтому не надо писать inflate... */
        fun bind(task: Task, listener: Listener) { /* заполнение view */
            binding.tvTitle.text = task.title
            itemView.setOnClickListener {
                listener.onClick(task)
            }


        }



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskHolder { /* берет разметку task_item, создает его и создает класс viewHolder(TaskHolder) */
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false) /* надуваем view с помощью inflate */
        return TaskHolder(view) /* передача view */
    }

    override fun onBindViewHolder(holder: TaskHolder, position: Int) { /* после onCreateViewHolder передается класс TaskHolder */
        holder.bind(taskList[position], listener)
    }

    override fun getItemCount(): Int {
        return taskList.size
    }



    fun addTask(task: Task) {
        taskList.add(task)
        notifyDataSetChanged() /* проверяет наличие новых элементов, если так, то добавляет его */
    }

    /* Взаимодействие с объектами 'задача' */
    interface Listener {
        fun onClick(task: Task)
    }

}