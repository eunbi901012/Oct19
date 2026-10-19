import ResourcePage, { type ScreenConfig } from "../components/ResourcePage";
import { listRoles, saveRole } from "../api/roles";
import type { Role, RoleInput } from "../api/types";

const config: ScreenConfig<Role, RoleInput> = {
  kind: "roles",
  title: "역할 관리",
  description: "역할의 목적과 부여 기준, 데이터 범위 기본값을 정의합니다.",
  editorTitle: "역할 정의",
  filters: [{ key: "role_id", label: "역할 식별정보", source: "roles" }],
  columns: [
    { key: "role_id", label: "역할 식별정보" },
    { key: "purpose", label: "목적" },
    { key: "grant_criteria", label: "부여 기준" },
    { key: "default_data_scope", label: "데이터 범위 기본값" },
  ],
  fields: [
    { key: "role_id", label: "역할 식별정보", readonly: true, required: true },
    { key: "purpose", label: "목적", type: "textarea" },
    { key: "grant_criteria", label: "부여 기준", type: "textarea" },
    {
      key: "default_data_scope",
      label: "데이터 범위 기본값",
      type: "textarea",
    },
  ],
  identity: (row) => row.role_id,
  read: (q, signal) =>
    listRoles(
      { page: q.page, size: q.size, role_id: String(q.role_id || "") },
      signal,
    ),
  write: saveRole,
  serialize: (draft, selected) => ({
    role_id: selected!.role_id,
    purpose: draft.purpose,
    grant_criteria: draft.grant_criteria,
    default_data_scope: draft.default_data_scope,
  }),
  policy:
    "역할 정의 저장은 사용자 부여내역과 별도입니다. 권한 코드·관리 범위는 OQ-AUTH-001 확인 대상입니다.",
};
export default function RolePage() {
  return <ResourcePage config={config} />;
}
