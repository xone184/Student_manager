import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-payment-success',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './payment-success.html',
})
export class PaymentSuccessComponent implements OnInit {
  message: string = '';
  isSuccess: boolean = true;
  transactionId: string = '';
  amount: string = '';
  orderInfo: string = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      // Xử lý callback từ VNPay
      const vnpResponseCode = params['vnp_ResponseCode'];
      const vnpTransactionStatus = params['vnp_TransactionStatus'];
      
      if (vnpResponseCode === '00' && vnpTransactionStatus === '00') {
        this.isSuccess = true;
        this.message = 'Thanh toán thành công!';
        this.transactionId = params['vnp_TransactionNo'] || '';
        this.amount = params['vnp_Amount'] || '';
        this.orderInfo = params['vnp_OrderInfo'] || '';
      } else {
        this.isSuccess = false;
        this.message = 'Thanh toán thất bại hoặc bị hủy.';
        this.transactionId = params['vnp_TransactionNo'] || '';
      }
    });
  }

  goToPayment(): void {
    this.router.navigate(['/user/payment']);
  }

  goToHome(): void {
    this.router.navigate(['/user']);
  }

  formatAmount(amount: string): string {
    if (!amount) return '';
    // VNPay trả về amount dưới dạng số (ví dụ: 100000 cho 1,000,000 VND)
    const numericAmount = parseInt(amount);
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(numericAmount);
  }
}