package com.aish.mvc.tools.seed;

import java.util.List;

// Ngân hàng nội dung THPT bám sát chương trình phổ thông Việt Nam. SeedFileGenerator chọn đúng
// 5 chủ đề cho mỗi môn canonical; các chủ đề dư vẫn được giữ làm nguồn mở rộng về sau.
final class ThptContentBank {

    private ThptContentBank() {
    }

    private static Topic t(String subject, String title, String p1, String p2, String p3) {
        return new Topic("THPT", null, subject, title, List.of(
                "I. KIẾN THỨC TRỌNG TÂM — " + p1,
                "II. PHÂN TÍCH VÀ MỞ RỘNG — " + p2,
                "III. VẬN DỤNG — " + p3,
                "IV. VÍ DỤ CÓ HƯỚNG DẪN — Xét một tình huống tiêu biểu của bài “" + title
                        + "”. Trước hết xác định dữ kiện, khái niệm hoặc quy tắc liên quan; tiếp theo lựa chọn phương pháp, trình bày từng bước và kiểm tra điều kiện áp dụng. Sau khi có kết quả, đối chiếu lại với yêu cầu, giải thích ý nghĩa và chỉ ra lỗi sai thường gặp. Cách làm này giúp học sinh không chỉ ghi nhớ đáp án mà còn hình thành quy trình giải quyết vấn đề có thể dùng cho các câu hỏi tương tự.",
                "V. BÀI TẬP LUYỆN TẬP — Bài 1: trình bày lại nội dung cốt lõi bằng sơ đồ hoặc bảng so sánh. Bài 2: giải một trường hợp cơ bản và giải thích từng bước. Bài 3: thay đổi một dữ kiện của ví dụ để dự đoán kết quả mới. Bài 4: tìm và sửa một lời giải hoặc nhận định sai. Bài 5: liên hệ kiến thức với một hiện tượng trong học tập hoặc đời sống. Học sinh nên làm độc lập trước, sau đó trao đổi cách giải và tự chấm theo tiêu chí đúng kiến thức, đủ lập luận, rõ trình bày.",
                "VI. TỔNG KẾT VÀ TỰ HỌC — Sau bài học, học sinh cần tự trả lời được ba câu hỏi: kiến thức nào là nền tảng, dấu hiệu nào cho biết nên áp dụng kiến thức đó, và làm thế nào kiểm tra kết quả. Hãy lập một trang ghi chú gồm từ khóa, công thức hoặc luận điểm chính, một ví dụ mẫu và một lỗi dễ mắc. Ôn lại sau một ngày và một tuần, đồng thời tự tạo thêm câu hỏi vận dụng để củng cố khả năng nhớ lâu và sử dụng kiến thức linh hoạt."
        ));
    }

    static List<Topic> all() {
        List<Topic> topics = new java.util.ArrayList<>();
        topics.addAll(toan());
        topics.addAll(ly());
        topics.addAll(hoa());
        topics.addAll(van());
        topics.addAll(anh());
        topics.addAll(su());
        topics.addAll(dia());
        topics.addAll(sinh());
        topics.addAll(tinHoc());
        topics.addAll(gdcd());
        return topics;
    }

    private static List<Topic> tinHoc() {
        String subject = "Tin học";
        return List.of(
                t(subject, "Thuật toán và mô tả thuật toán",
                        "Thuật toán là một dãy hữu hạn các thao tác xác định, được sắp xếp theo trình tự để biến dữ liệu đầu vào thành kết quả đầu ra và giải quyết một lớp bài toán. Một thuật toán tốt cần đúng đắn, hữu hạn, rõ ràng và có khả năng thực hiện được.",
                        "Có thể mô tả thuật toán bằng ngôn ngữ tự nhiên, sơ đồ khối hoặc mã giả. Khi thiết kế cần xác định input, output, chia bài toán thành bước nhỏ, xét các trường hợp biên rồi mới đánh giá độ phức tạp thời gian và bộ nhớ.",
                        "Ví dụ tìm số lớn nhất trong một dãy: khởi tạo giá trị lớn nhất bằng phần tử đầu, lần lượt so sánh với từng phần tử còn lại và cập nhật khi gặp giá trị lớn hơn. Thuật toán duyệt đúng một lần nên có độ phức tạp tuyến tính."),
                t(subject, "Lập trình Python với cấu trúc điều khiển",
                        "Chương trình Python được xây dựng từ biến, kiểu dữ liệu, biểu thức và câu lệnh. Cấu trúc rẽ nhánh if-elif-else giúp lựa chọn hành động theo điều kiện, còn vòng lặp for và while dùng để lặp lại một nhóm thao tác.",
                        "Điều kiện cần cho kết quả Boolean; các nhánh và thân vòng lặp được xác định bằng thụt lề. Khi dùng while phải bảo đảm biến điều khiển thay đổi để vòng lặp kết thúc, đồng thời nên kiểm thử các giá trị ở biên.",
                        "Một chương trình tính tổng các số chẵn từ 1 đến n có thể duyệt từng số, kiểm tra phần dư khi chia cho 2 rồi cộng vào biến tổng. Có thể cải tiến bằng range bắt đầu từ 2 với bước nhảy 2."),
                t(subject, "Dữ liệu và cơ sở dữ liệu quan hệ",
                        "Cơ sở dữ liệu là tập hợp dữ liệu có tổ chức phục vụ lưu trữ, tìm kiếm và cập nhật. Trong mô hình quan hệ, dữ liệu được biểu diễn bằng bảng gồm hàng và cột; mỗi bảng mô tả một loại đối tượng.",
                        "Khóa chính định danh duy nhất một bản ghi, khóa ngoại tạo liên kết giữa các bảng. Thiết kế hợp lý giúp giảm lặp dữ liệu, tránh mâu thuẫn khi cập nhật và hỗ trợ truy vấn chính xác.",
                        "Với hệ thống thư viện, bảng Sách có mã sách làm khóa chính, bảng BạnĐọc có mã bạn đọc, còn bảng MượnSách chứa các khóa ngoại tương ứng cùng ngày mượn và ngày trả. Truy vấn có thể kết hợp các bảng để tìm người đang giữ một cuốn sách."),
                t(subject, "Mạng máy tính và Internet an toàn",
                        "Mạng máy tính kết nối các thiết bị để trao đổi dữ liệu và dùng chung tài nguyên. Internet là mạng liên kết toàn cầu hoạt động dựa trên bộ giao thức TCP/IP; tên miền được DNS chuyển thành địa chỉ IP.",
                        "Dữ liệu truyền qua mạng theo các gói và đi qua nhiều thiết bị trung gian. Những dịch vụ phổ biến gồm web, thư điện tử, lưu trữ đám mây; HTTPS bổ sung mã hóa và xác thực cho giao tiếp web.",
                        "Để sử dụng Internet an toàn cần dùng mật khẩu dài và riêng biệt, bật xác thực nhiều lớp, cập nhật phần mềm, kiểm tra tên miền trước khi đăng nhập và không mở tệp hoặc liên kết đáng ngờ."),
                t(subject, "Đạo đức, pháp luật và văn hóa số",
                        "Công dân số cần tôn trọng quyền riêng tư, bản quyền, danh dự của người khác và chịu trách nhiệm về nội dung mình đăng tải. Thông tin trên môi trường mạng có thể được sao chép, tìm kiếm và tồn tại lâu dài.",
                        "Trước khi chia sẻ cần kiểm tra nguồn, tác giả, ngày công bố và đối chiếu với nguồn độc lập. Việc sử dụng tác phẩm phải tuân thủ giấy phép, trích dẫn phù hợp và không biến nội dung của người khác thành của mình.",
                        "Khi gặp tin giả hoặc bắt nạt trực tuyến, không tiếp tục phát tán; nên lưu bằng chứng, sử dụng công cụ báo cáo, chặn tài khoản vi phạm và tìm hỗ trợ từ giáo viên, gia đình hoặc cơ quan có trách nhiệm."));
    }

