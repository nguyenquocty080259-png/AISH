import React, { useState, useEffect } from 'react';
import { documentApi } from '../../api/documentApi';
import { subjectApi } from '../../api/subjectApi';
import { useAuth } from '../../context/AuthContext';
import UploadModal from '../../components/Document/UploadModal';
import { useNavigate } from 'react-router-dom';
const DocumentPage = () => {
    const { user } = useAuth();
    const [documents, setDocuments] = useState([]);
    const [viewMode, setViewMode] = useState('active');
    const [searchTerm, setSearchTerm] = useState('');
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [loading, setLoading] = useState(true);
    const [commentInputs, setCommentInputs] = useState({});
    const [subjects, setSubjects] = useState([]);
    const [selectedSubject, setSelectedSubject] = useState(null);
    const navigate = useNavigate();
    const [uploadingFiles, setUploadingFiles] = useState([
        { id: 1, name: 'BaoCao_DoAn_SWP.pdf', progress: 75 },
        { id: 2, name: 'TaiLieu_ThamKhao.docx', progress: 40 }
    ]);

    useEffect(() => {
        subjectApi.getAll().then(res => setSubjects(res.data || [])).catch(() => setSubjects([]));
    }, []);

    useEffect(() => { loadData(); }, [viewMode]);

    const loadData = async () => {
        try {
            setLoading(true);
            const res = viewMode === 'active' ? await documentApi.getAll() : await documentApi.getTrash();
            setDocuments(res.data || []);
        } catch (err) { setDocuments([]); } finally { setLoading(false); }
    };

    const handleUpload = async (data) => {
        const formData = new FormData();
        formData.append('title', data.title);
        formData.append('description', data.description);
        if (data.subjectId) formData.append('subjectId', data.subjectId);
        if (data.tags && data.tags.length) {
            data.tags.forEach(t => formData.append('tags', t));
        }
        formData.append('file', data.file);
        try {
            await documentApi.upload(formData);
            loadData();
            setIsModalOpen(false);
            alert("Tải lên tài liệu thành công!");
        } catch (err) { alert("Lỗi khi tải tệp lên!"); }
    };

    const handleFavorite = async (id) => {
        try {
            await documentApi.toggleFavorite(id);
            setDocuments(prevDocs => prevDocs.map(doc => {
                if (doc.id === id) {
                    const isNowFavorited = !doc.favorited;
                    return {
                        ...doc,
                        favorited: isNowFavorited,
                        favoriteCount: isNowFavorited ? doc.favoriteCount + 1 : doc.favoriteCount - 1
                    };
                }
                return doc;
            }));
        } catch (err) { alert("Lỗi tương tác yêu thích!"); }
    };

    const handleComment = async (id) => {
        const content = commentInputs[id];
        if (!content || !content.trim()) return;
        try {
            await documentApi.addComment(id, content);
            setCommentInputs(prev => ({ ...prev, [id]: '' }));
            loadData();
        } catch (err) { alert("Không thể gửi bình luận!"); }
    };

    const handleRate = async (id, star) => {
        try {
            await documentApi.rate(id, star);
            loadData();
            alert(`Đã đánh giá ${star} sao!`);
        } catch (err) { alert("Lỗi đánh giá!"); }
    };

    const handleDelete = async (id) => {
        if (window.confirm("Bạn có chắc chắn muốn xóa tài liệu này?")) {
            try {
                await documentApi.delete(id);
                setDocuments(prev => prev.filter(doc => doc.id !== id));
            } catch (err) { alert("Lỗi khi xóa!"); }
        }
    };

    const handleRestore = async (id) => {
        try {
            await documentApi.restore(id);
            setDocuments(prev => prev.filter(doc => doc.id !== id));
        } catch (err) { alert("Lỗi khi khôi phục!"); }
    };

    const handleDownload = async (id, fileName) => {
        try {
            const response = await documentApi.download(id);
            const blob = new Blob([response.data], { type: response.headers['content-type'] || 'application/octet-stream' });
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', fileName || `document_${id}.pdf`);
            document.body.appendChild(link);
            link.click();
            link.remove();
            setDocuments(prevDocs => prevDocs.map(doc => doc.id === id ? { ...doc, downloadCount: doc.downloadCount + 1 } : doc));
        } catch (err) { alert("Lỗi tải file!"); }
    };

    const handleToggleVisibility = async (id) => {
        try {
            await documentApi.toggleVisibility(id);
            setDocuments(prevDocs => prevDocs.map(doc =>
                doc.id === id
                    ? { ...doc, visibility: doc.visibility === 'PUBLIC' ? 'PRIVATE' : 'PUBLIC' }
                    : doc
            ));
        } catch (err) {
            alert("Không đổi được quyền (chỉ chủ tài liệu mới đổi được)!");
        }
    };

    const filteredDocuments = documents.filter(doc => {
        const matchSearch =
            doc.title?.toLowerCase().includes(searchTerm.toLowerCase()) ||
            doc.description?.toLowerCase().includes(searchTerm.toLowerCase());
        const matchSubject = selectedSubject === null || doc.subjectId === selectedSubject;
        return matchSearch && matchSubject;
    });

    return (
        <div className="flex w-full min-h-screen font-sans antialiased text-gray-700 bg-[#F8F9FD]">

            {/* SIDEBAR */}
            <div className="w-64 bg-white border-r border-gray-100 p-6 flex flex-col justify-between hidden md:flex shrink-0">
                <div>
                    <div className="flex items-center gap-3 mb-10">
                        <div className="w-10 h-10 rounded-xl bg-[#DB6700] flex items-center justify-center text-white font-black text-xl shadow-lg shadow-orange-500/20">S</div>
                        <span className="text-xl font-black text-gray-800 tracking-tight">Storage</span>
                    </div>

                    <nav className="flex flex-col gap-2">
                        <button className="flex items-center gap-4 px-4 py-3 rounded-xl text-gray-400 font-bold text-sm hover:bg-gray-50 transition-all">
                            <span>📊</span> Dashboard
                        </button>
                        <button
                            onClick={() => setViewMode('active')}
                            className={`flex items-center justify-between px-4 py-3 rounded-xl font-bold text-sm transition-all ${viewMode === 'active' ? 'bg-[#DB6700]/10 text-[#DB6700]' : 'text-gray-400 hover:bg-gray-50'}`}
                        >
                            <span className="flex items-center gap-4">📁 Documents</span>
                        </button>
                        <button
                            onClick={() => setViewMode('trash')}
                            className={`flex items-center justify-between px-4 py-3 rounded-xl font-bold text-sm transition-all ${viewMode === 'trash' ? 'bg-red-50 text-red-500' : 'text-gray-400 hover:bg-gray-50'}`}
                        >
                            <span className="flex items-center gap-4">🗑️ Thùng rác</span>
                        </button>
                        <button className="flex items-center gap-4 px-4 py-3 rounded-xl text-gray-400 font-bold text-sm hover:bg-gray-50 transition-all">
                            <span>🖼️</span> Images
                        </button>
                        <button className="flex items-center gap-4 px-4 py-3 rounded-xl text-gray-400 font-bold text-sm hover:bg-gray-50 transition-all">
                            <span>🎥</span> Video, Audio
                        </button>
                        <button className="flex items-center gap-4 px-4 py-3 rounded-xl text-gray-400 font-bold text-sm hover:bg-gray-50 transition-all">
                            <span>⚙️</span> Others
                        </button>

                        {/* Lọc theo môn học */}
                        <div className="mt-4 pt-4 border-t border-gray-100">
                            <p className="text-[10px] font-black text-gray-400 uppercase px-4 mb-2">Môn học</p>
                            <button
                                onClick={() => setSelectedSubject(null)}
                                className={`w-full text-left flex items-center gap-3 px-4 py-2 rounded-xl font-bold text-sm transition-all ${selectedSubject === null ? 'bg-[#DB6700]/10 text-[#DB6700]' : 'text-gray-400 hover:bg-gray-50'}`}
                            >
                                📚 Tất cả
                            </button>
                            {subjects.map(s => (
                                <button
                                    key={s.id}
                                    onClick={() => setSelectedSubject(s.id)}
                                    className={`w-full text-left flex items-center gap-3 px-4 py-2 rounded-xl font-bold text-sm transition-all ${selectedSubject === s.id ? 'bg-[#DB6700]/10 text-[#DB6700]' : 'text-gray-400 hover:bg-gray-50'}`}
                                >
                                    📖 {s.name}
                                </button>
                            ))}
                        </div>
                    </nav>
                </div>

                <div className="bg-gradient-to-br from-[#E59D1B]/10 to-[#DB6700]/10 p-4 rounded-2xl border border-[#E59D1B]/20">
                    <p className="text-xs font-black text-[#DB6700] mb-1">SWP391 Storage</p>
                    <p className="text-[10px] text-gray-500 font-medium">Hệ thống phân loại tương tác tài nguyên nâng cao.</p>
                </div>
            </div>

            {/* NỘI DUNG CHÍNH */}
            <div className="flex-1 p-6 md:p-8 overflow-y-auto relative">

                {/* TOP SEARCH BAR */}
                <div className="flex flex-col sm:flex-row justify-between items-center gap-4 mb-8 bg-white p-4 rounded-2xl shadow-sm border border-gray-50">
                    <div className="w-full sm:w-96 relative">
                        <span className="absolute left-4 top-2.5 text-gray-400 text-sm">🔍</span>
                        <input
                            type="text"
                            placeholder="Search documents by title or keyword..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="w-full pl-10 pr-4 py-2 bg-gray-50 rounded-xl outline-none text-sm border border-transparent focus:border-[#E59D1B] transition-all"
                        />
                    </div>

                    <div className="flex items-center gap-4 w-full sm:w-auto justify-end">
                        {viewMode === 'active' && (
                            <button
                                onClick={() => setIsModalOpen(true)}
                                className="bg-[#DB6700] hover:bg-[#DB6700]/90 text-white px-6 py-2 rounded-xl font-bold text-sm shadow-md shadow-orange-500/20 flex items-center gap-2 transition-all"
                            >
                                📤 Upload File
                            </button>
                        )}
                        <div className="flex items-center gap-2 pl-4 border-l border-gray-100">
                            <div className="w-8 h-8 rounded-full bg-[#E59D1B] text-white flex items-center justify-center font-bold text-xs">
                                {(user?.fullName || 'U').charAt(0)}
                            </div>
                            <span className="text-sm font-bold text-gray-800">{user?.fullName || 'User'}</span>
                        </div>
                    </div>
                </div>

                {/* TIÊU ĐỀ + TAB */}
                <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-6">
                    <div>
                        <h2 className="text-2xl font-black text-gray-800 tracking-tight">
                            {viewMode === 'active' ? 'Documents' : 'Trash Can'}
                        </h2>
                        <p className="text-xs text-gray-400 font-medium mt-0.5">Tổng số lượng: {filteredDocuments.length} files</p>
                    </div>

                    <div className="flex bg-gray-200/60 p-1 rounded-xl w-full sm:w-auto">
                        <button onClick={() => setViewMode('active')} className={`px-6 py-2 rounded-lg font-bold text-sm transition-all ${viewMode === 'active' ? 'bg-white text-[#DB6700] shadow-sm' : 'text-gray-500'}`}>Tài liệu hiện có</button>
                        <button onClick={() => setViewMode('trash')} className={`px-6 py-2 rounded-lg font-bold text-sm transition-all ${viewMode === 'trash' ? 'bg-white text-red-500 shadow-sm' : 'text-gray-500'}`}>Thùng rác</button>
                    </div>
                </div>

                {/* GRID CARD */}
                {loading ? (
                    <div className="text-center py-20 text-sm font-bold text-gray-400">Đang đồng bộ dữ liệu hệ thống...</div>
                ) : filteredDocuments.length === 0 ? (
                    <div className="text-center py-20 bg-white rounded-3xl border border-dashed border-gray-200 text-gray-400 font-bold text-sm w-full">
                        {viewMode === 'active' ? 'Thư mục trống. Hãy nhấn Tải lên tệp mới!' : 'Thùng rác trống.'}
                    </div>
                ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
                        {filteredDocuments.map((doc) => (
                            <div key={doc.id} className="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm hover:shadow-md transition-all flex flex-col justify-between relative">
                                <div>
                                    <div className="flex justify-between items-start mb-4">
                                        <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-[#E59D1B]/10 to-[#DB6700]/10 flex items-center justify-center text-[#DB6700] font-black text-xs">DOC</div>
                                        {viewMode === 'active' && (
                                            <div className="flex gap-0.5 text-sm">
                                                {[1, 2, 3, 4, 5].map(star => (
                                                    <button key={star} onClick={() => handleRate(doc.id, star)} className="hover:scale-125 transition-transform duration-100">
                                                        {star <= (doc.averageRating || 0) ? '⭐' : '☆'}
                                                    </button>
                                                ))}
                                                <span className="text-[10px] text-gray-400 font-black ml-1">({doc.averageRating?.toFixed(1) || 0.0})</span>
                                            </div>
                                        )}
                                    </div>

                                    <h4
    onClick={() => navigate(`/documents/${doc.id}`)}
    className="font-bold text-gray-800 text-base truncate cursor-pointer hover:text-[#DB6700] transition-colors"
>
    {doc.title}
</h4>

                                    {doc.subjectName && (
                                        <span className="inline-block text-[10px] font-black px-2 py-0.5 rounded-md bg-[#4318FF]/10 text-[#4318FF] mb-2">
                                            📚 {doc.subjectName}
                                        </span>
                                    )}

                                    {doc.tags && doc.tags.length > 0 && (
                                        <div className="flex flex-wrap gap-1 mb-2">
                                            {doc.tags.map(t => (
                                                <span key={t} className="text-[10px] font-bold px-2 py-0.5 rounded-md bg-gray-100 text-gray-500">#{t}</span>
                                            ))}
                                        </div>
                                    )}

                                    <p className="text-[10px] text-gray-400 font-bold truncate mb-2">Tác giả: {doc.ownerName || 'Hệ thống'} | Ngày tạo: {new Date(doc.createdAt).toLocaleDateString()}</p>
                                    <p className="text-gray-500 text-xs font-medium line-clamp-2 mb-4 h-8">{doc.description || "Không có mô tả chi tiết."}</p>

                                    {viewMode === 'active' && (
                                        <div className="flex gap-4 text-[11px] font-bold text-gray-400 bg-gray-50 p-2 rounded-xl mb-4">
                                            <span>📥 {doc.downloadCount || 0} Lượt tải</span>
                                            <span>❤️ {doc.favoriteCount || 0} Lượt thích</span>
                                        </div>
                                    )}
                                </div>

                                {/* BÌNH LUẬN */}
                                {viewMode === 'active' && (
                                    <div className="border-t border-gray-50 pt-3 mt-2">
                                        <span className="text-[11px] font-black text-gray-400 uppercase tracking-wider block mb-2">Thảo luận ({doc.comments?.length || 0})</span>
                                        <div className="max-h-24 overflow-y-auto flex flex-col gap-1.5 mb-3 pr-1">
                                            {doc.comments && doc.comments.length > 0 ? (
                                                doc.comments.map(c => (
                                                    <div key={c.id} className="text-[11px] bg-gray-50/70 p-2 rounded-lg leading-relaxed">
                                                        <span className="font-bold text-[#DB6700]">{c.ownerName}: </span>
                                                        <span className="text-gray-600">{c.content}</span>
                                                    </div>
                                                ))
                                            ) : (
                                                <p className="text-[10px] text-gray-400 italic">Chưa có bình luận.</p>
                                            )}
                                        </div>
                                        <div className="flex gap-2">
                                            <input
                                                type="text"
                                                placeholder="Write a comment..."
                                                value={commentInputs[doc.id] || ''}
                                                onChange={(e) => setCommentInputs(prev => ({ ...prev, [doc.id]: e.target.value }))}
                                                className="flex-1 p-2 bg-gray-50 text-xs rounded-xl outline-none border border-transparent focus:border-[#E59D1B] focus:bg-white transition-all"
                                            />
                                            <button onClick={() => handleComment(doc.id)} className="bg-[#E59D1B] text-white px-3 text-xs font-bold rounded-xl hover:bg-[#DB6700] transition-colors">Gửi</button>
                                        </div>
                                    </div>
                                )}

                                {/* NÚT DƯỚI CARD */}
                                <div className="flex justify-between items-center pt-3 border-t border-gray-100 mt-4">
                                    <div className="flex items-center gap-2">
                                        {viewMode === 'active' ? (
                                            <>
                                                <button onClick={() => handleFavorite(doc.id)} className={`text-sm p-1 rounded-lg transition-transform active:scale-75 ${doc.favorited ? 'text-red-500 scale-110' : 'text-gray-300'}`}>
                                                    {doc.favorited ? '❤️' : '🤍'}
                                                </button>
                                                <button
                                                    onClick={() => handleToggleVisibility(doc.id)}
                                                    className={`text-[10px] font-black px-2 py-0.5 rounded-md transition-all hover:opacity-80 ${
                                                        doc.visibility === 'PUBLIC'
                                                            ? 'bg-green-100 text-green-600'
                                                            : 'bg-gray-200 text-gray-500'
                                                    }`}
                                                    title="Bấm để đổi quyền"
                                                >
                                                    {doc.visibility === 'PUBLIC' ? '🌐 PUBLIC' : '🔒 PRIVATE'}
                                                </button>
                                                <button onClick={() => handleDelete(doc.id)} className="text-red-400 font-bold text-xs hover:underline ml-2">Xóa</button>
                                            </>
                                        ) : (
                                            <button onClick={() => handleRestore(doc.id)} className="text-green-500 font-black text-xs hover:underline">🔄 Khôi phục</button>
                                        )}
                                    </div>
                                    {viewMode === 'active' && (
                                        <button onClick={() => handleDownload(doc.id, doc.fileName)} className="text-[#DB6700] font-black text-xs hover:underline">Tải về</button>
                                    )}
                                </div>
                            </div>
                        ))}
                    </div>
                )}

                {/* MENU UPLOAD IN PROGRESS */}
                {viewMode === 'active' && uploadingFiles.length > 0 && (
                    <div className="fixed bottom-6 right-6 w-72 bg-white rounded-2xl shadow-2xl p-5 border border-gray-100 z-50">
                        <div className="flex justify-between items-center mb-3">
                            <span className="text-xs font-black text-gray-800">In Progress</span>
                            <span className="text-[10px] text-gray-400 font-bold bg-gray-50 px-1.5 py-0.5 rounded">⚡ Live</span>
                        </div>
                        <div className="flex flex-col gap-3">
                            {uploadingFiles.map(f => (
                                <div key={f.id} className="text-xs">
                                    <div className="flex justify-between font-bold text-gray-600 mb-1">
                                        <span className="truncate pr-2 w-40">{f.name}</span>
                                        <span className="text-[#DB6700]">{f.progress}%</span>
                                    </div>
                                    <div className="w-full bg-gray-100 h-1.5 rounded-full overflow-hidden">
                                        <div className="bg-gradient-to-r from-[#E59D1B] to-[#DB6700] h-full transition-all duration-300" style={{ width: `${f.progress}%` }}></div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                )}

            </div>

            <UploadModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} onUploadSuccess={handleUpload} />
        </div>
    );
};

export default DocumentPage;