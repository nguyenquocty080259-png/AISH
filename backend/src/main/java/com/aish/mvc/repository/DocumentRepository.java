package com.aish.mvc.repository;

import com.aish.mvc.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    // Spring Boot sẽ tự động sinh code cho các hàm save(), findById(), findAll(), delete()
}