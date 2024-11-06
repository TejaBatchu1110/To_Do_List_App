package www.project.to_do_list_app.util

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import www.project.to_do_list_app.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ItemAdapter(
    private val context: Context,
    private val items: MutableList<TaskItem>,
    private val listId: Int,
    private val listener: OnItemChangesListener
) : ArrayAdapter<TaskItem>(context, 0, items) {


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = getItem(position) ?: return super.getView(position, convertView, parent)
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_task, parent, false)

        val taskName = view.findViewById<TextView>(R.id.taskName)
        val dueDate = view.findViewById<TextView>(R.id.dueDate)
        val ivEditTask = view.findViewById<ImageView>(R.id.ivEditTask)
        val ivDeleteTask = view.findViewById<ImageView>(R.id.ivDeleteTask)
        val ivMoveTask = view.findViewById<ImageView>(R.id.ivMoveTask)

        taskName.text = item.description
        dueDate.text = item.dueDate

        //Edit
        ivEditTask.setOnClickListener {
            showEditDialog(item.id, item.description, item.dueDate.toString())
        }

        // Set up delete button
        ivDeleteTask.setOnClickListener {
            deleteTodoItem(item.id, listId)
        }

        //set up move button
        ivMoveTask.setOnClickListener {
            showMoveDialog(item.id)
        }

        return view
    }

    //DATE PICKER
    fun showDatePickerDialog(context: Context, onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()

        // Show DatePicker dialog
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                // Set the selected date in the format you prefer, e.g., "dd/MM/yyyy"
                calendar.set(year, month, dayOfMonth)
                val selectedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
                onDateSelected(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    //Delete Item
    private fun deleteTodoItem(itemId: Int, listId: Int) {
        AlertDialog.Builder(context)
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete this item?")
            .setPositiveButton("Delete") { dialog, _ ->
                TaskDatabaseHelper(context).deleteTaskItem(itemId)
                listener.onItemChanged(listId)  // Notify the listener to refresh the list
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Move Item
    private fun showMoveDialog(itemId: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Move Item")

        // Get list of all lists from the database
        val dbHelper = TaskDatabaseHelper(context)
        val listNames = dbHelper.getAllListNames() // Assume it returns a list of list names
        val listIds = dbHelper.getAllListIds() // Get the corresponding list IDs

        builder.setItems(listNames.toTypedArray()) { dialog, which ->
            val targetListId = listIds[which]
            moveTodoItem(itemId, targetListId) // Move item to the selected list
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    // Move item to a new list and refresh
    private fun moveTodoItem(itemId: Int, targetListId: Int) {
        val result = TaskDatabaseHelper(context).moveTaskItem(itemId, targetListId)
        if (result > 0) {
            listener.onItemChanged(listId) // Notify the listener to refresh the list
        } else {
            Toast.makeText(context, "Failed to move item", Toast.LENGTH_SHORT).show()
        }
    }


    //Edit Item
    private fun showEditDialog(itemId: Int, currentDescription: String, currentDueDate: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Edit Item")

        // Inflate the custom view for editing
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_item, null)
        val descriptionEditText = view.findViewById<EditText>(R.id.editDescription)
        val dueDateTextView = view.findViewById<TextView>(R.id.editDueDate)

        // Populate fields with current values
        descriptionEditText.setText(currentDescription)
        dueDateTextView.text = currentDueDate

        // Set up a date picker dialog for due date
        dueDateTextView.setOnClickListener {
            showDatePickerDialog(context) { selectedDate ->
                dueDateTextView.text = selectedDate
            }
        }

        builder.setView(view)

        // Confirm edit button
        builder.setPositiveButton("Save") { dialog, _ ->
            val newDescription = descriptionEditText.text.toString().trim()
            val newDueDate = dueDateTextView.text.toString().trim()

            if (newDescription.isNotEmpty()) {
                // Update the item in the database
                val result = TaskDatabaseHelper(context).editTaskItem(itemId, newDescription, newDueDate)
                if (result) {
                    listener.onItemChanged(listId) // Notify to refresh the list
                    Toast.makeText(context, "Item updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to update item", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Please enter a description", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }


}
