import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService, PaymentInfo, SemesterInfo } from './user.service';
import { StudentGuardService } from './student-guard.service';
import { Router } from '@angular/router';
import { PaymentService } from '../services/payment.service';
import { AuthService } from '../auth.service';

// Định nghĩa interface cho VNPAY
interface VNPayRequest {
  orderInfo: string;
  amount: number;
  studentId: number;
}

interface VNPayResponse {
  paymentUrl: string;
  transactionId: string;
}

@Component({
    selector: 'app-user-payment',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './payment.html',
    styleUrls: ['./payment.css']
})
export class UserPaymentComponent implements OnInit {
    paymentInfo: PaymentInfo | null = null;
    allPaymentInfo: PaymentInfo[] = [];
    availableSemesters: SemesterInfo[] = [];
    selectedSemester = '';
    loading = false;
    error = '';
    showAllSemesters = false;

    constructor(
        private userService: UserService,
        private studentGuard: StudentGuardService,
        private router: Router,
        private paymentService: PaymentService,
        private authService: AuthService
    ) { }

    ngOnInit() {
        // Kiểm tra studentId khi component khởi tạo
        const studentId = this.getStudentId();
        if (!studentId) {
            // Thử load dữ liệu với fallback studentId = 1 nếu có token
            if (this.authService.isLoggedIn()) {
                this.loadSemesters();
            } else {
                this.error = 'Không thể xác định ID sinh viên. Vui lòng đăng nhập lại.';
                return;
            }
        } else {
            this.loadSemesters();
        }
    }

    loadSemesters() {
        this.userService.getAllSemesters().subscribe({
            next: (semesters) => {
                this.availableSemesters = semesters;
                if (semesters.length > 0 && !this.selectedSemester) {
                    this.selectedSemester = semesters[0].semester;
                    this.loadPaymentInfo();
                }
            },
            error: (error) => {
                if (!this.studentGuard.handleStudentError(error)) {
                    console.error('Error loading semesters:', error);
                }
            }
        });
    }

    loadPaymentInfo() {
        this.loading = true;
        this.error = '';

        this.userService.getPaymentInfo(this.selectedSemester).subscribe({
            next: (data) => {
                console.log('Payment info loaded successfully:', data);
                // Ánh xạ để đảm bảo paymentStatus tồn tại (API có thể trả 'status')
                this.paymentInfo = this.mapPaymentInfo(data);
                this.loading = false;
            },
            error: (error) => {
                if (!this.studentGuard.handleStudentError(error)) {
                    console.error('Error loading payment info:', error);
                    this.error = `Lỗi khi tải thông tin học phí: ${error.status} - ${error.statusText}`;
                }
                this.loading = false;
            }
        });
    }

    loadAllPaymentInfo() {
        this.loading = true;
        this.error = '';

        this.userService.getAllPaymentInfo().subscribe({
            next: (data) => {
                console.log('All payment info loaded successfully:', data);
                // Ánh xạ từng phần tử để chuẩn hoá tên trường
                this.allPaymentInfo = Array.isArray(data) ? data.map(d => this.mapPaymentInfo(d)) : [];
                this.loading = false;
            },
            error: (error) => {
                if (!this.studentGuard.handleStudentError(error)) {
                    console.error('Error loading all payment info:', error);
                    this.error = `Lỗi khi tải thông tin học phí: ${error.status} - ${error.statusText}`;
                }
                this.loading = false;
            }
        });
    }

    onSemesterChange() {
        if (this.selectedSemester) {
            this.loadPaymentInfo();
        }
    }

    toggleView() {
        this.showAllSemesters = !this.showAllSemesters;
        if (this.showAllSemesters) {
            this.loadAllPaymentInfo();
        } else {
            this.loadPaymentInfo();
        }
    }

    createPayment(semester?: string) {
        if (confirm('🏦 Bạn có chắc chắn muốn tạo yêu cầu thanh toán cho học kỳ này?')) {
            this.userService.createPayment(semester).subscribe({
                next: (response) => {
                    console.log('Payment created:', response);
                    alert('✅ Tạo yêu cầu thanh toán thành công!');
                    if (this.showAllSemesters) {
                        this.loadAllPaymentInfo();
                    } else {
                        this.loadPaymentInfo();
                    }
                },
                error: (error) => {
                    console.error('Error creating payment:', error);
                    alert('❌ Lỗi khi tạo yêu cầu thanh toán: ' + error.message);
                }
            });
        }
    }

