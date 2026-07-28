# Module Tài liệu (Document)

Ghi chú kỹ thuật cho module quản lý tài liệu: các endpoint chính, **luật phân quyền truy cập
tài liệu**, chế độ lưu trữ và các chốt chặn khi upload. Phần đăng nhập/JWT thuộc module
Authentication, không mô tả ở đây.

## Thành phần chính

| Lớp | Vai trò |
|-----|---------|
| `controller/doc/DocumentController` | REST `/api/documents/**`: upload, đọc chi tiết, tải, xem trước, thumbnail, chia sẻ, đánh giá, bình luận |
| `service/doc/impl/DocumentServiceImpl` | Nghiệp vụ tài liệu + **chốt quyền truy cập** cho chi tiết/preview/download |
| `service/doc/impl/DocumentMapper` | `DocDocument` -> `DocumentResponseDTO` (đếm yêu thích/tải/điểm, danh sách bình luận theo quyền xem) |
| `service/doc/impl/EngagementServiceImpl` | Yêu thích, bình luận, đánh giá (1-5 sao), ghi nhận lượt xem/lượt tải |
| `service/doc/impl/DocumentShareServiceImpl` | Chia sẻ tài liệu và trả lời câu hỏi "user này có được xem không" |
| `service/doc/impl/DocumentAccessPortImpl` | Cổng cho module khác (AI chat, collection) hỏi quyền xem tài liệu |

## Trạng thái hiển thị

`DocumentVisibility`: `PRIVATE` (mặc định khi upload) · `SHARED` (chỉ người được chia sẻ) ·
`PUBLIC` (mọi người đã đăng nhập).

Chuyển sang `PUBLIC` không phải thao tác tự do (`toggleVisibility`):

1. Đối chiếu từ khoá cấm trong DB (`DocumentContentKeywordService`) — trúng thì **từ chối luôn**,
   không gọi AI, tài liệu về `PRIVATE` + `REJECTED`.
2. Không trúng thì mới qua AI pre-screen (`AiModerationService.screen`). `PASS` -> `PUBLIC` +
   `APPROVED`; `FLAG` -> giữ `PRIVATE` + `REJECTED` (người dùng có thể kháng cáo thủ công).

Chiều ngược lại (`PUBLIC` -> `PRIVATE`) không cần kiểm duyệt.

## Luồng phân quyền tài liệu

Ba lối đọc nội dung — chi tiết, xem trước, tải xuống — dùng **cùng một luật**:

> Cho phép khi: **chủ sở hữu** · hoặc tài liệu **PUBLIC** · hoặc người dùng **đang được chia sẻ**
> (`DocumentShareService.hasShareAccess`: được mời trực tiếp `RESTRICTED`, hoặc tài liệu đang bật
> link `ANYONE_WITH_LINK`). Ngoài ra -> `ForbiddenException` (403).

Tài liệu đã xoá mềm (`deletedAt != null`) coi như **không tồn tại với mọi người**, kể cả chủ sở
hữu, và kiểm tra này chạy **trước** kiểm tra quyền để 403 không lộ ra rằng id đó từng tồn tại.

| Endpoint | Quyền | Ghi chú |
|----------|-------|---------|
| `GET /api/documents/{id}` | owner / PUBLIC / shared | Đã xoá mềm -> 404. Chỉ ghi nhận lượt xem sau khi qua kiểm tra quyền |
| `GET /api/documents/{id}/preview` | owner / PUBLIC / shared | Sai quyền -> 403 |
| `GET /api/documents/{id}/download` | owner / PUBLIC / shared | Chỉ cộng lượt tải khi thật sự trả file về |
| `GET /api/documents/{id}/thumbnail` | như `/preview` | Không có thumbnail -> 204 để FE fallback |
| `GET /api/documents/{id}/preview-text` | như `/preview` | Không trích được text -> 204 |
| `PUT /api/documents/{id}`, `DELETE`, `/toggle-visibility`, `/share` | **chỉ chủ sở hữu** | Người được chia sẻ chỉ đọc, không sửa/xoá/đổi visibility |
| `/api/admin/documents/**` | ADMIN | Admin gỡ/sửa/khôi phục nhưng **không** trở thành owner (DEC-009) |

Tài liệu chưa gắn file (upload hỏng giữa chừng) -> `404 "Tài liệu chưa có file!"` ở cả download
lẫn preview, không phải 500.

## Lưu trữ file

`storage` = `LOCAL` | `CLOUD` | `BOTH`. Chế độ `BOTH` lưu **cả hai bản**; mọi nơi đọc file
(preview, download, ingest AI) đều **ưu tiên bản local** — đọc nhanh hơn và không dính hạn chế
deliver PDF của Cloudinary trên tài khoản free.

Chốt chặn khi upload, theo thứ tự:

1. **Tuổi tối thiểu** (`MIN_UPLOAD_AGE`, mặc định 16; ADMIN được miễn).
2. **Loại tệp**: allowlist đuôi + đối chiếu content-type thật (chống đổi đuôi), đọc tươi từ DB
   nên admin đổi là có hiệu lực ngay.
3. **Dung lượng một tệp** (`MAX_FILE_LOCAL_BYTES` / `MAX_FILE_CLOUD_BYTES`).
4. **Quota tổng của người dùng** (`QUOTA_LOCAL_BYTES` / `QUOTA_CLOUD_BYTES`). Dung lượng đã dùng
   **tính cả tài liệu trong thùng rác** — bytes vẫn chiếm chỗ tới khi xoá vĩnh viễn.
5. Mỗi tài liệu phải thuộc **ít nhất 1 môn học** hợp lệ (DEC-030), kiểm tra trước khi tạo
   document/lưu file để không sinh bản ghi mồ côi.

Với `BOTH`, giới hạn dung lượng và quota phải qua **cả hai phía**, kiểm tra độc lập.

## Tương tác

- **Đánh giá**: 1-5 sao, chốt khoảng ngay ở service — điểm ngoài khoảng bị từ chối (400) vì nó
  kéo lệch `averageRating` lẫn bộ lọc "điểm tối thiểu" ở trang Cộng đồng.
- **Bình luận**: lọc từ khoá độc trước khi lưu; trúng từ khoá -> `PENDING_REVIEW` và chỉ tác giả
  hoặc admin nhìn thấy, kèm đường kháng cáo.
- **Lượt xem / lượt tải**: chỉ ghi cho request thành công, không ghi cho 403/404.

## Test

Chạy `./mvnw test` trong `backend/`. Các test bám sát module này:

- `service/doc/impl/DocumentServiceImplGetDocumentByIdTest` — phân quyền đọc chi tiết + xoá mềm
- `service/doc/impl/DocumentServiceImplFileAccessTest` — phân quyền tải/xem trước, ưu tiên bản local
- `service/doc/impl/DocumentServiceImplUploadSizeLimitTest` / `...UploadQuotaTest` — giới hạn upload
- `service/doc/impl/EngagementServiceImplRatingTest` — thang điểm 1-5
- `controller/doc/DocumentControllerGetOneTest` / `...DownloadTest` / `...PreviewTextTest` — hành vi HTTP
