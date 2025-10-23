package com.example.app.dto;

import java.util.List;

/**
 * DTO cho Student Portal Lưu ý: StudentScheduleInfo và ScheduleItem đã được
 * thay thế bằng StudentScheduleDetailDTO Sử dụng StudentScheduleDetailDTO để
 * lấy thời khóa biểu từ bảng student_schedule
 */
public class StudentPortalInfo {

	// thông tin về điểm
	public static class StudentGradesInfo {
		private Long studentId;
		private String studentCode;
		private String studentName;
		private Double gpa;
		private Integer totalCredits;
		private Integer completedCredits;
		private List<GradeItem> gradeItems;
		// Statistics
		private Integer totalCourses;
		private Integer completedCourses;
		private Integer inProgressCourses;

		// Constructors
		public StudentGradesInfo() {
		}

		public StudentGradesInfo(Long studentId, String studentCode, String studentName, Double gpa,
				Integer totalCredits, Integer completedCredits, List<GradeItem> gradeItems, Integer totalCourses,
				Integer completedCourses, Integer inProgressCourses) {
			this.studentId = studentId;
			this.studentCode = studentCode;
			this.studentName = studentName;
			this.gpa = gpa;
			this.totalCredits = totalCredits;
			this.completedCredits = completedCredits;
			this.gradeItems = gradeItems;
			this.totalCourses = totalCourses;
			this.completedCourses = completedCourses;
			this.inProgressCourses = inProgressCourses;
		}

		// Getters and Setters
		public Long getStudentId() {
			return studentId;
		}

		public void setStudentId(Long studentId) {
			this.studentId = studentId;
		}

		public String getStudentCode() {
			return studentCode;
		}

		public void setStudentCode(String studentCode) {
			this.studentCode = studentCode;
		}

		public String getStudentName() {
			return studentName;
		}

		public void setStudentName(String studentName) {
			this.studentName = studentName;
		}

		public Double getGpa() {
			return gpa;
		}

		public void setGpa(Double gpa) {
			this.gpa = gpa;
		}

		public Integer getTotalCredits() {
			return totalCredits;
		}

		public void setTotalCredits(Integer totalCredits) {
			this.totalCredits = totalCredits;
		}

		public Integer getCompletedCredits() {
			return completedCredits;
		}

		public void setCompletedCredits(Integer completedCredits) {
			this.completedCredits = completedCredits;
		}

		public List<GradeItem> getGradeItems() {
			return gradeItems;
		}

		public void setGradeItems(List<GradeItem> gradeItems) {
			this.gradeItems = gradeItems;
		}

		public Integer getTotalCourses() {
			return totalCourses;
		}

		public void setTotalCourses(Integer totalCourses) {
			this.totalCourses = totalCourses;
		}

		public Integer getCompletedCourses() {
			return completedCourses;
		}

		public void setCompletedCourses(Integer completedCourses) {
			this.completedCourses = completedCourses;
		}

		public Integer getInProgressCourses() {
			return inProgressCourses;
		}

		public void setInProgressCourses(Integer inProgressCourses) {
			this.inProgressCourses = inProgressCourses;
		}
	}

	// điểm của từng môn
	public static class GradeItem {
		private Long courseId;
		private String courseCode;
		private String courseName;
		private Integer credit;
		private Double componentScore1;
		private Double componentScore2;
		private Double finalExamScore;
		private Double totalScore;
		private Double scoreCoefficient4;
		private String grade;
		private String semester;
		private String status;

		// Constructors
		public GradeItem() {
		}

		public GradeItem(Long courseId, String courseCode, String courseName, Integer credit, Double componentScore1,
				Double componentScore2, Double finalExamScore, Double totalScore, Double scoreCoefficient4,
				String grade, String semester, String status) {
			this.courseId = courseId;
			this.courseCode = courseCode;
			this.courseName = courseName;
			this.credit = credit;
			this.componentScore1 = componentScore1;
			this.componentScore2 = componentScore2;
			this.finalExamScore = finalExamScore;
			this.totalScore = totalScore;
			this.scoreCoefficient4 = scoreCoefficient4;
			this.grade = grade;
			this.semester = semester;
			this.status = status;
		}

		// Getters and Setters
		public Long getCourseId() {
			return courseId;
		}

		public void setCourseId(Long courseId) {
			this.courseId = courseId;
		}

