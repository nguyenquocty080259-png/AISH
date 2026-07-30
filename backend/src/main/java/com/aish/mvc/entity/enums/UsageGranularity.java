package com.aish.mvc.entity.enums;

public enum UsageGranularity {
    DAY,
    WEEK,
    MONTH,
    QUARTER,
    YEAR;

    // Đơn vị truyền vào date_trunc() của Postgres - khớp tên enum viết thường.
    public String toPgUnit() {
        return name().toLowerCase();
    }
}
