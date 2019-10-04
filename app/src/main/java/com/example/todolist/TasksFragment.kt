package com.example.todolist


import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.adapter.OnViewHolderClickListener
import com.example.todolist.adapter.OnViewHolderLongClickListener
import com.example.todolist.adapter.TasksAdapter
import com.example.todolist.data.TaskViewModel
import com.example.todolist.model.Task
import kotlinx.android.synthetic.main.dialog_input.view.*
import kotlinx.android.synthetic.main.fragment_tasks.*

class TasksFragment : Fragment(), OnViewHolderClickListener, OnViewHolderLongClickListener,
    View.OnClickListener {
    private val adapter = TasksAdapter(this)
    private lateinit var taskViewModel: TaskViewModel
    private var tasksList: List<Task>? = null
    private var animation: Animation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        animation = AnimationUtils.loadAnimation(context, R.anim.myrotate)
        taskViewModel = ViewModelProvider(this).get(TaskViewModel::class.java)
        taskViewModel.allTasks.observe(this, Observer { tasks ->
            tasks?.let { it ->
                tasksList = it
                tasksList?.let { adapter.setTasks(it) }
            }
        })
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tasks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(DividerItemDecoration(context, layoutManager.orientation))

        floatingActionButton.setOnClickListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.allItem -> {
                tasksList?.let { adapter.setTasks(it) }
            }
            R.id.doneItem -> {
                var tasksListDone: List<Task>? = tasksList
                tasksListDone = tasksListDone!!.filter { it.favorite }
                adapter.setTasks(tasksListDone)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.floatingActionButton -> {
                actionTaskDialog(TaskAction.NEW)
                floatingActionButton.startAnimation(animation)
            }
        }
    }

    override fun onViewHolderClick(holder: RecyclerView.ViewHolder, position: Int, id: Int) {
        when (id) {
            R.id.taskFavoriteImageView -> doneTask(adapter.getTask(position))
            R.id.container_single, R.id.container_two -> actionTaskDialog(TaskAction.EDIT, position)
        }
    }

    override fun onViewHolderLongClick(
        holder: RecyclerView.ViewHolder,
        position: Int,
        id: Int
    ): Boolean {
        actionTaskDialog(TaskAction.DELETE, position)
        return true
    }

    private fun actionTaskDialog(action: TaskAction, position: Int? = null) {

        val task: Task? = position?.let { adapter.getTask(it) }

        val builder = AlertDialog.Builder(context).setTitle(action.titleResId)
        var onShowListener: DialogInterface.OnShowListener? = null


        when (action) {
            TaskAction.NEW, TaskAction.EDIT -> {
                val view = View.inflate(context, R.layout.dialog_input, null)
                builder.setView(view).apply {
                    if (action == TaskAction.NEW) {
                        setPositiveButton(R.string.add) { _, _ ->
                            addTask(
                                Task(
                                    view.titleEditText.text.toString(),
                                    view.descriptionEditText.text.toString()
                                )
                            )
                        }
                    } else {
                        if (position != null) {
                            view.titleEditText.setText(task?.title)
                            view.descriptionEditText.setText(task?.descriptions)
                        }
                        setTitle(R.string.edit)
                        setPositiveButton(R.string.save) { _, _ ->
                            val title = view.titleEditText.text.toString()
                            val description = view.descriptionEditText.text.toString()
                            if (position == null) {
                                addTask(Task(title, description))
                            } else {
                                if (task != null) {
                                    editTask(
                                        Task(task.id, title, description, task.favorite)
                                    )
                                }
                            }
                        }
                    }
                }.setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }
                onShowListener = DialogInterface.OnShowListener { dialog ->
                    if (dialog !is AlertDialog) return@OnShowListener
                    val button = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    button.isEnabled = view.descriptionEditText.text.toString().isNotEmpty()
                    view.descriptionEditText.addTextChangedListener(object : TextWatcher {
                        override fun afterTextChanged(s: Editable) = Unit
                        override fun beforeTextChanged(
                            s: CharSequence, start: Int, count: Int, after: Int
                        ) = Unit

                        override fun onTextChanged(
                            s: CharSequence,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {
                            button.isEnabled = s.isNotEmpty()
                        }
                    })
                }
            }
            TaskAction.DELETE -> {
                builder.setNegativeButton(R.string.cancel) { _, _ -> }
                    .setPositiveButton(R.string.delete) { _, _ ->
                        if (position != null) deleteTask(adapter.getTask(position))
                    }
            }
        }

        val dialog = builder.create()
        onShowListener?.also { dialog.setOnShowListener(it) }
        dialog.show()
    }

    private fun addTask(task: Task) {
        taskViewModel.insert(task)
    }

    private fun deleteTask(task: Task) {
        taskViewModel.delete(task)
    }

    private fun editTask(task: Task) {
        taskViewModel.insert(task)
    }

    private fun doneTask(task: Task) {
        task.favorite = task.favorite != true
        taskViewModel.insert(task)
    }
}
