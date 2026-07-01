export default function FilterBar({
  subjects,
  subjectFilter,
  onSubjectChange,
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

      <button type="button" className="doc-filterbar__reset" onClick={onReset}>
        Xoá lọc
      </button>
    </div>
  );
}