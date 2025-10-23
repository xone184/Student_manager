package com.example.app.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.app.dto.PaymentDetailDTO;
import com.example.app.model.Course;
import com.example.app.model.Enrollment;
import com.example.app.model.PaymentDetail;
import com.example.app.repository.CourseRepository;
import com.example.app.repository.EnrollmentRepository;
import com.example.app.repository.PaymentDetailRepository;

/**
 * Service xử lý payment details - đơn giản, lưu ID và join để lấy dữ liệu đầy đủ
 */
@Service
public class PaymentDetailService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentDetailService.class);

    private final PaymentDetailRepository paymentDetailRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;

    public PaymentDetailService(PaymentDetailRepository paymentDetailRepository,
                                EnrollmentRepository enrollmentRepository,
                                CourseRepository courseRepository) {
        this.paymentDetailRepository = paymentDetailRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
    }

    /**
     * Tạo payment details cho một payment (giống generateScheduleForStudent)
     */
    @Transactional
    public void createPaymentDetails(Long paymentId, List<Long> enrollmentIds, String semester) {
        logger.info("Creating payment details for payment ID: {} with {} enrollments, semester: {}",
                    paymentId, enrollmentIds.size(), semester);

        // Xóa payment details cũ (nếu có)
        paymentDetailRepository.deleteByPaymentIdAndSemester(paymentId, semester);
        logger.info("Deleted existing payment details for payment ID: {}", paymentId);

        // Tạo payment detail cho từng enrollment
        for (Long enrollmentId : enrollmentIds) {
            try {
                PaymentDetail detail = new PaymentDetail(paymentId, enrollmentId, semester);
                PaymentDetail saved = paymentDetailRepository.save(detail);
                logger.info("Created payment detail: payment={}, enrollment={}, semester={}, detail_id={}",
                            paymentId, enrollmentId, semester, saved.getId());
            } catch (Exception e) {
                logger.error("Error creating payment detail for enrollment ID: {}", enrollmentId, e);
                throw e;
            }
        }

        logger.info("Successfully created {} payment details for payment ID: {}", enrollmentIds.size(), paymentId);
    }

    /**
     * Lấy payment details theo payment ID với thông tin đầy đủ (join enrollment + course)
     */
    public List<PaymentDetailDTO> getPaymentDetailsByPaymentId(Long paymentId) {
        logger.info("Getting payment details for payment ID: {}", paymentId);

        List<PaymentDetail> details = paymentDetailRepository.findByPaymentId(paymentId);
        List<PaymentDetailDTO> result = new ArrayList<>();

        for (PaymentDetail detail : details) {
            Enrollment enrollment = enrollmentRepository.findById(detail.getEnrollmentId()).orElse(null);
            if (enrollment == null) {
                logger.warn("Enrollment not found for ID: {}", detail.getEnrollmentId());
                continue;
            }

            Course course = courseRepository.findById(enrollment.getCourseId()).orElse(null);
            if (course == null) {
                logger.warn("Course not found for ID: {}", enrollment.getCourseId());
                continue;
            }

            PaymentDetailDTO dto = new PaymentDetailDTO(
                    detail.getId(),
                    detail.getPaymentId(),
                    detail.getEnrollmentId(),
                    detail.getSemester(),
                    course.getId(),
                    course.getCourseCode(),
                    course.getName(),
                    course.getCredit(),
                    course.getFee() != null ? course.getFee() : BigDecimal.valueOf(1000000)
            );

            result.add(dto);
        }

        logger.info("Returning {} payment detail DTOs", result.size());
        return result;
    }
}
