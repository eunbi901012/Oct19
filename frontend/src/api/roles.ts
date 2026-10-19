import { list, put } from "./client";
import type { Paging, Role, RoleInput } from "./types";
export interface RoleQuery extends Paging {
  role_id?: string;
  filter?: string;
}
export const listRoles = (q: RoleQuery, signal?: AbortSignal) =>
  list<Role>("/api/roles", { ...q }, signal);
export const saveRole = (input: RoleInput, signal?: AbortSignal) =>
  put<Role, RoleInput>("/api/roles", input, signal);
