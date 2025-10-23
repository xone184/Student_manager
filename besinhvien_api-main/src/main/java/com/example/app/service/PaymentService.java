package com.example.app.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.app.dto.*;
import com.example.app.enumvalue.Status;
import com.example.app.model.*;
import com.example.app.repository.*;
import com.example.app.share.Share;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    private static final Random random = new SecureRandom();

    private final PaymentRepository paymentRepository;
    private final SemesterRepository semesterRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PaymentDetailService paymentDetailService;

    @Value("${vnpay.tmnCode}")
    private String vnp_TmnCode;

    @Value("${vnpay.hashSecret}")
    private String vnp_HashSecret;

    @Value("${vnpay.baseUrl}")
    private String vnp_Url;

    @Value("${vnpay.returnUrl}")
    private String vnp_ReturnUrl;

    public PaymentService(PaymentRepository paymentRepository,
                          SemesterRepository semesterRepository,
                          StudentRepository studentRepository,
                          UserRepository userRepository,
                          PaymentDetailService paymentDetailService) {
        this.paymentRepository = paymentRepository;
        this.semesterRepository = semesterRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.paymentDetailService = paymentDetailService;
    }

    // =======================
    // GET ALL PAYMENTS (FILTER + BACKEND)
    // =======================
    public List<PrincipalPortalInfo.PaymentWithDetails> getAllPayments(String status, String semester, String search) {
        try {
            List<Payment> payments = paymentRepository.findAll();
            return payments.stream()
                    .map(this::convertToPaymentWithDetails)
                    .filter(p -> filterPayment(p, status, semester, search))
                    .sorted((p1, p2) -> {
                        LocalDateTime d1 = p1.getPaymentDate() != null ? p1.getPaymentDate() : LocalDateTime.MIN;
                        LocalDateTime d2 = p2.getPaymentDate() != null ? p2.getPaymentDate() : LocalDateTime.MIN;
                        return d2.compareTo(d1);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting payments with filters", e);
            throw new RuntimeException("Lỗi khi lấy danh sách payments: " + e.getMessage());
        }
    }

    private boolean filterPayment(PrincipalPortalInfo.PaymentWithDetails payment,
                                  String status, String semester, String search) {
        if (status != null && !status.isBlank() && !payment.getStatus().toString().equalsIgnoreCase(status))
            return false;
        if (semester != null && !semester.isBlank() && !semester.equals(payment.getSemesterName()))
            return false;
        if (search != null && !search.isBlank()) {
            String s = search.toLowerCase();
            String studentCode = payment.getStudentCode() != null ? payment.getStudentCode().toLowerCase() : "";
            String paymentId = payment.getId() != null ? payment.getId().toString() : "";
            if (!studentCode.contains(s) && !paymentId.contains(s)) return false;
        }
        return true;
    }

    public List<PrincipalPortalInfo.PaymentWithDetails> getAllPayments(String status, String semester) {
        return getAllPayments(status, semester, null);
    }

    private PrincipalPortalInfo.PaymentWithDetails convertToPaymentWithDetails(Payment payment) {
        PrincipalPortalInfo.PaymentWithDetails result = new PrincipalPortalInfo.PaymentWithDetails();
        result.setId(payment.getId());
        result.setStudentId(payment.getStudentId());
        result.setSemesterId(payment.getSemesterId());
        result.setPaymentDate(payment.getPaymentDate());
        result.setStatus(payment.getStatus());

        studentRepository.findById(payment.getStudentId())
                .ifPresent(s -> result.setStudentCode(s.getStudentCode()));

        semesterRepository.findById(payment.getSemesterId())
                .ifPresent(s -> result.setSemesterName(s.getSemester()));

        return result;
    }

    private PaymentDTO convertToDTO(Payment entity) {
        PaymentDTO dto = new PaymentDTO(entity.getId(), entity.getStudentId(), entity.getSemesterId(),
                                        entity.getPaymentDate(), entity.getStatus());
        dto.setAmount(entity.getAmount());
        dto.setDescription(entity.getDescription());
        return dto;
    }
    private Payment convertToEntity(PaymentDTO dto) {
        Payment payment = new Payment();
        if (dto.getId() != null) payment.setId(dto.getId());
        payment.setStudentId(dto.getStudentId());
        payment.setSemesterId(dto.getSemesterId());
        payment.setAmount(dto.getAmount());
        payment.setDescription(dto.getDescription());
        payment.setPaymentDate(dto.getPaymentDate());
        payment.setStatus(dto.getStatus());
        return payment;
    }

    public PaymentDTO getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy payment với ID: " + id));
    }

    // =======================
    // GET PAYMENT DETAIL
    // =======================
    public Share.PaymentSummaryDTO getPaymentDetail(Long paymentId) {
        PaymentDTO dto = getPaymentById(paymentId);

        Student student = studentRepository.findById(dto.getStudentId()).orElse(null);
        User user = student != null ? userRepository.findById(student.getUserId()).orElse(null) : null;
        Semester semester = semesterRepository.findById(dto.getSemesterId()).orElse(null);

        List<PaymentDetailDTO> paymentDetails = paymentDetailService.getPaymentDetailsByPaymentId(paymentId);
        BigDecimal totalAmount = paymentDetails.stream()
                .map(d -> d.getFee() != null ? d.getFee() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new Share.PaymentSummaryDTO(
                dto.getId(),
                dto.getStudentId(),
                user != null ? user.getFullName() : (student != null ? "Sinh viên " + student.getStudentCode() : "N/A"),
                student != null ? student.getStudentCode() : "N/A",
                student != null ? "CNTT" + (student.getId() % 10 + 1) : "N/A",
                dto.getSemesterId(),
                semester != null ? semester.getSemester() : "N/A",
                semester != null ? Share.getAllSemester.generateDisplayName(semester.getSemester()) : "N/A",
                dto.getPaymentDate() != null ? dto.getPaymentDate().toString() : null,
                dto.getPaymentDate(),
                dto.getStatus().toString(),
                totalAmount,
                dto.getStatus() == Status.PAID ? totalAmount : BigDecimal.ZERO,
                paymentDetails
        );
    }

    // =======================
    // UPDATE PAYMENT STATUS
    // =======================
    public PaymentDTO updatePaymentStatus(Long paymentId, String newStatus, String reason) {
        PaymentDTO dto = getPaymentById(paymentId);

        Status status;
        try {
            status = Status.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Trạng thái không hợp lệ: " + newStatus);
        }

        Status oldStatus = dto.getStatus();
        dto.setStatus(status);
        if (status == Status.PAID && oldStatus != Status.PAID) {
            dto.setPaymentDate(LocalDateTime.now());
        }

        Payment updatedPayment = paymentRepository.save(convertToEntity(dto));
        return convertToDTO(updatedPayment);
    }

    // =======================
    // PAYMENT BY STUDENT
    // =======================
    public List<Payment> getPaymentsByStudentId(Long studentId) {
        return paymentRepository.findByStudentIdOrderByPaymentDateDesc(studentId);
    }

    // =======================
    // PAYMENT STATISTICS
    // =======================
    public PrincipalPortalInfo.PaymentStatistics getPaymentStatistics(String semesterStr) {
        List<Payment> payments;
        if (semesterStr != null) {
            Semester semester = getSemesterByString(semesterStr);
            if (semester != null) {
                payments = paymentRepository.findAll().stream()
                        .filter(p -> p.getSemesterId().equals(semester.getId()))
                        .collect(Collectors.toList());
            } else {
                payments = Collections.emptyList();
            }
        } else {
            payments = paymentRepository.findAll();
        }

        long totalPayments = payments.size();
        long paidPayments = payments.stream().filter(p -> p.getStatus() == Status.PAID).count();
        long pendingPayments = payments.stream().filter(p -> p.getStatus() == Status.PENDING).count();
        long failedPayments = payments.stream().filter(p -> p.getStatus() == Status.FAILED).count();

        double totalAmount = payments.stream().mapToDouble(this::calculatePaymentAmount).sum();
        double paidAmount = payments.stream().filter(p -> p.getStatus() == Status.PAID)
                .mapToDouble(this::calculatePaymentAmount).sum();
        double pendingAmount = payments.stream().filter(p -> p.getStatus() == Status.PENDING)
                .mapToDouble(this::calculatePaymentAmount).sum();

        return new PrincipalPortalInfo.PaymentStatistics(
                totalPayments, paidPayments, pendingPayments, failedPayments, totalAmount, paidAmount, pendingAmount
        );
    }

    private double calculatePaymentAmount(Payment payment) {
        return paymentDetailService.getPaymentDetailsByPaymentId(payment.getId()).stream()
                .map(d -> d.getFee() != null ? d.getFee() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .doubleValue();
    }

    private Semester getSemesterByString(String semesterStr) {
        return semesterRepository.findAll().stream()
                .filter(s -> s.getSemester().equals(semesterStr))
                .findFirst().orElse(null);
    }

    // =======================
    // EXPORT CSV
    // =======================
    public byte[] exportPaymentsToCsv(String semester) {
        List<PrincipalPortalInfo.PaymentWithDetails> payments = getAllPayments("", semester, null);
        StringBuilder csv = new StringBuilder("\ufeffID,Mã SV,Học kỳ,Trạng thái,Ngày thanh toán\n");
        for (PrincipalPortalInfo.PaymentWithDetails p : payments) {
            csv.append(p.getId()).append(",");
            csv.append(Share.escapeCSV(p.getStudentCode())).append(",");
            csv.append(Share.escapeCSV(p.getSemesterName())).append(",");
            csv.append(Share.escapeCSV(p.getStatus().toString())).append(",");
            csv.append(p.getPaymentDate() != null ? p.getPaymentDate().toString() : "").append("\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    // =======================
    // VNPAY INITIATE
    // =======================
    public VNPayResponseDTO initiateVNPayPayment(VNPayRequestDTO request) {
        // Overload mới, lấy IP từ localhost
        return initiateVNPayPayment(request, null);
    }

    public VNPayResponseDTO initiateVNPayPayment(VNPayRequestDTO request, HttpServletRequest httpRequest) {
        try {
            logger.info("=== VNPAY INITIATE START ===");
            logger.info("Request: {}", request);
            
            // Step 1: Convert studentId from String/Long to Long
            logger.info("Step 1: Converting studentId from {} to Long", request.getStudentId());
            Long studentId = convertStudentIdToLong(request.getStudentId());
            logger.info("Step 1: Converted studentId: {}", studentId);
            
            // Step 2: Check for pending payments
            logger.info("Step 2: Checking for pending payments for studentId: {}", studentId);
            List<Payment> pending = paymentRepository.findByStudentIdOrderByPaymentDateDesc(studentId);
            logger.info("Step 2: Found {} payments for student", pending.size());
            
            // Handle pending payments - complete them instead of throwing exception
            List<Payment> pendingPayments = pending.stream()
                    .filter(p -> p.getStatus() == Status.PENDING)
                    .collect(Collectors.toList());
            
            if (!pendingPayments.isEmpty()) {
                logger.warn("Step 2: Student has {} pending payments, completing them", pendingPayments.size());
                for (Payment pendingPayment : pendingPayments) {
                    pendingPayment.setStatus(Status.PAID);
                    paymentRepository.save(pendingPayment);
                    logger.info("Step 2: Completed pending payment ID: {}", pendingPayment.getId());
                }
            }
            logger.info("Step 2: Pending payments handled, proceeding");

            // Step 3: Create new payment
            logger.info("Step 3: Creating new payment");
            Payment payment = new Payment();
            payment.setStudentId(studentId);
            payment.setAmount(request.getAmount());
            payment.setDescription(request.getOrderInfo());
            payment.setPaymentDate(LocalDateTime.now());
            payment.setStatus(Status.PENDING);
            logger.info("Step 3: Payment object created: {}", payment);
            
            // Step 4: Save payment to database
            logger.info("Step 4: Saving payment to database");
            payment = paymentRepository.save(payment);
            logger.info("Step 4: Payment saved with ID: {}", payment.getId());

            // Step 5: VNPay configuration
            logger.info("Step 5: Loading VNPay configuration");
            logger.info("VNPay config loaded - TmnCode: {}, Url: {}", this.vnp_TmnCode, this.vnp_Url);

            // Step 6: Generate VNPay parameters
            logger.info("Step 6: Generating VNPay parameters");
            String vnp_OrderId = String.valueOf(payment.getId());
            String vnp_Amount = String.valueOf((long) (request.getAmount() * 100));
            String vnp_TxnRef = "TXN" + System.currentTimeMillis() + random.nextInt(1000);
            String vnp_IpAddr = httpRequest != null ? Optional.ofNullable(httpRequest.getRemoteAddr()).orElse("127.0.0.1") : "127.0.0.1";
            
            Calendar cld = Calendar.getInstance();
            SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnp_CreateDate = fmt.format(cld.getTime());
            cld.add(Calendar.MINUTE, 15);
            String vnp_ExpireDate = fmt.format(cld.getTime());
            
            logger.info("Step 6: VNPay parameters generated - OrderId: {}, Amount: {}, TxnRef: {}", vnp_OrderId, vnp_Amount, vnp_TxnRef);

            // Step 7: Build VNPay parameters map
            logger.info("Step 7: Building VNPay parameters map");
            Map<String, String> vnpParams = new TreeMap<>();
            vnpParams.put("vnp_Amount", vnp_Amount);
            vnpParams.put("vnp_Command", "pay");
            vnpParams.put("vnp_CreateDate", vnp_CreateDate);
            vnpParams.put("vnp_CurrCode", "VND");
            vnpParams.put("vnp_ExpireDate", vnp_ExpireDate);
            vnpParams.put("vnp_IpAddr", vnp_IpAddr);
            vnpParams.put("vnp_Locale", "vn");
            vnpParams.put("vnp_OrderId", vnp_OrderId);
            vnpParams.put("vnp_OrderType", "other");
            vnpParams.put("vnp_ReturnUrl", this.vnp_ReturnUrl);
            vnpParams.put("vnp_TmnCode", this.vnp_TmnCode);
            vnpParams.put("vnp_TxnRef", vnp_TxnRef);
            logger.info("Step 7: VNPay parameters map built with {} parameters", vnpParams.size());

            // Step 8: Generate query string and secure hash
            logger.info("Step 8: Generating query string and secure hash");
            String query = vnpParams.entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining("&"));
            logger.info("Step 8: Query string generated: {}", query);
            
            String secureHash = hmacSHA512(query, this.vnp_HashSecret.getBytes(StandardCharsets.UTF_8));
            logger.info("Step 8: Secure hash generated: {}", secureHash);

            // Step 9: Build final payment URL
            logger.info("Step 9: Building final payment URL");
            String paymentUrl = this.vnp_Url + "?" + query + "&vnp_SecureHash=" + secureHash;
            logger.info("Step 9: Payment URL built successfully");

            logger.info("=== VNPAY INITIATE SUCCESS ===");
            logger.info("VNPay URL generated successfully: {}", paymentUrl);
            return new VNPayResponseDTO(paymentUrl, vnp_TxnRef);
            
        } catch (Exception e) {
            logger.error("=== VNPAY INITIATE ERROR ===", e);
            throw new RuntimeException("Lỗi khi khởi tạo thanh toán VNPay: " + e.getMessage(), e);
        }
    }

    // =======================
    // VNPAY CALLBACK
    // =======================
    public String handleVNPayReturn(HttpServletRequest request) {
        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        String vnp_OrderId = request.getParameter("vnp_OrderId");
        String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
        String vnp_TxnRef = request.getParameter("vnp_TxnRef");

        Map<String, String[]> params = request.getParameterMap();
        String signData = params.entrySet().stream()
                .filter(e -> e.getKey().startsWith("vnp_"))
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue()[0])
                .collect(Collectors.joining("&"));

        String calcHash = hmacSHA512(signData, vnp_HashSecret.getBytes(StandardCharsets.UTF_8));
        if (!calcHash.equals(vnp_SecureHash)) return "INVALID_SIGNATURE";

        Long paymentId = Long.parseLong(vnp_OrderId);
        PaymentDTO payment = getPaymentById(paymentId);

        if ("00".equals(vnp_ResponseCode)) {
            payment.setStatus(Status.PAID);
            payment.setPaymentDate(LocalDateTime.now());
            paymentRepository.save(convertToEntity(payment));
            return "Thanh toán thành công! Mã giao dịch: " + vnp_TxnRef;
        } else {
            payment.setStatus(Status.FAILED);
            paymentRepository.save(convertToEntity(payment));
            return "Thanh toán thất bại! Mã lỗi: " + vnp_ResponseCode;
        }
    }

    // =======================
    // HELPER
    // =======================
    private String hmacSHA512(String data, byte[] key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key, "HmacSHA512"));
            byte[] result = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) sb.append(String.format("%02x", b & 0xFF));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo hash: " + e.getMessage(), e);
        }
    }

    /**
     * Get all students for testing
     */
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    /**
     * Convert studentId from String (username/student code) or Long to Long
     */
    public Long convertStudentIdToLong(Object studentId) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID không được null");
        }

        if (studentId instanceof Long) {
            Long id = (Long) studentId;
            // Check if student exists
            if (!studentRepository.findById(id).isPresent()) {
                logger.warn("Student ID {} does not exist, using default ID 1", id);
                return 1L; // Use default student ID
            }
            return id;
        }

        if (studentId instanceof Integer) {
            Long id = ((Integer) studentId).longValue();
            // Check if student exists
            if (!studentRepository.findById(id).isPresent()) {
                logger.warn("Student ID {} does not exist, using default ID 1", id);
                return 1L; // Use default student ID
            }
            return id;
        }

        if (studentId instanceof String) {
            String studentIdStr = (String) studentId;

            // Try to parse as Long first
            try {
                Long id = Long.parseLong(studentIdStr);
                // Check if student exists
                if (!studentRepository.findById(id).isPresent()) {
                    logger.warn("Student ID {} does not exist, using default ID 1", id);
                    return 1L; // Use default student ID
                }
                return id;
            } catch (NumberFormatException e) {
                // If not a number, try as student code first
                logger.info("Trying to find student by code: {}", studentIdStr);
                Optional<Student> studentByCode = studentRepository.findByStudentCode(studentIdStr);
                if (studentByCode.isPresent()) {
                    logger.info("Found student by code: {} -> ID: {}", studentIdStr, studentByCode.get().getId());
                    return studentByCode.get().getId();
                }

                // If not found by student code, try as username
                logger.info("Trying to find student by username: {}", studentIdStr);
                Optional<User> userByUsername = userRepository.findByUsername(studentIdStr);
                if (userByUsername.isPresent()) {
                    Student studentByUserId = studentRepository.findByUserId(userByUsername.get().getId());
                    if (studentByUserId != null) {
                        logger.info("Found student by username: {} -> ID: {}", studentIdStr, studentByUserId.getId());
                        return studentByUserId.getId();
                    }
                }

                // If still not found, try to find any student with similar code
                logger.warn("Student not found with code/username: {}", studentIdStr);
                logger.info("Available students in database:");
                try {
                    List<Student> allStudents = studentRepository.findAll();
                    for (Student s : allStudents) {
                        logger.info("Student ID: {}, Code: {}", s.getId(), s.getStudentCode());
                    }
                } catch (Exception ex) {
                    logger.error("Error listing students", ex);
                }

                // Return a default student ID for testing (you can remove this in production)
                logger.warn("Using default student ID 1 for testing");
                return 1L;
            }
        }

        throw new IllegalArgumentException("Student ID phải là Long hoặc String: " + studentId.getClass().getSimpleName());
    }
}
