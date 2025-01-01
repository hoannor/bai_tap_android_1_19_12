package vn.edu.hust.studentman

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
  private lateinit var studentDao: StudentDao
  private var students: MutableList<StudentModel> = mutableListOf()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // Initialize Room database and DAO
    val db = AppDatabase.getInstance(this)
    studentDao = db.studentDao()

    lifecycleScope.launch {
      // Load students from database
      students = withContext(Dispatchers.IO) {
        studentDao.getAllStudents().map {
          StudentModel(it.studentName, it.studentId)
        }.toMutableList()
      }
      setupRecyclerView()
    }

    findViewById<Button>(R.id.btn_add_new).setOnClickListener {
      showAddStudentDialog()
    }
  }

  private fun setupRecyclerView() {
    val studentAdapter = StudentAdapter(
      students,
      onEditClick = { adapter, student, position -> showEditStudentDialog(adapter, student, position) },
      onDeleteClick = { adapter, student, position -> deleteStudent(adapter, student, position) }
    )

    findViewById<RecyclerView>(R.id.recycler_view_students).run {
      adapter = studentAdapter
      layoutManager = LinearLayoutManager(this@MainActivity)
    }
  }

  private fun showAddStudentDialog() {
    val dialogView = LayoutInflater.from(this).inflate(R.layout.layout_dialog_add, null)
    val editName = dialogView.findViewById<EditText>(R.id.editText_name)
    val editId = dialogView.findViewById<EditText>(R.id.editText_id)

    AlertDialog.Builder(this)
      .setTitle("Thêm sinh viên mới")
      .setView(dialogView)
      .setPositiveButton("Thêm") { _, _ ->
        val name = editName.text.toString().trim()
        val id = editId.text.toString().trim()

        if (name.isEmpty() || id.isEmpty()) {
          Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
          return@setPositiveButton
        }

        lifecycleScope.launch {
          withContext(Dispatchers.IO) {
            studentDao.insertStudent(StudentEntity(studentName = name, studentId = id))
          }
          students.add(StudentModel(name, id))
          findViewById<RecyclerView>(R.id.recycler_view_students).adapter?.notifyDataSetChanged()
          Toast.makeText(this@MainActivity, "Thêm sinh viên mới thành công!", Toast.LENGTH_SHORT).show()
        }
      }
      .setNegativeButton("Hủy", null)
      .show()
  }

  private fun showEditStudentDialog(adapter: StudentAdapter, student: StudentModel, position: Int) {
    val dialogView = LayoutInflater.from(this).inflate(R.layout.layout_dialog_add, null)
    val editName = dialogView.findViewById<EditText>(R.id.editText_name)
    val editId = dialogView.findViewById<EditText>(R.id.editText_id)

    editName.setText(student.studentName)
    editId.setText(student.studentId)

    AlertDialog.Builder(this)
      .setTitle("Chỉnh sửa sinh viên")
      .setView(dialogView)
      .setPositiveButton("Lưu") { _, _ ->
        val updatedName = editName.text.toString().trim()
        val updatedId = editId.text.toString().trim()

        if (updatedName.isEmpty() || updatedId.isEmpty()) {
          Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
          return@setPositiveButton
        }

        lifecycleScope.launch {
          withContext(Dispatchers.IO) {
            studentDao.updateStudent(StudentEntity(studentName = updatedName, studentId = updatedId))
          }
          students[position] = StudentModel(updatedName, updatedId)
          adapter.notifyDataSetChanged()
          Toast.makeText(this@MainActivity, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show()
        }
      }
      .setNegativeButton("Hủy", null)
      .show()
  }

  private fun deleteStudent(adapter: StudentAdapter, student: StudentModel, position: Int) {
    AlertDialog.Builder(this)
      .setTitle("Xóa sinh viên")
      .setMessage("Bạn có chắc chắn muốn xóa sinh viên ${student.studentName} không?")
      .setPositiveButton("Xóa") { _, _ ->
        lifecycleScope.launch {
          withContext(Dispatchers.IO) {
            studentDao.deleteStudentById(student.studentId)
          }
          students.removeAt(position)
          adapter.notifyDataSetChanged()
          Toast.makeText(this@MainActivity, "Đã xóa sinh viên!", Toast.LENGTH_SHORT).show()
        }
      }
      .setNegativeButton("Hủy", null)
      .show()
  }
}
