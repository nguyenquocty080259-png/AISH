import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { documentApi } from '../../api/documentApi';
import { subjectApi } from '../../api/subjectApi';
import { useAuth } from '../../context/AuthContext';

const CloudStoragePage = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    const fileInputRef = useRef(null);

    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [file, setFile] = useState(null);
    const [dragOver, setDragOver] = useState(false);

    const [subjects, setSubjects] = useState([]);
    const [subjectId, setSubjectId] = useState('');

    const [tags, setTags] = useState([]);
    const [tagInput, setTagInput] = useState('');

    const [uploading, setUploading] = useState(false);
    const [progress, setProgress] = useState(0);
    const [message, setMessage] = useState(null); // { type, text }

    useEffect(() => {
        subjectApi.getAll().then(res => setSubjects(res.data || [])).catch(() => setSubjects([]));
    }, []);

    const pickFile = (f) => {
        if (!f) return;
        setFile(f);
        if (!title) setTitle(f.name.replace(/\.[^/.]+$/, ''));
    };

    const addTag = () => {
        const t = tagInput.trim();
        if (t && !tags.includes(t)) setTags([...tags, t]);
        setTagInput('');
    };
    const removeTag = (t) => setTags(tags.filter(x => x !== t));

    const formatSize = (bytes) => {
        if (!bytes) return '0 B';
        const k = 1024, units = ['B', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return `${(bytes / Math.pow(k, i)).toFixed(1)} ${units[i]}`;
    };

    const reset = () => {
        setTitle(''); setDescription(''); setFile(null);
        setSubjectId(''); setTags([]); setTagInput(''); setProgress(0);
    };

    const handleUpload = async () => {
        if (!file) { setMessage({ type: 'error', text: 'Vui lòng chọn tệp để tải lên.' }); return; }
        if (!title.trim()) { setMessage({ type: 'error', text: 'Vui lòng nhập tiêu đề.' }); return; }

        const formData = new FormData();
        formData.append('title', title);
        formData.append('description', description);
        if (subjectId) formData.append('subjectId', subjectId);
        tags.forEach(t => formData.append('tags', t));
        formData.append('file', file);

        try {
            setUploading(true);
            setMessage(null);
            setProgress(0);
            await documentApi.upload(formData, (e) => {
                if (e.total) setProgress(Math.round((e.loaded * 100) / e.total));
            });
            setProgress(100);
            setMessage({ type: 'success', text: 'Tải tài liệu lên cloud thành công!' });
            reset();
        } catch {
            setMessage({ type: 'error', text: 'Lỗi khi tải tệp lên cloud!' });
        } finally {
            setUploading(false);
        }
    };

    return (
        <div className="min-h-screen w-full font-sans antialiased text-gray-700 bg-[#F8F9FD]">
            {/* TOP BAR */}
            <div className="bg-white border-b border-gray-100 px-6 md:px-10 py-4 flex items-center justify-between">
                <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-xl bg-[#DB6700] flex items-center justify-center text-white text-xl shadow-lg shadow-orange-500/20">☁️</div>
                    <div>
                        <h1 className="text-lg font-black text-gray-800 tracking-tight">Cloud Storage</h1>
                        <p className="text-[11px] text-gray-400 font-medium">Tải tài liệu của bạn lên cloud</p>
                    </div>
                </div>
                <div className="flex items-center gap-3">
                    <button onClick={() => navigate('/documents')} className="text-sm font-bold text-gray-500 hover:text-[#DB6700] transition-colors">
                        ← Tài liệu
                    </button>
                    <div className="flex items-center gap-2 pl-3 border-l border-gray-100">
                        <div className="w-8 h-8 rounded-full bg-[#E59D1B] text-white flex items-center justify-center font-bold text-xs">
                            {(user?.fullName || 'U').charAt(0)}
                        </div>
                        <span className="text-sm font-bold text-gray-800">{user?.fullName || 'User'}</span>
                    </div>
                </div>
            </div>

            <div className="max-w-3xl mx-auto p-6 md:p-10">
                <div className="bg-white rounded-3xl shadow-sm border border-gray-100 p-6 md:p-8">
                    <h2 className="text-2xl font-black text-gray-800 tracking-tight mb-1">Tải lên cloud</h2>
                    <p className="text-xs text-gray-400 font-medium mb-6">Hỗ trợ PDF, Word, hình ảnh và các định dạng khác.</p>

                    {message && (
                        <div className={`mb-6 px-4 py-3 rounded-2xl text-sm font-bold ${message.type === 'success' ? 'bg-green-50 text-green-600' : 'bg-red-50 text-red-500'}`}>
                            {message.text}
                        </div>
                    )}

                    {/* DROP ZONE */}
                    <div
                        onClick={() => fileInputRef.current?.click()}
                        onDragOver={(e) => { e.preventDefault(); setDragOver(true); }}
                        onDragLeave={() => setDragOver(false)}
                        onDrop={(e) => { e.preventDefault(); setDragOver(false); pickFile(e.dataTransfer.files[0]); }}
                        className={`cursor-pointer rounded-3xl border-2 border-dashed p-10 text-center transition-all ${dragOver ? 'border-[#DB6700] bg-[#DB6700]/5' : 'border-gray-200 hover:border-[#E59D1B] hover:bg-gray-50'}`}
                    >
                        <input ref={fileInputRef} type="file" className="hidden" onChange={(e) => pickFile(e.target.files[0])} />
                        <div className="text-4xl mb-3">{file ? '📄' : '☁️'}</div>
                        {file ? (
                            <div>
                                <p className="font-bold text-gray-800 text-sm">{file.name}</p>
                                <p className="text-[11px] text-gray-400 font-bold mt-1">{formatSize(file.size)}</p>
                                <button
                                    onClick={(e) => { e.stopPropagation(); setFile(null); }}
                                    className="mt-3 text-xs font-bold text-red-400 hover:underline"
                                >
                                    Xóa tệp
                                </button>
                            </div>
                        ) : (
                            <div>
                                <p className="font-bold text-gray-600 text-sm">Kéo & thả tệp vào đây</p>
                                <p className="text-[11px] text-gray-400 font-medium mt-1">hoặc bấm để chọn từ máy</p>
                            </div>
                        )}
                    </div>

                    {/* FORM */}
                    <div className="flex flex-col gap-4 mt-6">
                        <input
                            type="text" placeholder="Tiêu đề tài liệu..."
                            className="p-4 bg-[#F4F7FE] rounded-2xl outline-none border border-transparent focus:border-[#E59D1B] transition-all text-sm"
                            value={title} onChange={(e) => setTitle(e.target.value)}
                        />
                        <textarea
                            placeholder="Mô tả (tùy chọn)..."
                            rows={3}
                            className="p-4 bg-[#F4F7FE] rounded-2xl outline-none border border-transparent focus:border-[#E59D1B] transition-all text-sm resize-none"
                            value={description} onChange={(e) => setDescription(e.target.value)}
                        />
                        <select
                            className="p-4 bg-[#F4F7FE] rounded-2xl outline-none border border-transparent focus:border-[#E59D1B] transition-all text-sm"
                            value={subjectId} onChange={(e) => setSubjectId(e.target.value)}
                        >
                            <option value="">-- Chọn môn học (tùy chọn) --</option>
                            {subjects.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
                        </select>

                        {/* TAGS */}
                        <div className="bg-[#F4F7FE] rounded-2xl p-3">
                            <div className="flex flex-wrap gap-2 mb-2">
                                {tags.map(t => (
                                    <span key={t} className="flex items-center gap-1 text-xs font-bold px-2 py-1 rounded-lg bg-[#DB6700]/10 text-[#DB6700]">
                                        #{t}
                                        <button type="button" onClick={() => removeTag(t)} className="text-[#DB6700]">×</button>
                                    </span>
                                ))}
                            </div>
                            <input
                                type="text" placeholder="Nhập tag rồi Enter..."
                                className="w-full bg-transparent outline-none text-sm"
                                value={tagInput} onChange={(e) => setTagInput(e.target.value)}
                                onKeyDown={(e) => { if (e.key === 'Enter') { e.preventDefault(); addTag(); } }}
                            />
                        </div>

                        {/* PROGRESS */}
                        {uploading && (
                            <div>
                                <div className="flex justify-between text-xs font-bold text-gray-500 mb-1">
                                    <span>Đang tải lên cloud...</span>
                                    <span className="text-[#DB6700]">{progress}%</span>
                                </div>
                                <div className="w-full bg-gray-100 h-2 rounded-full overflow-hidden">
                                    <div className="bg-gradient-to-r from-[#E59D1B] to-[#DB6700] h-full transition-all duration-200" style={{ width: `${progress}%` }} />
                                </div>
                            </div>
                        )}

                        <div className="flex gap-4 mt-2">
                            <button
                                onClick={reset} disabled={uploading}
                                className="flex-1 py-3 font-bold text-gray-400 disabled:opacity-50"
                            >
                                Đặt lại
                            </button>
                            <button
                                onClick={handleUpload} disabled={uploading}
                                className="flex-1 bg-[#DB6700] hover:bg-[#DB6700]/90 text-white py-3 rounded-2xl font-bold shadow-md shadow-orange-500/20 transition-all disabled:opacity-60"
                            >
                                {uploading ? 'Đang tải...' : '📤 Tải lên Cloud'}
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default CloudStoragePage;
