package com.aish.mvc.repository.doc;

import com.aish.mvc.entity.doc.ModerationKeyword;
import com.aish.mvc.entity.enums.ModerationKeywordType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModerationKeywordRepository extends JpaRepository<ModerationKeyword, Long> {

    List<ModerationKeyword> findByTypeAndActiveTrue(ModerationKeywordType type);

    List<ModerationKeyword> findAllByOrderByCreatedAtDesc();

    List<ModerationKeyword> findByTypeOrderByCreatedAtDesc(ModerationKeywordType type);

    boolean existsByType(ModerationKeywordType type);

    boolean existsByKeywordAndType(String keyword, ModerationKeywordType type);

    boolean existsByKeywordAndTypeAndIdNot(String keyword, ModerationKeywordType type, Long id);
}
