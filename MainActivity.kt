
package com.example.todoalarm

import android.app.Activity
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : Activity() {

    private lateinit var taskInput: EditText
    private lateinit var addButton: Button
    private lateinit var clearButton: Button
    private lateinit var listView: ListView

    private val tasks = mutableListOf<TaskItem>()
    private lateinit var adapter: TaskAdapter

    private val prefs by lazy {
        getSharedPreferences("todo_prefs", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        taskInput = findViewById(R.id.taskInput)
        addButton = findViewById(R.id.addTaskBtn)
        clearButton = findViewById(R.id.clearAllBtn)
        listView = findViewById(R.id.listView)

        loadTasks()

        adapter = TaskAdapter()
        listView.adapter = adapter

        addButton.setOnClickListener {
            val text = taskInput.text.toString().trim()
            if (text.isNotEmpty()) {
                tasks.add(TaskItem(text, false))
                saveTasks()
                adapter.notifyDataSetChanged()
                taskInput.text.clear()
            }
        }

        clearButton.setOnClickListener {
            tasks.clear()
            saveTasks()
            adapter.notifyDataSetChanged()
        }
    }

    private fun saveTasks() {
        val jsonArray = JSONArray()
        for (task in tasks) {
            val obj = JSONObject()
            obj.put("text", task.text)
            obj.put("completed", task.completed)
            jsonArray.put(obj)
        }
        prefs.edit().putString("tasks", jsonArray.toString()).apply()
    }

    private fun loadTasks() {
        tasks.clear()
        val data = prefs.getString("tasks", null) ?: return
        val jsonArray = JSONArray(data)
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            tasks.add(
                TaskItem(
                    obj.getString("text"),
                    obj.getBoolean("completed")
                )
            )
        }
    }

    inner class TaskAdapter : BaseAdapter() {

        override fun getCount() = tasks.size
        override fun getItem(position: Int) = tasks[position]
        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(this@MainActivity)
                .inflate(R.layout.row_task, parent, false)

            val checkBox = view.findViewById<CheckBox>(R.id.taskCheckBox)
            val textView = view.findViewById<TextView>(R.id.taskText)

            val task = tasks[position]

            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = task.completed

            if (task.completed) {
                val span = SpannableString(task.text)
                span.setSpan(StrikethroughSpan(), 0, task.text.length, 0)
                textView.text = span
                textView.setTextColor(0xFFFF4444.toInt())
            } else {
                textView.text = task.text
                textView.setTextColor(0xFF000000.toInt())
            }

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                task.completed = isChecked
                saveTasks()
                notifyDataSetChanged()
            }

            return view
        }
    }
}

data class TaskItem(
    val text: String,
    var completed: Boolean
)
