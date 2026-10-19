import ResourcePage, {
  dateValue,
  type ScreenConfig,
} from "../components/ResourcePage";
import OrganizationHistory from "../components/OrganizationHistory";
import {
  getOrganizationRelations,
  saveOrganizationRelation,
} from "../api/organizations";
import type { Organization, OrganizationInput } from "../api/types";

const config: ScreenConfig<Organization, OrganizationInput> = {
  kind: "organizations",
  title: "조직 관리",
  description:
    "현재 상하위 관계와 적용기간을 확인하고 변경 전 이력을 조회합니다.",
  editorTitle: "상위관계와 적용기간 함께 변경",
  saveLabel: "상위관계 저장",
  requireQuery: "organization_code",
  filters: [
    { key: "organization_code", label: "조직코드", source: "organizations" },
  ],
  columns: [
    { key: "organization_code", label: "조직코드" },
    { key: "organization_type", label: "조직구분" },
    { key: "parent_code", label: "상위조직" },
    { key: "child_codes", label: "하위조직" },
    { key: "valid_from", label: "적용 시작일" },
    { key: "valid_to", label: "적용 종료일" },
  ],
  fields: [
    {
      key: "organization_code",
      label: "조직코드",
      readonly: true,
      required: true,
    },
    { key: "parent_code", label: "변경할 상위조직", source: "organizations" },
    { key: "valid_from", label: "적용 시작일", type: "date" },
    { key: "valid_to", label: "적용 종료일", type: "date" },
  ],
  identity: (row) => row.organization_code,
  read: (q, signal) =>
    getOrganizationRelations(
      {
        page: q.page,
        size: q.size,
        organization_code: String(q.organization_code || ""),
      },
      signal,
    ),
  write: saveOrganizationRelation,
  serialize: (draft, selected) => ({
    organization_code: selected!.organization_code,
    parent_code: draft.parent_code,
    valid_from: dateValue(draft.valid_from),
    valid_to: dateValue(draft.valid_to),
  }),
  extra: (selected, _rows, onDenied) =>
    selected ? (
      <OrganizationHistory
        key={
          selected.organization_code +
          selected.parent_code +
          selected.valid_from +
          selected.valid_to
        }
        organization_code={selected.organization_code}
        onDenied={onDenied}
      />
    ) : null,
  policy:
    "조직 소유권·기간 경계·중복·순환·과거 정정은 OQ-ORG-001 확인 대상입니다. 사용자 인사·역할은 변경하지 않습니다.",
};
export default function OrganizationPage() {
  return <ResourcePage config={config} />;
}
