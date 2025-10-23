package com.example.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.app.enumvalue.Status;
import com.example.app.model.Payment;

import jakarta.transaction.Transactional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Tìm payment theo student ID và semester ID
     */
    Optional<Payment> findByStudentIdAndSemesterId(Long studentId, Long semesterId);

    /**
     * Lấy tất cả payments của một sinh viên, sắp xếp theo ngày thanh toán giảm dần
     */
    List<Payment> findByStudentIdOrderByPaymentDateDesc(Long studentId);

    /**
     * Lấy payments của sinh viên theo semester, sắp xếp theo ngày thanh toán giảm dần
     */
    List<Payment> findByStudentIdAndSemesterIdOrderByPaymentDateDesc(Long studentId, Long semesterId);

    /**
     * Kiểm tra xem sinh viên đã thanh toán cho semester chưa
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Payment p " +
           "WHERE p.studentId = :studentId AND p.semesterId = :semesterId AND p.status = 'PAID'")
    boolean existsPaidPaymentByStudentAndSemester(@Param("studentId") Long studentId,
                                                  @Param("semesterId") Long semesterId);

    /**
     * Lấy danh sách payments theo studentId và status
     */
    List<Payment> findByStudentIdAndStatus(Long studentId, Status status);

    /**
     * Lấy tất cả payments theo status (dành cho admin)
     */
    List<Payment> findByStatus(Status status);

    /**
     * Lấy payments theo semester ID
     */
    List<Payment> findBySemesterId(Long semesterId);

    /**
     * Lấy payments theo status và semester ID
     */
    List<Payment> findByStatusAndSemesterId(Status status, Long semesterId);

    /**
     * Set studentId = null khi sinh viên bị xóa
     */
    @Transactional
    @Modifying
    @Query("UPDATE Payment p SET p.studentId = null WHERE p.studentId = :studentId")
    void setStudentIdNullByStudentId(@Param("studentId") Long studentId);

    /**
     * Set semesterId = null khi semester bị xóa
     */
    @Transactional
    @Modifying
    @Query("UPDATE Payment p SET p.semesterId = null WHERE p.semesterId = :semesterId")
    void setSemesterIdNullBySemesterId(@Param("semesterId") Long semesterId);

}
