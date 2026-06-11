import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { documentApi } from '../../api/documentApi';
import { Document, Page, pdfjs } from 'react-pdf';
import 'react-pdf/dist/Page/AnnotationLayer.css';
import 'react-pdf/dist/Page/TextLayer.css';

// cấu hình worker cho react-pdf
pdfjs.GlobalWorkerOptions.workerSrc = `https://unpkg.com/pdfjs-dist@${pdfjs.version}/build/pdf.worker.min.mjs`;

const FILE_BASE = 'http://localhost:8080/uploads/';

const DocumentDetailPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [doc, setDoc] = useState(null);
    const [loading, setLoading] = useState(true);
    const [comment, setComment] = useState('');

    // state cho PDF viewer
    const [numPages, setNumPages] = useState(null);
    const [pageNumber, setPageNumber] = useState(1);
    const [scale, setScale] = useState(1.0);

    useEffect(() => { load(); }, [id]);

    const load = async () => {
        try {
            setLoading(true);
            const res = await documentApi.getById(id);
            setDoc(res.data);
        } catch { setDoc(null); } finally { setLoading(false); }
    };

    const handleRate = async (star) => {
        try { await documentApi.rate(id, star); load(); } catch { alert('Lỗi đánh giá!'); }
    };

    const handleComment = async () => {
        if (!comment.trim()) return;
        try { await documentApi.addComment(id, comment); setComment(''); load(); }
        catch { alert('Lỗi bình luận!'); }
    };

    const handleDownload = async () => {
        try {
            const response = await documentApi.download(id);
            const blob = new Blob([response.data], { type: response.headers['content-type'] || 'application/octet-stream' });
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', doc.fileName || `document_${id}.pdf`);
            document.body.appendChild(link);
            link.click();
            link.remove();
        } catch { alert('Lỗi tải file!'); }
    };

    if (loading) return <div className="p-10 text-gray-400 font-bold">Đang tải...</div>;
    if (!doc) return <div className="p-10 text-red-400 font-bold">Không tìm thấy tài liệu.</div>;

    const fileUrl = doc.fileUrl ? FILE_BASE + doc.fileUrl : null;
    const isPdf = doc.fileType?.includes('pdf');
    const isImage = doc.fileType?.includes('image');

    return (
        <div className="min-h-screen bg-[#F8F9FD] p-6 md:p-10">
            <div className="max-w-4xl mx-auto">
                <button onClick={() => navigate(-1)} className="text-[#DB6700] font-bold text-sm mb-6 hover:underline">← Quay lại</button>

                {/* KHU VỰC XEM TRƯỚC FILE */}
                <div className="bg-white rounded-3xl p-6 shadow-sm border border-gray-100 mb-6">
                    <h3 className="text-lg font-black text-gray-800 mb-4">Xem trước</h3>

                    {isPdf && fileUrl && (
                        <div>
                            {/* Thanh điều khiển */}
                            <div className="flex flex-wrap items-center justify-center gap-3 mb-4 bg-gray-50 p-3 rounded-xl">
                                <button onClick={() => setPageNumber(p => Math.max(1, p - 1))} disabled={pageNumber <= 1}
                                    className="px-3 py-1 bg-white rounded-lg font-bold text-sm disabled:opacity-40">← Trước</button>
                                <span className="text-sm font-bold text-gray-600">Trang {pageNumber} / {numPages || '?'}</span>
                                <button onClick={() => setPageNumber(p => Math.min(numPages, p + 1))} disabled={pageNumber >= numPages}
                                    className="px-3 py-1 bg-white rounded-lg font-bold text-sm disabled:opacity-40">Sau →</button>
                                <span className="mx-2 text-gray-300">|</span>
                                <button onClick={() => setScale(s => Math.max(0.5, s - 0.2))} className="px-3 py-1 bg-white rounded-lg font-bold text-sm">➖</button>
                                <span className="text-sm font-bold text-gray-600">{Math.round(scale * 100)}%</span>
                                <button onClick={() => setScale(s => Math.min(2.5, s + 0.2))} className="px-3 py-1 bg-white rounded-lg font-bold text-sm">➕</button>
                            </div>
                            {/* Vùng hiển thị PDF */}
                            <div className="flex justify-center overflow-auto max-h-[70vh] bg-gray-100 rounded-xl p-4">
                                <Document
                                    file={fileUrl}
                                    onLoadSuccess={({ numPages }) => setNumPages(numPages)}
                                    loading={<div className="py-10 text-gray-400 font-bold">Đang tải PDF...</div>}
                                    error={<div className="py-10 text-red-400 font-bold">Không tải được PDF.</div>}
                                >
                                    <Page pageNumber={pageNumber} scale={scale} />
                                </Document>
                            </div>
                        </div>
                    )}

                    {isImage && fileUrl && (
                        <div className="flex justify-center bg-gray-100 rounded-xl p-4">
                            <img src={fileUrl} alt={doc.title} className="max-h-[70vh] rounded-lg" />
                        </div>
                    )}

                    {!isPdf && !isImage && (
                        <div className="text-center py-10 bg-gray-50 rounded-xl text-gray-400 font-bold">
                            Loại file này không xem trước được. Vui lòng tải về để xem.
                        </div>
                    )}
                </div>

                {/* THÔNG TIN TÀI LIỆU */}
                <div className="bg-white rounded-3xl p-8 shadow-sm border border-gray-100">
                    <div className="flex justify-between items-start mb-4">
                        <h1 className="text-3xl font-black text-gray-800">{doc.title}</h1>
                        <span className={`text-[11px] font-black px-3 py-1 rounded-md ${doc.visibility === 'PUBLIC' ? 'bg-green-100 text-green-600' : 'bg-gray-200 text-gray-500'}`}>
                            {doc.visibility === 'PUBLIC' ? '🌐 PUBLIC' : '🔒 PRIVATE'}
                        </span>
                    </div>

                    {doc.subjectName && (
                        <span className="inline-block text-xs font-black px-3 py-1 rounded-md bg-[#4318FF]/10 text-[#4318FF] mb-3">📚 {doc.subjectName}</span>
                    )}

                    {doc.tags && doc.tags.length > 0 && (
                        <div className="flex flex-wrap gap-2 mb-4">
                            {doc.tags.map(t => (
                                <span key={t} className="text-xs font-bold px-2 py-0.5 rounded-md bg-gray-100 text-gray-500">#{t}</span>
                            ))}
                        </div>
                    )}

                    <p className="text-sm text-gray-400 font-bold mb-4">
                        Tác giả: {doc.ownerName || 'Hệ thống'} · Ngày tạo: {new Date(doc.createdAt).toLocaleDateString()}
                    </p>

                    <p className="text-gray-600 mb-6">{doc.description || 'Không có mô tả.'}</p>

                    <div className="flex gap-6 text-sm font-bold text-gray-500 bg-gray-50 p-4 rounded-2xl mb-6">
                        <span>📥 {doc.downloadCount || 0} lượt tải</span>
                        <span>❤️ {doc.favoriteCount || 0} lượt thích</span>
                        <span>⭐ {doc.averageRating?.toFixed(1) || '0.0'}</span>
                    </div>

                    <div className="mb-6">
                        <p className="text-sm font-black text-gray-600 mb-2">Đánh giá:</p>
                        <div className="flex gap-1 text-2xl">
                            {[1, 2, 3, 4, 5].map(star => (
                                <button key={star} onClick={() => handleRate(star)} className="hover:scale-125 transition-transform">
                                    {star <= (doc.averageRating || 0) ? '⭐' : '☆'}
                                </button>
                            ))}
                        </div>
                    </div>

                    <button onClick={handleDownload} className="bg-[#DB6700] text-white px-6 py-3 rounded-2xl font-bold mb-8 hover:bg-[#DB6700]/90 transition-all">
                        ⬇️ Tải về ({doc.fileName})
                    </button>

                    <div className="border-t border-gray-100 pt-6">
                        <h3 className="text-lg font-black text-gray-800 mb-4">Thảo luận ({doc.comments?.length || 0})</h3>
                        <div className="flex flex-col gap-3 mb-4">
                            {doc.comments && doc.comments.length > 0 ? (
                                doc.comments.map(c => (
                                    <div key={c.id} className="bg-gray-50 p-3 rounded-xl">
                                        <span className="font-bold text-[#DB6700] text-sm">{c.ownerName}: </span>
                                        <span className="text-gray-600 text-sm">{c.content}</span>
                                    </div>
                                ))
                            ) : (
                                <p className="text-gray-400 text-sm italic">Chưa có bình luận.</p>
                            )}
                        </div>
                        <div className="flex gap-2">
                            <input
                                type="text"
                                placeholder="Viết bình luận..."
                                value={comment}
                                onChange={(e) => setComment(e.target.value)}
                                className="flex-1 p-3 bg-gray-50 rounded-xl outline-none border border-transparent focus:border-[#E59D1B]"
                            />
                            <button onClick={handleComment} className="bg-[#E59D1B] text-white px-5 rounded-xl font-bold hover:bg-[#DB6700] transition-colors">Gửi</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default DocumentDetailPage;