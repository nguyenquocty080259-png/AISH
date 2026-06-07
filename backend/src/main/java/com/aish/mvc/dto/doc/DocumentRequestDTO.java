package com.aish.mvc.dto.doc;

public class DocumentRequestDTO {
    private String title;
    private String description;
    private String fileName;
    private String storageUrl;
    private String visibility;
    private Long userId;
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public String getStorageUrl() {
        return storageUrl;
    }
    public void setStorageUrl(String storageUrl) {
        this.storageUrl = storageUrl;
    }
    public String getVisibility() {
        return visibility;
    }
    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }
    public Long getUserId() { return userId; }
public void setUserId(Long userId) { this.userId = userId; }
}

