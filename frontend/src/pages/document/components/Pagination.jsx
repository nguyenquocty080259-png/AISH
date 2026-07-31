import { useTranslation } from "react-i18next";
import UiPagination from "../../../components/ui/Pagination";

// Giữ NGUYÊN props: page (đánh số từ 1), totalPages, onPageChange.
// Chỉ bọc Pagination dùng chung lại để nhãn Trước/Sau vẫn lấy từ i18n của trang tài liệu.
export default function Pagination({ page, totalPages, onPageChange }) {
  const { t } = useTranslation();
  return (
    <UiPagination
      className="doc-pagination"
      page={page}
      totalPages={totalPages}
      onPageChange={onPageChange}
      prevLabel={t("documents.prev")}
      nextLabel={t("documents.next")}
      ariaLabel={t("documents.title")}
    />
  );
}
