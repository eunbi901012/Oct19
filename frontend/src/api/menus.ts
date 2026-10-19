import { list, put, request, ApiFailure } from "./client";
import type {
  AccessibleMenuResult,
  ApiResponse,
  MenuStructure,
  MenuStructureInput,
  Paging,
} from "./types";
export interface MenuQuery extends Paging {
  parent_menu_id?: string;
}
export const getMenuStructure = (q: MenuQuery, signal?: AbortSignal) =>
  list<MenuStructure>("/api/menu-structure", { ...q }, signal);
export const saveMenuStructure = (
  input: MenuStructureInput,
  signal?: AbortSignal,
) =>
  put<MenuStructure, MenuStructureInput>("/api/menu-structure", input, signal);
export async function getAccessibleMenus(
  signal?: AbortSignal,
): Promise<AccessibleMenuResult> {
  const response = await request<ApiResponse<AccessibleMenuResult>>(
    "/api/accessible-menus",
    { signal },
  );
  const menus = response.data?.menus;
  if (
    !Array.isArray(menus) ||
    !menus.every(
      (menu) =>
        typeof menu.menu_id === "string" &&
        typeof menu.parent_menu_id === "string" &&
        Number.isInteger(menu.display_order) &&
        typeof menu.screen_id === "string" &&
        typeof menu.url === "string",
    ) ||
    new Set(menus.map((menu) => menu.menu_id)).size !== menus.length
  ) {
    throw new ApiFailure(502, "메뉴 접근 판정 형식이 올바르지 않습니다.");
  }
  return response.data;
}
