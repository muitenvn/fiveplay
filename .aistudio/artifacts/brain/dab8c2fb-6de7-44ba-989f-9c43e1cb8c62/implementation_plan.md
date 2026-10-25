# Kế hoạch cải tổ toàn diện Giao diện In-Game (In-Game UI/UX Overhaul)

Tài liệu này chi tiết hóa phương án khắc phục triệt để các vấn đề giao diện khi vào game gây cản trở trải nghiệm người chơi: **nút bấm che kín màn hình, vị trí khó bấm, thanh công cụ trên cùng gây rối mắt, và màn hình loading che tầm nhìn**.

Theo yêu cầu trực tiếp của bạn (*"4 cái làm hết luôn"* và *"4 cái làm hết"*), ứng dụng sẽ tích hợp đầy đủ **4 chế độ bố cục nút bấm** cùng **4 phong cách thanh công cụ trên cùng**, cho phép chuyển đổi tức thì chỉ với 1 chạm trực tiếp trong lúc chơi game.

---

## 1. Phân tích hiện trạng & Vấn đề cần giải quyết

1. **Nút bấm quá dày đặc và choán diện tích:**
   - Phiên bản hiện tại đặt tất cả các nút ảo (Move, Jump, Sprint, Aim, Attack, Vehicle, Phone, Chat, v.v.) cố định trên màn hình với kích thước lớn và nền mờ đậm, khiến hơn 40% diện tích màn hình bị che khuất.
2. **Vị trí bấm chưa công thái học (Ergonomics):**
   - Các nút hành động bên phải xếp rời rạc, ngón cái phải với xa hoặc dễ chạm nhầm giữa nút Nhảy và Tấn công/Bắn.
3. **Thanh trạng thái trên cùng (Top Bar) cồng kềnh:**
   - Dải `SystematicTopPillBar` hiện tại chiếm toàn bộ khu vực phía trên, hiển thị cố định FPS, URL, Ping, nút bàn phím, tải lại... gây phân tâm và mất tính nhập vai (immersion).
4. **Màn hình Loading che khuất tương tác:**
   - Khung thẻ loading lớn chặn giữa màn hình khi tài nguyên đang stream, khiến người chơi không thao tác được ngay cả khi webview đã render xong nền tảng game.

---

## 2. Chi tiết 4 Chế độ Bố cục Phím bấm (Control Layouts)

Người chơi có thể chuyển đổi qua lại giữa 4 kiểu điều khiển bất kỳ lúc nào:

### Chế độ 1: Minimalist Transparent HUD (Tối giản trong suốt)
- **Đặc điểm:** Chỉ giữ lại các phím điều hướng cốt lõi (Joystick mờ, Action button thu nhỏ).
- **Độ trong suốt:** 80% trong suốt khi không chạm, sáng nhẹ 40% khi được giữ.
- **Mục tiêu:** Tối đa hóa tầm nhìn cảnh game, không vướng tầm mắt, cực kỳ thanh thoát.

### Chế độ 2: Floating Stick & Compact Combat Arc (Cần xoay linh hoạt & Cung công thái học)
- **Đặc điểm:** 
  - Joystick bên trái là dạng **Dynamic Floating** (chạm vào bất cứ điểm nào ở nửa trái màn hình để xuất hiện tâm điều khiển, không bị cố định một góc).
  - Cụm nút hành động bên phải xếp thành **hình vòng cung (Radial Arc)** ôm theo quỹ đạo tự nhiên của ngón tay cái bên phải (Bắn/Đánh ở vị trí chính giữa, Nhảy/Chạy nhanh ở 2 cánh vòng cung).
- **Mục tiêu:** Thao tác chiến đấu nhanh, ngón tay cử động tự nhiên không mỏi.

### Chế độ 3: Ultra-Clean Gesture Mode (Chế độ Cử chỉ Cực sạch)
- **Đặc điểm:** 
  - Toàn bộ màn hình gần như **100% không nút**.
  - Sử dụng vùng cảm ứng thông minh:
    - Nửa trái: vuốt để di chuyển (kéo dài để chạy nhanh).
    - Nửa phải: vuốt xoay camera, chạm đúp (double-tap) để tấn công/bắn, vuốt lên để nhảy.
  - Các nút phụ (Phương tiện, Điện thoại, Túi đồ) ẩn trong một thanh trượt mini ẩn cạnh mép.
- **Mục tiêu:** Đưa tầm nhìn về chuẩn rạp chiếu phim (cinematic gameplay), loại bỏ hoàn toàn cảm giác "bị ngập trong nút".

### Chế độ 4: Classic Console Gamepad (Tay cầm Cổ điển Công thái học)
- **Đặc điểm:** 
  - Mô phỏng trực quan bố cục tay cầm console (PlayStation / Xbox): D-pad rõ nét hoặc Analog Stick có haptic feedback nhẹ, cụm 4 phím chữ/biểu tượng (A, B, X, Y) với kích thước và khoảng cách chuẩn hóa.
  - Thêm 2 nút vai (L1/R1 - Aim/Fire) sát góc trên bên phải để bấm bằng ngón trỏ hoặc rìa ngón cái.
