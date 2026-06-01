package com.aish.mvc.repository;

import com.aish.mvc.entity.DocFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocFileRepository extends JpaRepository<DocFile, Long> {
}