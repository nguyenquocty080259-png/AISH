import React, { useState } from 'react';

const UploadModal = ({ isOpen, onClose, onUploadSuccess }) => {
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [file, setFile] = useState(null);

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50">
            <div className="bg-white p-8 rounded-[30px] w-[500px] shadow-2xl">
                <h2 className="text-2xl font-black text-[#2B3674] mb-6">Tải lên tài liệu</h2>
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
                    <input 
                        type="file" 
                        className="text-sm text-gray-500"
                        onChange={(e) => setFile(e.target.files[0])}
                    />
                    <div className="flex gap-4 mt-4">
                        <button onClick={onClose} className="flex-1 py-3 font-bold text-gray-400">Hủy</button>
                        <button 
                            onClick={() => onUploadSuccess({ title, description, file })}
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