    // Thêm phương thức thanh toán online bằng VNPAY
    payOnline(payment: PaymentInfo): void {
        // Chỉ kiểm tra paymentStatus
        if (payment.paymentStatus !== 'PENDING') {
            this.error = 'Chỉ có thể thanh toán online cho các khoản đang chờ!';
            return;
        }

        // Kiểm tra token trước khi thanh toán
        if (!this.authService.isLoggedIn()) {
            this.error = 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.';
            this.router.navigate(['/login']);
            return;
        }

        let studentId = this.getStudentId();
        
        // Fallback: sử dụng userId từ AuthService nếu không tìm thấy studentId
        if (!studentId && this.authService.isLoggedIn()) {
            const userId = this.authService.getCurrentUserId();
            // Chuyển đổi userId thành số nếu có thể
            if (typeof userId === 'string') {
                // Nếu userId là string như "sv005", sử dụng một ID số mặc định
                // hoặc extract số từ string
                const numericId = parseInt((userId as string).replace(/\D/g, ''), 10);
                studentId = numericId > 0 ? numericId : 1; // Fallback to 1
            } else if (typeof userId === 'number' && userId > 0) {
                studentId = userId;
            } else {
                studentId = 1; // Fallback ID
            }
        }
        
        if (!studentId || studentId <= 0) {
            this.error = 'Không thể xác định ID sinh viên hợp lệ. Vui lòng đăng nhập lại.';
            return;
        }

        const request: VNPayRequest = {
            orderInfo: `Thanh toán học phí kỳ ${payment.semester} cho sinh viên ${studentId}`,
            amount: payment.totalAmount ?? payment.remainingAmount ?? 0,
            studentId: studentId
        };

        this.loading = true;
        
        this.paymentService.initiateVNPay(request).subscribe({
            next: (response: VNPayResponse) => {
                this.loading = false;
                // Điều hướng tới URL VNPay
                window.location.href = response.paymentUrl;
            },
            error: (error) => {
                this.loading = false;
                console.error('Error initiating VNPay:', error);
                
                // Sử dụng error message từ service đã được xử lý
                this.error = error?.message || 'Lỗi khi khởi tạo thanh toán online';
            }
        });
    }

    // Sử dụng AuthService để lấy studentId một cách an toàn
    private getStudentId(): number | null {
        const userId = this.authService.getCurrentUserId();
        
        // Kiểm tra xem có token hợp lệ không
        if (!this.authService.isLoggedIn()) {
            return null;
        }
        
        if (userId && userId > 0) {
            return userId;
        }
        
        // Fallback: thử lấy từ StudentGuardService nếu có
        try {
            const guardAny = this.studentGuard as any;
            
            if (typeof guardAny.getStudentId === 'function') {
                const id = guardAny.getStudentId();
                if (typeof id === 'number' && !isNaN(id) && id > 0) {
                    return id;
                }
            }
            if (guardAny.currentStudent && typeof guardAny.currentStudent.id === 'number') {
                return guardAny.currentStudent.id;
            }
        } catch (e) {
            // Bỏ qua lỗi
        }
        
        return null;
    }

    // Hàm ánh xạ/chuẩn hoá payment object từ API về PaymentInfo có paymentStatus
    private mapPaymentInfo(raw: any): PaymentInfo {
        if (!raw) return raw;
        // copy tất cả thuộc tính, và đảm bảo có paymentStatus (ưu tiên paymentStatus, nếu không có dùng status)
        const normalized = {
            ...raw,
            paymentStatus: raw.paymentStatus ?? raw.status ?? raw.paymentState ?? null
        };
        return normalized as PaymentInfo;
    }

    formatCurrency(amount: number): string {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND'
        }).format(amount);
    }

    getStatusBadgeClass(status: string): string {
        const statusMap: { [key: string]: string } = {
            'PAID': 'bg-green-100 text-green-700',
            'PENDING': 'bg-yellow-100 text-yellow-700',
            'FAILED': 'bg-red-100 text-red-700'
        };
        return statusMap[status] || 'bg-gray-100 text-gray-700';
    }

    getStatusText(status: string): string {
        const statusTextMap: { [key: string]: string } = {
            'PAID': '✅ Đã thanh toán',
            'PENDING': '⏳ Chờ thanh toán',
            'FAILED': '❌ Thanh toán thất bại'
        };
        return statusTextMap[status] || '';
    }

    // Phương thức để clear error
    clearError(): void {
        this.error = '';
    }

    // Phương thức để thử lại load dữ liệu
    retryLoad(): void {
        this.error = '';
        this.ngOnInit();
    }

}