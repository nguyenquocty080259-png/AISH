package com.aish.mvc.dto.doc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class CommunityPageResponseDTO {
    private List<DocumentResponseDTO> items;
    private int page;        // trang hiện tại (bắt đầu từ 0)
    private int size;        // số item / trang
    private long totalItems; // tổng số tài liệu công khai khớp điều kiện lọc
    private int totalPages;
}