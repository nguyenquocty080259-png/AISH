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

    @Override
    @Transactional(readOnly = true)
    public List<RecommendedDocumentDTO> recommendRelatedToDocument(Long documentId, Long currentUserId, int limit) {
        DocDocument seed = docDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tài liệu không tồn tại!"));

        Set<Long> excludeIds = new HashSet<>();
        excludeIds.add(documentId);

        return rank(subjectIdsOf(seed), excludeIds, currentUserId, limit, false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecommendedDocumentDTO> recommendForUser(Long currentUserId, int limit) {
        return rank(personalSubjectAffinity(currentUserId), new HashSet<>(), currentUserId, limit, true);
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

    private List<RecommendedDocumentDTO> rank(Set<Long> affinitySubjectIds, Set<Long> excludeIds,
                                               Long currentUserId, int limit, boolean excludeOwnDocuments) {
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
                .map(d -> toScored(d, affinitySubjectIds))
                .sorted(Comparator.comparingDouble(RecommendedDocumentDTO::getScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    private RecommendedDocumentDTO toScored(DocDocument doc, Set<Long> affinitySubjectIds) {
        Set<Long> docSubjectIds = subjectIdsOf(doc);
        long overlap = docSubjectIds.stream().filter(affinitySubjectIds::contains).count();

        Long favoriteCount = favoriteRepository.countByDocumentId(doc.getId());
        Long downloadCount = downloadRepository.countByDocumentId(doc.getId());
        Double averageRating = ratingRepository.getAverageRatingByDocumentId(doc.getId());
        if (averageRating == null) averageRating = 0.0;

        double score = overlap * SUBJECT_OVERLAP_WEIGHT
                + (favoriteCount == null ? 0 : favoriteCount) * FAVORITE_WEIGHT
                + (downloadCount == null ? 0 : downloadCount) * DOWNLOAD_WEIGHT
                + averageRating * RATING_WEIGHT;

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
