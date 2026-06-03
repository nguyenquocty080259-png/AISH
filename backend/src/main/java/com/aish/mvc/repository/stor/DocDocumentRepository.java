package com.aish.mvc.repository.stor;

import com.aish.mvc.entity.doc.DocDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocDocumentRepository extends JpaRepository<DocDocument, Long> {
}