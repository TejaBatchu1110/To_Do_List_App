package www.project.to_do_list_app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import www.project.to_do_list_app.databinding.ActivityMainBinding
import www.project.to_do_list_app.util.TaskDatabaseHelper
import www.project.to_do_list_app.util.TaskList

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        displayTaskLists()

        binding.fab.setOnClickListener { view ->
            // Create an AlertDialog Builder
            val builder = AlertDialog.Builder(this)

            // Set the dialog title and message
            builder.setTitle("Create New Task List")
            builder.setMessage("Enter the name of the new task list:")

            // Create an EditText to enter the task list name
            val input = EditText(this)
            input.hint = "Task List Name"
            builder.setView(input)

            // Set up the positive button to add the task list
            builder.setPositiveButton("Add") { dialog, _ ->
                val taskListName = input.text.toString().trim()

                if (taskListName.isNotEmpty()) {
                    addTaskList(taskListName)  // Call your addTaskList function with the entered name
                    Snackbar.make(view, "Task list '$taskListName' created.", Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.fab)
                        .show()

                    // Refresh the ListView after adding the task list
                    displayTaskLists()

                } else {
                    Snackbar.make(view, "Please enter a name for the task list.", Snackbar.LENGTH_SHORT)
                        .setAnchorView(R.id.fab)
                        .show()
                }

                dialog.dismiss()
            }

            // Set up the negative button to cancel the dialog
            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

            // Show the AlertDialog
            builder.show()
        }

    }

    // Placeholder function for adding a new task list to your database
    private fun addTaskList(taskListName: String) {

        TaskDatabaseHelper(this).addTaskList(taskListName)
    }

    // Display all task lists
    private fun displayTaskLists() {
        val taskLists = TaskDatabaseHelper(this).getAllTaskLists()

        // Find the ListView and set the custom adapter
        val listView = findViewById<ListView>(R.id.listViewTodoLists)

        // Create a custom adapter
        val adapter = object : ArrayAdapter<TaskList>(this, R.layout.task_item, taskLists) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.task_item, parent, false)
                val taskNameTextView = view.findViewById<TextView>(R.id.tvTaskName)
                val tvTaskDateDue = view.findViewById<TextView>(R.id.tvTaskDateDue)
                val ivAddItem = view.findViewById<ImageView>(R.id.ivAddItem)
                val ivEditItem = view.findViewById<ImageView>(R.id.ivEditItem)

                // Set the task name
                taskNameTextView.text = taskLists[position].name

                // Set up the add button
                ivAddItem.setOnClickListener {

                    goToAddItemActivity(taskLists[position].name,taskLists[position].id)
                }

                ivEditItem.setOnClickListener {

                    EditExistingTaskName(taskLists[position].id,taskLists[position].name)

                }

                return view
            }
        }

        listView.adapter = adapter

        if (taskLists.isEmpty()) {
            binding.contentMainLayout.tvNoItem.visibility = View.VISIBLE
        } else {
            binding.contentMainLayout.tvNoItem.visibility = View.GONE
        }
    }

    private fun goToAddItemActivity(listName: String, listId: Int) {

        // Create an Intent to start AddToDoItemActivity
        val intent = Intent(this, AddToDoItemActivity::class.java)

        // Add extras (data) to the Intent
        intent.putExtra("listName", listName)
        intent.putExtra("listId", listId)

        // Start the activity with the intent
        startActivity(intent)

    }

    private fun EditExistingTaskName(taskId: Int, taskName: String) {
        // Create an AlertDialog Builder
        val builder = AlertDialog.Builder(this)

        // Set the dialog title and message
        builder.setTitle("Edit Task Name")

        // Create an EditText to enter the new task name
        val input = EditText(this)
        input.hint = "Task List Name"
        input.setText(taskName)  // Pre-fill with the current task name
        builder.setView(input)

        // Set up the positive button to update the task list
        builder.setPositiveButton("Update") { dialog, _ ->
            val newTaskName = input.text.toString().trim()

            if (newTaskName.isNotEmpty()) {
                // Update the task list name in the database
                val dbHelper = TaskDatabaseHelper(this)
                val isUpdated = dbHelper.updateTaskListName(taskId, newTaskName)

                if (isUpdated) {
                    Snackbar.make(binding.root, "Task list name updated to '$newTaskName'.", Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.fab)
                        .show()

                    // Refresh the ListView after updating the task list
                    displayTaskLists()
                } else {
                    Snackbar.make(binding.root, "Failed to update task list name.", Snackbar.LENGTH_SHORT)
                        .setAnchorView(R.id.fab)
                        .show()
                }
            } else {
                Snackbar.make(binding.root, "Please enter a valid name for the task list.", Snackbar.LENGTH_SHORT)
                    .setAnchorView(R.id.fab)
                    .show()
            }

            dialog.dismiss()
        }

        // Set up the negative button to cancel the dialog
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        // Show the AlertDialog
        builder.show()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }


}