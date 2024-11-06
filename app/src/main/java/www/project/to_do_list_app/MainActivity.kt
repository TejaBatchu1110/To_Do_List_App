package www.project.to_do_list_app

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import www.project.to_do_list_app.databinding.ActivityMainBinding
import www.project.to_do_list_app.util.TaskDatabaseHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

//        // Access tvNoItem through contentMainLayout binding
//        binding.contentMainLayout.tvNoItem.visibility = View.VISIBLE
//        binding.contentMainLayout.listViewTodoLists

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

    // Display all task lists in a ListView
    private fun displayTaskLists() {

        val taskLists =  TaskDatabaseHelper(this).getAllTaskLists()

        // Find the ListView and set the adapter
        val listView = findViewById<ListView>(R.id.listViewTodoLists)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, taskLists)
        listView.adapter = adapter

        // Show or hide the "No Items" message based on whether there are task lists
        if (taskLists.isEmpty()) {
            binding.contentMainLayout.tvNoItem.visibility = View.VISIBLE
        } else {
            binding.contentMainLayout.tvNoItem.visibility = View.GONE
        }
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