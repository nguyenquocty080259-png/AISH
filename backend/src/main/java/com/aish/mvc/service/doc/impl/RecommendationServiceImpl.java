package com.aish.mvc.service.doc.impl;

import com.aish.mvc.dto.doc.RecommendedDocumentDTO;
import com.aish.mvc.entity.doc.Collection;
import com.aish.mvc.entity.doc.CollectionItem;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.doc.Subject;
import com.aish.mvc.entity.enums.DocumentVisibility;
import com.aish.mvc.entity.enums.ModerationStatus;
import com.aish.mvc.exception.ResourceNotFoundException;
import com.aish.mvc.repository.doc.CollectionItemRepository;
import com.aish.mvc.repository.doc.CollectionRepository;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import com.aish.mvc.repository.doc.DownloadRepository;
import com.aish.mvc.repository.doc.FavoriteRepository;
import com.aish.mvc.repository.doc.RatingRepository;
import com.aish.mvc.repository.doc.ViewHistoryRepository;
import com.aish.mvc.service.config.SystemSettingService;
import com.aish.mvc.service.doc.DocumentAccessPort;
import com.aish.mvc.service.doc.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * MVP recommendation scoring (DEC-041/DEC-019, file 13 §12): heuristic dựa trên
 * subject overlap + favorite/download/rating + view history + collection.
 * Không dùng AI/embedding.
 *
 * Content/deep-user-similarity signals are explicitly Full/later scope.
 */
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    // Trọng số: subject overlap áp đảo (100 điểm/subject chung) để luôn xếp trên trending
    // thuần; favorite/download/rating chỉ phá vỡ hòa hoặc làm tín hiệu fallback khi không
    // có subject nào trùng (đảm bảo danh sách không bao giờ trống nếu có ứng viên).
    // Giá trị fallback nếu SystemSetting chưa có key/lỗi đọc — giữ nguyên giá trị hard-code cũ.
    private static final double SUBJECT_OVERLAP_WEIGHT = 100.0;
    private static final double FAVORITE_WEIGHT = 3.0;
    private static final double DOWNLOAD_WEIGHT = 1.0;
    private static final double RATING_WEIGHT = 10.0;

    private final DocDocumentRepository docDocumentRepository;
    private final FavoriteRepository favoriteRepository;
    private final DownloadRepository downloadRepository;
    private final RatingRepository ratingRepository;
    private final ViewHistoryRepository viewHistoryRepository;
    private final CollectionRepository collectionRepository;
    private final CollectionItemRepository collectionItemRepository;
    private final DocumentAccessPort documentAccessPort;
    private final SystemSettingService systemSettingService;

    // Gộp 4 trọng số đọc 1 lần/lượt (KHÔNG đọc lại per-candidate trong rank()/toScored()).
    private record ScoringWeights(double subjectOverlap, double favorite, double download, double rating) {}

    private ScoringWeights loadWeights() {
        return new ScoringWeights(
                systemSettingService.getDouble(SystemSettingService.RECO_SUBJECT_OVERLAP_WEIGHT_KEY, SUBJECT_OVERLAP_WEIGHT),
                systemSettingService.getDouble(SystemSettingService.RECO_FAVORITE_WEIGHT_KEY, FAVORITE_WEIGHT),
                systemSettingService.getDouble(SystemSettingService.RECO_DOWNLOAD_WEIGHT_KEY, DOWNLOAD_WEIGHT),
                systemSettingService.getDouble(SystemSettingService.RECO_RATING_WEIGHT_KEY, RATING_WEIGHT));
    }

    /**
     * GỢI Ý "TÀI LIỆU LIÊN QUAN" hiện ở trang chi tiết tài liệu.
     *
     * <p>Đầu vào: id tài liệu đang xem, id user, số lượng muốn lấy. Trả về: danh sách tài liệu
     * công khai khác, xếp theo điểm từ cao xuống thấp.
     *
     * <p>Cách tính: lấy các môn học của tài liệu đang xem làm "sở thích", rồi chấm điểm mọi tài
     * liệu công khai khác — trùng môn học được rất nhiều điểm, còn lượt thích/lượt tải/điểm sao
     * chỉ dùng để phân định khi bằng điểm. Loại chính tài liệu đang xem ra khỏi kết quả.
     */
    @Override
    @Transactional(readOnly = true)
    public List<RecommendedDocumentDTO> recommendRelatedToDocument(Long documentId, Long currentUserId, int limit) {
        DocDocument seed = docDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tài liệu không tồn tại!"));

        Set<Long> excludeIds = new HashSet<>();
        excludeIds.add(documentId);

        return rank(subjectIdsOf(seed), excludeIds, currentUserId, limit, false, loadWeights());
    }

    /**
     * GỢI Ý "DÀNH CHO BẠN" — cá nhân hoá theo thói quen của từng người.
     *
     * <p>Đầu vào: id user + số lượng muốn lấy. Trả về: danh sách tài liệu gợi ý.
     *
     * <p>"Sở thích" được suy ra từ các môn học của: tài liệu user đã đăng, đã yêu thích, đã xem
     * gần đây và đã bỏ vào bộ sưu tập. User hoàn toàn mới (chưa có tín hiệu nào) thì rơi về gợi
     * ý tài liệu đang được quan tâm nhiều nhất. Không gợi ý lại tài liệu của chính user.
     */
    @Override
    @Transactional(readOnly = true)
    public List<RecommendedDocumentDTO> recommendForUser(Long currentUserId, int limit) {
        return rank(personalSubjectAffinity(currentUserId), new HashSet<>(), currentUserId, limit, true, loadWeights());
    }

    // Tập subject user "quan tâm", gộp từ: tài liệu của chính mình + đã yêu thích + xem gần
    // đây + có mặt trong collection nào đó. Rỗng nếu user hoàn toàn mới -> rank() fallback
    // về trending thuần (mọi ứng viên vẫn có điểm >0 nhờ favorite/download/rating).
    private Set<Long> personalSubjectAffinity(Long userId) {
        Set<Long> docIds = new HashSet<>();

        docDocumentRepository.findByDeletedAtIsNullAndUser_Id(userId)
                .forEach(d -> docIds.add(d.getId()));

        docIds.addAll(favoriteRepository.findDocumentIdsByUserId(userId));

        viewHistoryRepository.findByUser_IdOrderByViewedAtDesc(userId)
                .forEach(v -> docIds.add(v.getDocumentId()));

        collectionRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(Collection::getId)
                .flatMap(cid -> collectionItemRepository.findByCollection_IdOrderByAddedAtAsc(cid).stream())
                .map(CollectionItem::getDocumentId)
                .forEach(docIds::add);

        if (docIds.isEmpty()) return Set.of();

        return docDocumentRepository.findAllById(docIds).stream()
                .flatMap(d -> subjectIdsOf(d).stream())
                .collect(Collectors.toSet());
    }

    // Chấm điểm và xếp hạng ứng viên. Ứng viên CHỈ gồm tài liệu vừa PUBLIC vừa đã được Admin
    // duyệt (APPROVED) — tài liệu riêng tư hay chưa duyệt không bao giờ lọt vào gợi ý.
    private List<RecommendedDocumentDTO> rank(Set<Long> affinitySubjectIds, Set<Long> excludeIds,
                                               Long currentUserId, int limit, boolean excludeOwnDocuments,
                                               ScoringWeights weights) {
        List<DocDocument> candidates = docDocumentRepository.findPublicApprovedDocuments(
                DocumentVisibility.PUBLIC, ModerationStatus.APPROVED);

        return candidates.stream()
                .filter(d -> !excludeIds.contains(d.getId()))
                // Chỉ với "Discover": không gợi ý lại tài liệu chính chủ user đã có sẵn.
                .filter(d -> !excludeOwnDocuments || currentUserId == null
                        || !d.getUser().getId().equals(currentUserId))
                // Double-check quyền truy cập theo đúng luật availability dùng chung toàn app,
                // dù về lý thuyết PUBLIC+APPROVED đã luôn khả dụng với mọi người.
                .filter(d -> documentAccessPort.isAvailableTo(d.getId(), currentUserId))
                .map(d -> toScored(d, affinitySubjectIds, weights))
                .sorted(Comparator.comparingDouble(RecommendedDocumentDTO::getScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    // Tính điểm cho MỘT tài liệu ứng viên theo công thức:
    // điểm = (số môn học trùng sở thích × 100) + (lượt thích × 3) + (lượt tải × 1) + (điểm sao × 10)
    private RecommendedDocumentDTO toScored(DocDocument doc, Set<Long> affinitySubjectIds, ScoringWeights weights) {
        Set<Long> docSubjectIds = subjectIdsOf(doc);
        // Đếm số môn học của tài liệu này trùng với tập môn học user quan tâm.
        long overlap = docSubjectIds.stream().filter(affinitySubjectIds::contains).count();

        Long favoriteCount = favoriteRepository.countByDocumentId(doc.getId());
        Long downloadCount = downloadRepository.countByDocumentId(doc.getId());
        Double averageRating = ratingRepository.getAverageRatingByDocumentId(doc.getId());
        if (averageRating == null) averageRating = 0.0;

        double score = overlap * weights.subjectOverlap()
                + (favoriteCount == null ? 0 : favoriteCount) * weights.favorite()
                + (downloadCount == null ? 0 : downloadCount) * weights.download()
                + averageRating * weights.rating();

        List<String> subjectNames = doc.getSubjects() == null
                ? List.of()
                : doc.getSubjects().stream().map(Subject::getName).collect(Collectors.toList());

        return new RecommendedDocumentDTO(
                doc.getId(),
                doc.getTitle(),
                doc.getUser() != null ? doc.getUser().getFullName() : null,
                subjectNames,
                favoriteCount,
                downloadCount,
                averageRating,
                score);
    }

    private static Set<Long> subjectIdsOf(DocDocument doc) {
        if (doc.getSubjects() == null) return Set.of();
        return doc.getSubjects().stream().map(Subject::getId).collect(Collectors.toSet());
    }
}
