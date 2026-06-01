import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { 
  Search, Upload, LayoutGrid, List, MoreVertical, 
  FileText, Image as ImageIcon, Video, Folder, 
  Star, Clock, ShieldCheck, HardDrive, Bell, Settings
} from 'lucide-react';

const DocumentPage = () => {
    const [documents, setDocuments] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        axios.get('http://localhost:8080/api/documents')
            .then(res => {
                setDocuments(res.data);
                setLoading(false);
            })
            .catch(err => {
                console.error(err);
                setLoading(false);
            });
    }, []);

    return (
        // Sửa thành w-screen h-screen và xóa mọi padding mặc định
        <div className="h-screen w-screen bg-[#F4F7FE] flex font-sans overflow-hidden m-0 p-0">
            
            {/* --- SIDEBAR: Thêm hiệu ứng nổi khối --- */}
            <div className="w-80 bg-white border-r border-gray-100 p-8 flex flex-col h-full shadow-[4px_0_24px_rgba(0,0,0,0.02)] z-10 flex-shrink-0">
                <div className="flex items-center gap-4 text-[#4318FF] font-black text-2xl mb-12 px-2">
                    <div className="w-12 h-12 bg-gradient-to-br from-[#4318FF] to-[#b45fff] rounded-2xl shadow-lg shadow-indigo-200 flex items-center justify-center text-white rotate-3">
                        <HardDrive size={28}/>
                    </div>
                    <span>AISH Cloud</span>
                </div>
                
                <nav className="flex flex-col gap-1.5 flex-1">
                    <SidebarItem icon={<LayoutGrid size={22}/>} label="Bảng điều khiển" />
                    <SidebarItem icon={<Folder size={22}/>} label="Tất cả tài liệu" active />
                    <SidebarItem icon={<Star size={22}/>} label="Yêu thích" />
                    <SidebarItem icon={<Clock size={22}/>} label="Gần đây" />
                    <div className="my-6 border-t border-gray-100 mx-4"></div>
                    <SidebarItem icon={<ImageIcon size={22}/>} label="Hình ảnh" />
                    <SidebarItem icon={<Video size={22}/>} label="Đa phương tiện" />
                    <SidebarItem icon={<Settings size={22}/>} label="Cài đặt" />
                </nav>

                {/* Storage Status: Làm mờ Glassmorphism */}
                <div className="mt-auto bg-gradient-to-br from-[#4318FF] to-[#707EFF] p-6 rounded-[32px] text-white shadow-xl shadow-indigo-200 relative overflow-hidden">
                    <div className="absolute -right-4 -top-4 w-20 h-20 bg-white/10 rounded-full blur-2xl"></div>
                    <p className="text-xs font-medium opacity-80 mb-1">Dung lượng sử dụng</p>
                    <h4 className="text-xl font-black mb-4">12.4 GB / 50 GB</h4>
                    <div className="w-full bg-white/20 h-2.5 rounded-full mb-5 overflow-hidden">
                        <div className="bg-white h-full rounded-full shadow-[0_0_12px_rgba(255,255,255,0.5)]" style={{width: '35%'}}></div>
                    </div>
                    <button className="w-full bg-white text-[#4318FF] py-3 rounded-2xl font-black text-sm hover:scale-[1.02] active:scale-[0.98] transition-all">
                        Nâng cấp gói
                    </button>
                </div>
            </div>

            {/* --- MAIN CONTENT --- */}
            <div className="flex-1 flex flex-col h-full overflow-hidden bg-[#F4F7FE] relative">
                
                {/* Header: Thanh tìm kiếm nổi lên */}
                <header className="px-12 py-8 flex justify-between items-center relative z-20">
                    <div className="relative group">
                        <Search className="absolute left-6 top-1/2 -translate-y-1/2 text-[#A3AED0] group-focus-within:text-[#4318FF] transition-colors" size={20} />
                        <input 
                            type="text" 
                            placeholder="Tìm kiếm tài liệu học tập..." 
                            className="w-[500px] bg-white border-none rounded-[24px] py-4.5 pl-16 pr-8 shadow-[0_4px_20px_rgba(0,0,0,0.03)] focus:ring-4 ring-indigo-50 outline-none transition-all text-[#2B3674] font-medium"
                        />
                    </div>
                    
                    <div className="flex items-center gap-5">
                        <button className="p-3 bg-white rounded-2xl text-[#A3AED0] hover:text-[#4318FF] shadow-sm transition-all">
                            <Bell size={22}/>
                        </button>
                        <div className="h-10 w-[1px] bg-gray-200 mx-2"></div>
                        <div className="flex items-center gap-4 bg-white p-2 pr-6 rounded-3xl shadow-sm border border-white">
                            <img 
                                src="https://ui-avatars.com/api/?name=Quoc+Ty&background=4318FF&color=fff" 
                                className="w-10 h-10 rounded-2xl shadow-inner object-cover"
                                alt="avatar"
                            />
                            <div className="flex flex-col">
                                <span className="text-sm font-black text-[#2B3674]">Nguyễn Quốc Tỷ</span>
                                <span className="text-[10px] font-bold text-green-500 uppercase tracking-tighter">Premium Student</span>
                            </div>
                        </div>
                    </div>
                </header>

                {/* Content Area */}
                <main className="flex-1 px-12 pb-12 overflow-y-auto custom-scrollbar relative z-10">
                    
                    {/* Welcome Section */}
                    <div className="mb-12 flex justify-between items-end">
                        <div>
                            <span className="text-[#4318FF] font-bold text-sm tracking-widest uppercase">Trang chủ / Tài liệu</span>
                            <h1 className="text-5xl font-black text-[#2B3674] mt-2 tracking-tight">Thư viện của tôi</h1>
                            <p className="text-[#A3AED0] mt-3 font-semibold flex items-center gap-2">
                                <span className="w-2 h-2 bg-green-500 rounded-full animate-ping"></span>
                                Đang trực tuyến • {documents.length} tệp tin đã tải lên
                            </p>
                        </div>
                        <button className="bg-[#4318FF] text-white px-10 py-5 rounded-[24px] flex items-center gap-3 font-black shadow-2xl shadow-indigo-200 hover:translate-y-[-4px] active:translate-y-0 transition-all">
                            <Upload size={24}/> Tải lên tệp mới
                        </button>
                    </div>

                    {/* Stats Grid: Làm mượt hơn */}
                    <div className="grid grid-cols-4 gap-8 mb-12">
                        <StatSmall label="Học kỳ 1" value="12 Files" color="from-blue-500 to-blue-600" />
                        <StatSmall label="Học kỳ 2" value="08 Files" color="from-purple-500 to-purple-600" />
                        <StatSmall label="Đồ án SWP" value="03 Files" color="from-orange-500 to-orange-600" />
                        <StatSmall label="AI Study" value="05 Files" color="from-cyan-500 to-cyan-600" />
                    </div>

                    {/* Document Grid */}
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-8">
                        {loading ? (
                            <div className="col-span-full text-center py-32">
                                <div className="inline-block w-12 h-12 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin"></div>
                                <p className="mt-4 text-[#2B3674] font-black text-xl tracking-tight">Đang đồng bộ dữ liệu...</p>
                            </div>
                        ) : documents.length > 0 ? (
                            documents.map((doc) => (
                                <FileCard key={doc.id} title={doc.title} desc={doc.description} />
                            ))
                        ) : (
                            <div className="col-span-full bg-white/60 backdrop-blur-md rounded-[40px] p-24 text-center border-4 border-dashed border-white shadow-inner">
                                <div className="w-24 h-24 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-6">
                                    <Folder size={48} className="text-gray-300" />
                                </div>
                                <h3 className="text-2xl font-black text-[#2B3674] mb-2">Chưa có tài liệu nào</h3>
                                <p className="text-[#A3AED0] font-bold">Hãy bắt đầu bằng cách tải lên giáo trình hoặc đồ án của bạn.</p>
                            </div>
                        )}
                    </div>
                </main>
            </div>
        </div>
    );
};

