import "./Table.css";

// Wrapper <table> mỏng theo token — không có API data/columns, chỉ là các sub-component
// đã style sẵn (Table, Table.Head, Table.Body, Table.Row, Table.HeaderCell, Table.Cell)
// để mỗi trang vẫn tự viết JSX từng ô (Badge, Button, nội dung có điều kiện) mà không phải sửa gì.
//
// Bảng luôn được bọc trong .ui-table-scroll: màn hẹp thì cuộn ngang trong khung của nó,
// KHÔNG đẩy cả trang tràn ngang.
export default function Table({ className = "", wrapperClassName = "", children, ...rest }) {
  return (
    <div className={`ui-table-scroll ${wrapperClassName}`.trim()}>
      <table className={`ui-table ${className}`.trim()} {...rest}>
        {children}
      </table>
    </div>
  );
}

Table.Head = function TableHead({ children }) {
  return <thead>{children}</thead>;
};

Table.Body = function TableBody({ children }) {
  return <tbody>{children}</tbody>;
};

Table.Row = function TableRow({ children, ...rest }) {
  return <tr {...rest}>{children}</tr>;
};

Table.HeaderCell = function TableHeaderCell({ children, ...rest }) {
  return <th {...rest}>{children}</th>;
};

Table.Cell = function TableCell({ children, className = "", ...rest }) {
  return (
    <td className={className} {...rest}>
      {children}
    </td>
  );
};
