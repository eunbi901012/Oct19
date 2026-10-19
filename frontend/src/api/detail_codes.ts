import { list, put } from "./client";
import type { DetailCode, DetailCodeInput, Paging } from "./types";
export interface DetailQuery extends Paging {
  group_id: string;
  filter?: string;
}
export const listDetailCodes = (q: DetailQuery, signal?: AbortSignal) =>
  list<DetailCode>("/api/detail-codes", { ...q }, signal);
export const saveDetailCode = (input: DetailCodeInput, signal?: AbortSignal) =>
  put<DetailCode, DetailCodeInput>("/api/detail-codes", input, signal);
