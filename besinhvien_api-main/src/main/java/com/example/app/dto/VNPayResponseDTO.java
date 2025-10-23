package com.example.app.dto;

public class VNPayResponseDTO {
    private String paymentUrl;
    private String transactionId;  // Tạo tạm trước khi gửi VNPAY
    
    // Constructor, Getters/Setters
    public VNPayResponseDTO(String paymentUrl, String transactionId) {
        this.paymentUrl = paymentUrl;
        this.transactionId = transactionId;
    }
    
    public String getPaymentUrl() { return paymentUrl; }
    public void setPaymentUrl(String paymentUrl) { this.paymentUrl = paymentUrl; }
    
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
}