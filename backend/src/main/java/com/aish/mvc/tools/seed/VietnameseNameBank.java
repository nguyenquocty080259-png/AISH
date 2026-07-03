package com.aish.mvc.tools.seed;

// Sinh tên + email tiếng Việt "thật vừa đủ" cho user demo — không lấy từ nguồn ngoài,
// chỉ tổ hợp họ/tên đệm/tên phổ biến. Trùng tên giữa vài user là bình thường (đúng thực tế),
// email luôn duy nhất nhờ hậu tố số thứ tự.
final class VietnameseNameBank {

    private static final String[] SURNAMES = {
            "Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Huỳnh", "Phan", "Vũ",
            "Võ", "Đặng", "Bùi", "Đỗ", "Hồ", "Ngô", "Dương", "Lý"
    };
    private static final String[] MIDDLE_MALE = {"Văn", "Hữu", "Đức", "Minh", "Quang", "Thành", "Công", "Anh"};
    private static final String[] MIDDLE_FEMALE = {"Thị", "Ngọc", "Thu", "Kim", "Bảo", "Diệu", "Hồng", "Mỹ"};
    private static final String[] GIVEN_MALE = {
            "An", "Bình", "Cường", "Dũng", "Duy", "Hải", "Hùng", "Khang", "Kiên", "Long",
            "Minh", "Nam", "Phong", "Quân", "Sơn", "Tài", "Thắng", "Tuấn", "Việt", "Vinh"
    };
    private static final String[] GIVEN_FEMALE = {
            "Anh", "Chi", "Diệp", "Giang", "Hà", "Hoa", "Huyền", "Lan", "Linh", "Mai",
            "My", "Nga", "Ngọc", "Nhi", "Phương", "Quỳnh", "Thảo", "Trang", "Vy", "Yến"
    };

    private VietnameseNameBank() {
    }

    static String fullNameFor(int index) {
        boolean female = index % 2 == 0;
        String surname = SURNAMES[index % SURNAMES.length];
        String middle = female ? MIDDLE_FEMALE[index % MIDDLE_FEMALE.length] : MIDDLE_MALE[index % MIDDLE_MALE.length];
        String[] given = female ? GIVEN_FEMALE : GIVEN_MALE;
        String givenName = given[(index / 2) % given.length];
        return surname + " " + middle + " " + givenName;
    }

    // vd. "Nguyễn Văn An" (index 0) -> "nguyenvanan1@seed.aish.local"
    static String emailFor(int index, String fullName, String domain) {
        String slug = SlugUtil.slugify(fullName).replace("-", "");
        return slug + (index + 1) + domain;
    }
}