// --- CÁC COMPONENT CON (ĐÃ NÂNG CẤP) ---

const SidebarItem = ({ icon, label, active = false }) => (
    <div className={`group flex items-center gap-4 px-6 py-4.5 rounded-[20px] cursor-pointer transition-all duration-400 ${active ? 'bg-white shadow-[0_20px_40px_rgba(0,0,0,0.05)] text-[#4318FF]' : 'text-[#A3AED0] hover:bg-white/50 hover:text-[#2B3674]'}`}>
        <span className={`transition-transform duration-300 group-hover:scale-110 ${active ? 'text-[#4318FF]' : 'text-[#A3AED0]'}`}>{icon}</span>
        <span className={`font-black text-base tracking-tight ${active ? 'text-[#2B3674]' : ''}`}>{label}</span>
        {active && <div className="ml-auto w-1.5 h-6 bg-[#4318FF] rounded-full"></div>}
    </div>
);

const StatSmall = ({ label, value, color }) => (
    <div className="bg-white p-7 rounded-[32px] shadow-[0_4px_20px_rgba(0,0,0,0.02)] flex items-center gap-5 border border-white hover:shadow-xl transition-all duration-300 group cursor-default">
        <div className={`w-14 h-14 bg-gradient-to-br ${color} rounded-2xl flex items-center justify-center text-white shadow-lg shadow-opacity-30 group-hover:rotate-12 transition-transform`}>
            <Folder size={24} />
        </div>
        <div>
            <p className="text-[#A3AED0] text-[10px] font-black uppercase tracking-[2px] mb-0.5">{label}</p>
            <h4 className="text-[#2B3674] font-black text-2xl">{value}</h4>
        </div>
    </div>
);

