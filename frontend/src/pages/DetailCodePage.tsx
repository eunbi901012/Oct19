import ResourcePage, { type ScreenConfig } from "../components/ResourcePage";
import { listDetailCodes, saveDetailCode } from "../api/detail_codes";
import type { DetailCode, DetailCodeInput } from "../api/types";

const config: ScreenConfig<DetailCode, DetailCodeInput> = {
  kind: "codes",
  title: "상세코드 관리",
  description:
    "선택한 코드그룹의 상세코드와 상위 연결, 선택값 제공 순서를 관리합니다.",
  editorTitle: "상세코드·상위 연결",
  requireQuery: "group_id",
  filters: [{ key: "group_id", label: "코드그룹", source: "groups" }],
  columns: [
    { key: "code", label: "상세코드 계층" },
    { key: "parent_code", label: "상위코드" },
    { key: "sort_order", label: "정렬순서" },
  ],
  fields: [
    { key: "group_id", label: "코드그룹", readonly: true, required: true },
    { key: "code", label: "상세코드", readonly: true, required: true },
    { key: "parent_code", label: "상위코드", source: "codes" },
    { key: "sort_order", label: "정렬순서", type: "number", required: true },
  ],
  identity: (row) => `${row.group_id}:${row.code}`,
  read: (q, signal) =>
    listDetailCodes(
      { page: q.page, size: q.size, group_id: String(q.group_id) },
      signal,
    ),
  write: saveDetailCode,
  serialize: (draft, selected) => ({
    group_id: selected!.group_id,
    code: selected!.code,
    // Omitted optional parent preserves the persisted value; an unchanged root
    // must not be reinterpreted as a new (unapproved) empty-parent reference.
    ...(draft.parent_code !== selected!.parent_code
      ? { parent_code: draft.parent_code || "" }
      : {}),
    sort_order: Number(draft.sort_order),
  }),
  policy:
    "추가 명시 필드는 OQ-UI-002 확인 필요입니다. 현재 계약의 코드·그룹·정렬순서·상위코드만 저장합니다.",
};
export default function DetailCodePage() {
  return <ResourcePage config={config} />;
}
