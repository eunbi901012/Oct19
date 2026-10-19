import { list, put } from "./client";
import type { MenuPermission, MenuPermissionInput, Paging } from "./types";
export interface PermissionQuery extends Paging {
  role_id?: string;
  organization_code?: string;
  user_id?: string;
  menu_id?: string;
  filter?: string;
}
export const listMenuPermissions = (q: PermissionQuery, signal?: AbortSignal) =>
  list<MenuPermission>("/api/menu-permissions", { ...q }, signal);
export const saveMenuPermission = (
  input: MenuPermissionInput,
  signal?: AbortSignal,
) =>
  put<MenuPermission, MenuPermissionInput>(
    "/api/menu-permissions",
    input,
    signal,
  );
