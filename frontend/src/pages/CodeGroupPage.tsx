import ResourcePage, { type ScreenConfig } from "../components/ResourcePage";
import { listCodeGroups, saveCodeGroup } from "../api/code_groups";
import type { CodeGroup, CodeGroupInput } from "../api/types";

const config: ScreenConfig<CodeGroup, CodeGroupInput> = {
  kind: "groups",
  title: "코드그룹 관리",
  description: "그룹ID별 독립적인 명칭·설명·관리부서를 관리합니다.",
  editorTitle: "선택 그룹 정의",
  filters: [{ key: "group_id", label: "그룹ID", source: "groups" }],
  columns: [
    { key: "group_id", label: "그룹ID" },
    { key: "name", label: "명칭" },
    { key: "description", label: "설명" },
    { key: "managing_department", label: "관리부서" },
  ],
  fields: [
    { key: "group_id", label: "그룹ID", readonly: true, required: true },
    { key: "name", label: "명칭" },
    { key: "managing_department", label: "관리부서", source: "organizations" },
    { key: "description", label: "설명", type: "textarea" },
  ],
  identity: (row) => row.group_id,
  read: (q, signal) =>
    listCodeGroups(
      { page: q.page, size: q.size, group_id: String(q.group_id || "") },
      signal,
    ),
  write: saveCodeGroup,
  serialize: (draft, selected) => ({
    group_id: selected!.group_id,
    name: draft.name,
    description: draft.description,
    managing_department: draft.managing_department || "",
  }),
  policy:
    "그룹 정의 저장은 상세코드 값을 변경하지 않습니다. 삭제·보관·참조 보호는 OQ-DATA-002 확인 대상입니다.",
};
export default function CodeGroupPage() {
  return <ResourcePage config={config} />;
}
