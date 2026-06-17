import { useState, useEffect } from 'react';
import { subjectApi } from '../../api/subjectApi';

const UploadModal = ({ isOpen, onClose, onUploadSuccess }) => {
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [file, setFile] = useState(null);

    const [subjects, setSubjects] = useState([]);
    const [subjectId, setSubjectId] = useState('');
    const [newSubject, setNewSubject] = useState('');
    const [adding, setAdding] = useState(false);

    const [tags, setTags] = useState([]);
    const [tagInput, setTagInput] = useState('');

    useEffect(() => {
        if (isOpen) {
            subjectApi.getAll()
                .then(res => setSubjects(res.data || []))
                .catch(() => setSubjects([]));
        }
    }, [isOpen]);

    if (!isOpen) return null;

    const addTag = () => {
        const t = tagInput.trim();
        if (t && !tags.includes(t)) setTags([...tags, t]);
        setTagInput('');
    };

    const removeTag = (t) => setTags(tags.filter(x => x !== t));

    const handleConfirm = async () => {
        let finalSubjectId = subjectId || null;

        if (adding && newSubject.trim()) {
            try {
                const res = await subjectApi.create(newSubject.trim());
                finalSubjectId = res.data.id;
            } catch {
                alert('Không tạo được môn học mới!');
                return;
            }
        }

        onUploadSuccess({ title, description, file, subjectId: finalSubjectId, tags });
    };

    return (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50">
            <div className="bg-white p-8 rounded-[30px] w-[500px] shadow-2xl">
                <h2 className="text-2xl font-black text-[#2B3674] mb-6">Tải lên tài liệu - TEST TAG</h2>
                <div className="flex flex-col gap-4">
                    <input
                        type="text" placeholder="Tiêu đề..."
                        className="p-4 bg-[#F4F7FE] rounded-2xl outline-none border-none"
                        value={title} onChange={(e) => setTitle(e.target.value)}
                    />
                    <textarea
                        placeholder="Mô tả..."
                        className="p-4 bg-[#F4F7FE] rounded-2xl outline-none border-none"
                        value={description} onChange={(e) => setDescription(e.target.value)}
                    />

                    {/* Chọn môn học */}
                    {!adding ? (
                        <div className="flex gap-2">
                            <select
                                className="flex-1 p-4 bg-[#F4F7FE] rounded-2xl outline-none border-none"
                                value={subjectId}
                                onChange={(e) => setSubjectId(e.target.value)}
                            >
                                <option value="">-- Chọn môn học --</option>
                                {subjects.map(s => (
                                    <option key={s.id} value={s.id}>{s.name}</option>
                                ))}
                            </select>
                            <button
                                type="button"
                                onClick={() => setAdding(true)}
                                className="px-4 bg-[#F4F7FE] rounded-2xl font-bold text-[#4318FF]"
                            >
                                + Mới
                            </button>
                        </div>
                    ) : (
                        <div className="flex gap-2">
                            <input
                                type="text" placeholder="Tên môn học mới..."
                                className="flex-1 p-4 bg-[#F4F7FE] rounded-2xl outline-none border-none"
                                value={newSubject} onChange={(e) => setNewSubject(e.target.value)}
                            />
                            <button
                                type="button"
                                onClick={() => { setAdding(false); setNewSubject(''); }}
                                className="px-4 bg-[#F4F7FE] rounded-2xl font-bold text-gray-400"
                            >
                                Hủy
                            </button>
                        </div>
                    )}

                    {/* Tags */}
                    <div className="bg-[#F4F7FE] rounded-2xl p-3">
                        <div className="flex flex-wrap gap-2 mb-2">
                            {tags.map(t => (
                                <span key={t} className="flex items-center gap-1 text-xs font-bold px-2 py-1 rounded-lg bg-[#4318FF]/10 text-[#4318FF]">
                                    #{t}
                                    <button type="button" onClick={() => removeTag(t)} className="text-[#4318FF]">×</button>
                                </span>
                            ))}
                        </div>
                        <input
                            type="text"
                            placeholder="Nhập tag rồi Enter..."
                            className="w-full bg-transparent outline-none text-sm"
                            value={tagInput}
                            onChange={(e) => setTagInput(e.target.value)}
                            onKeyDown={(e) => {
                                if (e.key === 'Enter') { e.preventDefault(); addTag(); }
                            }}
                        />
                    </div>

                    <input
                        type="file"
                        className="text-sm text-gray-500"
                        onChange={(e) => setFile(e.target.files[0])}
                    />
                    <div className="flex gap-4 mt-4">
                        <button onClick={onClose} className="flex-1 py-3 font-bold text-gray-400">Hủy</button>
                        <button
                            onClick={handleConfirm}
                            className="flex-1 bg-[#4318FF] text-white py-3 rounded-2xl font-bold"
                        >
                            Xác nhận
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default UploadModal;
