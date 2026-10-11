# Phát hành native-alert

Tài liệu này mô tả các bước dev phải làm mỗi khi sửa `native-alert` và muốn app dùng bản sửa đó.

Phiên bản của module nằm trong `package.json` và được app chốt bằng git tag, nên **sửa code xong mà không phát hành thì app vẫn dùng bản cũ**.

## Quy ước phiên bản

Số phiên bản có dạng `A.B.C` (SemVer):

| Loại thay đổi | Tăng | Ví dụ |
| :--- | :--- | :--- |
| Sửa lỗi, không đổi cách dùng | `C` | `1.1.0` lên `1.1.1` |
| Thêm tính năng, code cũ vẫn chạy | `B` | `1.1.1` lên `1.2.0` |
| Đổi API làm vỡ code cũ (đổi tên hàm, đổi tham số, bỏ tính năng) | `A` | `1.2.0` lên `2.0.0` |

Tag đặt theo mẫu `native-alert-v<phiên bản>` (ví dụ `native-alert-v1.2.0`). Repo chứa nhiều module, nên tên module trong tag cho biết tag thuộc module nào.

## Các bước phát hành

Chạy trong thư mục `native-alert/`. Thay `<phiên-bản>` bằng số mới, ví dụ `1.2.0`.

| Bước | Việc cần làm |
| :--- | :--- |
| 1 | Sửa code (`src/`, `ios/`, `android/`) và chạy thử trong ứng dụng |
| 2 | Tăng phiên bản trong `package.json` |
| 3 | Sửa `s.version` trong `ios/NativeAlert.podspec` cho **bằng** số ở bước 2 |
| 4 | Thêm một mục mới ở đầu `CHANGELOG.md` |
| 5 | Biên dịch TypeScript sang `build/` |
| 6 | Cập nhật README nếu API hoặc cách dùng đổi |
| 7 | Commit, tạo tag và push |
| 8 | Báo cho ứng dụng đổi sang tag mới |

### Bước 2: Tăng phiên bản

```bash
npm version <phiên-bản> --no-git-tag-version
```

Cờ `--no-git-tag-version` để npm chỉ sửa `package.json`, không tự tạo commit hay tag.

### Bước 5: Biên dịch

```bash
npm install
```

```bash
npm run build
```

`build/` được commit lên git và `package.json` trỏ `main` vào đó, nên **quên bước này thì app nhận bản JS cũ**. Chỉ cần `npm install` lần đầu để có `typescript`.

Kiểm tra `build/` có khớp `src/` chưa: chạy `npm run build` rồi `git status`. Nếu có thay đổi mới ở `build/` thì cần commit.

### Bước 7: Commit, tạo tag và push

```bash
git add -A
```

```bash
git commit -m "release(native-alert): <phiên-bản>"
```

```bash
git tag native-alert-v<phiên-bản>
```

```bash
git push origin <nhánh> native-alert-v<phiên-bản>
```

### Bước 8: Ứng dụng dùng bản mới

Trong `package.json` của ứng dụng, đổi tag của module:

```json
"native-alert": "git+<địa-chỉ-repo>.git#native-alert-v<phiên-bản>&path:/native-alert"
```

Rồi cài lại và build lại native nếu có đổi `ios/` hoặc `android/`:

```bash
npx pnpm@11.13.0 install
```

## Mẫu `CHANGELOG.md`

```markdown
## <phiên-bản> - <ngày>

- Added: tính năng mới
- Changed: thay đổi hành vi
- Fixed: lỗi đã sửa
```

## Bảng kiểm trước khi push tag

| Hạng mục | Tiêu chí |
| :--- | :--- |
| Phiên bản | `package.json` và `ios/NativeAlert.podspec` cùng một số |
| Biên dịch | `npm run build` chạy không lỗi, `build/` đã commit |
| Changelog | Có mục mới ở đầu `CHANGELOG.md` |
| Tài liệu | README khớp với API hiện tại |
| Thử nghiệm | Đã chạy thử trên iOS và Android (nếu sửa phần native) |
| Tag | Tên đúng mẫu `native-alert-v<phiên-bản>`, chưa từng dùng |

## Lưu ý

- **Không dùng lại tag cũ.** Nếu phát hành nhầm, tạo bản vá mới (tăng `C`) thay vì sửa hoặc xóa tag đã push, vì app khác có thể đã cài tag đó.
- **Không sửa tay `build/`.** File này được sinh từ `src/`; sửa `src/` rồi build lại.
- **Sửa mã native** (`ios/`, `android/`) bắt buộc build lại ứng dụng. Chỉ sửa TypeScript thì chỉ cần khởi động lại Metro.