    private static List<Topic> toan() {
        String subject = "Toán";
        return List.of(
                t(subject, "Đạo hàm và ý nghĩa hình học",
                        "Đạo hàm của hàm số y = f(x) tại điểm x0 là giới hạn của tỉ số giữa số gia của hàm số và số gia của biến số khi số gia của biến số dần tới 0, ký hiệu f'(x0). Về mặt hình học, f'(x0) chính là hệ số góc của tiếp tuyến với đồ thị hàm số tại điểm có hoành độ x0, từ đó suy ra phương trình tiếp tuyến y = f'(x0)(x - x0) + f(x0).",
                        "Các quy tắc tính đạo hàm cơ bản gồm đạo hàm của tổng, hiệu, tích, thương hai hàm số, cùng đạo hàm của hàm hợp thông qua công thức (u(v(x)))' = u'(v(x)).v'(x). Bảng đạo hàm của các hàm sơ cấp như hàm lũy thừa, hàm lượng giác, hàm mũ và hàm lôgarit là công cụ nền tảng để giải quyết các bài toán khảo sát hàm số.",
                        "Đạo hàm còn được ứng dụng để xét tính đồng biến, nghịch biến của hàm số thông qua dấu của f'(x), tìm cực trị (điểm mà đạo hàm đổi dấu), và giải các bài toán tối ưu trong thực tế như tìm giá trị lớn nhất, nhỏ nhất của một đại lượng phụ thuộc vào một biến số, ví dụ tối ưu diện tích, thể tích hay chi phí sản xuất."),
                t(subject, "Khảo sát và vẽ đồ thị hàm số",
                        "Khảo sát hàm số là quá trình tìm tập xác định, xét chiều biến thiên dựa trên dấu đạo hàm, xác định cực trị, tính giới hạn tại vô cực và tại các điểm gián đoạn để tìm tiệm cận đứng, tiệm cận ngang. Kết quả được tổng hợp trong bảng biến thiên, cho biết đầy đủ hình dạng tổng quát của đồ thị trước khi vẽ.",
                        "Với hàm bậc ba y = ax³ + bx² + cx + d, đồ thị có thể có 0 hoặc 2 điểm cực trị tùy vào dấu của biệt thức đạo hàm; hàm phân thức bậc nhất trên bậc nhất luôn có một tiệm cận đứng và một tiệm cận ngang, đồ thị nhận giao điểm hai tiệm cận làm tâm đối xứng.",
                        "Từ bảng biến thiên, học sinh xác định tọa độ các điểm đặc biệt (giao với trục tọa độ, điểm cực trị, điểm uốn) rồi vẽ phác đồ thị đảm bảo đúng chiều biến thiên và tiệm cận. Dạng bài này thường đi kèm yêu cầu biện luận số nghiệm phương trình bằng phương pháp tương giao đồ thị với đường thẳng y = m."),
                t(subject, "Nguyên hàm và tích phân cơ bản",
                        "Nguyên hàm của hàm số f(x) trên một khoảng là hàm số F(x) sao cho F'(x) = f(x) trên khoảng đó. Họ nguyên hàm của f(x) được viết là F(x) + C với C là hằng số tùy ý. Việc tìm nguyên hàm dựa trên bảng nguyên hàm các hàm cơ bản và các phương pháp đổi biến số, nguyên hàm từng phần.",
                        "Tích phân xác định của f(x) trên đoạn [a; b], ký hiệu ∫ từ a đến b f(x)dx, được tính bằng công thức Newton-Leibniz: bằng F(b) - F(a), trong đó F là một nguyên hàm bất kỳ của f. Ý nghĩa hình học của tích phân xác định (khi f(x) ≥ 0) là diện tích hình phẳng giới hạn bởi đồ thị hàm số, trục hoành và hai đường thẳng x = a, x = b.",
                        "Tích phân còn được dùng để tính diện tích hình phẳng giới hạn bởi hai đồ thị và tính thể tích khối tròn xoay khi quay một hình phẳng quanh trục hoành, theo công thức V = π∫ từ a đến b [f(x)]²dx. Đây là ứng dụng quan trọng nối liền giải tích với hình học không gian."),
                t(subject, "Phương trình lượng giác cơ bản",
                        "Phương trình lượng giác cơ bản gồm bốn dạng: sin x = m, cos x = m, tan x = m và cot x = m. Với sin x = m và cos x = m, phương trình chỉ có nghiệm khi -1 ≤ m ≤ 1; nghiệm được biểu diễn theo họ nghiệm tổng quát cộng thêm k2π hoặc kπ tùy dạng, với k là số nguyên.",
                        "Để giải các phương trình phức tạp hơn, học sinh cần đưa về một trong bốn dạng cơ bản thông qua các công thức biến đổi: công thức cộng, công thức nhân đôi, công thức hạ bậc, hoặc công thức biến đổi tổng thành tích và tích thành tổng. Việc chọn phương pháp phù hợp giúp rút gọn phương trình về dạng chứa một hàm lượng giác duy nhất.",
                        "Một số phương trình đặc biệt như phương trình bậc hai đối với một hàm lượng giác (đặt t = sin x hoặc t = cos x), hay phương trình đẳng cấp bậc hai đối với sin x và cos x, đòi hỏi kỹ thuật đặt ẩn phụ và kiểm tra điều kiện của ẩn phụ trước khi kết luận nghiệm."),
                t(subject, "Tổ hợp và xác suất",
                        "Quy tắc cộng và quy tắc nhân là hai nguyên lý đếm cơ bản: quy tắc cộng áp dụng khi một công việc có thể hoàn thành theo một trong nhiều phương án loại trừ nhau, còn quy tắc nhân áp dụng khi một công việc gồm nhiều giai đoạn liên tiếp độc lập. Từ đó phát triển các khái niệm hoán vị, chỉnh hợp và tổ hợp của n phần tử.",
                        "Hoán vị của n phần tử là số cách sắp xếp có thứ tự tất cả n phần tử, bằng n!. Chỉnh hợp chập k của n phần tử là số cách chọn có thứ tự k trong n phần tử. Tổ hợp chập k của n phần tử là số cách chọn không phân biệt thứ tự k trong n phần tử, ký hiệu C(n,k) = n!/(k!(n-k)!), đóng vai trò nền tảng cho nhị thức Newton.",
                        "Xác suất của một biến cố A trong không gian mẫu hữu hạn đồng khả năng được tính bằng tỉ số giữa số kết quả thuận lợi cho A và tổng số kết quả có thể xảy ra. Các quy tắc cộng xác suất (với biến cố xung khắc) và nhân xác suất (với biến cố độc lập) giúp tính xác suất của các biến cố phức tạp hơn từ các biến cố đơn giản."),
                t(subject, "Hình học không gian: quan hệ song song",
                        "Trong không gian, hai đường thẳng được gọi là song song nếu chúng đồng phẳng và không có điểm chung. Để chứng minh hai đường thẳng song song, ta thường dùng định lý về giao tuyến của hai mặt phẳng song song, hoặc tính chất đường trung bình của tam giác, hình thang trong không gian.",
                        "Đường thẳng song song với mặt phẳng khi đường thẳng đó không nằm trong mặt phẳng và song song với một đường thẳng nào đó nằm trong mặt phẳng. Hai mặt phẳng song song khi mặt phẳng này chứa hai đường thẳng cắt nhau, mỗi đường đều song song với mặt phẳng còn lại — đây là dấu hiệu nhận biết quan trọng nhất trong các bài toán chứng minh.",
                        "Các bài toán về quan hệ song song thường yêu cầu xác định giao tuyến của hai mặt phẳng, thiết diện của một hình chóp hoặc hình lăng trụ khi cắt bởi một mặt phẳng, đòi hỏi kỹ năng vẽ hình không gian chính xác và vận dụng linh hoạt các định lý về giao tuyến, đường thẳng song song mặt phẳng."),
                t(subject, "Dãy số, cấp số cộng và cấp số nhân",
                        "Cấp số cộng là dãy số trong đó kể từ số hạng thứ hai, mỗi số hạng bằng số hạng đứng trước cộng với một số không đổi d gọi là công sai. Số hạng tổng quát là un = u1 + (n-1)d, và tổng n số hạng đầu Sn = n(u1 + un)/2.",
                        "Cấp số nhân là dãy số trong đó kể từ số hạng thứ hai, mỗi số hạng bằng số hạng đứng trước nhân với một số không đổi q gọi là công bội. Số hạng tổng quát là un = u1.q^(n-1), tổng n số hạng đầu Sn = u1(1-q^n)/(1-q) với q ≠ 1.",
                        "Hai loại dãy số này xuất hiện nhiều trong bài toán thực tế như tính lãi kép (cấp số nhân), tính tổng tiền tiết kiệm đều đặn (cấp số cộng), hay các bài toán tăng trưởng dân số, khấu hao tài sản — là cầu nối giữa đại số và các bài toán ứng dụng tài chính.")
        );
    }