const FileCard = ({ title, desc }) => (
    <div className="group bg-white p-8 rounded-[40px] border border-white shadow-[0_10px_30px_rgba(0,0,0,0.02)] hover:shadow-[0_24px_48px_rgba(67,24,255,0.08)] hover:translate-y-[-10px] transition-all duration-500 relative cursor-pointer overflow-hidden">
        <div className="absolute -right-6 -top-6 w-24 h-24 bg-[#4318FF]/5 rounded-full group-hover:scale-[3] transition-transform duration-700"></div>
        
        <div className="relative z-10">
            <div className="flex justify-between items-start mb-8">
                <div className="w-16 h-16 bg-[#F4F7FE] rounded-[24px] flex items-center justify-center text-[#4318FF] group-hover:bg-[#4318FF] group-hover:text-white transition-all duration-300 shadow-inner">
                    <FileText size={32} />
                </div>
                <button className="p-2 hover:bg-gray-100 rounded-xl transition-colors">
                    <MoreVertical className="text-[#A3AED0]" size={20} />
                </button>
            </div>
            
            <h4 className="text-[#2B3674] font-black text-xl mb-3 line-clamp-1 group-hover:text-[#4318FF] transition-colors">{title}</h4>
            <p className="text-[#A3AED0] text-sm font-bold mb-8 line-clamp-2 min-h-[40px] leading-relaxed">
                {desc || "Tài liệu này hiện chưa có mô tả chi tiết từ sinh viên."}
            </p>
            
            <div className="flex justify-between items-center pt-6 border-t border-gray-50">
                <div className="flex items-center gap-2">
                    <div className="w-8 h-8 rounded-full bg-indigo-100 flex items-center justify-center text-[10px] font-black text-[#4318FF]">QT</div>
                    <span className="text-[11px] font-black text-[#2B3674]">Quốc Tỷ</span>
                </div>
                <span className="text-[9px] font-black text-[#4318FF] bg-[#4318FF]/10 px-3 py-1.5 rounded-full uppercase tracking-widest">Public</span>
            </div>
        </div>
    </div>
);

export default DocumentPage;