export default function FilterBar({
  subjects,
  tags,
  subjectFilter,
  onSubjectChange,
  tagFilter,
  onTagChange,
  onReset,
}) {
  return (
    <div className="doc-filterbar">
      <select
        value={subjectFilter}
        onChange={(e) => onSubjectChange(e.target.value)}
      >
        <option value="">Tất cả môn học</option>
        {subjects.map((subject) => (
          <option key={subject.id} value={subject.id}>
            {subject.name}
          </option>
        ))}
      </select>

      <select value={tagFilter} onChange={(e) => onTagChange(e.target.value)}>
        <option value="">Tất cả tag</option>
        {tags.map((tag) => (
          <option key={tag.id} value={tag.name}>
            {tag.name}
          </option>
        ))}
      </select>

      <button type="button" className="doc-filterbar__reset" onClick={onReset}>
        Xoá lọc
      </button>
    </div>
  );
}
