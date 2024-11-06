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

    // Inserting a new task list
    fun addTaskList(name: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_LIST_NAME, name)
        }
        return db.insert(TABLE_LISTS, null, values)
    }

    // Inserting a new task item
    fun addTaskItem(description: String, dueDate: String?, listId: Int): Long {
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

    // Logging database structure
    @SuppressLint("Range")
    fun logDatabaseSchema() {
        val db = readableDatabase
        val cursor = db.rawQuery("PRAGMA table_info($TABLE_ITEMS)", null)
        if (cursor.moveToFirst()) {
            do {
                val columnName = cursor.getString(cursor.getColumnIndex("name"))
                Log.d("SchemaInfo", "Column: $columnName")
            } while (cursor.moveToNext())
        }
        cursor.close()
    }
}



