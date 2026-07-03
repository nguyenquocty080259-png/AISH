import "./Table.css";

// Thin, token-based <table> wrapper — no data/columns API, just styled subcomponents
// (Table, Table.Head, Table.Body, Table.Row, Table.HeaderCell, Table.Cell) so pages keep
// writing their own JSX per cell (Badge, Button, conditional content) with zero churn.
export default function Table({ className = "", children, ...rest }) {
  return (
    <table className={`ui-table ${className}`.trim()} {...rest}>
      {children}
    </table>
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
