import { list, put } from "./client";
import type {
  MenuExecutionInfo,
  MenuExecutionInfoInput,
  Paging,
} from "./types";
export interface ExecutionQuery extends Paging {
  menu_id?: string;
  filter?: string;
}
export const listMenuExecutionInfo = (
  q: ExecutionQuery,
  signal?: AbortSignal,
) => list<MenuExecutionInfo>("/api/menu-execution-info", { ...q }, signal);
export const saveMenuExecutionInfo = (
  input: MenuExecutionInfoInput,
  signal?: AbortSignal,
) =>
  put<MenuExecutionInfo, MenuExecutionInfoInput>(
    "/api/menu-execution-info",
    input,
    signal,
  );
