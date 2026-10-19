import { list, put } from "./client";
import type { CodeGroup, CodeGroupInput, Paging } from "./types";
export interface GroupQuery extends Paging {
  group_id?: string;
  filter?: string;
}
export const listCodeGroups = (q: GroupQuery, signal?: AbortSignal) =>
  list<CodeGroup>("/api/code-groups", { ...q }, signal);
export const saveCodeGroup = (input: CodeGroupInput, signal?: AbortSignal) =>
  put<CodeGroup, CodeGroupInput>("/api/code-groups", input, signal);
