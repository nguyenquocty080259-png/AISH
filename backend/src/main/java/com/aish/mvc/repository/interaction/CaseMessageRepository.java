package com.aish.mvc.repository.interaction;

import com.aish.mvc.entity.enums.CaseType;
import com.aish.mvc.entity.interaction.CaseMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaseMessageRepository extends JpaRepository<CaseMessage, Long> {

    List<CaseMessage> findByCaseTypeAndCaseIdOrderByCreatedAtAsc(CaseType caseType, Long caseId);
}
