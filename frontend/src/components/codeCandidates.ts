import { useEffect, useState } from "react";
import { allPages, messageOf } from "../api/client";
import { listDetailCodes } from "../api/detail_codes";
import type { DetailCode } from "../api/types";

export function useCodeCandidates(group_id: string) {
  const [rows, setRows] = useState<DetailCode[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [revision, setRevision] = useState(0);
  useEffect(() => {
    const controller = new AbortController();
    setRows([]);
    setError("");
    if (!group_id) return () => controller.abort();
    setLoading(true);
    allPages(
      (paging, signal) => listDetailCodes({ ...paging, group_id }, signal),
      controller.signal,
    )
      .then((result) => {
        if (!controller.signal.aborted) setRows(result);
      })
      .catch((reason) => {
        if (!controller.signal.aborted)
          setError(`상위코드 후보 조회 실패: ${messageOf(reason)}`);
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false);
      });
    return () => controller.abort();
  }, [group_id, revision]);
  return {
    rows,
    loading,
    error,
    retry: () => setRevision((value) => value + 1),
  };
}
