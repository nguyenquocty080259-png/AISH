package com.aish.mvc.dto.doc;

import lombok.Getter;
import lombok.Setter;

// Body cho tạo/đổi tên collection: { "name": "..." }
@Getter
@Setter
public class CollectionRequestDTO {
    private String name;
}
