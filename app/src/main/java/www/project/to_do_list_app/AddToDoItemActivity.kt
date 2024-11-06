package www.project.to_do_list_app

import www.project.to_do_list_app.util.TaskItem

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import www.project.to_do_list_app.util.ItemAdapter
import www.project.to_do_list_app.util.OnItemChangesListener
import www.project.to_do_list_app.util.TaskDatabaseHelper
import java.util.Calendar

class AddToDoItemActivity : AppCompatActivity(), OnItemChangesListener {

    private lateinit var itemTotalCounter: TextView
    private lateinit var completedItemCounter: TextView
    private lateinit var tvNoItem: TextView
    private lateinit var listViewItems: ListView
    private lateinit var taskDatabaseHelper: TaskDatabaseHelper

    private lateinit var itemAdapter: ItemAdapter

    // List to store the task items
    private val itemList = mutableListOf<TaskItem>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_to_do_item)

        // Retrieve the extras from the intent
        val listName = intent.getStringExtra("listName")
        val listId = intent.getIntExtra("listId", -1)

        val tvTaskNameHeader = findViewById<TextView>(R.id.tvTaskNameHeader)
        val ivGoBack = findViewById<ImageView>(R.id.ivGoBack)

        ivGoBack.setOnClickListener{

            finish()
        }

        // Use the retrieved data (listName and listId)
        if (listName != null && listId != -1) {
            // Do something with listName and listId
            title = "Add Item to $listName"
            tvTaskNameHeader.text = title
        } else {
            // Handle the case where no data was passed or invalid data
            title = "Add Item"
            tvTaskNameHeader.text = title
        }

        // Initialize views
        itemTotalCounter = findViewById(R.id.itemTotalCounter)
        completedItemCounter = findViewById(R.id.completedItemCounter)
        tvNoItem = findViewById(R.id.tvNoItem)
        listViewItems = findViewById(R.id.listViewItems)

        taskDatabaseHelper = TaskDatabaseHelper(this)

        // Initialize adapter and set the listener
        itemAdapter = ItemAdapter(this, itemList, listId, this)  // Pass `this` as listener
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
        val itemDueDateTextView: TextView = view.findViewById(R.id.itemDueDate)

        // Set up the date picker for the due date TextView
        itemDueDateTextView.setOnClickListener {
            showDatePickerDialog { selectedDate ->
                itemDueDateTextView.text = selectedDate
            }
        }

        builder.setView(view)

        // Add item button
        builder.setPositiveButton("Add") { dialog, _ ->
            val description = itemDescriptionEditText.text.toString().trim()
            val dueDate = itemDueDateTextView.text.toString().trim()

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

    // Function to show DatePickerDialog and return selected date as a formatted string
    private fun showDatePickerDialog(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, day ->
                // Format the selected date
                val selectedDate = String.format("%02d-%02d-%d", day, month + 1, year)
                onDateSelected(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    // Display items in the ListView
    private fun displayItemsInListView(listId: Int) {
        val items = taskDatabaseHelper.getItemsByListId(listId)

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
       // completedItemCounter.text = "Completed: ${items.filter { it.isCompleted }.size}"
    }

    override fun onItemChanged(listId: Int) {

        //Refresh
        displayItemsInListView(listId)
    }


}
