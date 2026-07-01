package com.aish.mvc.repository.doc;

import com.aish.mvc.entity.doc.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {

    List<Collection> findByUser_IdOrderByCreatedAtDesc(Long userId);

    // Đảm bảo collection thuộc đúng user hiện tại (nếu không -> coi như không tồn tại).
    Optional<Collection> findByIdAndUser_Id(Long id, Long userId);

    // Check trùng tên trong cùng 1 user (bổ trợ cho UNIQUE(user_id, name)).
    boolean existsByUser_IdAndName(Long userId, String name);
}
