import type { ReactNode } from "react";
import type { PageMeta } from "../api/types";
import { Icon } from "./Icon";

export function Notice({
  children,
  error = false,
  success = false,
}: {
  children: ReactNode;
  error?: boolean;
  success?: boolean;
}) {
  return (
    <div
      className={`notice ${error ? "error" : success ? "success" : ""}`}
      role={error ? "alert" : "status"}
    >
      <Icon name={error ? "alert" : success ? "check" : "info"} />
      <div>{children}</div>
    </div>
  );
}
export function LoadingState({ label = "조회 중입니다." }: { label?: string }) {
  return (
    <section
      className="loading-state panel"
      aria-label={label}
      aria-busy="true"
      role="status"
    >
      <span>{label}</span>
      {[0, 1, 2].map((index) => (
        <div className="skeleton-row" key={index} aria-hidden="true">
          <i />
          <i />
          <i />
        </div>
      ))}
    </section>
  );
}
export function EmptyState({
  title = "조회 조건에 맞는 결과가 없습니다.",
  description = "조회 조건을 변경한 뒤 다시 조회해 주세요.",
}: {
  title?: string;
  description?: string;
}) {
  return (
    <section className="empty-state panel" role="status">
      <span className="empty-icon">
        <Icon name="search" width="28" height="28" />
      </span>
      <h2>{title}</h2>
      <p>{description}</p>
    </section>
  );
}
export function DataValue({ value }: { value: unknown }) {
  if (typeof value === "boolean") {
    return (
      <span className={`badge ${value ? "positive" : "neutral"}`}>
        {display(value)}
      </span>
    );
  }
  if (Array.isArray(value) && value.length) {
    return (
      <span className="badge-list">
        {value.map((item, index) => (
          <span className="badge" key={index}>
            {display(item)}
          </span>
        ))}
      </span>
    );
  }
  return <>{display(value)}</>;
}
export function Pager({
  pagination,
  disabled,
  onPage,
}: {
  pagination?: PageMeta;
  disabled: boolean;
  onPage: (page: number) => void;
}) {
  if (!pagination) return null;
  return (
    <div className="pager">
      <span>전체 {pagination.total}건</span>
      <div>
        <button
          disabled={disabled || pagination.page <= 1}
          onClick={() => onPage(pagination.page - 1)}
        >
          이전
        </button>
        <span>{pagination.page} 페이지</span>
        <button
          disabled={
            disabled || pagination.page * pagination.size >= pagination.total
          }
          onClick={() => onPage(pagination.page + 1)}
        >
          다음
        </button>
      </div>
    </div>
  );
}
export function display(value: unknown): string {
  if (value === null || value === undefined || value === "") return "—";
  if (Array.isArray(value)) return value.join(", ") || "—";
  if (typeof value === "boolean") return value ? "예" : "아니오";
  return String(value);
}
