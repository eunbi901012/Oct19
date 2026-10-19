import type { ApiResponse, Page, Paging } from "./types";

export type Query = Record<
  string,
  string | number | boolean | null | undefined
>;
export interface FieldError {
  field: string;
  message: string;
}
export class ApiFailure extends Error {
  constructor(
    public status: number,
    message: string,
    public field_errors: FieldError[] = [],
  ) {
    super(message);
  }
}
export function isDenied(error: unknown): boolean {
  return (
    error instanceof ApiFailure &&
    (error.status === 401 || error.status === 403)
  );
}
export function messageOf(error: unknown): string {
  return error instanceof Error ? error.message : "요청을 완료하지 못했습니다.";
}
export function queryString(query: Query): string {
  const params = new URLSearchParams();
  Object.entries(query).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "")
      params.set(key, String(value));
  });
  return params.toString();
}
export async function request<T>(
  path: string,
  init: RequestInit = {},
): Promise<T> {
  const response = await fetch(path, {
    ...init,
    credentials: "same-origin",
    headers: { Accept: "application/json", ...init.headers },
  });
  let body: unknown;
  try {
    body = await response.json();
  } catch {
    throw new ApiFailure(
      response.status,
      "서버 응답 형식을 확인할 수 없습니다.",
    );
  }
  const value = body as {
    success?: boolean;
    message?: string;
    meta?: unknown;
    field_errors?: FieldError[];
  };
  if (!response.ok || value.success !== true) {
    throw new ApiFailure(
      response.status,
      value.message || "요청에 실패했습니다.",
      value.field_errors || [],
    );
  }
  if (!value.meta || typeof value.meta !== "object") {
    throw new ApiFailure(502, "응답 추적 정보가 누락되었습니다.");
  }
  return body as T;
}
export function list<T>(
  path: string,
  query: Query,
  signal?: AbortSignal,
): Promise<Page<T>> {
  return request<Page<T>>(`${path}?${queryString(query)}`, { signal });
}
export async function put<T, I>(
  path: string,
  input: I,
  signal?: AbortSignal,
): Promise<T> {
  const response = await request<ApiResponse<T>>(path, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(input),
    signal,
  });
  return response.data;
}
export async function allPages<T>(
  read: (query: Paging, signal?: AbortSignal) => Promise<Page<T>>,
  signal?: AbortSignal,
): Promise<T[]> {
  const rows: T[] = [];
  let page = 1;
  for (;;) {
    const response = await read({ page, size: 100 }, signal);
    rows.push(...response.data);
    if (rows.length >= response.pagination.total) return rows;
    if (response.data.length === 0)
      throw new ApiFailure(502, "후보 목록의 페이지 정보가 일치하지 않습니다.");
    page += 1;
  }
}