		public String getCourseCode() {
			return courseCode;
		}

		public void setCourseCode(String courseCode) {
			this.courseCode = courseCode;
		}

		public String getCourseName() {
			return courseName;
		}

		public void setCourseName(String courseName) {
			this.courseName = courseName;
		}

		public Integer getCredit() {
			return credit;
		}

		public void setCredit(Integer credit) {
			this.credit = credit;
		}

		public Double getComponentScore1() {
			return componentScore1;
		}

		public void setComponentScore1(Double componentScore1) {
			this.componentScore1 = componentScore1;
		}

		public Double getComponentScore2() {
			return componentScore2;
		}

		public void setComponentScore2(Double componentScore2) {
			this.componentScore2 = componentScore2;
		}

		public Double getFinalExamScore() {
			return finalExamScore;
		}

		public void setFinalExamScore(Double finalExamScore) {
			this.finalExamScore = finalExamScore;
		}

		public String getGrade() {
			return grade;
		}

		public void setGrade(String grade) {
			this.grade = grade;
		}

		public String getSemester() {
			return semester;
		}

		public void setSemester(String semester) {
			this.semester = semester;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public Double getTotalScore() {
			return totalScore;
		}

		public void setTotalScore(Double totalScore) {
			this.totalScore = totalScore;
		}

		public Double getScoreCoefficient4() {
			return scoreCoefficient4;
		}

		public void setScoreCoefficient4(Double scoreCoefficient4) {
			this.scoreCoefficient4 = scoreCoefficient4;
		}
	}

	// học phần có thể đăng ký
	public static class AvailableCourseInfo {
		private Long courseId;
		private String courseCode;
		private String courseName;
		private Integer credit;
		private boolean canRegister;
		private String reason;
		private Integer availableSlots;
		private Integer maxSlots;
		private String lecturerName;
		private String period;
		private String dayOfWeek;
		private String classroom;
		private String semester;
		private boolean canUnregister;

		// Constructors
		public AvailableCourseInfo() {
		}

		public AvailableCourseInfo(Long courseId, String courseCode, String courseName, Integer credit,
				boolean canRegister, String reason, Integer availableSlots, Integer maxSlots, String lecturerName,
				String period, String dayOfWeek, String classroom, String semester, boolean canUnregister) {
			this.courseId = courseId;
			this.courseCode = courseCode;
			this.courseName = courseName;
			this.credit = credit;
			this.canRegister = canRegister;
			this.reason = reason;
			this.availableSlots = availableSlots;
			this.maxSlots = maxSlots;
			this.lecturerName = lecturerName;
			this.period = period;
			this.dayOfWeek = dayOfWeek;
			this.classroom = classroom;
			this.semester = semester;
			this.canUnregister = canUnregister;
		}

		// Getters and Setters
		public Long getCourseId() {
			return courseId;
		}

		public void setCourseId(Long courseId) {
			this.courseId = courseId;
		}

		public String getCourseCode() {
			return courseCode;
		}

		public void setCourseCode(String courseCode) {
			this.courseCode = courseCode;
		}

		public String getCourseName() {
			return courseName;
		}

		public void setCourseName(String courseName) {
			this.courseName = courseName;
		}

		public Integer getCredit() {
			return credit;
		}

		public void setCredit(Integer credit) {
			this.credit = credit;
		}

		public boolean isCanRegister() {
			return canRegister;
		}

		public void setCanRegister(boolean canRegister) {
			this.canRegister = canRegister;
		}

		public String getReason() {
			return reason;
		}

		public void setReason(String reason) {
			this.reason = reason;
		}

		public Integer getAvailableSlots() {
			return availableSlots;
		}

		public void setAvailableSlots(Integer availableSlots) {
			this.availableSlots = availableSlots;
		}

		public Integer getMaxSlots() {
			return maxSlots;
		}

		public void setMaxSlots(Integer maxSlots) {
			this.maxSlots = maxSlots;
		}

		public String getLecturerName() {
			return lecturerName;
		}

		public void setLecturerName(String lecturerName) {
			this.lecturerName = lecturerName;
		}

		public String getPeriod() {
			return period;
		}

		public void setPeriod(String period) {
			this.period = period;
		}

		public String getDayOfWeek() {
			return dayOfWeek;
		}

		public void setDayOfWeek(String dayOfWeek) {
			this.dayOfWeek = dayOfWeek;
		}