    private static List<Topic> ly() {
        String subject = "Vật lý";
        return List.of(
                t(subject, "Định luật bảo toàn động lượng",
                        "Động lượng của một vật là đại lượng vectơ p = mv, có cùng hướng với vận tốc. Trong một hệ kín (không chịu tác dụng của ngoại lực hoặc tổng ngoại lực bằng 0), tổng động lượng của hệ được bảo toàn theo thời gian, đây là một trong những định luật bảo toàn cơ bản nhất của cơ học.",
                        "Định luật bảo toàn động lượng được ứng dụng trực tiếp để phân tích va chạm giữa các vật. Trong va chạm mềm (va chạm hoàn toàn không đàn hồi), hai vật dính vào nhau sau va chạm và chuyển động với cùng vận tốc; trong va chạm đàn hồi, cả động lượng và động năng của hệ đều được bảo toàn.",
                        "Nguyên lý chuyển động bằng phản lực của tên lửa cũng dựa trên bảo toàn động lượng: khí phụt ra phía sau với động lượng lớn khiến tên lửa nhận được động lượng ngược chiều để tiến về phía trước, minh họa rõ ràng cho định luật III Newton kết hợp với bảo toàn động lượng trong hệ kín."),
                t(subject, "Dao động điều hòa",
                        "Dao động điều hòa là dao động trong đó li độ của vật là hàm côsin (hoặc sin) của thời gian: x = A.cos(ωt + φ), với A là biên độ, ω là tần số góc và φ là pha ban đầu. Chu kỳ T = 2π/ω là khoảng thời gian để vật thực hiện một dao động toàn phần.",
                        "Con lắc lò xo dao động điều hòa với tần số góc ω = căn(k/m), trong đó k là độ cứng lò xo và m là khối lượng vật nặng; con lắc đơn dao động điều hòa với biên độ góc nhỏ có tần số góc ω = căn(g/l), với l là chiều dài dây treo và g là gia tốc trọng trường.",
                        "Trong dao động điều hòa, cơ năng của hệ (tổng động năng và thế năng) được bảo toàn nếu bỏ qua ma sát, và có giá trị không đổi bằng W = ½kA². Khi vật dao động tắt dần do ma sát hoặc lực cản, biên độ giảm dần theo thời gian và cơ năng chuyển hóa dần thành nhiệt năng."),
                t(subject, "Sóng cơ và giao thoa sóng",
                        "Sóng cơ là dao động lan truyền trong một môi trường vật chất (rắn, lỏng, khí), mang theo năng lượng nhưng không mang theo vật chất của môi trường. Sóng ngang có phương dao động vuông góc với phương truyền sóng, còn sóng dọc có phương dao động trùng với phương truyền sóng, ví dụ điển hình là sóng âm.",
                        "Giao thoa sóng là hiện tượng hai sóng kết hợp (cùng tần số, hiệu số pha không đổi theo thời gian) gặp nhau tạo ra những điểm dao động với biên độ cực đại (giao thoa tăng cường) xen kẽ với những điểm dao động với biên độ cực tiểu hoặc đứng yên (giao thoa triệt tiêu).",
                        "Điều kiện để tại một điểm có cực đại giao thoa là hiệu đường đi của hai sóng từ hai nguồn tới điểm đó bằng một số nguyên lần bước sóng; điều kiện có cực tiểu là hiệu đường đi bằng một số bán nguyên lần bước sóng. Hiện tượng sóng dừng trên dây đàn hồi cũng là kết quả của giao thoa giữa sóng tới và sóng phản xạ."),
                t(subject, "Dòng điện xoay chiều",
                        "Dòng điện xoay chiều là dòng điện có cường độ biến thiên điều hòa theo thời gian theo quy luật hàm sin hoặc côsin: i = I0.cos(ωt + φ). Giá trị hiệu dụng của dòng điện xoay chiều I = I0/√2 được dùng phổ biến trong tính toán công suất tiêu thụ trên các thiết bị điện.",
                        "Mạch điện xoay chiều RLC nối tiếp có tổng trở Z phụ thuộc vào điện trở R, cảm kháng ZL = ωL và dung kháng ZC = 1/(ωC). Hiện tượng cộng hưởng điện xảy ra khi ZL = ZC, lúc đó tổng trở của mạch đạt giá trị nhỏ nhất bằng R và cường độ dòng điện trong mạch đạt giá trị cực đại.",
                        "Máy biến áp hoạt động dựa trên hiện tượng cảm ứng điện từ, dùng để biến đổi điện áp xoay chiều mà không làm thay đổi tần số, với tỉ số điện áp giữa hai cuộn dây bằng tỉ số số vòng dây tương ứng. Đây là thiết bị then chốt trong truyền tải điện năng đi xa với hao phí thấp."),
                t(subject, "Thuyết tương đối hẹp của Einstein (giới thiệu)",
                        "Thuyết tương đối hẹp do Albert Einstein công bố năm 1905 dựa trên hai tiên đề: các định luật vật lý có cùng dạng trong mọi hệ quy chiếu quán tính, và tốc độ ánh sáng trong chân không là như nhau đối với mọi quan sát viên, không phụ thuộc vào chuyển động của nguồn sáng hay người quan sát.",
                        "Từ hai tiên đề trên, Einstein suy ra các hệ quả đáng chú ý: thời gian trôi chậm lại đối với vật chuyển động (giãn thời gian), và chiều dài của vật bị co lại theo phương chuyển động khi quan sát từ hệ quy chiếu đứng yên (co độ dài). Các hiệu ứng này chỉ trở nên rõ rệt khi tốc độ vật tiến gần tới tốc độ ánh sáng.",
                        "Hệ quả nổi tiếng nhất của thuyết tương đối hẹp là hệ thức khối lượng - năng lượng E = mc², cho thấy khối lượng và năng lượng có thể chuyển hóa lẫn nhau. Công thức này là nền tảng lý thuyết cho các quá trình giải phóng năng lượng trong phản ứng hạt nhân."),
                t(subject, "Hiện tượng cảm ứng điện từ",
                        "Hiện tượng cảm ứng điện từ do Faraday phát hiện: mỗi khi từ thông qua một mạch kín biến thiên thì trong mạch xuất hiện một dòng điện cảm ứng. Định luật Faraday phát biểu suất điện động cảm ứng tỉ lệ với tốc độ biến thiên của từ thông qua mạch.",
                        "Định luật Lenz xác định chiều của dòng điện cảm ứng: dòng điện cảm ứng luôn có chiều sao cho từ trường do nó sinh ra chống lại nguyên nhân đã sinh ra nó, thể hiện nguyên lý bảo toàn năng lượng trong hiện tượng cảm ứng điện từ.",
                        "Hiện tượng cảm ứng điện từ là nguyên lý hoạt động của máy phát điện xoay chiều: khi khung dây quay đều trong từ trường đều, từ thông qua khung biến thiên điều hòa theo thời gian, sinh ra suất điện động xoay chiều — nền tảng của toàn bộ hệ thống sản xuất điện năng hiện nay."),
                t(subject, "Lượng tử ánh sáng và hiệu ứng quang điện",
                        "Thuyết lượng tử ánh sáng của Einstein cho rằng ánh sáng gồm các hạt gọi là photon, mỗi photon mang năng lượng ε = hf, với h là hằng số Planck và f là tần số ánh sáng. Ánh sáng vừa có tính chất sóng (giao thoa, nhiễu xạ) vừa có tính chất hạt (quang điện), gọi là lưỡng tính sóng - hạt.",
                        "Hiện tượng quang điện ngoài xảy ra khi chiếu ánh sáng thích hợp vào bề mặt kim loại làm bật electron ra khỏi bề mặt đó. Hiện tượng chỉ xảy ra khi tần số ánh sáng chiếu tới lớn hơn hoặc bằng một giá trị giới hạn gọi là giới hạn quang điện, đặc trưng cho từng kim loại.",
                        "Phương trình Einstein về hiện tượng quang điện: hf = A + Wđmax, trong đó A là công thoát electron ra khỏi kim loại và Wđmax là động năng ban đầu cực đại của electron quang điện. Công thức này giải thích được các đặc điểm thực nghiệm mà thuyết sóng ánh sáng cổ điển không giải thích được.")
        );
    }

    private static List<Topic> hoa() {
        String subject = "Hóa học";
        return List.of(
                t(subject, "Phản ứng oxi hóa - khử",
                        "Phản ứng oxi hóa - khử là phản ứng hóa học trong đó có sự chuyển electron giữa các chất phản ứng, thể hiện qua sự thay đổi số oxi hóa của các nguyên tố. Chất khử là chất nhường electron (số oxi hóa tăng), chất oxi hóa là chất nhận electron (số oxi hóa giảm).",
                        "Để lập phương trình phản ứng oxi hóa - khử, phương pháp thăng bằng electron được sử dụng phổ biến: xác định số oxi hóa thay đổi, viết quá trình oxi hóa và quá trình khử riêng biệt, sau đó cân bằng sao cho tổng số electron nhường bằng tổng số electron nhận.",
                        "Phản ứng oxi hóa - khử có ứng dụng rộng rãi trong đời sống và sản xuất, như quá trình luyện kim (khử oxit kim loại), pin điện hóa (chuyển hóa năng lượng hóa học thành điện năng), và các phản ứng đốt cháy nhiên liệu cung cấp năng lượng cho động cơ và sinh hoạt."),
                t(subject, "Cấu tạo nguyên tử và bảng tuần hoàn",
                        "Nguyên tử gồm hạt nhân mang điện tích dương (chứa proton và neutron) và lớp vỏ electron mang điện tích âm chuyển động xung quanh hạt nhân. Số proton trong hạt nhân quyết định số hiệu nguyên tử Z, đặc trưng cho mỗi nguyên tố hóa học và xác định vị trí của nguyên tố trong bảng tuần hoàn.",
                        "Bảng tuần hoàn các nguyên tố hóa học được sắp xếp theo chiều tăng dần của điện tích hạt nhân, các nguyên tố có cùng số lớp electron xếp thành một chu kỳ, các nguyên tố có cấu hình electron lớp ngoài cùng tương tự nhau xếp thành một nhóm, thể hiện tính chất hóa học tương tự nhau.",
                        "Trong một chu kỳ, khi điện tích hạt nhân tăng, bán kính nguyên tử giảm dần và tính kim loại giảm, tính phi kim tăng. Trong một nhóm A, khi số lớp electron tăng, bán kính nguyên tử tăng dần và tính kim loại tăng, tính phi kim giảm — đây là quy luật biến đổi tuần hoàn quan trọng nhất."),
                t(subject, "Liên kết hóa học",
                        "Liên kết ion được hình thành do lực hút tĩnh điện giữa các ion trái dấu, thường gặp giữa kim loại điển hình và phi kim điển hình, ví dụ NaCl. Liên kết cộng hóa trị được hình thành do sự dùng chung một hay nhiều cặp electron giữa hai nguyên tử, thường gặp giữa các nguyên tố phi kim.",
                        "Liên kết cộng hóa trị có thể phân cực hoặc không phân cực tùy vào hiệu độ âm điện giữa hai nguyên tử liên kết. Khi hiệu độ âm điện đủ lớn, cặp electron dùng chung lệch hẳn về một nguyên tử, liên kết chuyển dần sang bản chất ion — cho thấy ranh giới giữa hai loại liên kết là tương đối, không tuyệt đối.",
                        "Ngoài liên kết ion và cộng hóa trị, liên kết hydrogen (liên kết giữa nguyên tử H đã liên kết với nguyên tố có độ âm điện lớn và một nguyên tử có độ âm điện lớn khác) tuy yếu hơn nhiều nhưng có vai trò quan trọng, quyết định nhiệt độ sôi bất thường của nước và cấu trúc xoắn của ADN."),
                t(subject, "Este và lipit",
                        "Este là sản phẩm của phản ứng ester hóa giữa acid carboxylic và alcohol, có công thức tổng quát RCOOR' với xúc tác acid sulfuric đặc và đun nóng. Phản ứng ester hóa là phản ứng thuận nghịch, cần loại bỏ nước hoặc este sinh ra để tăng hiệu suất theo nguyên lý chuyển dịch cân bằng.",
                        "Lipit là este phức tạp giữa glycerol và các acid béo (acid carboxylic mạch dài), tồn tại phổ biến dưới dạng chất béo (triglycerid) trong tự nhiên. Chất béo lỏng (dầu thực vật) chứa nhiều gốc acid béo không no, còn chất béo rắn (mỡ động vật) chứa chủ yếu gốc acid béo no.",
                        "Phản ứng xà phòng hóa là phản ứng thủy phân chất béo trong môi trường kiềm (NaOH hoặc KOH), tạo ra glycerol và muối của acid béo (chính là xà phòng). Đây là phản ứng một chiều, khác với phản ứng ester hóa thuận nghịch, và là cơ sở của công nghệ sản xuất xà phòng truyền thống."),
                t(subject, "Đại cương về kim loại",
                        "Kim loại có cấu tạo mạng tinh thể với các ion dương kim loại nằm ở nút mạng và các electron hóa trị chuyển động tự do trong toàn khối tinh thể (electron tự do), tạo nên các tính chất vật lý chung như tính dẻo, tính dẫn điện, tính dẫn nhiệt và ánh kim.",
                        "Tính chất hóa học đặc trưng của kim loại là tính khử: kim loại dễ nhường electron để tạo thành ion dương. Mức độ hoạt động hóa học của kim loại được sắp xếp theo dãy điện hóa, kim loại đứng trước có tính khử mạnh hơn kim loại đứng sau và có thể đẩy kim loại đứng sau ra khỏi dung dịch muối.",
                        "Ăn mòn kim loại là quá trình kim loại bị oxi hóa bởi các chất trong môi trường, gồm ăn mòn hóa học (không phát sinh dòng điện) và ăn mòn điện hóa (phát sinh dòng điện do có hai điện cực khác bản chất tiếp xúc với dung dịch chất điện li). Các biện pháp chống ăn mòn phổ biến gồm sơn phủ bề mặt và bảo vệ điện hóa."),
                t(subject, "Ancol và phenol",
                        "Ancol là hợp chất hữu cơ có nhóm hydroxyl (-OH) liên kết trực tiếp với nguyên tử carbon no. Bậc của ancol được xác định theo bậc của nguyên tử carbon liên kết với nhóm -OH. Ancol có phản ứng đặc trưng với kim loại kiềm giải phóng khí hydrogen và phản ứng ester hóa với acid carboxylic.",
                        "Phenol là hợp chất hữu cơ có nhóm -OH liên kết trực tiếp với nguyên tử carbon của vòng benzene. Do ảnh hưởng của vòng benzene, nguyên tử H trong nhóm -OH của phenol linh động hơn ancol, thể hiện qua khả năng phản ứng được với dung dịch NaOH, điều mà ancol thông thường không có.",
                        "Sự khác biệt về tính chất hóa học giữa ancol và phenol xuất phát từ ảnh hưởng qua lại giữa nhóm -OH và gốc hydrocarbon: ở phenol, vòng benzene làm tăng tính acid yếu của nhóm -OH, đồng thời nhóm -OH cũng làm phản ứng thế vào vòng benzene ở phenol dễ dàng hơn so với benzene."),
                t(subject, "Tốc độ phản ứng và cân bằng hóa học",
                        "Tốc độ phản ứng hóa học được xác định bằng sự biến thiên nồng độ của một chất phản ứng hoặc sản phẩm trong một đơn vị thời gian. Các yếu tố ảnh hưởng đến tốc độ phản ứng gồm nồng độ, nhiệt độ, áp suất (với chất khí), diện tích bề mặt tiếp xúc và chất xúc tác.",
                        "Cân bằng hóa học là trạng thái của phản ứng thuận nghịch khi tốc độ phản ứng thuận bằng tốc độ phản ứng nghịch, khi đó nồng độ các chất trong hệ không đổi theo thời gian (nhưng phản ứng vẫn diễn ra ở cấp độ vi mô). Hằng số cân bằng K đặc trưng cho mỗi phản ứng ở một nhiệt độ xác định.",
                        "Nguyên lý chuyển dịch cân bằng Le Chatelier phát biểu rằng khi một hệ đang ở trạng thái cân bằng chịu tác động từ bên ngoài (thay đổi nồng độ, nhiệt độ, áp suất) thì cân bằng sẽ chuyển dịch theo chiều làm giảm tác động đó, giúp dự đoán chiều chuyển dịch cân bằng trong các bài toán và ứng dụng công nghiệp.")
        );
    }

