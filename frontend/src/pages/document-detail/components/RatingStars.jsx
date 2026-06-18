export default function RatingStars({ average, onRate }) {
  const stars = [1, 2, 3, 4, 5];
  return (
    <div className="detail-rating">
      <div className="detail-rating__stars">
        {stars.map((star) => (
          <button
            key={star}
            type="button"
            className="detail-rating__star"
            onClick={() => onRate(star)}
            title={`Đánh giá ${star} sao`}
          >
            ★
          </button>
        ))}
      </div>
      <span className="detail-rating__avg">
        Trung bình: {average?.toFixed?.(1) ?? "Chưa có đánh giá"}
      </span>
    </div>
  );
}
