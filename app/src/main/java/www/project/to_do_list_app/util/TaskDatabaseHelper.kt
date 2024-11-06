package www.project.to_do_list_app.util

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class TaskDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "tasks.db"
        private const val DB_VERSION = 1

        // Table for task lists
        const val TABLE_LISTS = "task_lists"
        const val COL_LIST_ID = "id"
        const val COL_LIST_NAME = "name"

        // Table for task items
        const val TABLE_ITEMS = "task_items"
        const val COL_ITEM_ID = "item_id"
        const val COL_ITEM_DESC = "description"
        const val COL_ITEM_DUE = "due_date"
        const val COL_ITEM_STATUS = "status"
        const val COL_LIST_REF_ID = "list_id"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createListTable = """
            CREATE TABLE $TABLE_LISTS (
                $COL_LIST_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_LIST_NAME TEXT NOT NULL UNIQUE
            )
        """.trimIndent()

        val createItemTable = """
            CREATE TABLE $TABLE_ITEMS (
                $COL_ITEM_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_ITEM_DESC TEXT NOT NULL,
                $COL_ITEM_DUE TEXT,
                $COL_ITEM_STATUS INTEGER DEFAULT 0,
                $COL_LIST_REF_ID INTEGER,
                FOREIGN KEY($COL_LIST_REF_ID) REFERENCES $TABLE_LISTS($COL_LIST_ID)
            )
        """.trimIndent()

        db.execSQL(createListTable)
        db.execSQL(createItemTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ITEMS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LISTS")
        onCreate(db)
    }

    //Check if task exists
    fun isListNameExists(listName: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT COUNT(*) FROM task_lists WHERE name = ?"
        val cursor = db.rawQuery(query, arrayOf(listName))

        var exists = false
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0
        }
        cursor.close()
        return exists
    }


    // Inserting a new task list
    fun addTaskList(name: String): Boolean {
        // Check if the list name already exists
        if (isListNameExists(name)) {
            return false
        }

        // Proceed with adding the list if it doesn't exist
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_LIST_NAME, name)
        }
        db.insert(TABLE_LISTS, null, values)
        return true
    }

    //Check items duplicate
    fun isItemDescriptionExistsInList(listId: Int, description: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT COUNT(*) FROM $TABLE_ITEMS WHERE $COL_LIST_REF_ID = ? AND $COL_ITEM_DESC = ?"
        val cursor = db.rawQuery(query, arrayOf(listId.toString(), description))

        var exists = false
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0
        }
        cursor.close()
        return exists
    }

    // Inserting a new task item
    fun addTaskItem(description: String, dueDate: String?, listId: Int): Long {

        // Check if an item with the same description already exists in the list
        if (isItemDescriptionExistsInList(listId, description)) {
            throw IllegalArgumentException("Item with the same description already exists in this list.")
            // return -1L // Return -1 to indicate the item already exists
        }

        // Insert the item if it doesn't exist
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_ITEM_DESC, description)
            put(COL_ITEM_DUE, dueDate)
            put(COL_LIST_REF_ID, listId)
        }
        return db.insert(TABLE_ITEMS, null, values)
    }

    // Fetching task lists
    @SuppressLint("Range")
    fun getAllTaskLists(): List<TaskList> {
        val taskLists = mutableListOf<TaskList>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_LISTS", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex(COL_LIST_ID))
                val name = cursor.getString(cursor.getColumnIndex(COL_LIST_NAME))
                taskLists.add(TaskList(id, name))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return taskLists
    }


    // Fetching task items for a specific list
    @SuppressLint("Range")
    fun getItemsByListId(listId: Int): List<TaskItem> {
        val items = mutableListOf<TaskItem>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_ITEMS WHERE $COL_LIST_REF_ID = ?", arrayOf(listId.toString()))
        if (cursor.moveToFirst()) {
            do {
                val itemId = cursor.getInt(cursor.getColumnIndex(COL_ITEM_ID))
                val description = cursor.getString(cursor.getColumnIndex(COL_ITEM_DESC))
                val dueDate = cursor.getString(cursor.getColumnIndex(COL_ITEM_DUE))
                val status = cursor.getInt(cursor.getColumnIndex(COL_ITEM_STATUS))
                items.add(TaskItem(itemId, description, dueDate, status, listId))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return items
    }

    //Move task Item to another List
    fun moveTaskItem(itemId: Int, targetListId: Int): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_LIST_REF_ID, targetListId) // Assuming your column is named listId
        }
        return db.update(TABLE_ITEMS, values, "item_id = ?", arrayOf(itemId.toString()))
    }


    // Update the task list's name by its ID
    fun updateTaskListName(taskId: Int, newName: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_LIST_NAME, newName)
        }

        // Update the record in the database
        val rowsAffected = db.update(TABLE_LISTS, values, "$COL_LIST_ID = ?", arrayOf(taskId.toString()))
        return rowsAffected > 0
    }

    // Updating the completion status of a task item
    fun updateTaskStatus(itemId: Int, status: Int): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_ITEM_STATUS, status)
        }
        val rowsAffected = db.update(TABLE_ITEMS, values, "$COL_ITEM_ID = ?", arrayOf(itemId.toString()))
        db.close()
        return rowsAffected > 0
    }

    // Deleting a task item by ID
    fun deleteTaskItem(itemId: Int): Boolean {
        val db = writableDatabase
        val rowsDeleted = db.delete(TABLE_ITEMS, "$COL_ITEM_ID = ?", arrayOf(itemId.toString()))
        db.close()
        return rowsDeleted > 0
    }

    fun editTaskItem(itemId: Int, newDescription: String, newDueDate: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_ITEM_DESC, newDescription)
            put(COL_ITEM_DUE, newDueDate)
        }
        // Update the item in the database by its ID
        val rowsAffected = db.update(TABLE_ITEMS, values, "$COL_ITEM_ID = ?", arrayOf(itemId.toString()))
        return rowsAffected > 0
    }


    // Updating a task item's description and due date
    fun updateTaskDetails(itemId: Int, newDesc: String, newDueDate: String?): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_ITEM_DESC, newDesc)
            put(COL_ITEM_DUE, newDueDate)
        }
        val rowsAffected = db.update(TABLE_ITEMS, values, "$COL_ITEM_ID = ?", arrayOf(itemId.toString()))
        db.close()
        return rowsAffected > 0
    }

    // Method to get all list names
    fun getAllListNames(): List<String> {
        val listNames = mutableListOf<String>()
        val db = readableDatabase
        val cursor = db.query(TABLE_LISTS, arrayOf(COL_LIST_NAME), null, null, null, null, null)
        cursor?.use {
            while (it.moveToNext()) {
                val name = it.getString(it.getColumnIndexOrThrow(COL_LIST_NAME))
                listNames.add(name)
            }
        }
        cursor.close()
        return listNames
    }

    // Method to get all list IDs
    fun getAllListIds(): List<Int> {
        val listIds = mutableListOf<Int>()
        val db = readableDatabase
        val cursor = db.query(TABLE_LISTS, arrayOf(COL_LIST_ID), null, null, null, null, null)
        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndexOrThrow(COL_LIST_ID))
                listIds.add(id)
            }
        }
        cursor.close()
        return listIds
    }

    // Get the nearest due date for items in a specific list