    private static List<Topic> van() {
        String subject = "Ngữ văn";
        return List.of(
                t(subject, "Phân tích bài thơ Tây Tiến của Quang Dũng",
                        "Tây Tiến được Quang Dũng sáng tác năm 1948, khi ông đã rời đơn vị Tây Tiến, mang nỗi nhớ da diết về đồng đội và núi rừng miền Tây Bắc. Bài thơ khắc họa hình ảnh thiên nhiên hùng vĩ, dữ dội qua các địa danh Sài Khao, Mường Lát, Pha Luông, đồng thời gợi lên vẻ đẹp thơ mộng của đêm liên hoan, đêm sương Châu Mộc.",
                        "Hình tượng người lính Tây Tiến hiện lên vừa hào hùng vừa hào hoa: họ chịu đựng gian khổ, bệnh sốt rét rừng, cái chết cận kề (áo bào thay chiếu), nhưng vẫn giữ tâm hồn lãng mạn, mơ mộng hướng về Hà Nội (mắt trừng gửi mộng qua biên giới, đêm mơ Hà Nội dáng kiều thơm).",
                        "Bút pháp lãng mạn kết hợp với chất bi tráng là nét đặc sắc nghệ thuật nổi bật của bài thơ: cái chết của người lính được miêu tả trang trọng qua các từ Hán Việt (áo bào, độc hành) thay vì bi lụy, thể hiện tinh thần lạc quan, coi cái chết nhẹ tựa lông hồng của một thế hệ thanh niên ra trận."),
                t(subject, "Giá trị nhân đạo trong truyện ngắn Vợ nhặt",
                        "Vợ nhặt của Kim Lân lấy bối cảnh nạn đói năm 1945, khắc họa tình cảnh thê thảm của người dân khi cái đói cái chết bủa vây khắp nơi. Giữa hoàn cảnh ấy, Tràng - một người nông dân nghèo xấu xí - bất ngờ nhặt được vợ chỉ qua vài câu nói đùa và bốn bát bánh đúc, một chi tiết vừa hài hước vừa xót xa.",
                        "Giá trị nhân đạo của tác phẩm thể hiện qua niềm tin vào khát vọng sống và hạnh phúc của con người ngay trong nghịch cảnh tăm tối nhất: dù đói khát, các nhân vật vẫn hướng về nhau, chăm sóc nhau, và cùng nhen nhóm hy vọng về một tương lai tươi sáng hơn qua hình ảnh lá cờ đỏ trong tâm trí Tràng ở cuối truyện.",
                        "Kim Lân xây dựng nhân vật bà cụ Tứ với tấm lòng bao dung, thương con sâu sắc, chấp nhận nàng dâu mới trong hoàn cảnh trớ trêu bằng sự cảm thông và lạc quan, qua đó tác giả khẳng định: dù trong hoàn cảnh khốn cùng nhất, con người vẫn không thôi khao khát tổ ấm gia đình và sự sống."),
                t(subject, "Phong cách nghệ thuật của Nguyễn Tuân qua Người lái đò Sông Đà",
                        "Nguyễn Tuân là nhà văn suốt đời đi tìm cái đẹp, và trong tùy bút Người lái đò Sông Đà, ông đã khắc họa sông Đà như một nhân vật có tính cách kép: vừa hung bạo với đá, thác, xoáy nước dữ dội, vừa trữ tình như một áng tóc trữ tình của thiếu nữ Tây Bắc.",
                        "Hình tượng người lái đò được xây dựng như một nghệ sĩ tài hoa trong cuộc chiến với thiên nhiên: ông lái đò vượt qua ba trùng vi thạch trận bằng sự am hiểu quy luật dòng sông, phản xạ nhanh nhạy và bản lĩnh phi thường, thể hiện quan niệm của Nguyễn Tuân về chủ nghĩa anh hùng trong lao động bình dị.",
                        "Nét đặc sắc trong phong cách Nguyễn Tuân là vận dụng tri thức đa ngành (điện ảnh, quân sự, thể thao, hội họa) để miêu tả, cùng vốn từ ngữ phong phú, câu văn co duỗi nhịp nhàng tạo nên chất tài hoa uyên bác đặc trưng, không thể lẫn với bất kỳ cây bút nào khác trong văn học Việt Nam hiện đại."),
                t(subject, "Nghị luận xã hội: Lý tưởng sống của thanh niên",
                        "Lý tưởng sống là mục tiêu cao đẹp mà con người hướng tới, là kim chỉ nam định hướng hành động và lựa chọn trong cuộc sống. Đối với thanh niên - lực lượng nòng cốt của xã hội, việc xác định lý tưởng sống đúng đắn có ý nghĩa quyết định đến sự phát triển của bản thân và đóng góp cho cộng đồng.",
                        "Trong bối cảnh hội nhập và chuyển đổi số hiện nay, lý tưởng sống của thanh niên cần gắn liền với tinh thần học tập suốt đời, dám nghĩ dám làm, dám chịu trách nhiệm, đồng thời không quên trách nhiệm với gia đình, cộng đồng và đất nước, tránh lối sống thực dụng, hưởng thụ đơn thuần.",
                        "Để nuôi dưỡng lý tưởng sống đẹp, mỗi thanh niên cần rèn luyện bản lĩnh, trau dồi tri thức và đạo đức, đồng thời cần môi trường giáo dục, gia đình và xã hội định hướng đúng đắn, tạo điều kiện để thế hệ trẻ phát huy năng lực, cống hiến cho sự phát triển chung của đất nước."),
                t(subject, "Đặc trưng thể loại kí qua Ai đã đặt tên cho dòng sông?",
                        "Ai đã đặt tên cho dòng sông? của Hoàng Phủ Ngọc Tường là bút kí kết hợp nhuần nhuyễn giữa chất trữ tình và chất trí tuệ, khắc họa vẻ đẹp của sông Hương từ nhiều góc nhìn: địa lý, lịch sử, văn hóa và thi ca, thể hiện tình yêu tha thiết của tác giả với xứ Huế.",
                        "Sông Hương được nhân hóa như một người con gái mang nhiều tính cách qua từng chặng hành trình: dữ dội và mãnh liệt ở thượng nguồn, dịu dàng và trầm mặc khi qua kinh thành Huế, rồi lưu luyến chia tay trước khi đổ ra biển, ẩn dụ cho hành trình tìm kiếm và trở về của con người.",
                        "Thể kí cho phép Hoàng Phủ Ngọc Tường tự do kết hợp yếu tố miêu tả, biểu cảm, nghị luận và khảo cứu, thể hiện vốn kiến thức uyên bác qua các liên tưởng về sông Seine, sông Danube, đồng thời bộc lộ cái tôi trữ tình tài hoa, giàu chất thơ đặc trưng của tác giả."),
                t(subject, "Bi kịch nhân vật Chí Phèo trong tác phẩm cùng tên",
                        "Chí Phèo của Nam Cao khắc họa bi kịch của một người nông dân lương thiện bị xã hội thực dân phong kiến tha hóa cả nhân hình lẫn nhân tính, từ anh canh điền hiền lành trở thành con quỷ dữ của làng Vũ Đại sau khi bị Bá Kiến đẩy vào tù oan.",
                        "Bi kịch lớn nhất của Chí Phèo là bi kịch bị cự tuyệt quyền làm người: sau cuộc gặp gỡ với Thị Nở, khát vọng hoàn lương trỗi dậy mãnh liệt trong Chí, nhưng định kiến xã hội qua lời bà cô Thị Nở đã dập tắt hy vọng đó, đẩy Chí vào bi kịch cùng đường không lối thoát.",
                        "Cái chết của Chí Phèo bên xác Bá Kiến là hành động phản kháng tuyệt vọng cuối cùng, thể hiện khát khao được sống lương thiện đến giây phút cuối, đồng thời tố cáo mạnh mẽ xã hội thực dân phong kiến đã cướp đi quyền làm người của những người nông dân lương thiện."),
                t(subject, "Hình tượng người lính trong thơ ca kháng chiến",
                        "Hình tượng người lính là đề tài xuyên suốt trong thơ ca kháng chiến chống Pháp và chống Mỹ, được khắc họa qua nhiều tác phẩm tiêu biểu như Đồng chí (Chính Hữu), Tây Tiến (Quang Dũng), Bài thơ về tiểu đội xe không kính (Phạm Tiến Duật), mỗi tác phẩm mang một sắc thái riêng nhưng đều ngợi ca tinh thần yêu nước.",
                        "Trong Đồng chí, người lính hiện lên chân thực, giản dị với tình đồng đội gắn bó xuất phát từ sự tương đồng về hoàn cảnh xuất thân nông dân nghèo khó; còn trong Bài thơ về tiểu đội xe không kính, hình ảnh người lính lái xe Trường Sơn hiện lên trẻ trung, ngang tàng, lạc quan giữa mưa bom bão đạn.",
                        "Điểm chung xuyên suốt các tác phẩm là tinh thần lạc quan cách mạng, ý chí chiến đấu kiên cường vì độc lập dân tộc, đồng thời vẫn giữ được vẻ đẹp tâm hồn lãng mạn, tình cảm đồng đội thắm thiết - tạo nên một tượng đài văn học về người lính cụ Hồ trong hai cuộc kháng chiến.")
        );
    }

