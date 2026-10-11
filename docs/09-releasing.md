# Phát hành và quản lý phiên bản

[← Mục lục tài liệu](README.md)

Tài liệu mô tả cách đánh số phiên bản và các bước dev làm mỗi khi sửa một module. Bản chi tiết từng module nằm trong `RELEASING.md` ở thư mục module đó.

## Nguyên tắc

- Repo chứa nhiều module và mỗi module có số phiên bản riêng, ghi trong `package.json` của module.
- Sửa code xong mà không phát hành thì ứng dụng vẫn dùng bản cũ, vì ứng dụng chốt module bằng mã commit (trong `pnpm-lock.yaml`) hoặc bằng tag.
- Thư mục `build/` được commit kèm, nên **mọi lần sửa `src/` đều phải chạy `npm run build` trước khi commit**.

## Quy ước số phiên bản

Số phiên bản có dạng `A.B.C` (SemVer):

| Loại thay đổi | Tăng | Ví dụ |
| :--- | :--- | :--- |
| Sửa lỗi, không đổi cách dùng | `C` | `1.1.0` lên `1.1.1` |
| Thêm tính năng, code cũ vẫn chạy | `B` | `1.1.1` lên `1.2.0` |
| Đổi API làm vỡ code cũ | `A` | `1.2.0` lên `2.0.0` |

## Quy trình phát hành một module

Chạy trong thư mục module, ví dụ `native-toast/`.

| Bước | Việc cần làm |
| :--- | :--- |
| 1 | Sửa code (`src/`, `ios/`, `android/`) và chạy thử trong ứng dụng |
| 2 | Tăng phiên bản: `npm version <phiên-bản> --no-git-tag-version` |
| 3 | Sửa `s.version` trong `ios/<TênPod>.podspec` cho bằng số ở bước 2 |
| 4 | Thêm một mục ở đầu `CHANGELOG.md` |
| 5 | Biên dịch: `npm install` (lần đầu) rồi `npm run build` |
| 6 | Cập nhật `README.md` nếu API hoặc cách dùng đổi |
| 7 | Commit và push |
| 8 | Ứng dụng cập nhật module (xem bên dưới) |

Nếu muốn ghim phiên bản bằng tag, đặt tên theo mẫu `<module>-v<phiên bản>` (ví dụ `native-toast-v1.2.0`) và gắn lên commit đã merge vào nhánh chính. Dự án hiện chưa ghim bằng tag, ứng dụng dựa vào lockfile.

## Ứng dụng cập nhật module

| Cách | Lệnh | Kết quả |
| :--- | :--- | :--- |
| Lấy bản mới nhất của nhánh | `pnpm update <tên-module>` | Đổi mã commit trong `pnpm-lock.yaml` |
| Ghim vào tag (nếu có) | Sửa dòng module trong `package.json` thêm `#<tag>&` trước `path:`, rồi `pnpm install` | `pnpm-lock.yaml` tự cập nhật |

Sau khi cập nhật, commit `package.json` và `pnpm-lock.yaml` cùng nhau, rồi build lại native nếu có đổi `ios/` hoặc `android/`.

## Bảng kiểm trước khi push

| Hạng mục | Tiêu chí |
| :--- | :--- |
| Phiên bản | `package.json` và `ios/<TênPod>.podspec` cùng một số |
| Biên dịch | `npm run build` không lỗi, `build/` khớp với `src/` và đã commit |
| Changelog | Có mục mới ở đầu `CHANGELOG.md` |
| Tài liệu | README khớp với API hiện tại |
| Thử nghiệm | Đã chạy thử iOS và Android nếu sửa phần native |
| File thừa | Không commit `package-lock.json` hay `node_modules` |

## Lưu ý

- Không sửa tay `build/`: file này sinh ra từ `src/`.
- Không dùng lại tag đã push. Phát hành nhầm thì tạo bản vá mới.
- Dự án dùng pnpm, không commit `package-lock.json` của npm.
