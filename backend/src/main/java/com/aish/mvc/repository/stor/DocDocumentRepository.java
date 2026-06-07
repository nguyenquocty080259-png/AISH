package com.aish.mvc.repository.stor;

import com.aish.mvc.entity.doc.DocDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocDocumentRepository extends JpaRepository<DocDocument, Long> {
    List<DocDocument> findByDeletedAtIsNull(); //

    // BẮT BUỘC PHẢI CÓ DÒNG NÀY ĐỂ SERVICE GỌI:
    List<DocDocument> findByDeletedAtIsNotNull();
}