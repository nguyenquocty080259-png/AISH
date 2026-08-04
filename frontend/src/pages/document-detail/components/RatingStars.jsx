import { useTranslation } from "react-i18next";

// 5 nút sao để chấm điểm tài liệu + hiển thị điểm trung bình hiện tại.
export default function RatingStars({ average, onRate }) {
  const { t } = useTranslation();
  const stars = [1, 2, 3, 4, 5];
  return (
    <div className="detail-rating">
      <div className="detail-rating__stars">
        {stars.map((star) => (
          <button
            key={star}
            type="button"
            className="detail-rating__star has-custom-focus"
            onClick={() => onRate(star)}
            title={t("docDetail.ratingStarTitle", { star })}
            aria-label={t("docDetail.ratingStarTitle", { star })}
          >
            <span aria-hidden="true">★</span>
          </button>
        ))}
      </div>
      <span className="detail-rating__avg">
        {t("docDetail.ratingAverage", { value: average?.toFixed?.(1) ?? t("docDetail.ratingNone") })}
      </span>
    </div>
  );
}
