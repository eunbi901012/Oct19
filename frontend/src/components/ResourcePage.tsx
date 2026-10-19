import { useCallback, useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { ApiFailure, isDenied, messageOf } from "../api/client";
import type { AssignmentAction, Entity, Page, PageMeta } from "../api/types";
import { useAccess, safeInternalUrl } from "./access";
import { useLookups, type Option, type Source } from "./lookups";
import { useCodeCandidates } from "./codeCandidates";
import {
  DataValue,
  display,
  EmptyState,
  LoadingState,
  Notice,
  Pager,
} from "./primitives";
import { Icon } from "./Icon";

export type Draft = Record<string, string>;
export interface ReadQuery {
  page: number;
  size: number;
  [key: string]: string | number;
}
export interface Field {
  key: string;
  label: string;
  type?: "text" | "textarea" | "date" | "number" | "boolean";
  source?: Source | "codes";
  readonly?: boolean;
  required?: boolean;
}
export interface ScreenConfig<T extends Entity, I> {
  kind:
    | "users"
    | "organizations"
    | "roles"
    | "assignments"
    | "permissions"
    | "structure"
    | "execution"
    | "groups"
    | "codes";
  title: string;
  description: string;
  editorTitle: string;
  saveLabel?: string;
  fields: Field[];
  columns: Field[];
  filters: Field[];
  initialQuery?: Draft;
  requireQuery?: string;
  sources?: Source[];
  policy?: string;
  identity: (row: T) => string;
  read: (query: ReadQuery, signal: AbortSignal) => Promise<Page<T>>;
  write: (input: I, signal: AbortSignal) => Promise<T>;
  serialize: (
    draft: Draft,
    selected: T | undefined,
    id: string,
    action?: AssignmentAction,
  ) => I;
  extra?: (
    selected: T | undefined,
    rows: T[],
    onDenied: () => void,
  ) => React.ReactNode;
}
export function toDraft(row?: Entity): Draft {
  if (!row) return {};
  return Object.fromEntries(
    Object.entries(row).map(([key, value]) => [
      key,
      value === null
        ? ""
        : Array.isArray(value)
          ? value.join(", ")
          : String(value),
    ]),
  );
}
export const dateValue = (value: string | undefined) => value || null;

export default function ResourcePage<T extends Entity, I>({
  config,
}: {
  config: ScreenConfig<T, I>;
}) {
  const access = useAccess();
  const [filters, setFilters] = useState<Draft>(config.initialQuery || {});
  const [query, setQuery] = useState<ReadQuery>({
    page: 1,
    size: 20,
    ...config.initialQuery,
  });
  const [rows, setRows] = useState<T[]>([]);
  const [selected, setSelected] = useState<T>();
  const [draft, setDraft] = useState<Draft>({});
  const [baseline, setBaseline] = useState<Draft>({});
  const [pagination, setPagination] = useState<PageMeta>();
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [denied, setDenied] = useState(false);
  const [error, setError] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [filterErrors, setFilterErrors] = useState<Record<string, string>>({});
  const [success, setSuccess] = useState("");
  const [loaded, setLoaded] = useState(false);
  const [revision, setRevision] = useState(0);
  const [retryMode, setRetryMode] = useState<"read" | "write">("read");
  const lastAction = useRef<AssignmentAction>();
  const generation = useRef(0);
  const mutation = useRef<AbortController>();
  const newId = useRef("");
  const selectedRef = useRef<T>();
  selectedRef.current = selected;
  const sources = [
    ...(config.sources || []),
    ...[...config.filters, ...config.fields].flatMap((field) =>
      field.source && field.source !== "codes" ? [field.source] : [],
    ),
  ];
  const lookups = useLookups(sources);
  const codes = useCodeCandidates(
    config.kind === "codes" ? String(query.group_id || "") : "",
  );
  const ready = !config.requireQuery || Boolean(query[config.requireQuery]);

  const choose = useCallback((row?: T) => {
    setSelected(row);
    const values = row ? toDraft(row) : {};
    setDraft(values);
    setBaseline(values);
    setFieldErrors({});
    setSuccess("");
    newId.current = "";
  }, []);

  const read = useCallback(
    async (signal: AbortSignal, epoch: number, saved?: T) => {
      setFilterErrors({});
      const result = await config.read(query, signal);
      if (signal.aborted || generation.current !== epoch) return;
      setRows(result.data);
      setPagination(result.pagination);
      setLoaded(true);
      const previous = saved || selectedRef.current;
      const current = previous
        ? result.data.find(
            (row) => config.identity(row) === config.identity(previous),
          )
        : ["execution", "organizations"].includes(config.kind) &&
            result.data.length === 1
          ? result.data[0]
          : undefined;
      choose(current);
      if (!current && config.kind === "permissions") {
        const values = Object.fromEntries(
          Object.entries(query).filter(
            ([key]) => !["page", "size"].includes(key),
          ),
        );
        const initial = Object.fromEntries(
          Object.entries(values).map(([key, value]) => [key, String(value)]),
        );
        setDraft(initial);
        setBaseline(initial);
      }
    },
    [config, query, choose],
  );

  useEffect(() => {
    const controller = new AbortController();
    const epoch = ++generation.current;
    mutation.current?.abort();
    setSaving(false);
    if (!ready) return () => controller.abort();
    setLoading(true);
    setError("");
    setSuccess("");
    read(controller.signal, epoch)
      .catch((reason) => {
        if (controller.signal.aborted || generation.current !== epoch) return;
        setError(messageOf(reason));
        setRetryMode("read");
        if (reason instanceof ApiFailure) {
          setFilterErrors(
            Object.fromEntries(
              reason.field_errors.map((field) => [field.field, field.message]),
            ),
          );
        }
        if (isDenied(reason)) {
          setDenied(true);
          setRows([]);
          choose();
        }
      })
      .finally(() => {
        if (!controller.signal.aborted && generation.current === epoch)
          setLoading(false);
      });
    return () => {
      controller.abort();
      mutation.current?.abort();
    };
  }, [read, revision, ready, choose]);

  function changeContext(next: ReadQuery) {
    mutation.current?.abort();
    generation.current += 1;
    choose();
    setRows([]);
    setPagination(undefined);
    setLoaded(false);
    setSuccess("");
    setError("");
    setFilterErrors({});
    setQuery(next);
  }

  async function save(action?: AssignmentAction) {
    if (
      saving ||
      loading ||
      denied ||
      lookups.error ||
      lookups.loading ||
      codes.error ||
      codes.loading
    )
      return;
    if (
      config.kind === "permissions" &&
      !selected &&
      (!loaded || !ready || pagination?.total !== 0)
    )
      return;
    const errors: Record<string, string> = {};
    config.fields.forEach((field) => {
      if (field.required && !draft[field.key])
        errors[field.key] = `${field.label} 항목을 입력해 주세요.`;
      if (
        field.type === "number" &&
        (!draft[field.key] || !Number.isInteger(Number(draft[field.key])))
      ) {
        errors[field.key] = `${field.label} 항목은 정수로 입력해 주세요.`;
      }
      if (
        field.type === "boolean" &&
        !["true", "false"].includes(draft[field.key] || "")
      ) {
        errors[field.key] = `${field.label} 항목을 선택해 주세요.`;
      }
    });
    if (
      config.kind === "permissions" &&
      !["role_id", "organization_code", "user_id"].some((key) => draft[key])
    ) {
      errors.role_id = "역할·조직·사용자 중 한 대상을 선택해 주세요.";
    }
    if ((action === "변경" || action === "회수") && !selected) return;
    setFieldErrors(errors);
    if (Object.keys(errors).length) return;
    const controller = new AbortController();
    mutation.current = controller;
    const epoch = generation.current;
    setSaving(true);
    lastAction.current = action;
    setError("");
    setSuccess("");
    if (!newId.current) newId.current = crypto.randomUUID();
    let committed = false;
    try {
      const result = await config.write(
        config.serialize(draft, selected, newId.current, action),
        controller.signal,
      );
      committed = true;
      if (controller.signal.aborted || generation.current !== epoch) return;
      if (["permissions", "structure", "execution"].includes(config.kind))
        await access.refresh();
      if (controller.signal.aborted || generation.current !== epoch) return;
      await read(controller.signal, epoch, result);
      if (!controller.signal.aborted && generation.current === epoch) {
        if (config.kind === "codes") codes.retry();
        setSuccess("저장 후 재조회가 완료되었습니다.");
        newId.current = "";
      }
    } catch (reason) {
      if (controller.signal.aborted || generation.current !== epoch) return;
      setError(messageOf(reason));
      setRetryMode(committed ? "read" : "write");
      if (reason instanceof ApiFailure) {
        const setErrors = committed ? setFilterErrors : setFieldErrors;
        setErrors(
          Object.fromEntries(
            reason.field_errors.map((field) => [field.field, field.message]),
          ),
        );
      }
      if (isDenied(reason)) {
        setDenied(true);
        setRows([]);
        choose();
      }
    } finally {
      if (!controller.signal.aborted && generation.current === epoch)
        setSaving(false);
    }
  }

  const codeOptions = codes.rows.map((row) => ({
    value: row.code,
    label: row.code,
  }));
  function codeDepth(code: string): number {
    const visited = new Set<string>();
    let parent = codes.rows.find((row) => row.code === code)?.parent_code;
    let depth = 0;
    while (parent && !visited.has(parent)) {
      visited.add(parent);
      depth += 1;
      parent = codes.rows.find((row) => row.code === parent)?.parent_code;
    }
    return Math.min(depth, 12);
  }
  function control(
    field: Field,
    values: Draft,
    update: (key: string, value: string) => void,
    filter = false,
  ) {
    const id = `${filter ? "filter" : "edit"}-${field.key}`;
    const fieldError = (filter ? filterErrors : fieldErrors)[field.key];
    const errorId = fieldError ? `${id}-error` : undefined;
    const options: Option[] =
      field.source === "codes"
        ? codeOptions
        : field.source
          ? lookups.options[field.source] || []
          : [];
    const value = values[field.key] || "";
    const locked = field.readonly && !filter;
    const select =
      !locked && (Boolean(field.source) || field.type === "boolean");
    const label = filter ? `${field.label} 조회` : field.label;
    return (
      <label
        className={`field ${field.type === "textarea" ? "wide" : ""}`}
        key={field.key}
        htmlFor={id}
      >
        <span>
          {field.label}
          {field.required && !filter ? <em>필수</em> : null}
        </span>
        {select ? (
          <select
            id={id}
            aria-label={label}
            value={value}
            disabled={saving || (Boolean(field.source) && lookups.loading)}
            onChange={(event) => update(field.key, event.target.value)}
            aria-invalid={Boolean(fieldError)}
            aria-describedby={errorId}
          >
            <option value="">
              {filter ? "전체 / 선택 안 함" : "선택해 주세요"}
            </option>
            {field.type === "boolean" ? (
              <>
                <option value="true">
                  {field.key === "allowed" ? "허용" : "사용"}
                </option>
                <option value="false">
                  {field.key === "allowed" ? "허용 안 함" : "사용 안 함"}
                </option>
              </>
            ) : (
              <>
                {value && !options.some((option) => option.value === value) ? (
                  <option value={value}>{value}</option>
                ) : null}
                {options
                  .filter(
                    (option, i, all) =>
                      all.findIndex((item) => item.value === option.value) ===
                      i,
                  )
                  .map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
              </>
            )}
          </select>
        ) : field.type === "textarea" ? (
          <textarea
            id={id}
            aria-label={label}
            value={value}
            readOnly={locked}
            disabled={saving}
            onChange={(event) => update(field.key, event.target.value)}
            aria-invalid={Boolean(fieldError)}
            aria-describedby={errorId}
          />
        ) : (
          <input
            id={id}
            aria-label={label}
            type={
              field.type === "date" || field.type === "number"
                ? field.type
                : "text"
            }
            value={value}
            readOnly={locked}
            disabled={saving}
            step={field.type === "number" ? 1 : undefined}
            onChange={(event) => update(field.key, event.target.value)}
            aria-invalid={Boolean(fieldError)}
            aria-describedby={errorId}
          />
        )}
        {fieldError ? (
          <small className="field-error" id={errorId}>
            {fieldError}
          </small>
        ) : null}
      </label>
    );
  }
  const updateDraft = (key: string, value: string) => {
    setDraft((previous) => ({ ...previous, [key]: value }));
    setSuccess("");
  };
  const editing =
    Boolean(selected) ||
    config.kind === "assignments" ||
    (config.kind === "permissions" &&
      loaded &&
      ready &&
      pagination?.total === 0);
  return (
    <div className={`screen screen-${config.kind}`}>
      <div className="page-heading">
        <div>
          <span className="eyebrow">공통 관리 / 업무 설정</span>
          <h1>{config.title}</h1>
          <p>{config.description}</p>
        </div>
        <span className="page-icon">
          <Icon name={config.kind} width="28" height="28" />
        </span>
      </div>
      {error && !denied ? <Notice error>{error}</Notice> : null}
      {denied ? (
        <Notice error>접근 권한이 없습니다.</Notice>
      ) : (
        <>
          <form
            className="panel search-panel"
            onSubmit={(event) => {
              event.preventDefault();
              const next = { page: 1, size: 20, ...filters };
              if (JSON.stringify(next) === JSON.stringify(query))
                setRevision((value) => value + 1);
              else changeContext(next);
            }}
          >
            <div className="filter-grid">
              {config.filters.map((field) =>
                control(
                  field,
                  filters,
                  (key, value) => {
                    setFilters((previous) => ({ ...previous, [key]: value }));
                    setFilterErrors((previous) => ({ ...previous, [key]: "" }));
                    setSuccess("");
                  },
                  true,
                ),
              )}
              <button
                className="primary"
                type="submit"
                disabled={
                  saving ||
                  Boolean(config.requireQuery && !filters[config.requireQuery])
                }
              >
                <Icon name="search" />
                {config.kind === "users"
                  ? "검색"
                  : config.kind === "structure"
                    ? "계층 조회"
                    : config.kind === "permissions"
                      ? "현재 설정 조회"
                      : "조회"}
              </button>
            </div>
          </form>
          {lookups.loading ? (
            <Notice>선택 후보를 불러오는 중입니다.</Notice>
          ) : null}
          {lookups.error ? (
            <Notice error>
              {lookups.error}{" "}
              <button onClick={lookups.retry}>후보 재시도</button>
            </Notice>
          ) : null}
          {codes.loading ? (
            <Notice>상위코드 후보를 불러오는 중입니다.</Notice>
          ) : null}
          {codes.error ? (
            <Notice error>
              {codes.error}{" "}
              <button onClick={codes.retry}>상위코드 후보 재시도</button>
            </Notice>
          ) : null}
          {loading ? <LoadingState /> : null}
          {!ready ? (
            <Notice>
              먼저 {config.filters[0]?.label} 항목을 선택해 주세요.
            </Notice>
          ) : null}
          {success ? <Notice success>{success}</Notice> : null}
          {ready && loaded && !loading && !error && !rows.length ? (
            <EmptyState />
          ) : null}
          {rows.length &&
          !["execution", "organizations"].includes(config.kind) ? (
            <section
              className="panel records"
              aria-label={`${config.title} 조회 목록`}
            >
              <div className="section-heading">
                <h2>
                  {config.kind === "structure"
                    ? "메뉴 계층"
                    : config.kind === "codes"
                      ? "그룹별 상세코드"
                      : "조회 목록"}
                </h2>
                <span>행을 선택하여 아래에서 편집합니다</span>
              </div>
              <div className="table-scroll">
                <table>
                  <thead>
                    <tr>
                      {config.columns.map((field) => (
                        <th scope="col" key={field.key}>
                          {field.label}
                        </th>
                      ))}
                      <th scope="col">작업</th>
                    </tr>
                  </thead>
                  <tbody>
                    {rows.map((row) => {
                      const data = row as unknown as Record<string, unknown>;
                      return (
                        <tr
                          key={config.identity(row)}
                          aria-selected={
                            selected &&
                            config.identity(selected) === config.identity(row)
                          }
                        >
                          {config.columns.map((field, index) => (
                            <td
                              key={field.key}
                              className={
                                field.key.endsWith("_id")
                                  ? "identifier"
                                  : undefined
                              }
                            >
                              {index === 0 &&
                              (config.kind === "structure" ||
                                config.kind === "codes") ? (
                                <span
                                  className="tree-label"
                                  style={{
                                    paddingLeft:
                                      config.kind === "structure"
                                        ? `${Math.min(Number(data.level) || 0, 12) * 14}px`
                                        : `${codeDepth(String(data.code)) * 20}px`,
                                  }}
                                >
                                  {data.parent_code || data.parent_menu_id
                                    ? "↳ "
                                    : ""}
                                  {display(data[field.key])}
                                </span>
                              ) : (
                                <DataValue value={data[field.key]} />
                              )}
                            </td>
                          ))}
                          <td>
                            <button
                              disabled={saving || loading}
                              onClick={() => choose(row)}
                            >
                              선택
                            </button>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            </section>
          ) : null}
          {config.kind === "organizations" || config.kind === "execution" ? (
            pagination ? (
              <div className="result-total">전체 {pagination.total}건</div>
            ) : null
          ) : (
            <Pager
              pagination={pagination}
              disabled={loading || saving}
              onPage={(page) => changeContext({ ...query, page })}
            />
          )}
          {editing ? (
            <section className="panel editor" aria-label={config.editorTitle}>
              <div className="section-heading">
                <h2>{config.editorTitle}</h2>
                {config.kind === "users" ? (
                  <span className="readonly-tag">원천 인사정보 읽기전용</span>
                ) : null}
              </div>
              {config.kind === "organizations" && selected ? (
                <div className="relation-map">
                  <div>
                    <small>현재 상위조직</small>
                    <strong>{display(toDraft(selected).parent_code)}</strong>
                  </div>
                  <span>↓</span>
                  <div className="current">
                    <small>조회 조직 / 조직구분</small>
                    <strong>
                      {display(toDraft(selected).organization_code)} /{" "}
                      {display(toDraft(selected).organization_type)}
                    </strong>
                  </div>
                  <span>↓</span>
                  <div>
                    <small>현재 하위조직</small>
                    <strong>{display(toDraft(selected).child_codes)}</strong>
                  </div>
                </div>
              ) : null}
              {config.kind === "permissions" ? (
                <div className="permission-summary">
                  <strong>현재 접근 허용 여부</strong>
                  <span>
                    {selected
                      ? display(toDraft(selected).allowed === "true")
                      : "미설정 — 불허 판정이 아닙니다."}
                  </span>
                </div>
              ) : null}
              <div className="editor-grid">
                {config.fields.map((field) =>
                  control(field, draft, updateDraft),
                )}
              </div>
              {config.kind === "assignments" ? (
                <p className="source-line">
                  보직 기반·수동 구분:{" "}
                  {selected
                    ? toDraft(selected).origin
                    : "저장 후 서버에서 확인"}
                </p>
              ) : null}
              <div className="editor-actions">
                <button
                  disabled={saving}
                  onClick={() => {
                    setDraft({ ...baseline });
                    setFieldErrors({});
                    setSuccess("");
                  }}
                >
                  취소
                </button>
                {config.kind === "assignments" ? (
                  <>
                    <button
                      className="primary"
                      disabled={saving || loading || Boolean(lookups.error)}
                      onClick={() => void save("부여")}
                    >
                      부여
                    </button>
                    <button
                      disabled={!selected || saving || loading}
                      onClick={() => void save("변경")}
                    >
                      변경
                    </button>
                    <button
                      className="danger"
                      disabled={!selected || saving || loading}
                      onClick={() => void save("회수")}
                    >
                      회수
                    </button>
                  </>
                ) : (
                  <button
                    className="primary"
                    disabled={
                      saving ||
                      loading ||
                      lookups.loading ||
                      codes.loading ||
                      Boolean(lookups.error || codes.error)
                    }
                    onClick={() => void save()}
                  >
                    <Icon name="save" />
                    {saving ? "저장 중…" : config.saveLabel || "저장"}
                  </button>
                )}
              </div>
              {config.kind === "users" &&
              selected &&
              access.allowed("SCR-USER-ROLES", "/admin/user-roles") ? (
                <Link
                  className="text-link"
                  to={`/admin/user-roles?user_id=${encodeURIComponent(toDraft(selected).user_id)}`}
                >
                  사용자 역할 관리로 이동
                </Link>
              ) : null}
            </section>
          ) : null}
          {config.extra?.(selected, rows, () => {
            setDenied(true);
            setRows([]);
            choose();
          })}
          {config.kind === "execution" ? (
            <section
              className="panel execution-links"
              aria-label="사용 가능한 메뉴"
            >
              <h2>사용 가능한 메뉴</h2>
              <p>연결 화면은 서버에서 허용한 실행정보를 사용합니다.</p>
              {access.menus.map((menu) => (
                <div key={menu.menu_id}>
                  {safeInternalUrl(menu.url) ? (
                    <Link to={menu.url}>
                      {menu.screen_id} · {menu.url}
                    </Link>
                  ) : (
                    <span>{menu.screen_id} — URL 안전성 정책 확인 대기</span>
                  )}
                </div>
              ))}
            </section>
          ) : null}
          {config.policy ? (
            <aside className="policy">
              <strong>정책 확인 필요</strong> {config.policy}
            </aside>
          ) : null}
          {error && !denied ? (
            <button
              disabled={loading || saving}
              onClick={() => {
                if (retryMode === "write") void save(lastAction.current);
                else setRevision((value) => value + 1);
              }}
            >
              {retryMode === "write" ? "저장 재시도" : "조회 재시도"}
            </button>
          ) : null}
        </>
      )}
    </div>
  );
}