    private static List<Topic> anh() {
        String subject = "Tiếng Anh";
        return List.of(
                t(subject, "Present Perfect vs Past Simple",
                        "The Present Perfect tense (have/has + past participle) is used to talk about actions that happened at an unspecified time in the past and are still relevant to the present, or actions that started in the past and continue now. For example: 'I have lived in Hanoi for ten years' emphasizes the connection to the present.",
                        "The Past Simple tense is used to talk about a completed action at a specific, definite time in the past, often with time markers such as yesterday, last week, or in 2020. For example: 'I visited Hue last summer' clearly states when the action happened and treats it as finished.",
                        "A common mistake among learners is mixing these two tenses: Present Perfect should never be used with a specific past time expression (never say 'I have visited Hue last summer'), while Past Simple cannot be used to express an action with unfinished relevance to the present. Mastering the difference is essential for accurate written and spoken English."),
                t(subject, "Conditional Sentences (Types 1-3)",
                        "The First Conditional (if + present simple, will + infinitive) is used for real and possible future situations, such as 'If it rains tomorrow, we will cancel the trip.' It expresses a realistic condition and its likely result in the future.",
                        "The Second Conditional (if + past simple, would + infinitive) is used for hypothetical or unlikely present/future situations, such as 'If I won the lottery, I would travel around the world.' This structure describes an imagined situation that is contrary to the present reality.",
                        "The Third Conditional (if + past perfect, would have + past participle) is used to talk about hypothetical situations in the past that did not happen, along with their imagined results, such as 'If she had studied harder, she would have passed the exam.' It often expresses regret about a past action."),
                t(subject, "Passive Voice in Academic Writing",
                        "The passive voice is formed with a form of 'to be' plus the past participle of the main verb, shifting the focus from the doer of an action to the action itself or the receiver of the action. It is widely used in academic and scientific writing where the process or result matters more than who performed it.",
                        "For example, instead of writing 'Researchers conducted the experiment in a controlled laboratory,' academic style often prefers 'The experiment was conducted in a controlled laboratory,' which sounds more objective and formal, a quality highly valued in scientific reports and research papers.",
                        "However, overusing the passive voice can make writing vague or difficult to read, especially when the agent is important information. Good academic writers balance active and passive structures, using passive voice strategically to maintain objectivity while keeping sentences clear and direct."),
                t(subject, "Reported Speech",
                        "Reported speech (indirect speech) is used to report what someone else said without quoting their exact words. When the reporting verb is in the past tense, the tense of the reported clause usually shifts back one step, known as backshift: present simple becomes past simple, present perfect becomes past perfect, and so on.",
                        "Besides tense changes, pronouns and time/place expressions must also be adjusted to match the perspective of the reporter: 'today' becomes 'that day', 'tomorrow' becomes 'the next day', and 'here' becomes 'there', reflecting the shift in context between the original speaker and the reporter.",
                        "Reported questions follow statement word order (not question word order) and do not use question marks: 'Where do you live?' becomes 'She asked where I lived.' Reported commands use an infinitive structure: 'Close the door' becomes 'He told me to close the door.'"),
                t(subject, "Relative Clauses",
                        "Relative clauses provide additional information about a noun in the main clause, introduced by relative pronouns such as who, which, that, whose, and whom. Defining relative clauses give essential information to identify the noun and are not separated by commas, for example: 'The book that I borrowed is fascinating.'",
                        "Non-defining relative clauses provide extra, non-essential information and are always separated by commas; 'that' cannot be used in this type of clause. For example: 'My brother, who lives in Da Nang, is a doctor,' where the relative clause simply adds extra detail about a brother already identified.",
                        "Relative pronouns can sometimes be omitted when they function as the object of the relative clause (contact clauses), for example 'The film (that) we watched last night was amazing.' However, the relative pronoun cannot be omitted when it acts as the subject of the relative clause."),
                t(subject, "Writing an Argumentative Essay",
                        "An argumentative essay presents a clear position on a debatable topic and supports it with logical reasoning and credible evidence. A strong introduction states the thesis clearly, giving the reader immediate insight into the writer's stance on the issue being discussed.",
                        "Each body paragraph should focus on one main argument, supported by specific evidence such as statistics, expert opinions, or real-life examples, and should also acknowledge and refute counterarguments to strengthen the overall persuasiveness of the essay.",
                        "A compelling conclusion restates the thesis in new words, summarizes the key arguments, and may end with a call to action or a broader reflection on the significance of the issue, leaving the reader with a strong final impression of the writer's position."),
                t(subject, "Common Phrasal Verbs for IELTS Speaking",
                        "Phrasal verbs are combinations of a verb and one or more particles (prepositions or adverbs) that create a meaning different from the original verb, such as 'give up' (to stop trying) or 'look forward to' (to feel excited about something in the future). Using them naturally can significantly boost a speaker's fluency score.",
                        "In IELTS Speaking, common useful phrasal verbs include 'end up' (to finally be in a situation), 'come across' (to find by chance), 'put off' (to postpone), and 'get along with' (to have a good relationship with someone), all of which sound more natural than their formal single-word equivalents.",
                        "To use phrasal verbs effectively, candidates should practice them in context rather than memorizing lists in isolation, ensuring correct word order especially with separable phrasal verbs (e.g., 'turn it down' not 'turn down it'), and should avoid overusing very informal ones in a formal speaking context.")
        );
    }