- **Mục tiêu:** Thân thuộc với game thủ quen chơi tay cầm, cảm giác bấm chắc tay và độ chính xác cao.

---

## 3. Chi tiết 4 Phong cách Thanh Công cụ Trên cùng (Top HUD Styles)

### Phong cách 1: Floating FAB Quick Drawer (Nút nổi thông minh tự ẩn)
- Mặc định thu gọn thành một nút tròn nhỏ trong suốt (`Floating Action Button`) sát góc trên.
- Sau 3 giây không chạm, nút tự động mờ đi (alpha = 0.25).
- Chạm vào nút sẽ mở ra khay phím nhanh (FPS, Bàn phím ảo, Tải lại, Chế độ xem, Đổi layout).

### Phong cách 2: Slim Pinned Pill Bar (Dải thuốc con nhộng siêu mỏng)
- Thanh ngang siêu mỏng (chiều cao chỉ 32dp), làm bằng kính mờ viền tinh xảo (glassmorphism), ép sát mép trên cùng.
- Chỉ hiển thị icon tối giản, không chiếm diện tích dọc của game.

### Phong cách 3: Slide-out Side Panel (Ngăn kéo trượt từ cạnh viền)
- Trên đỉnh hoàn toàn sạch bóng.
- Thay vào đó là một tay nắm nhỏ (pill handle) ẩn sát mép phải hoặc trái màn hình.
- Vuốt nhẹ từ mép để mở bảng điều khiển công cụ dọc, buông tay sẽ tự đóng lại.

### Phong cách 4: Immersive Hidden (Ẩn tuyệt đối toàn màn hình)
- Toàn bộ giao diện hệ thống và thanh công cụ bị ẩn 100%.
- Game chạy full màn hình không tì vết.
- Chỉ xuất hiện khi người chơi **vuốt từ mép trên xuống** (edge-swipe) hoặc bấm phím Back của Android, hiển thị thanh mini-bar trong 4 giây rồi tự động biến mất.

---

## 4. Tinh gọn Màn hình Loading (Non-intrusive Loading)

- **Xóa bỏ:** Không dùng hộp thoại (Dialog/Card) to đùng nằm giữa màn hình chặn người dùng.
- **Thay thế bằng:**
  - Dải tiến trình viền LED siêu mảnh (2dp) chạy sát đỉnh hoặc đáy màn hình.
  - Huy hiệu trạng thái nhỏ gọn góc màn hình hiển thị `% tải tài nguyên`, tự động biến mất khi tải xong hoặc người chơi có thể vuốt gạt bỏ bất cứ lúc nào.
  - Người chơi có thể quan sát hoặc thao tác với game ngay khi trang bắt đầu dựng hình mà không bị che khuất.

---

## 5. Bộ chuyển đổi nhanh trực tiếp trong game (In-Game Quick Switcher)

- Thêm nút chuyển đổi nhanh **"Layout Switcher"** vào menu công cụ:
  - Cho phép người chơi thử ngay từng layout (1 -> 2 -> 3 -> 4) với hiệu ứng chuyển đổi mượt mà.
  - Cho phép thử từng kiểu Top Bar (1 -> 2 -> 3 -> 4).
  - Tự động lưu lựa chọn vào `DataStore / FivePlayPreferences` để giữ nguyên cấu hình cho những lần chơi tiếp theo.

---

## 6. Kế hoạch triển khai kỹ thuật theo từng bước

1. **Bước 1: Mở rộng Model cài đặt (`FivePlayPreferences.kt`)**
   - Bổ sung `enum class InGameControlStyle { MINIMAL_TRANSPARENT, FLOATING_ARC, GESTURE_IMMERSIVE, CLASSIC_GAMEPAD }`.
   - Bổ sung `enum class TopHudStyle { FLOATING_FAB_DRAWER, SLIM_PINNED_PILL, SLIDE_SIDE_PANEL, IMMERSIVE_HIDDEN }`.
   - Thêm các trường lưu trữ vào `FivePlayPreferences`.

2. **Bước 2: Cập nhật ViewModel (`FivePlayViewModel.kt`)**
   - Bổ sung StateFlow quản lý chế độ điều khiển hiện tại và phong cách Top HUD.
   - Thêm hàm chuyển đổi nhanh `cycleControlStyle()` và `setTopHudStyle()`.

3. **Bước 3: Tái cấu trúc `VirtualControlsOverlay.kt`**
   - Tách giao diện nút bấm thành 4 bộ renderer tương ứng với 4 layout.
   - Thêm Gesture Detector cho chế độ `GESTURE_IMMERSIVE`.
   - Tối ưu hóa kích thước nút, padding, góc bo và độ trong suốt theo chuẩn Material 3.
   - Tích hợp 4 kiểu Top HUD và hộp thoại Quick Switcher.

4. **Bước 4: Cải tiến `GameWebViewScreen.kt`**
   - Thay thế loading card cồng kềnh bằng slim ambient progress bar.
   - Gắn kết với các sự kiện hiển thị HUD / ẩn thanh công cụ.

5. **Bước 5: Kiểm thử biên dịch (`compile_applet`)**
   - Đảm bảo biên dịch Gradle thành công không có lỗi cú pháp hay thiếu import.
