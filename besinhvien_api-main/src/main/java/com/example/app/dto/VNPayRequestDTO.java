package com.example.app.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class VNPayRequestDTO {

    @NotBlank(message = "Order information is required")
    private String orderInfo;  // Mô tả đơn hàng, ví dụ: "Thanh toán học phí kỳ 1"

    @Min(value = 1, message = "Amount must be greater than 0")
    private double amount;  // Số tiền VND (sẽ *100 khi gửi VNPAY)

    @NotNull(message = "Student ID is required")
    private Object studentId;  // ID sinh viên hoặc mã sinh viên (String/Long)

    // Constructors
    public VNPayRequestDTO() {
    }

    public VNPayRequestDTO(String orderInfo, double amount, Object studentId) {
        this.orderInfo = orderInfo;
        this.amount = amount;
        this.studentId = studentId;
    }

    // Getters and Setters
    public String getOrderInfo() {
        return orderInfo;
    }

    public void setOrderInfo(String orderInfo) {
        this.orderInfo = orderInfo;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Object getStudentId() {
        return studentId;
    }

    public void setStudentId(Object studentId) {
        this.studentId = studentId;
    }

    @Override
    public String toString() {
        return "VNPayRequestDTO{" +
                "orderInfo='" + orderInfo + '\'' +
                ", amount=" + amount +
                ", studentId=" + studentId +
                '}';
    }
}
