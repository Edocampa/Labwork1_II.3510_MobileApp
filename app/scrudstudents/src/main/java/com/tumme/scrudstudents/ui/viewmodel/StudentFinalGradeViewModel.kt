package com.tumme.scrudstudents.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumme.scrudstudents.data.local.model.SubscribeWithCourseAndTeacher
import com.tumme.scrudstudents.data.repository.AuthRepository
import com.tumme.scrudstudents.data.repository.SCRUDRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * StudentFinalGradeViewModel - Calculates weighted average grade
 *
 * Formula: Final Grade = Σ(grade × ECTS) / Σ(ECTS)
 * Only includes courses with grades (score > 0)
 */
@HiltViewModel
class StudentFinalGradeViewModel @Inject constructor(
    private val repository: SCRUDRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _gradedCourses = MutableStateFlow<List<SubscribeWithCourseAndTeacher>>(emptyList())
    val gradedCourses: StateFlow<List<SubscribeWithCourseAndTeacher>> = _gradedCourses.asStateFlow()

    private val _finalGrade = MutableStateFlow(0f)
    val finalGrade: StateFlow<Float> = _finalGrade.asStateFlow()

    private val _totalECTS = MutableStateFlow(0)
    val totalECTS: StateFlow<Int> = _totalECTS.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        loadFinalGrade()
    }

    private fun loadFinalGrade() = viewModelScope.launch {
        _isLoading.value = true
        try {
            val currentUser = authRepository.currentUser.value
            if (currentUser != null) {
                val student = repository.getStudentByUserId(currentUser.idUser)
                if (student != null) {
                    val allSubscriptions = repository.getStudentSubscriptionsWithDetails(student.idStudent)

                    // Filter only graded courses (score > 0)
                    val graded = allSubscriptions.filter { it.subscribe.score > 0f }
                    _gradedCourses.value = graded

                    if (graded.isNotEmpty()) {
                        // Calculate weighted average
                        val totalWeightedScore = graded.sumOf {
                            (it.subscribe.score * it.courseWithTeacher.course.ectsCourse).toDouble()
                        }
                        val totalECTS = graded.sumOf {
                            it.courseWithTeacher.course.ectsCourse.toDouble()
                        }

                        _totalECTS.value = totalECTS.toInt()
                        _finalGrade.value = (totalWeightedScore / totalECTS).toFloat()
                    } else {
                        _totalECTS.value = 0
                        _finalGrade.value = 0f
                    }
                }
            }
        } catch (e: Exception) {
            _message.value = "Error calculating final grade: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