		public String getClassroom() {
			return classroom;
		}

		public void setClassroom(String classroom) {
			this.classroom = classroom;
		}

		public String getSemester() {
			return semester;
		}

		public void setSemester(String semester) {
			this.semester = semester;
		}

		public boolean isCanUnregister() {
			return canUnregister;
		}

		public void setCanUnregister(boolean canUnregister) {
			this.canUnregister = canUnregister;
		}
	}

	// đăng ký học phần
	public static class CourseRegistrationRequest {
		private Long courseId;
		private String semester;

		// Constructors
		public CourseRegistrationRequest() {
		}

		public CourseRegistrationRequest(Long courseId, String semester) {
			this.courseId = courseId;
			this.semester = semester;
		}

		// Getters and Setters
		public Long getCourseId() {
			return courseId;
		}

		public void setCourseId(Long courseId) {
			this.courseId = courseId;
		}

		public String getSemester() {
			return semester;
		}

		public void setSemester(String semester) {
			this.semester = semester;
		}
	}

	// phản hồi khi đăng ký học phần
	public static class CourseRegistrationResponse {
		private boolean success;
		private String message;

		// Constructors
		public CourseRegistrationResponse() {
		}

		public CourseRegistrationResponse(boolean success, String message) {
			this.success = success;
			this.message = message;
		}

		// Getters and Setters
		public boolean isSuccess() {
			return success;
		}

		public void setSuccess(boolean success) {
			this.success = success;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	// thông tin về semester filtering
	public static class SemesterFilterInfo {
		private String currentSemester;
		private String latestSemester;
		private boolean isFilteredByLatest;

		// Constructors
		public SemesterFilterInfo() {
		}

		public SemesterFilterInfo(String currentSemester, String latestSemester, boolean isFilteredByLatest) {
			this.currentSemester = currentSemester;
			this.latestSemester = latestSemester;
			this.isFilteredByLatest = isFilteredByLatest;
		}

		// Getters and Setters
		public String getCurrentSemester() {
			return currentSemester;
		}

		public void setCurrentSemester(String currentSemester) {
			this.currentSemester = currentSemester;
		}

		public String getLatestSemester() {
			return latestSemester;
		}

		public void setLatestSemester(String latestSemester) {
			this.latestSemester = latestSemester;
		}

		public boolean isFilteredByLatest() {
			return isFilteredByLatest;
		}

		public void setFilteredByLatest(boolean isFilteredByLatest) {
			this.isFilteredByLatest = isFilteredByLatest;
		}
	}

	// DTO cho thông tin cá nhân sinh viên
	public static class StudentProfileInfo {
		private Long studentId;
		private String studentCode;
		private String fullName;
		private String email;
		private String phone;
		private String className;
		private String departmentName;
		private String year;

		// Constructors
		public StudentProfileInfo() {
		}

		public StudentProfileInfo(Long studentId, String studentCode, String fullName, String email, String phone,
				String className, String departmentName, String year) {
			this.studentId = studentId;
			this.studentCode = studentCode;
			this.fullName = fullName;
			this.email = email;
			this.phone = phone;
			this.className = className;
			this.departmentName = departmentName;
			this.year = year;
		}

		// Getters and Setters
		public Long getStudentId() {
			return studentId;
		}

		public void setStudentId(Long studentId) {
			this.studentId = studentId;
		}

		public String getStudentCode() {
			return studentCode;
		}

		public void setStudentCode(String studentCode) {
			this.studentCode = studentCode;
		}

		public String getFullName() {
			return fullName;
		}

		public void setFullName(String fullName) {
			this.fullName = fullName;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getPhone() {
			return phone;
		}

		public void setPhone(String phone) {
			this.phone = phone;
		}

		public String getClassName() {
			return className;
		}

		public void setClassName(String className) {
			this.className = className;
		}

		public String getDepartmentName() {
			return departmentName;
		}

		public void setDepartmentName(String departmentName) {
			this.departmentName = departmentName;
		}

		public String getYear() {
			return year;
		}

		public void setYear(String year) {
			this.year = year;
		}
	}

	// DTO cho thông tin thanh toán học phí - Sử dụng Share.PaymentSummaryDTO thay
	// thế
	// public static class PaymentInfo { ... } - ĐÃ XÓA, sử dụng
	// Share.PaymentSummaryDTO

}