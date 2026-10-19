import ResourcePage, { type ScreenConfig } from "../components/ResourcePage";
import {
  listMenuPermissions,
  saveMenuPermission,
} from "../api/menu_permissions";
import type { MenuPermission, MenuPermissionInput } from "../api/types";

const targets = [
  { key: "role_id", label: "역할", source: "roles" as const },
  { key: "organization_code", label: "조직", source: "organizations" as const },
  { key: "user_id", label: "사용자", source: "users" as const },
  { key: "menu_id", label: "메뉴", source: "menus" as const },
];
const config: ScreenConfig<MenuPermission, MenuPermissionInput> = {
  kind: "permissions",
  title: "메뉴 권한 관리",
  description:
    "권한 주체와 접근 대상 메뉴를 선택하여 단일 허용 설정을 관리합니다.",
  editorTitle: "선택 대상과 메뉴의 단일 설정",
  saveLabel: "허용 설정 저장",
  filters: targets,
  columns: [
    { key: "role_id", label: "역할" },
    { key: "organization_code", label: "조직" },
    { key: "user_id", label: "사용자" },
    { key: "menu_id", label: "메뉴" },
    { key: "allowed", label: "현재 접근 허용 여부" },
  ],
  fields: [
    ...targets.map((field) => ({
      ...field,
      required: field.key === "menu_id",
    })),
    {
      key: "allowed",
      label: "변경할 접근 허용 여부",
      type: "boolean",
      required: true,
    },
  ],
  identity: (row) => row.permission_id,
  read: (q, signal) =>
    listMenuPermissions(
      {
        page: q.page,
        size: q.size,
        role_id: String(q.role_id || ""),
        organization_code: String(q.organization_code || ""),
        user_id: String(q.user_id || ""),
        menu_id: String(q.menu_id || ""),
      },
      signal,
    ),
  write: saveMenuPermission,
  serialize: (draft, selected, id) => ({
    permission_id: selected?.permission_id || id,
    role_id: draft.role_id || null,
    organization_code: draft.organization_code || null,
    user_id: draft.user_id || null,
    menu_id: draft.menu_id,
    allowed: draft.allowed === "true",
  }),
  policy:
    "미설정을 불허로 판단하지 않습니다. 대상 조합·우선순위와 후보 조회 범위는 OQ-AUTH-001/002 확인 대상입니다.",
};
export default function MenuPermissionPage() {
  return <ResourcePage config={config} />;
}
