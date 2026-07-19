import { NavLink, Link } from "react-router-dom";

// Props:
//   brand: { logoSrc, name, caption, homeTo }
//   navGroups: [{ label?: string, items: [{ to, label, icon: ReactNode }] }]
//   bottomAction?: { label, to, icon: ReactNode }
// Nếu chỉ 1 group không label → render phẳng (không hiện caption group + không divider).
// Nhiều group hoặc group có label → render label caps + divider giữa các group.

function linkClass({ isActive }) {
  return [
    "flex items-center gap-3 rounded-input px-3 py-2.5 text-sm font-medium transition-colors justify-center lg:justify-start",
    isActive ? "bg-surface-soft text-primary-dark" : "text-secondary hover:bg-surface-soft hover:text-primary",
  ].join(" ");
}

export default function AppSidebar({ brand, navGroups, bottomAction }) {
  const showGroupLabels = navGroups.length > 1 || navGroups.some((g) => g.label);

  return (
    <aside className="sticky top-0 flex h-screen w-16 lg:w-64 flex-shrink-0 flex-col overflow-y-auto border-r border-border bg-surface px-2 lg:px-4 py-6">
      <Link to={brand.homeTo} className="flex items-center gap-2 px-1 lg:px-2 justify-center lg:justify-start">
        <img src={brand.logoSrc} alt={brand.name} className="h-9 w-9 object-contain" />
        <div className="hidden lg:block leading-tight">
          <span className="block text-lg font-bold text-primary tracking-tight">{brand.name}</span>
          {brand.caption && (
            <span className="block text-[10px] font-semibold uppercase tracking-widest text-secondary">{brand.caption}</span>
          )}
        </div>
      </Link>

      <nav className="mt-8 flex flex-1 flex-col gap-1">
        {navGroups.map((group, gi) => (
          <div key={gi} className="flex flex-col gap-1">
            {gi > 0 && <div className="my-3 border-t border-border" />}
            {showGroupLabels && group.label && (
              <span className="hidden lg:block px-3 pb-1 text-[10px] font-semibold uppercase tracking-widest text-secondary">
                {group.label}
              </span>
            )}
            {group.items.map((it) => (
              <NavLink key={it.to} to={it.to} className={linkClass}>
                {it.icon}
                <span className="hidden lg:inline">{it.label}</span>
              </NavLink>
            ))}
          </div>
        ))}
      </nav>

      {bottomAction && (
        <Link to={bottomAction.to}
          className="mt-4 flex items-center justify-center gap-2 rounded-input bg-primary py-3 font-semibold text-white transition-colors hover:bg-primary-dark">
          {bottomAction.icon}
          <span className="hidden lg:inline">{bottomAction.label}</span>
        </Link>
      )}
    </aside>
  );
}
