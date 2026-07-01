# AISH Frontend — Architecture Reference

Tài liệu này tóm tắt kiến trúc thực tế của frontend AISH (AI Study Hub) đang được code, dùng để team và các AI coding tool (Claude Code, Copilot, Cursor...) hiểu đúng quy ước trước khi sinh code mới. Khi đưa cho AI đọc, hãy đưa nguyên file này làm context.

## 1. Tech stack

Frontend: React 19, Vite, React Router v7, Axios, CSS thuần (không Tailwind, không CSS Module, không styled-components).
Backend: Spring Boot REST API, Spring Security + JWT (stateless), JPA.
Không dùng: Redux, Zustand, hay bất kỳ state management library ngoài React Context.

## 2. Nguyên tắc kiến trúc (bắt buộc tuân theo)

1. Không dùng `fetch()` — mọi request đi qua `apiClient` (axios instance duy nhất tại `src/lib/apiClient.js`).
2. Không `import axios` ở đâu khác ngoài `src/lib/apiClient.js`.
3. Component/Page không gọi API trực tiếp — phải đi qua Hook. Luồng dữ liệu cố định: `Page → Hook → api/*.js → apiClient → Backend`.
4. Logic (state, side-effect, business flow) nằm trong Hook; UI nằm trong Component; Page chỉ compose giao diện và gọi Hook tương ứng.
5. Không dùng `alert()` — dùng `useToast()` (`showSuccess`, `showError`).
6. Không hardcode URL — dùng `import.meta.env.VITE_API_URL` (đã wrap sẵn trong `apiClient.js`).
7. Không hardcode role (`"ADMIN"`) — dùng `ROLES` từ `constants/roles.js`.
8. Không hardcode key localStorage — dùng `STORAGE_KEYS` từ `constants/storageKeys.js`.
9. Không hardcode path route (`"/login"`) — dùng `ROUTES` từ `constants/routes.js`.
10. Không hardcode màu trong CSS — dùng CSS variable từ `assets/css/variables.css` (ví dụ `var(--color-primary)`). Nếu cần màu mới, thêm token vào `variables.css`, không viết hex trực tiếp trong file CSS của page/component.
11. Mỗi feature là 1 thư mục trong `pages/`, tự chứa component và hook riêng. Không tạo `components/common/`, `shared/`, `ui/` cho logic riêng của 1 feature.

## 3. Cấu trúc thư mục thực tế

```text
src/
├── api/                     # 1 file/resource, hàm thuần gọi apiClient, trả res.data
│   ├── authApi.js
│   ├── documentApi.js
│   ├── profileApi.js
│   ├── subjectApi.js
│   ├── tagApi.js
│   └── aiChatApi.js
│
├── context/
│   └── AuthContext.jsx      # Global state DUY NHẤT: user, role, accessToken, isAuthenticated, login(), logout()
│
├── hooks/                   # Hook dùng chung toàn app (không chứa hook riêng của 1 page)
│   ├── useAuth.js           # wrap AuthContext
│   ├── useToast.js          # useToast() để emit + useToastListener() để App.jsx render
│   └── useDebounce.js
│
├── routes/
│   ├── AppRoutes.jsx        # Khai báo toàn bộ <Route>
│   ├── AppLayout.jsx        # Header/nav dùng chung cho mọi route (ngoại lệ được phép, xem mục 6)
│   ├── PrivateRoute.jsx     # Chặn route cần đăng nhập
│   └── GuestRoute.jsx       # Chặn route chỉ cho khách (login/signup/otp)
│
├── constants/
│   ├── roles.js
│   ├── routes.js            # ROUTES + buildRoute(route, params) cho path động (:id)
│   └── storageKeys.js
│
├── lib/
│   └── apiClient.js         # axios instance duy nhất + interceptor gắn Bearer token + chuẩn hoá lỗi
│
├── assets/css/
│   ├── reset.css            # import 1 lần trong main.jsx
│   ├── variables.css        # design tokens — màu, spacing, radius, font, shadow
│   ├── global.css           # style mặc định body/container
│   └── toast.css            # style cho toast (.toast-stack)
│
└── pages/
    ├── auth/                # LoginPage, SignUpPage, OtpPage dùng chung auth.css
    │   ├── LoginPage.jsx / SignUpPage.jsx / OtpPage.jsx
    │   ├── auth.css
    │   └── hooks/useLoginPage.js, useSignUpPage.js, useOtpPage.js
    ├── home/HomePage.jsx + home.css
    ├── dashboard/DashboardPage.jsx + dashboard.css + hooks/useDashboardPage.js
    ├── document/
    │   ├── DocumentPage.jsx + document.css
    │   ├── components/ DocumentCard, SearchBar, FilterBar, Pagination, UploadModal
    │   └── hooks/useDocumentPage.js
    ├── document-detail/
    │   ├── DocumentDetailPage.jsx + document-detail.css
    │   ├── components/ RatingStars, CommentSection
    │   └── hooks/useDocumentDetailPage.js
    ├── profile/ProfilePage.jsx + profile.css + hooks/useProfilePage.js
    ├── ai-chat/
    │   ├── AiChatPage.jsx + ai-chat.css
    │   ├── components/ ChatMessage, ChatInput
    │   └── hooks/useAiChatPage.js
    └── error/NotFoundPage.jsx + notfound.css
```

