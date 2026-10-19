import { useMemo } from "react";
import { useLocation, useSearchParams } from "react-router-dom";
import ResourcePage, {
  dateValue,
  type ScreenConfig,
} from "../components/ResourcePage";
import {
  listUserRoleAssignments,
  saveUserRoleAssignment,
} from "../api/user_role_assignments";
import type { UserRoleAssignment, UserRoleAssignmentInput } from "../api/types";

const config: ScreenConfig<UserRoleAssignment, UserRoleAssignmentInput> = {
  kind: "assignments",
  title: "사용자 역할 관리",
  description:
    "사용자별 다중 역할과 각 유효기간을 비교하고 승인자를 기록합니다.",
  editorTitle: "역할 부여·변경·회수 기록",
  filters: [{ key: "user_id", label: "대상 사용자", source: "users" }],
  columns: [
    { key: "user_id", label: "대상 사용자" },
    { key: "role_id", label: "역할" },
    { key: "valid_from", label: "유효 시작일" },
    { key: "valid_to", label: "유효 종료일" },
    { key: "approver_id", label: "승인자" },
    { key: "origin", label: "보직 기반·수동" },
    { key: "action", label: "명령" },
    { key: "is_current", label: "현재 적용 (서버 판정)" },
  ],
  fields: [
    { key: "user_id", label: "대상 사용자", source: "users", required: true },
    { key: "role_id", label: "역할", source: "roles", required: true },
    { key: "valid_from", label: "유효 시작일", type: "date" },
    { key: "valid_to", label: "유효 종료일", type: "date" },
    { key: "approver_id", label: "승인자", source: "users", required: true },
  ],
  identity: (row) => row.assignment_id,
  read: (q, signal) =>
    listUserRoleAssignments(
      {
        page: q.page,
        size: q.size,
        user_id: String(q.user_id || ""),
      },
      signal,
    ),
  write: saveUserRoleAssignment,
  serialize: (draft, selected, id, action = "부여") => ({
    assignment_id: action === "부여" ? id : selected!.assignment_id,
    user_id: draft.user_id,
    role_id: draft.role_id,
    valid_from: dateValue(draft.valid_from),
    valid_to: dateValue(draft.valid_to),
    approver_id: draft.approver_id,
    action,
  }),
  policy:
    "승인자 신원 검증·승인 절차·회수 효력·보직 매핑은 OQ-ROLE-001 확인 대상입니다. 현재 행위자를 자동 승인자로 지정하지 않습니다.",
};
export default function UserRoleAssignmentPage() {
  const [params] = useSearchParams();
  const location = useLocation();
  const user_id = params.get("user_id") || "";
  const contextual = useMemo(
    () => ({ ...config, initialQuery: { user_id } }),
    [user_id],
  );
  return (
    <ResourcePage key={`${location.pathname}:${user_id}`} config={contextual} />
  );
}
