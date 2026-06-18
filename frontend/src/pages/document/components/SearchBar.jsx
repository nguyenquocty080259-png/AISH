export default function SearchBar({ value, onChange }) {
  return (
    <input
      className="doc-search"
      type="text"
      value={value}
      onChange={(e) => onChange(e.target.value)}
      placeholder="Tìm theo tên hoặc mô tả tài liệu..."
    />
  );
}