## 4. Ngoại lệ kiến trúc đã chấp nhận

`routes/AppLayout.jsx` là 1 component có UI (header + nav + nút đăng xuất) đặt trong `routes/`, không phải trong `pages/`. Lý do: nó là phần khung điều hướng dùng cho mọi route (không riêng feature nào), nên xếp vào "Quản lý routing" thay vì coi là `shared/`. Nếu sau này có Sidebar/Footer dùng chung, nên đặt cùng vị trí này.

`useToast.js` không tự render UI (đúng rule "Hook không render UI") — nó chỉ emit/subscribe qua một pub-sub nội bộ. Phần hiển thị toast thật sự nằm trong `App.jsx` (gọi `useToastListener()`), không nằm trong hooks/.

## 5. Auth flow

`AuthContext` giữ `user`, `accessToken`, `role`, `isAuthenticated`, `loading`. Khi load app: nếu có access token trong localStorage, gọi `GET /auth/me` để khôi phục `user`; nếu lỗi thì tự xoá token.

`apiClient.js` tự gắn `Authorization: Bearer <token>` vào mọi request, và nếu response 401 thì xoá token + phát `window` event `auth:unauthorized` — `AuthContext` lắng nghe event này để tự logout, tránh phải import router vào file axios.

**Giới hạn từ backend (quan trọng, AI cần biết để không tự bịa thêm tính năng không tồn tại):**

- `POST /auth/login` trả `refreshToken` nhưng backend **không lưu** token này và **không có endpoint `/refresh`** — token này hiện vô dụng. FE chỉ dùng access token JWT (hết hạn sau 24h), không có silent-refresh.
- `GET /auth/me` chỉ trả `{ email, fullName, status }`, **không có field `role`** dù entity có. `AuthContext.role` hiện luôn là `null`. `ROLES` constant đã khai báo sẵn nhưng chưa có chỗ nào filter theo role thực sự — `PrivateRoute` chỉ check đăng nhập, không check role.
- `POST /auth/logout` không revoke token phía server (chỉ log), logout thực chất là FE tự xoá token khỏi localStorage.

## 6. Backend API contract

Base URL: `VITE_API_URL` (mặc định dev: `http://localhost:8080/api`). Tất cả response lỗi đi qua `RuntimeException` không có `GlobalExceptionHandler`, nên body lỗi là format mặc định của Spring Boot, nhưng vẫn có field `message` đáng tin để hiển thị toast — `apiClient.js` đã tự bóc field này ra.

### Auth — `/auth` (permitAll trừ `/me`, `/logout`)

| Method | Path | Request body | Response |
|---|---|---|---|
| POST | `/signup` | `{ email, password, fullName }` | text "Register success" |
| POST | `/login` | `{ email, password }` | `{ accessToken, refreshToken, tokenType }` |
| POST | `/verify-otp` | `{ email, otp }` | text |
| POST | `/resend-otp` | `{ email }` | text |
| GET | `/me` | — (cần Bearer) | `{ email, fullName, status }` |
| POST | `/logout` | — (cần Bearer) | text |

### Profile — `/profile` (cần Bearer)

| Method | Path | Request | Response |
|---|---|---|---|
| GET | `/me` | — | `ProfileResponse` |
| PUT | `/me` | `UpdateProfileRequest` | `ProfileResponse` |

