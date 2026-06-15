package com.aish.mvc.repository.stor;

import com.aish.mvc.entity.doc.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    Optional<Subject> findByName(String name);
    boolean existsByName(String name);
}