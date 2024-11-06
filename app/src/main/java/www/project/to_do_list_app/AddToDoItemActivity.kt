package www.project.to_do_list_app

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import www.project.to_do_list_app.util.TaskItem

package www.project.to_do_list_app

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import www.project.to_do_list_app.util.TaskDatabaseHelper

class AddToDoItemActivity : AppCompatActivity() {

    private lateinit var itemTotalCounter: TextView
    private lateinit var completedItemCounter: TextView
    private lateinit var tvNoItem: TextView
    private lateinit var listViewItems: ListView
    private lateinit var taskDatabaseHelper: TaskDatabaseHelper

    // List to store the task items
    private val itemList = mutableListOf<TaskItem>()
    private lateinit var itemAdapter: ItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_to_do_item)

        // Retrieve the extras from the intent
        val listName = intent.getStringExtra("listName")
        val listId = intent.getIntExtra("listId", -1)

        // Use the retrieved data (listName and listId)
        if (listName != null && listId != -1) {
            // Do something with listName and listId
            title = "Add Item to $listName"
        } else {
            // Handle the case where no data was passed or invalid data
            title = "Add Item"
        }

        // Initialize views
        itemTotalCounter = findViewById(R.id.itemTotalCounter)
        completedItemCounter = findViewById(R.id.completedItemCounter)
        tvNoItem = findViewById(R.id.tvNoItem)
        listViewItems = findViewById(R.id.listViewItems)

        taskDatabaseHelper = TaskDatabaseHelper(this)

        // Set up the adapter and bind it to the ListView
        itemAdapter = ItemAdapter(this, itemList)
        listViewItems.adapter = itemAdapter

        // Add item button
        val btnAddItem = findViewById<Button>(R.id.btnAddItem)
        btnAddItem.setOnClickListener {
            showAddItemDialog(listId) // Show dialog to add new item
        }

        // Display current task items for the list
        displayItemsInListView(listId)
    }

    // Show dialog to add a new item
    private fun showAddItemDialog(listId: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Item")

        // Create a layout with EditText for task description and due date
        val view = layoutInflater.inflate(R.layout.dialog_add_item, null)
        val itemDescriptionEditText: EditText = view.findViewById(R.id.itemDescription)
        val itemDueDateEditText: EditText = view.findViewById(R.id.itemDueDate)

        builder.setView(view)

        // Add item button
        builder.setPositiveButton("Add") { dialog, _ ->
            val description = itemDescriptionEditText.text.toString().trim()
            val dueDate = itemDueDateEditText.text.toString().trim()

            if (description.isNotEmpty()) {
                // Insert the task item into the database
                val result = taskDatabaseHelper.addTaskItem(description, dueDate, listId)
                if (result != -1L) {
                    // Refresh ListView after adding the new item
                    displayItemsInListView(listId)
                    Toast.makeText(this, "Item added successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to add item", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show()
            }

            dialog.dismiss()
        }

        // Cancel button
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    // Display items in the ListView
    private fun displayItemsInListView(listId: Int) {
        val items = taskDatabaseHelper.getItemsForList(listId)

        if (items.isEmpty()) {
            tvNoItem.visibility = TextView.VISIBLE
        } else {
            tvNoItem.visibility = TextView.GONE
        }

        // Update the list in the adapter and notify changes
        itemList.clear()  // Clear the current list
        itemList.addAll(items)  // Add the new items
        itemAdapter.notifyDataSetChanged()

        // Update the counters
        itemTotalCounter.text = "Total: ${items.size}"
        completedItemCounter.text = "Completed: ${items.filter { it.isCompleted }.size}"
    }

    //Item adapter
    class ItemAdapter(private val context: Context, private val items: MutableList<TaskItem>) :
        ArrayAdapter<TaskItem>(context, 0, items) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val item = getItem(position) ?: return super.getView(position, convertView, parent)
            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_task, parent, false)

            val taskName = view.findViewById<TextView>(R.id.taskName)
            val dueDate = view.findViewById<TextView>(R.id.dueDate)

            taskName.text = item.description
            dueDate.text = item.dueDate

            return view
        }
    }

}
