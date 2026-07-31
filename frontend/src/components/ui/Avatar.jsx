import { useState } from "react";
import "./Avatar.css";

// Lấy chữ cái đầu của tên: "Trần Vũ Đình Lang" -> "TL".
export function initialsOf(name) {
  if (!name) return "?";
  const parts = String(name).trim().split(/\s+/).filter(Boolean);
  if (parts.length === 0) return "?";
  const first = parts[0][0] ?? "";
  const last = parts.length > 1 ? parts[parts.length - 1][0] : "";
  return (first + last).toUpperCase();
}

const SIZE_CLASS = {
  xs: "ui-avatar--xs",
  sm: "ui-avatar--sm",
  md: "ui-avatar--md",
  lg: "ui-avatar--lg",
  xl: "ui-avatar--xl",
};

// src rỗng hoặc ảnh lỗi -> tự rơi về chữ cái đầu, không để ảnh vỡ.
export default function Avatar({ src, name, size = "md", className = "", ...rest }) {
  const [broken, setBroken] = useState(false);
  const showImage = src && !broken;

  return (
    <span
      className={`ui-avatar ${SIZE_CLASS[size] ?? SIZE_CLASS.md} ${className}`.trim()}
      {...rest}
    >
      {showImage ? (
        <img
          className="ui-avatar__img"
          src={src}
          alt={name ? `Avatar ${name}` : ""}
          loading="lazy"
          onError={() => setBroken(true)}
        />
      ) : (
        <span aria-hidden="true">{initialsOf(name)}</span>
      )}
    </span>
  );
}
