package com.aish.mvc.service.doc;

import com.aish.mvc.dto.doc.DocumentDownloadResult;
import com.aish.mvc.dto.doc.DocumentResponseDTO;
import com.aish.mvc.entity.doc.DocFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {

    List<DocumentResponseDTO> getAllDocuments();

    DocumentResponseDTO uploadDocumentToServer(String title, String description, Long subjectId,
                                               List<String> tagNames, MultipartFile file);

    DocumentResponseDTO uploadDocumentToCloud(String title, String description, Long subjectId,
                                              List<String> tagNames, MultipartFile file);

    void addComment(Long documentId, String content);

    void toggleFavorite(Long documentId);

    void rateDocument(Long documentId, Integer star);

    void logDownload(Long documentId);

    void deleteDocument(Long id);

    List<DocumentResponseDTO> getDeletedDocuments();

    void restoreDocument(Long id);

    DocFile getFileByDocumentId(Long documentId);

    void toggleVisibility(Long documentId);

    DocumentResponseDTO getDocumentById(Long id);

    DocumentDownloadResult prepareDownload(Long documentId);
}