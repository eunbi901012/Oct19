import ResourcePage, { type ScreenConfig } from "../components/ResourcePage";
import {
  listMenuExecutionInfo,
  saveMenuExecutionInfo,
} from "../api/menu_execution_info";
import type { MenuExecutionInfo, MenuExecutionInfoInput } from "../api/types";

const config: ScreenConfig<MenuExecutionInfo, MenuExecutionInfoInput> = {
  kind: "execution",
  title: "메뉴 정보 관리",
  description: "메뉴와 화면ID·URL을 연결하고 사용 가능한 메뉴를 실행합니다.",
  editorTitle: "메뉴 → 실행정보",
  requireQuery: "menu_id",
  filters: [{ key: "menu_id", label: "메뉴", source: "menus" }],
  columns: [
    { key: "menu_id", label: "메뉴" },
    { key: "screen_id", label: "화면ID" },
    { key: "url", label: "URL" },
  ],
  fields: [
    { key: "menu_id", label: "메뉴", readonly: true, required: true },
    { key: "screen_id", label: "화면ID", required: true },
    { key: "url", label: "URL", required: true },
  ],
  identity: (row) => row.menu_id,
  read: (q, signal) =>
    listMenuExecutionInfo(
      {
        page: q.page,
        size: q.size,
        menu_id: String(q.menu_id || ""),
      },
      signal,
    ),
  write: saveMenuExecutionInfo,
  serialize: (draft, selected) => ({
    menu_id: selected!.menu_id,
    screen_id: draft.screen_id,
    url: draft.url,
  }),
  policy:
    "사용 가능 판정·URL 안전성 범위는 OQ-UI-003 확인 대기입니다. 실행은 안전한 동일 출처 내부 URL만 제공하며 서버 정책을 대체하지 않습니다.",
};
export default function MenuExecutionInfoPage() {
  return <ResourcePage config={config} />;
}
