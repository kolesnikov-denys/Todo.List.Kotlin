package com.example.todolist


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.adapter.OnViewHolderClickListener
import com.example.todolist.adapter.TasksAdapter
import kotlinx.android.synthetic.main.dialog_input.view.*
import kotlinx.android.synthetic.main.fragment_tasks.*

class TasksFragment : Fragment(), OnViewHolderClickListener {
    private var tasks = mutableListOf<String>()
    private val adapter = TasksAdapter(tasks, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.addItem -> showCreateTaskDialog(TaskAction.NEW)
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }

    @SuppressLint("InflateParams")
    private fun showCreateTaskDialog(action: TaskAction, position: Int? = null) {
        val builder = AlertDialog.Builder(context)
            .setTitle(action.titleResId)

        var onShowListener: DialogInterface.OnShowListener? = null

        when (action) {
            TaskAction.NEW, TaskAction.EDIT -> {
                val view = LayoutInflater.from(context).inflate(R.layout.dialog_input, null)

                builder.setView(view).apply {
                    if (action == TaskAction.NEW) {
                        setPositiveButton(R.string.add) { _, _ ->
                            addTask(view.descriptionEditText.text.toString())
                        }
                    } else {
                        if (position != null) {
                            view.descriptionEditText.setText(tasks[position])
                        }

                        setTitle(R.string.edit)
                        setNeutralButton(R.string.delete) { _, _ ->
                            showCreateTaskDialog(TaskAction.DELETE, position)
                        }
                        setPositiveButton(R.string.save) { _, _ ->
                            val text = view.descriptionEditText.text.toString()
                            if (position == null) {
                                addTask(text)
                            } else {
                                editTask(position, text)
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
                            s: CharSequence, start: Int,
                            count: Int, after: Int
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
                builder.setNegativeButton(R.string.cancel) { _, _ ->
                    showCreateTaskDialog(TaskAction.EDIT, position)
                }.setPositiveButton(R.string.delete) { _, _ ->
                    if (position != null) deleteTask(position)
                }
            }
        }

        val dialog = builder.create()

        onShowListener?.also { dialog.setOnShowListener(it) }

        dialog.show()
    }

    private fun addTask(task: String) {
        tasks.add(task)
        adapter.notifyDataSetChanged()
    }

    private fun deleteTask(position: Int) {
        tasks.removeAt(position)
        adapter.notifyDataSetChanged()
    }

    private fun editTask(position: Int, task: String) {
        tasks[position] = task
        adapter.notifyDataSetChanged()
    }

    override fun onViewHolderClick(holder: RecyclerView.ViewHolder, position: Int, id: Int) {
        showCreateTaskDialog(TaskAction.EDIT, position)
    }
}