`ProfileResponse`: `userId, fullName, avatarUrl, username, bio, dob (yyyy-MM-dd), gender, phoneNumber, university, faculty, major, country, city, githubUrl, linkedinUrl, websiteUrl`. `UpdateProfileRequest` giống vậy trừ `userId`/`avatarUrl` (avatar hiện **không có endpoint upload**, luôn đọc, không sửa được từ FE).

### Document — `/documents` (cần Bearer, trừ không có path permitAll riêng)

| Method | Path | Request | Response |
|---|---|---|---|
| GET | `` | — | `DocumentResponseDTO[]` (toàn bộ, không phân trang/lọc server-side) |
| GET | `/{id}` | — | `DocumentResponseDTO` |
| GET | `/trash` | — | `DocumentResponseDTO[]` (đã xoá mềm) |
| POST | `/upload` | multipart: `title, description, subjectId?, tags[]?, file` | `DocumentResponseDTO` (201) |
| POST | `/{id}/favorite` | — | 200, không body |
| POST | `/{id}/comment` | raw string (không phải JSON object) | 200 |
| POST | `/{id}/rate` | query param `?star=1..5` | 200 |
| DELETE | `/{id}` | — | 204 (xoá mềm) |
| PUT | `/{id}/restore` | — | 200 |
| PUT | `/{id}/toggle-visibility` | — | 200 |
| GET | `/{id}/download` | — | binary, header `Content-Disposition` chứa filename |

`DocumentResponseDTO`: `id, title, description, status, visibility (PUBLIC/PRIVATE/SHARED), ownerName, fileName, createdAt, favoriteCount, downloadCount, averageRating, favorited, comments[], subjectId, subjectName, tags[], fileUrl, fileType`. **Không có `ownerId`** — FE chỉ đoán quyền owner bằng so khớp `fullName` (tương đối, không đáng tin tuyệt đối), quyền thật do backend tự chặn.

Search/filter/pagination hiện đều xử lý ở client (FE tải toàn bộ list rồi `filter`/`slice`) vì backend chưa hỗ trợ query param.

### Subject / Tag

| Method | Path | Request | Response |
|---|---|---|---|
| GET | `/subjects` | — | `Subject[]` |
| POST | `/subjects` | `{ name, description? }` | `Subject` (trả lại bản đã có nếu trùng tên) |
| GET | `/tags` | — | `Tag[]` |

### AI Chat — `/ai/chat` (permitAll, guest dùng được)

| Method | Path | Request | Response |
|---|---|---|---|
| POST | `/chat` | `{ message, documentId?, conversationId? }` | `{ message, mode }` (`mode`: `RAG` \| `SYSTEM` \| `GENERAL`) |

**Giới hạn:** response không trả lại `conversationId`, nên thực chất không có hội thoại nối tiếp ở backend — mỗi lượt chat FE gửi độc lập, chỉ giữ lịch sử hiển thị ở state local của trang, mất khi rời trang.

## 7. CORS / chạy local

Backend `CorsConfig` chỉ allow origin `http://localhost:5173` — nếu Vite dev server nhảy cổng khác (do 5173 bị chiếm), phải sửa lại `CorsConfig.java` (và `@CrossOrigin` trong `DocumentController`) cho khớp, không chỉ sửa phía FE.

`.env` (copy từ `.env.example`, không commit) cần `VITE_API_URL=http://localhost:8080/api`. Sửa `.env` phải restart `npm run dev` vì Vite chỉ đọc lúc khởi động.

## 8. Design tokens (assets/css/variables.css)

Màu chủ đạo cam/vàng: `--color-primary (#E08307)`, `--color-primary-dark`, `--color-accent`. Nền/chữ: `--color-background`, `--color-surface`, `--color-surface-soft`, `--color-text`, `--color-text-secondary`, `--color-border`. Trạng thái: `--color-success/--warning/--error`. Spacing theo bậc `--spacing-xs..xl`. Bo góc `--radius-input/--radius-card/--radius-pill`. Font size theo role: `--display-lg, --headline-lg, --title-md, --body-lg, --body-sm`. Mọi CSS file mới phải dùng token này, không viết số/hex tay.

## 9. Việc còn thiếu / có thể làm tiếp

Trang quản lý Trash (`/documents/trash`, restore) chưa có UI riêng — API đã có sẵn ở `documentApi.js`. Role-based routing chưa làm được vì backend chưa trả `role` ở `/me`. Refresh token thật chưa có ở backend nên FE chưa cần (và không nên) làm silent-refresh.