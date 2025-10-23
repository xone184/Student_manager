package com.example.app.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.app.dto.*;
import com.example.app.enumvalue.Status;
import com.example.app.model.Payment;
import com.example.app.model.Student;
import com.example.app.service.PaymentDetailService;
import com.example.app.service.PaymentService;
import com.example.app.share.Share;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "http://localhost:4200")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;
    private final PaymentDetailService paymentDetailService;

    public PaymentController(PaymentService paymentService, PaymentDetailService paymentDetailService) {
        this.paymentService = paymentService;
        this.paymentDetailService = paymentDetailService;
    }

    // ===================== PAYMENTS =====================

    @GetMapping
    public ResponseEntity<List<PrincipalPortalInfo.PaymentWithDetails>> getAllPayments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String search) {
        try {
            List<PrincipalPortalInfo.PaymentWithDetails> payments =
                    paymentService.getAllPayments(status, semester, search);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            logger.error("Error getting all payments", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDTO> getPaymentById(@PathVariable Long id) {
        try {
            PaymentDTO payment = paymentService.getPaymentById(id);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            logger.error("Error getting payment by ID: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<Share.PaymentSummaryDTO> getPaymentDetail(@PathVariable Long id) {
        try {
            Share.PaymentSummaryDTO paymentDetail = paymentService.getPaymentDetail(id);
            return ResponseEntity.ok(paymentDetail);
        } catch (Exception e) {
            logger.error("Error getting payment detail for ID: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Share.ApiResponse> updatePaymentStatus(@PathVariable Long id,
                                                                 @RequestBody PrincipalPortalInfo.PaymentStatusUpdateRequest request) {
        try {
            paymentService.updatePaymentStatus(id, request.getStatus(), request.getReason());
            return ResponseEntity.ok(Share.ResponseUtils.success("Cập nhật trạng thái thanh toán thành công"));
        } catch (Exception e) {
            logger.error("Error updating payment status for ID: {}", id, e);
            return ResponseEntity.ok(Share.ResponseUtils.error("Lỗi khi cập nhật trạng thái: " + e.getMessage()));
        }
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Payment>> getPaymentsByStudentId(@PathVariable Long studentId) {
        try {
            List<Payment> payments = paymentService.getPaymentsByStudentId(studentId);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            logger.error("Error getting payments for student ID: {}", studentId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<PrincipalPortalInfo.PaymentStatistics> getPaymentStatistics(
            @RequestParam(required = false) String semester) {
        try {
            PrincipalPortalInfo.PaymentStatistics stats = paymentService.getPaymentStatistics(semester);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error getting payment statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportPayments(@RequestParam(required = false) String semester) {
        try {
            byte[] csvData = paymentService.exportPaymentsToCsv(semester);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
            headers.setContentDispositionFormData("attachment", "danh_sach_thanh_toan.csv");

            return ResponseEntity.ok().headers(headers).body(csvData);
        } catch (Exception e) {
            logger.error("Error exporting payments", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{paymentId}/details")
    public ResponseEntity<List<PaymentDetailDTO>> getPaymentDetails(@PathVariable Long paymentId) {
        try {
            List<PaymentDetailDTO> details = paymentDetailService.getPaymentDetailsByPaymentId(paymentId);
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            logger.error("Error getting payment details for payment ID: {}", paymentId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===================== VNPAY =====================

    @PostMapping("/vnpay/test-repo")
    public ResponseEntity<?> testRepo(@RequestBody String requestBody) {
        try {
            logger.info("=== TEST REPOSITORY ===");
            
            // Test basic repository operations
            try {
                List<Student> allStudents = paymentService.getAllStudents();
                logger.info("Found {} students", allStudents.size());
                
                Map<String, Object> result = new HashMap<>();
                result.put("message", "Repository test successful");
                result.put("studentCount", allStudents.size());
                
                return ResponseEntity.ok(result);
            } catch (Exception repoError) {
                logger.error("Repository error: ", repoError);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Repository error: " + repoError.getMessage());
                errorResponse.put("type", repoError.getClass().getSimpleName());
                return ResponseEntity.internalServerError().body(errorResponse);
            }
        } catch (Exception e) {
            logger.error("=== TEST REPO ERROR ===", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("type", e.getClass().getSimpleName());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/vnpay/initiate-public")
    public ResponseEntity<?> initiateVNPayPublic(@Valid @RequestBody VNPayRequestDTO request) {
        try {
            if (request.getOrderInfo() == null || request.getOrderInfo().trim().isEmpty() || request.getAmount() < 1 || request.getStudentId() == null) {
                return ResponseEntity.badRequest().body("Invalid VNPay request data");
            }
            VNPayResponseDTO response = paymentService.initiateVNPayPayment(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error initiating VNPay public", e);
            return ResponseEntity.internalServerError().body("Internal server error when initiating VNPay: " + e.getMessage());
        }
    }

    @PostMapping("/vnpay/initiate")
    public ResponseEntity<?> initiateVNPay(@Valid @RequestBody VNPayRequestDTO request) {
        try {
            if (request.getOrderInfo() == null || request.getOrderInfo().trim().isEmpty() || request.getAmount() < 1 || request.getStudentId() == null) {
                return ResponseEntity.badRequest().body("Invalid VNPay request data");
            }
            VNPayResponseDTO response = paymentService.initiateVNPayPayment(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error initiating VNPay", e);
            return ResponseEntity.internalServerError().body("Internal server error when initiating VNPay");
        }
    }

    @GetMapping("/vnpay-return")
    public void vnpayReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String result = paymentService.handleVNPayReturn(request);
        response.sendRedirect("http://localhost:4200/payment-success?message=" +
                java.net.URLEncoder.encode(result, StandardCharsets.UTF_8));
    }

    @PostMapping("/vnpay-ipn")
    public ResponseEntity<String> vnpayIpn(HttpServletRequest request) {
        paymentService.handleVNPayReturn(request);
        return ResponseEntity.ok("<VNPAY><RESULT>00</RESULT></VNPAY>");
    }
}