    private static List<Topic> su() {
        String subject = "Lịch sử";
        return List.of(
                t(subject, "Cách mạng tháng Tám năm 1945",
                        "Cách mạng tháng Tám năm 1945 nổ ra trong bối cảnh phát xít Nhật đầu hàng Đồng minh, tạo ra thời cơ ngàn năm có một cho cách mạng Việt Nam. Đảng Cộng sản Đông Dương và Mặt trận Việt Minh đã nhanh chóng phát động tổng khởi nghĩa giành chính quyền trên cả nước từ ngày 14 đến 28 tháng 8 năm 1945.",
                        "Khởi nghĩa giành chính quyền diễn ra nhanh chóng và ít đổ máu tại các đô thị lớn như Hà Nội (19/8), Huế (23/8) và Sài Gòn (25/8), nhờ sự chuẩn bị lực lượng chính trị và vũ trang chu đáo trong suốt giai đoạn 1941-1945, cùng nghệ thuật chớp thời cơ chính xác của Đảng.",
                        "Ngày 2 tháng 9 năm 1945, tại quảng trường Ba Đình, Chủ tịch Hồ Chí Minh đọc bản Tuyên ngôn Độc lập, khai sinh nước Việt Nam Dân chủ Cộng hòa, chấm dứt hơn 80 năm đô hộ của thực dân Pháp và chế độ phong kiến, mở ra kỷ nguyên độc lập, tự do cho dân tộc Việt Nam."),
                t(subject, "Chiến dịch Điện Biên Phủ 1954",
                        "Điện Biên Phủ là tập đoàn cứ điểm mạnh nhất của Pháp ở Đông Dương, được xây dựng nhằm thu hút và tiêu diệt chủ lực Việt Minh. Bộ Chỉ huy chiến dịch do Đại tướng Võ Nguyên Giáp đứng đầu đã quyết định thay đổi phương châm tác chiến từ 'đánh nhanh, thắng nhanh' sang 'đánh chắc, tiến chắc' để đảm bảo chắc thắng.",
                        "Chiến dịch diễn ra qua ba đợt tấn công từ ngày 13 tháng 3 đến ngày 7 tháng 5 năm 1954, lần lượt tiêu diệt các cụm cứ điểm phía Bắc, sau đó là các cứ điểm phía Đông và cuối cùng là khu trung tâm Mường Thanh, buộc tướng De Castries cùng toàn bộ Bộ Chỉ huy Pháp phải đầu hàng.",
                        "Chiến thắng Điện Biên Phủ là chiến thắng quân sự lớn nhất trong cuộc kháng chiến chống Pháp, trực tiếp dẫn đến việc ký kết Hiệp định Genève năm 1954, chấm dứt chiến tranh, lập lại hòa bình ở Đông Dương và công nhận độc lập, chủ quyền của Việt Nam, Lào, Campuchia."),
                t(subject, "Phong trào Cần Vương cuối thế kỷ XIX",
                        "Sau khi kinh thành Huế thất thủ năm 1885, vua Hàm Nghi xuất bôn ra vùng núi Quảng Trị và ban chiếu Cần Vương, kêu gọi văn thân, sĩ phu và nhân dân cả nước đứng lên giúp vua cứu nước, mở đầu phong trào đấu tranh vũ trang chống Pháp cuối thế kỷ XIX.",
                        "Phong trào Cần Vương phát triển qua hai giai đoạn: giai đoạn 1885-1888 có sự lãnh đạo trực tiếp của vua Hàm Nghi và Tôn Thất Thuyết; giai đoạn 1888-1896 tiếp tục dưới sự lãnh đạo của các văn thân, sĩ phu dù nhà vua đã bị bắt, tiêu biểu là các cuộc khởi nghĩa Ba Đình, Bãi Sậy và Hương Khê.",
                        "Khởi nghĩa Hương Khê do Phan Đình Phùng lãnh đạo là cuộc khởi nghĩa tiêu biểu nhất, có quy mô lớn và tổ chức chặt chẽ nhất trong phong trào Cần Vương, nhưng cuối cùng cũng thất bại năm 1896, đánh dấu sự chấm dứt của phong trào đấu tranh theo ý thức hệ phong kiến."),
                t(subject, "Trật tự thế giới hai cực Ianta",
                        "Trật tự hai cực Ianta được hình thành sau Hội nghị Ianta (tháng 2/1945) giữa ba cường quốc Liên Xô, Mỹ và Anh, phân chia phạm vi ảnh hưởng và khu vực đóng quân trên thế giới sau khi Thế chiến thứ hai kết thúc, đặt nền móng cho trật tự thế giới mới do hai siêu cường Mỹ và Liên Xô đứng đầu.",
                        "Đặc trưng nổi bật của trật tự hai cực Ianta là sự đối đầu gay gắt giữa hai hệ thống chính trị - xã hội đối lập: chủ nghĩa tư bản do Mỹ đứng đầu và chủ nghĩa xã hội do Liên Xô đứng đầu, dẫn đến Chiến tranh Lạnh kéo dài suốt hơn bốn thập kỷ với nhiều cuộc chạy đua vũ trang và xung đột khu vực.",
                        "Trật tự hai cực Ianta sụp đổ vào cuối những năm 1980 - đầu 1990 cùng với sự tan rã của Liên Xô và hệ thống xã hội chủ nghĩa ở Đông Âu, mở ra một giai đoạn mới trong quan hệ quốc tế theo xu hướng đa cực hóa, toàn cầu hóa và hợp tác cùng phát triển."),
                t(subject, "Cuộc kháng chiến chống Mỹ cứu nước 1954-1975",
                        "Sau Hiệp định Genève 1954, đất nước tạm thời chia cắt thành hai miền với vĩ tuyến 17 làm ranh giới quân sự tạm thời. Đế quốc Mỹ từng bước thay chân Pháp, dựng lên chính quyền tay sai ở miền Nam, biến miền Nam Việt Nam thành thuộc địa kiểu mới, châm ngòi cho cuộc kháng chiến chống Mỹ kéo dài 21 năm.",
                        "Quân và dân Việt Nam lần lượt đánh bại các chiến lược chiến tranh của Mỹ: Chiến tranh đơn phương, Chiến tranh đặc biệt, Chiến tranh cục bộ và Việt Nam hóa chiến tranh, với các mốc son như cuộc Tổng tiến công và nổi dậy Tết Mậu Thân 1968 và Điện Biên Phủ trên không cuối năm 1972.",
                        "Chiến dịch Hồ Chí Minh lịch sử (26/4 - 30/4/1975) là trận quyết chiến chiến lược cuối cùng, kết thúc bằng sự kiện xe tăng quân giải phóng tiến vào Dinh Độc Lập trưa ngày 30 tháng 4 năm 1975, giải phóng hoàn toàn miền Nam, thống nhất đất nước sau 21 năm chia cắt."),
                t(subject, "Quá trình toàn cầu hóa cuối thế kỷ XX",
                        "Toàn cầu hóa là xu thế khách quan, là kết quả tất yếu của quá trình phát triển mạnh mẽ của lực lượng sản xuất, dẫn đến sự gia tăng mạnh mẽ các mối liên hệ, sự phụ thuộc lẫn nhau giữa các quốc gia, dân tộc trên phạm vi toàn thế giới, đặc biệt tăng tốc từ những năm 1980-1990.",
                        "Biểu hiện của toàn cầu hóa bao gồm sự phát triển nhanh chóng của thương mại quốc tế, sự phát triển và tác động to lớn của các công ty xuyên quốc gia, sự sáp nhập và hợp nhất các công ty thành những tập đoàn lớn, và sự ra đời của các tổ chức liên kết kinh tế, thương mại, tài chính quốc tế và khu vực.",
                        "Toàn cầu hóa vừa là thời cơ vừa là thách thức đối với các quốc gia đang phát triển như Việt Nam: mở ra cơ hội thu hút vốn, công nghệ và kinh nghiệm quản lý tiên tiến, nhưng cũng đặt ra thách thức về cạnh tranh gay gắt, nguy cơ tụt hậu và đánh mất bản sắc văn hóa dân tộc nếu không có chiến lược hội nhập phù hợp."),
                t(subject, "Công cuộc Đổi mới ở Việt Nam từ 1986",
                        "Đại hội Đảng lần thứ VI (tháng 12/1986) đã đề ra đường lối Đổi mới toàn diện đất nước, trọng tâm là đổi mới kinh tế, chuyển từ nền kinh tế kế hoạch hóa tập trung, quan liêu, bao cấp sang nền kinh tế hàng hóa nhiều thành phần vận hành theo cơ chế thị trường có sự quản lý của Nhà nước.",
                        "Trong lĩnh vực kinh tế, công cuộc Đổi mới thực hiện xóa bỏ cơ chế bao cấp, phát triển kinh tế nhiều thành phần, khuyến khích các thành phần kinh tế tư nhân, thu hút đầu tư nước ngoài và mở rộng quan hệ kinh tế đối ngoại, tạo nên những chuyển biến tích cực rõ rệt về đời sống nhân dân từ cuối những năm 1980.",
                        "Sau gần 40 năm Đổi mới, Việt Nam đã đạt được những thành tựu to lớn, có ý nghĩa lịch sử: thoát khỏi khủng hoảng kinh tế - xã hội, trở thành nước đang phát triển có thu nhập trung bình, hội nhập sâu rộng vào nền kinh tế thế giới, khẳng định vị thế ngày càng cao trên trường quốc tế.")
        );
    }

    private static List<Topic> dia() {
        String subject = "Địa lý";
        return List.of(
                t(subject, "Đặc điểm địa hình Việt Nam",
                        "Địa hình Việt Nam mang tính chất nhiệt đới ẩm gió mùa và chịu tác động mạnh mẽ của con người, trong đó đồi núi chiếm tới ba phần tư diện tích lãnh thổ nhưng chủ yếu là đồi núi thấp, còn đồng bằng chỉ chiếm một phần tư diện tích nhưng lại là nơi tập trung dân cư đông đúc nhất.",
                        "Địa hình nước ta được chia thành các khu vực chính: vùng núi Đông Bắc, Tây Bắc, Trường Sơn Bắc và Trường Sơn Nam, cùng hai đồng bằng châu thổ lớn là đồng bằng sông Hồng và đồng bằng sông Cửu Long, được bồi đắp bởi phù sa của hai hệ thống sông lớn, rất thuận lợi cho phát triển nông nghiệp.",
                        "Địa hình bờ biển và thềm lục địa cũng là một bộ phận quan trọng, với đường bờ biển dài hơn 3.260 km, tạo điều kiện thuận lợi để phát triển kinh tế biển như đánh bắt, nuôi trồng thủy hải sản, giao thông vận tải biển, du lịch biển và khai thác dầu khí trên thềm lục địa."),
                t(subject, "Khí hậu nhiệt đới ẩm gió mùa",
                        "Khí hậu Việt Nam mang tính chất nhiệt đới ẩm gió mùa với nền nhiệt độ trung bình năm cao trên 20°C ở hầu hết cả nước, tổng lượng bức xạ lớn, cân bằng bức xạ dương quanh năm, độ ẩm không khí cao trên 80% và lượng mưa lớn, trung bình từ 1.500 đến 2.000 mm mỗi năm.",
                        "Gió mùa là nhân tố chi phối mạnh mẽ khí hậu nước ta, gồm gió mùa mùa đông (gió mùa Đông Bắc) thổi từ áp cao Xibia mang lại thời tiết lạnh khô vào đầu mùa và lạnh ẩm vào cuối mùa cho miền Bắc, và gió mùa mùa hạ mang lại thời tiết nóng ẩm, mưa nhiều cho cả nước.",
                        "Khí hậu Việt Nam có sự phân hóa đa dạng theo chiều Bắc - Nam (miền Bắc có mùa đông lạnh, miền Nam nóng quanh năm), theo độ cao (vùng núi cao có khí hậu mát mẻ, cận nhiệt hoặc ôn đới) và theo mùa, tạo nên tính đa dạng sinh học và cơ cấu mùa vụ nông nghiệp phong phú."),
                t(subject, "Dân số và phân bố dân cư Việt Nam",
                        "Việt Nam là quốc gia đông dân, với quy mô dân số hơn 100 triệu người, đứng thứ ba khu vực Đông Nam Á. Dân số nước ta có tốc độ gia tăng đã chậm lại nhờ thực hiện tốt chính sách dân số - kế hoạch hóa gia đình, nhưng vẫn tăng thêm mỗi năm một số lượng dân số đáng kể do quy mô dân số lớn.",
                        "Phân bố dân cư nước ta rất không đều: dân cư tập trung đông đúc ở các vùng đồng bằng, ven biển và các đô thị lớn (mật độ có nơi trên 2.000 người/km²), trong khi vùng trung du, miền núi có mật độ dân số thấp hơn nhiều dù chiếm phần lớn diện tích lãnh thổ.",
                        "Cơ cấu dân số Việt Nam đang trong thời kỳ 'cơ cấu dân số vàng' với tỉ lệ dân số trong độ tuổi lao động cao, tạo ra nguồn lực lao động dồi dào cho phát triển kinh tế, nhưng đồng thời cũng đặt ra thách thức về giải quyết việc làm và chuẩn bị cho quá trình già hóa dân số trong tương lai."),
                t(subject, "Cơ cấu ngành công nghiệp Việt Nam",
                        "Cơ cấu ngành công nghiệp Việt Nam khá đa dạng, bao gồm công nghiệp khai thác, công nghiệp chế biến, chế tạo và công nghiệp sản xuất, phân phối điện, khí đốt, nước. Trong đó, công nghiệp chế biến, chế tạo chiếm tỉ trọng cao nhất và có xu hướng ngày càng tăng trong cơ cấu giá trị sản xuất công nghiệp.",
                        "Công nghiệp Việt Nam đang chuyển dịch theo hướng công nghiệp hóa, hiện đại hóa, tăng tỉ trọng các ngành công nghiệp công nghệ cao như điện tử, công nghệ thông tin, đồng thời giảm dần tỉ trọng các ngành công nghiệp khai thác tài nguyên thô để hướng tới phát triển bền vững.",
                        "Các trung tâm công nghiệp lớn của Việt Nam tập trung chủ yếu ở vùng Đông Nam Bộ (TP. Hồ Chí Minh, Bình Dương, Đồng Nai), đồng bằng sông Hồng (Hà Nội, Hải Phòng, Bắc Ninh) nhờ lợi thế về vị trí địa lý, cơ sở hạ tầng, nguồn lao động và khả năng thu hút vốn đầu tư nước ngoài."),
                t(subject, "Vùng kinh tế trọng điểm phía Nam",
                        "Vùng kinh tế trọng điểm phía Nam gồm TP. Hồ Chí Minh và các tỉnh lân cận như Bình Dương, Đồng Nai, Bà Rịa - Vũng Tàu, Tây Ninh, Long An, là vùng kinh tế năng động và có đóng góp lớn nhất vào GDP cả nước, đóng vai trò đầu tàu tăng trưởng kinh tế của Việt Nam.",
                        "Vùng có nhiều lợi thế phát triển: vị trí địa lý thuận lợi cho giao thương quốc tế, hệ thống cảng biển nước sâu (Cái Mép - Thị Vải), nguồn lao động dồi dào có tay nghề, cơ sở hạ tầng phát triển và là nơi tập trung nhiều khu công nghiệp, khu chế xuất lớn nhất cả nước.",
                        "Định hướng phát triển của vùng là đẩy mạnh các ngành công nghiệp công nghệ cao, dịch vụ chất lượng cao (tài chính, ngân hàng, logistics), đồng thời giải quyết các thách thức về hạ tầng giao thông quá tải, ô nhiễm môi trường và chênh lệch phát triển giữa các địa phương trong vùng."),
                t(subject, "Biến đổi khí hậu toàn cầu",
                        "Biến đổi khí hậu toàn cầu là sự thay đổi của hệ thống khí hậu Trái Đất, biểu hiện qua sự gia tăng nhiệt độ trung bình toàn cầu, thay đổi lượng mưa, nước biển dâng và gia tăng tần suất, cường độ các hiện tượng thời tiết cực đoan như bão, hạn hán, lũ lụt.",
                        "Nguyên nhân chủ yếu của biến đổi khí hậu hiện nay là do hoạt động của con người, đặc biệt là việc đốt nhiên liệu hóa thạch (than, dầu mỏ, khí đốt) làm gia tăng nồng độ khí nhà kính (CO2, CH4) trong khí quyển, gây ra hiệu ứng nhà kính và làm Trái Đất nóng lên.",
                        "Việt Nam được đánh giá là một trong những quốc gia chịu ảnh hưởng nặng nề nhất của biến đổi khí hậu, đặc biệt là đồng bằng sông Cửu Long đối mặt với nguy cơ ngập lụt, xâm nhập mặn nghiêm trọng, đòi hỏi các giải pháp thích ứng và giảm nhẹ đồng bộ ở cả cấp quốc gia và địa phương."),
                t(subject, "Tài nguyên và bảo vệ môi trường biển",
                        "Vùng biển Việt Nam có nguồn tài nguyên phong phú và đa dạng: tài nguyên sinh vật biển với nhiều loài hải sản có giá trị kinh tế cao, tài nguyên khoáng sản với trữ lượng dầu khí lớn ở thềm lục địa, cùng tiềm năng phát triển du lịch biển đảo và năng lượng tái tạo từ gió, sóng biển.",
                        "Việc khai thác tài nguyên biển trong những năm qua đã đóng góp quan trọng vào phát triển kinh tế đất nước thông qua các ngành đánh bắt, nuôi trồng thủy sản, khai thác dầu khí, vận tải biển và du lịch, đồng thời khẳng định chủ quyền biển đảo thiêng liêng của Tổ quốc.",
                        "Tuy nhiên, môi trường biển đang đứng trước nhiều thách thức như ô nhiễm từ rác thải nhựa, khai thác quá mức làm suy giảm nguồn lợi hải sản và biến đổi khí hậu, đòi hỏi phải kết hợp hài hòa giữa khai thác kinh tế biển với bảo vệ môi trường và phát triển bền vững tài nguyên biển.")
        );
    }

