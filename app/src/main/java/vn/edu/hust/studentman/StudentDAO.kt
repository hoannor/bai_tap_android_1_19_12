package vn.edu.hust.studentman

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface StudentDao {
    @Query("SELECT * FROM students")
    fun getAllStudents(): List<StudentEntity>

    @Insert
    fun insertStudent(student: StudentEntity)

    @Update
    fun updateStudent(student: StudentEntity)

    @Query("DELETE FROM students WHERE studentId = :studentId")
    fun deleteStudentById(studentId: String)
}
