# HƯỚNG DỊCH VỤ
- Giới thiệu:
- Dự án xây dựng 1 trang web quản lý sinh viên với 4 role cơ bản:
- + Hiệu trưởng(Admin) Người có quyền cao nhất trong hệ thống, trực tiếp quyết định các hoạt động vận hành trong hệ thống.
  + Giảng viên(Lectures) Có quyền xem thông tin sinh viên, xét lịch(Thời khóa biểu), Chấm điểm quá trình học, báo  cáo kết quả học tập.
  + Sinh viên(Students) Có quyền xem lịch học,thi, đăng ký học, thanh toán học phí, thực hiện các yêu cầu cơ bản.
  + Sinh viên có tài khoản chưa được xác nhận: có quyền xem các thông tin cơ bản của role sinh viên, nhưng không có quyền thực thi.
- Chức năng mở rộng:
- + Thanh toán học phí online: Sử dụng API của VNPay: thực hiện thanh toán học phí
  + ChatBotAI: Sử dụng API của Gemini, hỗ trợ người dùng giải đáp thắc mắc
  + Chatrealtime: Sử dụng API của Vchat, hỗ trợ sinh viên nhắn trực tiếp, giải đáp thắc mắc về hệ thống với Admin    