//    fun getNearestDueDateForList(listId: Int): String? {
//        val db = this.readableDatabase
//        val query = """
//        SELECT $COL_ITEM_DUE
//        FROM $TABLE_ITEMS
//        WHERE $COL_LIST_REF_ID = ?
//        ORDER BY $COL_ITEM_DUE ASC
//        LIMIT 1
//    """
//        val cursor = db.rawQuery(query, arrayOf(listId.toString()))
//        val nearestDueDate = if (cursor.moveToFirst()) cursor.getString(0) else null
//        cursor.close()
//        return nearestDueDate
//    }

    @SuppressLint("Range")
    fun getNearestDueDateForList(taskListId: Int): String? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_ITEM_DUE FROM $TABLE_ITEMS WHERE $COL_LIST_REF_ID = ? AND $COL_ITEM_DUE IS NOT NULL ORDER BY $COL_ITEM_DUE ASC LIMIT 1",
            arrayOf(taskListId.toString())
        )

        return if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndex(COL_ITEM_DUE))
        } else {
            null
        }
    }



    //Update checkbox
    fun updateCheckboxTodoItem(taskItem: TaskItem) {
        val db = this.writableDatabase  // Use writableDatabase to update
        val contentValues = ContentValues()
        contentValues.put(COL_ITEM_STATUS, taskItem.status)

        // Update the task in the database using its ID
        val whereClause = "$COL_ITEM_ID = ?"
        val whereArgs = arrayOf(taskItem.id.toString())

        val rowsUpdated = db.update(TABLE_ITEMS, contentValues, whereClause, whereArgs)

        if (rowsUpdated == 0) {
            Log.e("DBHelper", "Error updating task with ID: ${taskItem.id}")
        }
    }



}



