import { useEffect, useState } from "react";
import * as reportApi from "../../../api/reportApi";
import { useToast } from "../../../hooks/useToast";

export function useMyReportsPage() {
  const { showError } = useToast();
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    reportApi.getMyReports()
      .then((data) => active && setReports(data))
      .catch((error) => active && showError(error.message))
      .finally(() => active && setLoading(false));
    return () => { active = false; };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return { reports, loading };
}
