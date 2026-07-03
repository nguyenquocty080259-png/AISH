import { NavLink, Outlet } from "react-router-dom";
import { ROUTES } from "../../constants/routes";
import "./admin-layout.css";

// Nested-routes shell for the Admin panel. Sub-nav tabs are plain NavLinks so each sub-page
// keeps its own bookmarkable URL (/admin/stats, /admin/appeals, ...), matching how the rest
// of the app already nests routes under AppLayout's <Outlet/>.
// NOTE: Comment moderation was NOT added — no admin comment-moderation endpoint exists on
// the backend (only POST /api/documents/{id}/comment to create). See Step 3 report.
const ADMIN_NAV_ITEMS = [
  { to: ROUTES.ADMIN_STATS, label: "Bảng điều khiển" },
  { to: ROUTES.ADMIN_APPEALS, label: "Kháng nghị" },
  { to: ROUTES.ADMIN_DOCUMENTS, label: "Tài liệu" },
  { to: ROUTES.ADMIN_SUBJECTS, label: "Môn học" },
];

function tabClassName({ isActive }) {
  return `admin-tabs__link${isActive ? " admin-tabs__link--active" : ""}`;
}

export default function AdminLayout() {
  return (
    <div className="admin-layout">
      <nav className="admin-tabs">
        {ADMIN_NAV_ITEMS.map((item) => (
          <NavLink key={item.to} to={item.to} className={tabClassName}>
            {item.label}
          </NavLink>
        ))}
      </nav>

      <div className="admin-layout__content">
        <Outlet />
      </div>
    </div>
  );
}
