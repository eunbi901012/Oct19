import ResourcePage, { type ScreenConfig } from "../components/ResourcePage";
import { getMenuStructure, saveMenuStructure } from "../api/menus";
import type { MenuStructure, MenuStructureInput } from "../api/types";

const config: ScreenConfig<MenuStructure, MenuStructureInput> = {
  kind: "structure",
  title: "메뉴 구조 관리",
  description: "메뉴의 부모 연결과 계층, 동일 부모 내 표시순서를 편집합니다.",
  editorTitle: "선택 메뉴 구조",
  saveLabel: "구조 저장",
  filters: [{ key: "parent_menu_id", label: "부모메뉴", source: "menus" }],
  columns: [
    { key: "menu_id", label: "메뉴 계층" },
    { key: "parent_menu_id", label: "부모메뉴" },
    { key: "level", label: "계층단계" },
    { key: "display_order", label: "동일 계층 표시순서" },
  ],
  fields: [
    { key: "menu_id", label: "메뉴", readonly: true, required: true },
    { key: "parent_menu_id", label: "부모메뉴", source: "menus" },
    { key: "level", label: "계층단계", type: "number", required: true },
    { key: "display_order", label: "표시순서", type: "number", required: true },
  ],
  identity: (row) => row.menu_id,
  read: (q, signal) =>
    getMenuStructure(
      {
        page: q.page,
        size: q.size,
        parent_menu_id: String(q.parent_menu_id || ""),
      },
      signal,
    ),
  write: saveMenuStructure,
  serialize: (draft, selected) => ({
    menu_id: selected!.menu_id,
    parent_menu_id: draft.parent_menu_id || "",
    level: Number(draft.level),
    display_order: Number(draft.display_order),
  }),
  policy:
    "동일 부모 내 순서만 변경합니다. 계층 완전성과 순환 판정·허용 범위는 정책 확인 대상입니다.",
};
export default function MenuStructurePage() {
  return <ResourcePage config={config} />;
}
