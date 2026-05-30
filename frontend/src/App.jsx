import React, { useState, useEffect } from 'react';

// Sidebar Item Component
const SidebarItem = ({ icon, label, active = false }) => (
  <div className={`flex items-center space-x-3 p-3 rounded-lg cursor-pointer transition-all ${active ? 'bg-indigo-600 text-white' : 'text-gray-400 hover:bg-gray-800'}`}>
    <span className="text-xl">{icon}</span>
    <span className="font-medium">{label}</span>
  </div>
);

export default function App() {
  const [docs, setDocs] = useState([]);
  const [loading, setLoading] = useState(true);

  // Lấy dữ liệu từ Backend
  const fetchDocs = async () => {
    try {
      const response = await fetch('http://localhost:8088/api/documents');
      const data = await response.json();
      setDocs(data);
      setLoading(false);
    } catch (error) {
      console.error("Lỗi lấy dữ liệu:", error);
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDocs();
  }, []);

  return (
    <div className="flex h-screen bg-[#0f172a] text-white font-sans">
      {/* SIDEBAR */}
      <div className="w-64 bg-[#1e293b] p-6 flex flex-col border-r border-gray-700">
        <div className="flex items-center space-x-3 mb-10">
          <div className="w-8 h-8 bg-indigo-500 rounded-lg flex items-center justify-center font-bold">A</div>
          <span className="text-xl font-bold tracking-tight">AISH System</span>
        </div>
        
        <nav className="flex-1 space-y-2">
          <SidebarItem icon="🏠" label="Dashboard" active />
          <SidebarItem icon="📁" label="All Documents" />
          <SidebarItem icon="📚" label="Subjects" />
          <SidebarItem icon="⭐" label="Favorites" />
          <SidebarItem icon="🗑️" label="Trash" />
        </nav>

        <div className="mt-auto p-4 bg-gray-800 rounded-xl">
          <p className="text-xs text-gray-400 mb-2">Storage Usage</p>
          <div className="w-full bg-gray-700 h-1.5 rounded-full mb-2">
            <div className="bg-indigo-500 h-1.5 rounded-full" style={{ width: '45%' }}></div>
          </div>
          <p className="text-[10px] text-gray-400">4.5GB of 10GB used</p>
        </div>
      </div>

      {/* MAIN CONTENT */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* HEADER */}
        <header className="h-16 border-b border-gray-700 flex items-center justify-between px-8 bg-[#0f172a]">
          <div className="relative w-96">
            <span className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">🔍</span>
            <input 
              type="text" 
              placeholder="Search documents..." 
              className="w-full bg-gray-800 border-none rounded-full py-2 pl-10 pr-4 text-sm focus:ring-2 focus:ring-indigo-500 outline-none"
            />
          </div>
          <div className="flex items-center space-x-4">
            <button className="bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-lg text-sm font-medium transition-all flex items-center">
              <span className="mr-2">➕</span> Upload File
            </button>
            <div className="w-10 h-10 rounded-full bg-gradient-to-tr from-purple-500 to-indigo-500 border-2 border-gray-700"></div>
          </div>
        </header>

        {/* CONTENT AREA */}
        <main className="flex-1 overflow-y-auto p-8">
          <div className="flex items-center justify-between mb-8">
            <div>
              <h1 className="text-2xl font-bold text-white">My Documents</h1>
              <p className="text-gray-400 text-sm">Manage your study materials and AI documents</p>
            </div>
            <div className="flex space-x-2 bg-gray-800 p-1 rounded-lg">
              <button className="px-3 py-1 bg-gray-700 rounded text-xs">Grid</button>
              <button className="px-3 py-1 text-xs text-gray-400">List</button>
            </div>
          </div>

          {/* DOCUMENT GRID */}
          {loading ? (
            <div className="flex items-center justify-center h-64">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-500"></div>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
              {docs.length === 0 ? (
                <div className="col-span-full text-center py-20 bg-gray-800 rounded-2xl border-2 border-dashed border-gray-700">
                  <p className="text-gray-400">No documents found. Start by uploading one!</p>
                </div>
              ) : (
                docs.map((doc) => (
                  <div key={doc.id} className="bg-[#1e293b] group hover:bg-gray-800 border border-gray-700 rounded-2xl p-5 transition-all cursor-pointer hover:shadow-2xl hover:-translate-y-1">
                    <div className="w-full h-32 bg-gray-900 rounded-xl mb-4 flex items-center justify-center text-3xl group-hover:scale-105 transition-transform">
                      {doc.fileName?.endsWith('.pdf') ? '📕' : '📘'}
                    </div>
                    <h3 className="font-semibold text-gray-100 truncate mb-1">{doc.title}</h3>
                    <div className="flex items-center justify-between">
                      <span className="text-xs text-gray-500">24 May 2026</span>
                      <span className={`text-[10px] px-2 py-0.5 rounded-full ${doc.visibility === 'PUBLIC' ? 'bg-green-500/10 text-green-500' : 'bg-yellow-500/10 text-yellow-500'}`}>
                        {doc.visibility}
                      </span>
                    </div>
                  </div>
                ))
              )}
            </div>
          )}
        </main>
      </div>
    </div>
  );
}