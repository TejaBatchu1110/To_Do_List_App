package www.project.to_do_list_app.util

data class TaskItem(
    val id: Int,
    val description: String,
    val dueDate: String?,
    val status: Int,
    val listId: Int
)