    private static List<Topic> sinh() {
        String subject = "Sinh học";
        return List.of(
                t(subject, "Cơ chế di truyền và biến dị",
                        "Cơ chế di truyền ở cấp độ phân tử diễn ra qua ba quá trình chính: nhân đôi ADN (truyền thông tin di truyền qua các thế hệ tế bào), phiên mã (tổng hợp ARN thông tin từ mạch khuôn ADN) và dịch mã (tổng hợp chuỗi polypeptide dựa trên trình tự codon của mARN tại ribosome).",
                        "Đột biến gen là những biến đổi trong cấu trúc của gen, thường liên quan đến một hoặc một số cặp nucleotide, gồm các dạng mất, thêm, thay thế cặp nucleotide. Đột biến gen là nguồn nguyên liệu sơ cấp quan trọng cho quá trình tiến hóa và chọn giống.",
                        "Đột biến nhiễm sắc thể gồm đột biến cấu trúc (mất đoạn, lặp đoạn, đảo đoạn, chuyển đoạn) và đột biến số lượng nhiễm sắc thể (thể dị bội và thể đa bội). Các dạng đột biến này có thể gây ra hậu quả nghiêm trọng nhưng cũng là nguồn nguyên liệu quý trong chọn giống cây trồng, vật nuôi."),
                t(subject, "Quy luật di truyền Mendel",
                        "Quy luật phân li của Mendel phát biểu rằng mỗi tính trạng do một cặp nhân tố di truyền (alen) quy định, khi giảm phân tạo giao tử, mỗi giao tử chỉ chứa một trong hai alen của cặp, và các alen này tổ hợp lại một cách ngẫu nhiên khi thụ tinh, tạo nên các kiểu tổ hợp khác nhau ở đời con.",
                        "Quy luật phân li độc lập phát biểu rằng các cặp nhân tố di truyền quy định các tính trạng khác nhau phân li độc lập với nhau trong quá trình hình thành giao tử, với điều kiện các gen quy định các tính trạng nằm trên các cặp nhiễm sắc thể tương đồng khác nhau.",
                        "Từ hai quy luật cơ bản trên, Mendel đã giải thích được tỉ lệ phân li kiểu hình 3:1 ở lai một cặp tính trạng và tỉ lệ 9:3:3:1 ở lai hai cặp tính trạng trong các thí nghiệm lai giống đậu Hà Lan, đặt nền móng cho di truyền học hiện đại."),
                t(subject, "Quang hợp ở thực vật",
                        "Quang hợp là quá trình lá cây sử dụng năng lượng ánh sáng mặt trời, nước và khí CO2 để tổng hợp chất hữu cơ (glucose) và giải phóng khí O2, diễn ra chủ yếu ở lục lạp của tế bào lá nhờ sắc tố diệp lục hấp thụ ánh sáng.",
                        "Quá trình quang hợp gồm hai pha: pha sáng diễn ra ở màng thylakoid, chuyển năng lượng ánh sáng thành năng lượng hóa học dự trữ trong ATP và NADPH, đồng thời giải phóng O2 từ quá trình quang phân li nước; pha tối (chu trình Calvin) diễn ra ở chất nền lục lạp, sử dụng ATP và NADPH để cố định CO2 thành chất hữu cơ.",
                        "Quang hợp có vai trò đặc biệt quan trọng: là nguồn cung cấp chất hữu cơ và năng lượng cho hầu hết sự sống trên Trái Đất, đồng thời điều hòa thành phần khí quyển thông qua việc hấp thụ CO2 và giải phóng O2, góp phần duy trì cân bằng sinh thái toàn cầu."),
                t(subject, "Hô hấp tế bào",
                        "Hô hấp tế bào là quá trình phân giải các hợp chất hữu cơ (chủ yếu là glucose) thành CO2 và nước, đồng thời giải phóng năng lượng dưới dạng ATP để cung cấp cho các hoạt động sống của tế bào. Đây là quá trình oxi hóa - khử diễn ra trong ti thể.",
                        "Hô hấp tế bào hiếu khí gồm ba giai đoạn chính: đường phân diễn ra ở tế bào chất, chu trình Krebs và chuỗi truyền electron diễn ra ở ti thể, trong đó chuỗi truyền electron tạo ra phần lớn ATP thông qua quá trình photphoril hóa oxi hóa.",
                        "So với hô hấp hiếu khí (cần oxi, hiệu suất cao), lên men là hình thức hô hấp kỵ khí (không cần oxi, hiệu suất thấp hơn nhiều) xảy ra ở một số vi sinh vật và trong tế bào cơ khi thiếu oxi tạm thời, tạo ra sản phẩm là acid lactic hoặc ethanol tùy loại sinh vật."),
                t(subject, "Học thuyết tiến hóa Darwin",
                        "Học thuyết tiến hóa của Charles Darwin, công bố năm 1859, đề xuất rằng các loài sinh vật tiến hóa qua thời gian dài nhờ cơ chế chọn lọc tự nhiên: những cá thể có biến dị thích nghi tốt hơn với môi trường sống sẽ có khả năng sống sót và sinh sản cao hơn, truyền lại đặc điểm đó cho thế hệ sau.",
                        "Theo Darwin, biến dị cá thể là nguyên liệu cho chọn lọc tự nhiên, xuất hiện một cách ngẫu nhiên vô hướng ở các cá thể trong quần thể; chọn lọc tự nhiên tác động trực tiếp lên kiểu hình, qua đó gián tiếp làm biến đổi tần số kiểu gen của quần thể qua nhiều thế hệ.",
                        "Học thuyết tiến hóa hiện đại đã bổ sung, hoàn thiện học thuyết Darwin bằng các phát hiện của di truyền học: đột biến và biến dị tổ hợp là nguồn nguyên liệu chủ yếu, chọn lọc tự nhiên là nhân tố định hướng quá trình tiến hóa, hình thành nên sự đa dạng và thích nghi của sinh giới ngày nay."),
                t(subject, "Hệ sinh thái và chuỗi thức ăn",
                        "Hệ sinh thái là hệ thống bao gồm quần xã sinh vật và môi trường vô sinh của quần xã, trong đó các sinh vật tương tác với nhau và với môi trường tạo nên một thể thống nhất tương đối ổn định, thực hiện chu trình vật chất và dòng năng lượng.",
                        "Chuỗi thức ăn là một dãy các loài sinh vật có quan hệ dinh dưỡng với nhau, mỗi loài là một mắt xích, vừa là sinh vật tiêu thụ mắt xích phía trước vừa là sinh vật bị mắt xích phía sau tiêu thụ, bắt đầu từ sinh vật sản xuất (thực vật) qua các bậc sinh vật tiêu thụ đến sinh vật phân giải.",
                        "Trong hệ sinh thái, năng lượng chỉ truyền theo một chiều từ sinh vật sản xuất qua các bậc dinh dưỡng và mất dần dưới dạng nhiệt ở mỗi bậc (hiệu suất sinh thái thường chỉ khoảng 10%), trong khi vật chất được tuần hoàn qua các chu trình sinh địa hóa như chu trình carbon, chu trình nitơ."),
                t(subject, "Công nghệ gen và ứng dụng",
                        "Công nghệ gen là quy trình công nghệ dùng để tạo ra những tế bào hoặc sinh vật có gen bị biến đổi hoặc có thêm gen mới, dựa trên kỹ thuật cơ bản là kỹ thuật chuyển gen: chuyển một đoạn ADN từ tế bào cho sang tế bào nhận nhờ thể truyền (plasmid, virus).",
                        "Quy trình chuyển gen gồm ba bước chính: tạo ADN tái tổ hợp (cắt gen cần chuyển bằng enzyme giới hạn và gắn vào thể truyền nhờ enzyme nối), đưa ADN tái tổ hợp vào tế bào nhận, và chọn lọc các dòng tế bào có chứa ADN tái tổ hợp mong muốn.",
                        "Công nghệ gen có nhiều ứng dụng quan trọng: tạo ra các chủng vi sinh vật sản xuất số lượng lớn sản phẩm sinh học (insulin, hormone tăng trưởng), tạo giống cây trồng biến đổi gen kháng sâu bệnh, và trong y học phục vụ chẩn đoán, điều trị bệnh di truyền và liệu pháp gen.")
        );
    }

