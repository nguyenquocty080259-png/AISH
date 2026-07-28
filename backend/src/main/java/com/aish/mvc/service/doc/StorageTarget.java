package com.aish.mvc.service.doc;

import java.util.Set;

/**
 * Nơi lưu tệp tài liệu, dùng chung cho tham số {@code storage} lúc upload và cho trường
 * {@code storageType} trả về FE. Ba chuỗi này đi xuyên toàn bộ module (chọn nơi lưu, giới hạn
 * dung lượng, quota, hiển thị nhãn) nên gom về một chỗ thay vì rải literal — sai chính tả ở một
 * nhánh sẽ âm thầm bỏ qua đúng cái giới hạn mà nhánh đó cần kiểm tra.
 */
public final class StorageTarget {

    /** Chỉ lưu trên đĩa máy chủ. */
    public static final String LOCAL = "LOCAL";

    /** Chỉ lưu trên Cloudinary. */
    public static final String CLOUD = "CLOUD";

    /** Lưu cả hai bản; mọi lối đọc ưu tiên bản local. */
    public static final String BOTH = "BOTH";

    /** Tập giá trị hợp lệ cho tham số {@code storage} của API upload. */
    public static final Set<String> ALL = Set.of(LOCAL, CLOUD, BOTH);

    private StorageTarget() {
    }
}
