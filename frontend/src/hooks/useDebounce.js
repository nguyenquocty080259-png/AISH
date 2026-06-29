import { useEffect, useState } from "react";

// Trả về giá trị "trễ" sau delay ms, dùng cho search input để tránh
// filter lại danh sách trên mỗi keystroke.
export function useDebounce(value, delay = 400) {
  const [debounced, setDebounced] = useState(value);

  useEffect(() => {
    const timer = setTimeout(() => setDebounced(value), delay);
    return () => clearTimeout(timer);
  }, [value, delay]);

  return debounced;
}