    private static List<Topic> gdcd() {
        String subject = "GDCD";
        return List.of(
                t(subject, "Quyền và nghĩa vụ cơ bản của công dân",
                        "Quyền công dân là những lợi ích mà pháp luật ghi nhận và bảo vệ cho công dân trong các lĩnh vực chính trị, dân sự, kinh tế, văn hóa, xã hội, bao gồm quyền bầu cử, ứng cử, quyền tự do ngôn luận, quyền học tập, quyền sở hữu tài sản hợp pháp.",
                        "Song song với quyền, công dân cũng có các nghĩa vụ cơ bản như nghĩa vụ tuân theo Hiến pháp và pháp luật, nghĩa vụ bảo vệ Tổ quốc, nghĩa vụ nộp thuế, nghĩa vụ học tập và lao động, thể hiện mối quan hệ hai chiều gắn bó chặt chẽ giữa Nhà nước và công dân.",
                        "Việc thực hiện đầy đủ quyền và nghĩa vụ công dân không chỉ bảo đảm lợi ích cá nhân mà còn góp phần xây dựng xã hội dân chủ, công bằng, văn minh; mỗi công dân cần hiểu rõ quyền của mình để tự bảo vệ và đồng thời tự giác thực hiện nghĩa vụ đối với cộng đồng, đất nước."),
                t(subject, "Pháp luật và đời sống",
                        "Pháp luật là hệ thống các quy tắc xử sự chung do Nhà nước ban hành và bảo đảm thực hiện bằng quyền lực nhà nước, có tính quy phạm phổ biến, tính quyền lực bắt buộc chung và tính xác định chặt chẽ về hình thức, thể hiện ý chí của giai cấp cầm quyền.",
                        "Pháp luật có vai trò là phương tiện để Nhà nước quản lý xã hội một cách hiệu quả nhất, đồng thời là phương tiện để công dân thực hiện và bảo vệ quyền, lợi ích hợp pháp của mình thông qua các quy định cụ thể trong các bộ luật, luật chuyên ngành.",
                        "Mối quan hệ giữa pháp luật với đạo đức rất mật thiết: pháp luật là phương tiện đặc thù để thể hiện và bảo vệ các giá trị đạo đức, nhiều quy tắc đạo đức phù hợp với sự phát triển xã hội được Nhà nước thể chế hóa thành các quy phạm pháp luật cụ thể, có tính cưỡng chế thi hành."),
                t(subject, "Quyền bình đẳng giữa các dân tộc, tôn giáo",
                        "Quyền bình đẳng giữa các dân tộc là các dân tộc trong một quốc gia không phân biệt đa số hay thiểu số, trình độ phát triển cao hay thấp, đều được Nhà nước và pháp luật tôn trọng, bảo vệ và tạo điều kiện phát triển như nhau về chính trị, kinh tế, văn hóa, xã hội.",
                        "Nhà nước Việt Nam thực hiện nhiều chính sách cụ thể để bảo đảm bình đẳng dân tộc trên thực tế, như chính sách phát triển kinh tế - xã hội vùng đồng bào dân tộc thiểu số và miền núi, chính sách cử tuyển trong giáo dục, chính sách bảo tồn và phát huy bản sắc văn hóa các dân tộc.",
                        "Quyền bình đẳng giữa các tôn giáo thể hiện ở việc các tôn giáo được Nhà nước công nhận đều bình đẳng trước pháp luật, có quyền hoạt động tôn giáo trong khuôn khổ pháp luật, được pháp luật bảo hộ nơi thờ tự, đồng thời các tín đồ tôn giáo bình đẳng về quyền và nghĩa vụ công dân như mọi công dân khác."),
                t(subject, "Trách nhiệm pháp lý của công dân",
                        "Trách nhiệm pháp lý là nghĩa vụ mà các cá nhân, tổ chức phải gánh chịu hậu quả bất lợi từ hành vi vi phạm pháp luật của mình, được áp dụng bởi cơ quan nhà nước có thẩm quyền theo quy định của pháp luật.",
                        "Căn cứ vào tính chất và mức độ vi phạm, trách nhiệm pháp lý được chia thành các loại: trách nhiệm hình sự (áp dụng đối với tội phạm), trách nhiệm hành chính (áp dụng đối với vi phạm hành chính), trách nhiệm dân sự (áp dụng khi vi phạm nghĩa vụ dân sự, hợp đồng) và trách nhiệm kỷ luật.",
                        "Việc quy định trách nhiệm pháp lý nhằm mục đích trừng phạt, giáo dục người vi phạm, đồng thời răn đe, phòng ngừa chung đối với xã hội, góp phần bảo đảm trật tự, kỷ cương xã hội và bảo vệ quyền, lợi ích hợp pháp của các chủ thể khác trong xã hội."),
                t(subject, "Cạnh tranh trong nền kinh tế thị trường",
                        "Cạnh tranh là sự ganh đua giữa các chủ thể kinh tế nhằm giành lấy những điều kiện thuận lợi trong sản xuất, tiêu thụ hàng hóa để thu được lợi ích tối đa cho mình, là một quy luật kinh tế tất yếu của nền kinh tế thị trường.",
                        "Cạnh tranh có tính hai mặt: mặt tích cực thúc đẩy sản xuất phát triển, cải tiến kỹ thuật, nâng cao chất lượng sản phẩm và hạ giá thành, mang lại lợi ích cho người tiêu dùng; mặt tiêu cực nếu không được kiểm soát có thể dẫn đến cạnh tranh không lành mạnh, gây tổn hại đến lợi ích chung của xã hội.",
                        "Nhà nước có vai trò quan trọng trong việc điều tiết cạnh tranh thông qua pháp luật (như Luật Cạnh tranh), nhằm khuyến khích cạnh tranh lành mạnh, ngăn chặn các hành vi độc quyền, cạnh tranh không lành mạnh, bảo vệ quyền lợi hợp pháp của doanh nghiệp và người tiêu dùng."),
                t(subject, "Quyền tự do kinh doanh và nghĩa vụ nộp thuế",
                        "Quyền tự do kinh doanh là quyền của công dân được lựa chọn hình thức tổ chức kinh tế, ngành nghề và quy mô kinh doanh theo quy định của pháp luật, trừ những ngành nghề pháp luật cấm kinh doanh, thể hiện qua Luật Doanh nghiệp và các văn bản pháp luật liên quan.",
                        "Thuế là khoản thu bắt buộc của Nhà nước đối với tổ chức, cá nhân nhằm đáp ứng nhu cầu chi tiêu của Nhà nước vì lợi ích chung, không mang tính hoàn trả trực tiếp, gồm nhiều sắc thuế như thuế thu nhập doanh nghiệp, thuế thu nhập cá nhân, thuế giá trị gia tăng.",
                        "Nộp thuế đầy đủ, đúng hạn vừa là nghĩa vụ pháp lý bắt buộc, vừa thể hiện trách nhiệm của công dân, doanh nghiệp đối với sự phát triển chung của đất nước, vì nguồn thu từ thuế là nguồn lực chủ yếu để Nhà nước đầu tư cho giáo dục, y tế, cơ sở hạ tầng và an sinh xã hội."),
                t(subject, "Bảo vệ Tổ quốc là nghĩa vụ thiêng liêng",
                        "Bảo vệ Tổ quốc là nghĩa vụ thiêng liêng và quyền cao quý của công dân được Hiến pháp quy định, bao gồm xây dựng nền quốc phòng toàn dân, thực hiện nghĩa vụ quân sự, tham gia xây dựng lực lượng vũ trang nhân dân và giữ gìn trật tự, an toàn xã hội.",
                        "Trong tình hình hiện nay, bảo vệ Tổ quốc không chỉ giới hạn ở bảo vệ chủ quyền lãnh thổ mà còn mở rộng sang bảo vệ an ninh chính trị, trật tự an toàn xã hội, bảo vệ nền tảng tư tưởng, đấu tranh phòng chống các âm mưu diễn biến hòa bình, bảo vệ an ninh mạng và an ninh kinh tế.",
                        "Mỗi công dân, đặc biệt là thế hệ trẻ, cần nhận thức rõ trách nhiệm của mình đối với sự nghiệp bảo vệ Tổ quốc thông qua việc học tập tốt, rèn luyện sức khỏe, tham gia nghĩa vụ quân sự khi đến tuổi, đồng thời tích cực tham gia phong trào toàn dân bảo vệ an ninh Tổ quốc tại nơi cư trú.")
        );
    }
}
