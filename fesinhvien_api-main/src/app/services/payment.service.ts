import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

interface VNPayRequest {
  orderInfo: string;
  amount: number;
  studentId: number;
}

interface VNPayResponse {
  paymentUrl: string;
  transactionId: string;
}

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private apiUrl = 'http://localhost:8080/api/payments';

  constructor(private http: HttpClient) {}

  initiateVNPay(request: VNPayRequest): Observable<VNPayResponse> {
    return this.http.post<VNPayResponse>(`${this.apiUrl}/vnpay/initiate`, request)
      .pipe(
        catchError(this.handleError)
      );
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'Đã xảy ra lỗi không mong muốn';
    
    if (error.status === 401) {
      errorMessage = 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.';
    } else if (error.status === 403) {
      errorMessage = 'Bạn không có quyền thực hiện thao tác này. Vui lòng kiểm tra lại thông tin đăng nhập.';
    } else if (error.status === 500) {
      // Hiển thị chi tiết lỗi từ backend nếu có
      const backendError = error.error?.message || error.error?.error || error.error || error.message;
      errorMessage = `Lỗi máy chủ (500): ${backendError}`;
    } else if (error.error?.message) {
      errorMessage = error.error.message;
    } else if (error.message) {
      errorMessage = error.message;
    }
    
    return throwError(() => new Error(errorMessage));
  }

  getPaymentsByStudentId(studentId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/student/${studentId}`);
  }
}