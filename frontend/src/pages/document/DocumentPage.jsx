import React, { useState, useEffect } from 'react';
import { documentApi } from '../../api/documentApi';
import UploadModal from '../../components/Document/UploadModal';

const DocumentPage = () => {
    const [documents, setDocuments] = useState([]);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [loading, setLoading] = useState(true);

    // 1. Tự động load dữ liệu khi mở trang
    useEffect(() => {
        loadDocuments();
    }, []);

    const loadDocuments = async () => {
        try {
            setLoading(true);
            const res = await documentApi.getAll();
            setDocuments(res.data);
        } catch (err) {
            console.error("Không lấy được dữ liệu:", err);
        } finally {
            setLoading(false);
        }
    };

    // 2. Hàm xử lý upload
    const handleUpload = async (data) => {
        const formData = new FormData();
        formData.append('title', data.title);
        formData.append('description', data.description);
        formData.append('file', data.file); 

        try {
            const res = await documentApi.upload(formData);
            setDocuments(prevDocs => [res.data, ...prevDocs]);
            setIsModalOpen(false);
            alert("Tải lên tài liệu thành công!");
        } catch (err) {
            console.error("Lỗi khi upload:", err);
            alert("Có lỗi xảy ra khi tải file lên Backend!");
        }
    };

    // 3. Hàm xử lý xóa
    const handleDelete = async (id) => {
        if (window.confirm("Bạn có chắc chắn muốn xóa tài liệu này không?")) {
            try {
                await documentApi.delete(id);
                setDocuments(prevDocs => prevDocs.filter(doc => doc.id !== id));
                alert("Đã xóa tài liệu thành công!");
            } catch (err) {
                console.error("Lỗi khi xóa:", err);
                alert("Không thể xóa tài liệu này!");
            }
        }
    };
const handleDownload = async (id, fileName) => {
    try {
        console.log("1. Bắt đầu gọi API tải file cho ID:", id);
        const response = await documentApi.download(id);
        
        console.log("2. Backend đã trả về dữ liệu. Đang tạo Blob...");
        // Ép kiểu Blob để chắc chắn trình duyệt hiểu đây là file
        const blob = new Blob([response.data], { 
            type: response.headers['content-type'] || 'application/octet-stream' 
        });
        
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        
        // Đặt tên file khi tải về
        const finalFileName = fileName || `document_${id}.pdf`;
        link.setAttribute('download', finalFileName);
        
        console.log("3. Đang kích hoạt lệnh tải cho file:", finalFileName);
        document.body.appendChild(link);
        link.click();
        
        // Dọn dẹp bộ nhớ
        setTimeout(() => {
            link.remove();
            window.URL.revokeObjectURL(url);
            console.log("4. Hoàn tất quy trình tải.");
        }, 100);

    } catch (err) {
        console.error("Lỗi khi tải file:", err);
        alert("Có lỗi xảy ra trong quá trình xử lý file!");
    }
};
    // 4. Phần hiển thị UI
    return (
        <div className="p-10 bg-[#F4F7FE] min-h-screen">
            <div className="flex justify-between items-center mb-10">
                <h1 className="text-3xl font-bold text-[#2B3674]">Tài liệu hệ thống</h1>
                <button 
                    onClick={() => setIsModalOpen(true)}
                    className="bg-[#4318FF] text-white px-8 py-3 rounded-2xl font-bold shadow-lg hover:bg-[#3311CC] transition-all"
                >
                    + Tải lên tệp mới
                </button>
            </div>

            {loading ? (
                <p>Đang tải tài liệu...</p>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
                    {documents.map((doc) => (
                        <div key={doc.id} className="bg-white p-8 rounded-[30px] shadow-sm border border-gray-50 hover:shadow-md transition-shadow">
                            <div className="flex items-center gap-4 mb-4">
                                <div className="w-12 h-12 bg-indigo-50 rounded-2xl flex items-center justify-center text-indigo-500 font-bold">
                                    DOC
                                </div>
                                <div>
                                    <h3 className="font-bold text-[#2B3674] text-xl">{doc.title}</h3>
                                    <p className="text-gray-400 text-xs">ID: #{doc.id}</p>
                                </div>
                            </div>
                            
                            <p className="text-gray-500 text-sm mb-6 line-clamp-2">
                                {doc.description || "Không có mô tả cho tài liệu này."}
                            </p>

                            <div className="flex justify-between items-center pt-4 border-t border-gray-50">
                                <div className="flex gap-2">
                                    <span className="text-[10px] font-bold px-2 py-1 bg-green-50 text-green-500 rounded-lg">
                                        {doc.status}
                                    </span>
                                    <button 
                                        onClick={() => handleDelete(doc.id)}
                                        className="text-red-500 font-bold text-sm hover:underline"
                                    >
                                        Xóa
                                    </button>
                                </div>
                                <button 
    onClick={() => handleDownload(doc.id, doc.fileName)}
    className="text-[#4318FF] font-bold text-sm hover:underline"
>
    Tải về
</button>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            <UploadModal 
                isOpen={isModalOpen} 
                onClose={() => setIsModalOpen(false)} 
                onUploadSuccess={handleUpload} 
            />
        </div>
    );
};

export default DocumentPage;