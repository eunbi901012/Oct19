import { useEffect, useState } from "react";
import { listOrganizationRelationHistory } from "../api/organizations";
import { isDenied, messageOf } from "../api/client";
import type { OrganizationRelation, PageMeta } from "../api/types";
import { display, Notice, Pager } from "./primitives";

export default function OrganizationHistory({
  organization_code,
  onDenied,
}: {
  organization_code: string;
  onDenied: () => void;
}) {
  const [page, setPage] = useState(1);
  const [requested, setRequested] = useState(false);
  const [rows, setRows] = useState<OrganizationRelation[]>([]);
  const [pagination, setPagination] = useState<PageMeta>();
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [revision, setRevision] = useState(0);
  useEffect(() => {
    if (!requested) return;
    const controller = new AbortController();
    setLoading(true);
    setError("");
    listOrganizationRelationHistory(
      { organization_code, page, size: 20 },
      controller.signal,
    )
      .then((result) => {
        if (!controller.signal.aborted) {
          setRows(result.data);
          setPagination(result.pagination);
        }
      })
      .catch((reason) => {
        if (!controller.signal.aborted) {
          setError(messageOf(reason));
          if (isDenied(reason)) {
            setRows([]);
            onDenied();
          }
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false);
      });
    return () => controller.abort();
  }, [organization_code, page, requested, revision]);
  return (
    <section className="panel history">
      <div className="section-heading">
        <h2>변경 전 조직관계 이력</h2>
        <button
          disabled={loading}
          onClick={() => {
            setRequested(true);
            setRevision((value) => value + 1);
          }}
        >
          이력 조회
        </button>
      </div>
      {loading ? <Notice>이력을 조회 중입니다.</Notice> : null}
      {error ? <Notice error>{error}</Notice> : null}
      {requested && !loading && !error && !rows.length ? (
        <Notice>변경 전 관계 이력이 없습니다.</Notice>
      ) : null}
      {rows.length ? (
        <table>
          <thead>
            <tr>
              <th>변경 전 상위조직</th>
              <th>적용 시작일</th>
              <th>적용 종료일</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((row) => (
              <tr key={row.relation_id}>
                <td>{display(row.parent_code)}</td>
                <td>{display(row.valid_from)}</td>
                <td>{display(row.valid_to)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      ) : null}
      <Pager pagination={pagination} disabled={loading} onPage={setPage} />
    </section>
  );
}
