import ResourcePage, { type ScreenConfig } from "../components/ResourcePage";
import { searchUsers, updateUserSettings } from "../api/users";
import type { User, UserInput } from "../api/types";

const config: ScreenConfig<User, UserInput> = {
  kind: "users",
  title: "사용자 관리",
  description: "인사 원천정보를 조회하고 시스템 사용여부를 관리합니다.",
  editorTitle: "선택 사용자 · 로컬 설정",
  saveLabel: "사용여부 저장",
  filters: [
    { key: "employee_number", label: "교번" },
    { key: "person_name", label: "성명" },
    { key: "organization_code", label: "소속", source: "organizations" },
    { key: "job_grade", label: "직급" },
    { key: "employment_status", label: "재직상태" },
    { key: "business_roles", label: "역할", source: "roles" },
    { key: "enabled", label: "사용여부", type: "boolean" },
  ],
  columns: [
    { key: "employee_number", label: "교번" },
    { key: "person_name", label: "성명" },
    { key: "organization_code", label: "소속" },
    { key: "job_grade", label: "직급" },
    { key: "employment_status", label: "재직상태" },
    { key: "business_roles", label: "역할" },
    { key: "enabled", label: "사용여부" },
    { key: "position", label: "보직" },
    { key: "retired_on", label: "퇴직일자" },
    { key: "last_synced_at", label: "최종 동기화일시" },
  ],
  fields: [
    {
      key: "enabled",
      label: "시스템 사용여부",
      type: "boolean",
      required: true,
    },
  ],
  identity: (row) => row.user_id,
  read: (q, signal) =>
    searchUsers(
      {
        page: q.page,
        size: q.size,
        employee_number: String(q.employee_number || ""),
        person_name: String(q.person_name || ""),
        organization_code: String(q.organization_code || ""),
        job_grade: String(q.job_grade || ""),
        employment_status: String(q.employment_status || ""),
        business_roles: String(q.business_roles || ""),
        enabled:
          q.enabled === "true"
            ? true
            : q.enabled === "false"
              ? false
              : undefined,
      },
      signal,
    ),
  write: updateUserSettings,
  serialize: (draft, selected) => ({
    user_id: selected!.user_id,
    enabled: draft.enabled === "true",
  }),
  policy:
    "KORUS 원천정보는 수정할 수 없습니다. 동기화·접속 차단 정책은 OQ-DATA-001/002 확인 대상입니다.",
};
export default function UserPage() {
  return <ResourcePage config={config} />;
}
