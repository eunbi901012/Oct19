import { render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import App from "../../src/App";

const definitions = [
  ["users", "SCR-USERS", "사용자 관리", "/api/users"],
  ["organizations", "SCR-ORGANIZATIONS", "조직 관리", "/api/organizations"],
  ["roles", "SCR-ROLES", "역할 관리", "/api/roles"],
  [
    "user-roles",
    "SCR-USER-ROLES",
    "사용자 역할 관리",
    "/api/user-role-assignments",
  ],
  [
    "menu-permissions",
    "SCR-MENU-PERMISSIONS",
    "메뉴 권한 관리",
    "/api/menu-permissions",
  ],
  [
    "menu-structure",
    "SCR-MENU-STRUCTURE",
    "메뉴 구조 관리",
    "/api/menu-structure",
  ],
  ["menu-info", "SCR-MENU-INFO", "메뉴 정보 관리", "/api/menu-execution-info"],
  ["code-groups", "SCR-CODE-GROUPS", "코드그룹 관리", "/api/code-groups"],
  ["detail-codes", "SCR-DETAIL-CODES", "상세코드 관리", "/api/detail-codes"],
] as const;
const key = "fixture-key";
const user = {
  user_id: key,
  employee_number: key,
  person_name: key,
  organization_code: key,
  job_grade: key,
  employment_status: key,
  business_roles: [key],
  enabled: true,
  position: key,
  retired_on: null,
  last_synced_at: null,
};
const records: Record<string, Record<string, unknown>> = {
  "/api/users": user,
  "/api/organizations": {
    organization_code: key,
    organization_type: key,
    parent_code: key,
    child_codes: [],
    valid_from: null,
    valid_to: null,
  },
  "/api/roles": {
    role_id: key,
    purpose: key,
    grant_criteria: key,
    default_data_scope: key,
  },
  "/api/user-role-assignments": {
    assignment_id: key,
    user_id: key,
    role_id: key,
    approver_id: key,
    valid_from: null,
    valid_to: null,
    origin: "보직 기반",
    action: "부여",
    is_current: false,
  },
  "/api/menu-permissions": {
    permission_id: key,
    role_id: key,
    organization_code: null,
    user_id: null,
    menu_id: key,
    allowed: true,
  },
  "/api/menu-structure": {
    menu_id: key,
    parent_menu_id: "",
    level: 1,
    display_order: 1,
  },
  "/api/menu-execution-info": {
    menu_id: key,
    screen_id: "SCR-USERS",
    url: "/admin/users",
  },
  "/api/code-groups": {
    group_id: key,
    name: key,
    description: key,
    managing_department: key,
  },
  "/api/detail-codes": {
    group_id: key,
    code: key,
    parent_code: "",
    sort_order: 1,
  },
  "/api/organization-relation-history": {
    relation_id: key,
    organization_code: key,
    parent_code: "previous-parent",
    valid_from: null,
    valid_to: null,
  },
};
let denied: string | undefined;
let failWrite = false;
let emptyPermission = false;
let menus = definitions.map(([route, screen_id], i) => ({
  menu_id: `menu-${i}`,
  parent_menu_id: "",
  display_order: i,
  screen_id,
  url: `/admin/${route}`,
}));
const writes: { path: string; body: Record<string, unknown> }[] = [];
const fetcher = vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
  const url = new URL(String(input), "http://localhost");
  if (url.pathname === denied) {
    return new Response(
      JSON.stringify({
        success: false,
        meta: {},
        message: "접근 권한이 없습니다.",
      }),
      { status: 403 },
    );
  }
  if (url.pathname === "/api/accessible-menus") {
    return new Response(
      JSON.stringify({ success: true, meta: {}, data: { menus } }),
    );
  }
  if (init?.method === "PUT") {
    const body = JSON.parse(String(init.body)) as Record<string, unknown>;
    writes.push({ path: url.pathname, body });
    if (failWrite) {
      return new Response(
        JSON.stringify({ success: false, meta: {}, message: "저장 실패" }),
        { status: 500 },
      );
    }
    records[url.pathname] = { ...records[url.pathname], ...body };
    return new Response(
      JSON.stringify({ success: true, meta: {}, data: records[url.pathname] }),
    );
  }
  expect(url.searchParams.get("page")).toBeTruthy();
  expect(url.searchParams.get("size")).toBeTruthy();
  return new Response(
    JSON.stringify({
      success: true,
      meta: {},
      data:
        emptyPermission && url.pathname === "/api/menu-permissions"
          ? []
          : records[url.pathname]
            ? [records[url.pathname]]
            : [],
      pagination: {
        page: Number(url.searchParams.get("page")),
        size: 20,
        total:
          emptyPermission && url.pathname === "/api/menu-permissions" ? 0 : 1,
      },
    }),
  );
});
function open(route: string) {
  window.history.replaceState({}, "", `/admin/${route}`);
  return render(<App />);
}
async function selectRow() {
  await userEvent.click(await screen.findByRole("button", { name: "선택" }));
}
async function querySelected(label: string, button = "조회") {
  await waitFor(() =>
    expect(
      Array.from(
        (screen.getByLabelText(label) as HTMLSelectElement).options,
      ).some((option) => option.value === key),
    ).toBe(true),
  );
  await userEvent.selectOptions(screen.getByLabelText(label), key);
  await userEvent.click(screen.getByRole("button", { name: button }));
}
beforeEach(() => {
  denied = undefined;
  failWrite = false;
  emptyPermission = false;
  writes.length = 0;
  menus = definitions.map(([route, screen_id], i) => ({
    menu_id: `menu-${i}`,
    parent_menu_id: "",
    display_order: i,
    screen_id,
    url: `/admin/${route}`,
  }));
  fetcher.mockClear();
  vi.stubGlobal("fetch", fetcher);
});
afterEach(() => vi.unstubAllGlobals());

describe("실제 App 접근 판정과 화면 계약", () => {
  it("허용 응답만 탐색에 표시하고 링크를 통해 보호 화면으로 이동한다", async () => {
    menus = menus.filter((menu) => menu.screen_id === "SCR-ROLES");
    open("roles");
    const nav = await screen.findByRole("navigation", { name: "업무 메뉴" });
    expect(
      within(nav).queryByRole("link", { name: "사용자 관리" }),
    ).not.toBeInTheDocument();
    await userEvent.click(within(nav).getByRole("link", { name: "역할 관리" }));
    expect(
      await screen.findByRole("heading", { name: "역할 관리", level: 1 }),
    ).toBeVisible();
  });
  it("미등재 직접 경로는 관리 데이터를 요청하지 않는다", async () => {
    menus = [];
    open("users");
    expect(await screen.findByText("접근 권한이 없습니다.")).toBeVisible();
    expect(
      fetcher.mock.calls.every(([url]) =>
        String(url).startsWith("/api/accessible-menus"),
      ),
    ).toBe(true);
  });
  it.each(definitions)(
    "%s의 조회와 페이지 합계를 표시한다",
    async (route, _id, title) => {
      open(route);
      if (route === "detail-codes") await querySelected("코드그룹 조회");
      if (route === "menu-info") await querySelected("메뉴 조회");
      if (route === "organizations") await querySelected("조직코드 조회");
      expect(
        await screen.findByRole("heading", { name: title, level: 1 }),
      ).toBeVisible();
      expect(await screen.findByText("전체 1건")).toBeVisible();
    },
  );
  it("사용자 저장은 원천 필드를 제외하고 취소한 boolean을 복원한다", async () => {
    open("users");
    await selectRow();
    const enabled = screen.getByLabelText("시스템 사용여부");
    await userEvent.selectOptions(enabled, "false");
    await userEvent.click(screen.getByRole("button", { name: "취소" }));
    await userEvent.click(
      screen.getByRole("button", { name: "사용여부 저장" }),
    );
    await waitFor(() => expect(writes).toHaveLength(1));
    expect(writes[0].body).toEqual({ user_id: key, enabled: true });
    expect(
      screen.getByRole("link", { name: "사용자 역할 관리로 이동" }),
    ).toHaveAttribute("href", `/admin/user-roles?user_id=${key}`);
  });
  it("조직 이력은 현재 관계가 아닌 실제 이력 API를 읽는다", async () => {
    open("organizations");
    await querySelected("조직코드 조회");
    await screen.findByLabelText("변경할 상위조직");
    await userEvent.click(screen.getByRole("button", { name: "이력 조회" }));
    expect(await screen.findByText("previous-parent")).toBeVisible();
    expect(
      fetcher.mock.calls.some(([url]) =>
        String(url).includes("/api/organization-relation-history?"),
      ),
    ).toBe(true);
  });
  it("역할 정의 취소는 세 정의와 저장 payload를 함께 복원한다", async () => {
    open("roles");
    await selectRow();
    await userEvent.clear(screen.getByLabelText("목적"));
    await userEvent.click(screen.getByRole("button", { name: "취소" }));
    await userEvent.click(screen.getByRole("button", { name: "저장" }));
    await waitFor(() => expect(writes[0]?.body.purpose).toBe(key));
    expect(writes[0].body.role_id).toBe(key);
  });
  it("부여 실패 재시도는 UUID를 보존하고 출처는 전송하지 않는다", async () => {
    open("user-roles");
    await selectRow();
    await userEvent.selectOptions(screen.getByLabelText("승인자"), key);
    failWrite = true;
    await userEvent.click(screen.getByRole("button", { name: "부여" }));
    await screen.findByText("저장 실패");
    await userEvent.click(screen.getByRole("button", { name: "부여" }));
    await waitFor(() => expect(writes).toHaveLength(2));
    expect(writes[0].body.assignment_id).toBe(writes[1].body.assignment_id);
    expect(writes[0].body.assignment_id).not.toBe(key);
    expect(writes[0].body).not.toHaveProperty("origin");
    expect(writes[0].body.action).toBe("부여");
  });
  it("권한 저장 후 서버 탐색 판정을 다시 읽는다", async () => {
    open("menu-permissions");
    await selectRow();
    const before = fetcher.mock.calls.filter(
      ([url]) => String(url) === "/api/accessible-menus",
    ).length;
    await userEvent.click(
      screen.getByRole("button", { name: "허용 설정 저장" }),
    );
    await waitFor(() =>
      expect(
        fetcher.mock.calls.filter(
          ([url]) => String(url) === "/api/accessible-menus",
        ).length,
      ).toBeGreaterThan(before),
    );
    expect(writes[0].body.permission_id).toBe(key);
  });
  it("미설정 권한은 불허로 채우지 않고 최초 저장 UUID를 재시도에 보존한다", async () => {
    emptyPermission = true;
    failWrite = true;
    open("menu-permissions");
    await screen.findByText("미설정 — 불허 판정이 아닙니다.");
    await waitFor(() =>
      expect(
        screen.queryByText("선택 후보를 불러오는 중입니다."),
      ).not.toBeInTheDocument(),
    );
    expect(screen.getByLabelText("변경할 접근 허용 여부")).toHaveValue("");
    await userEvent.selectOptions(
      screen.getByRole("combobox", { name: "역할" }),
      key,
    );
    await userEvent.selectOptions(
      screen.getByRole("combobox", { name: "메뉴" }),
      key,
    );
    await userEvent.selectOptions(
      screen.getByLabelText("변경할 접근 허용 여부"),
      "true",
    );
    await userEvent.click(
      screen.getByRole("button", { name: "허용 설정 저장" }),
    );
    await screen.findByText("저장 실패");
    await userEvent.click(screen.getByRole("button", { name: "저장 재시도" }));
    await waitFor(() => expect(writes).toHaveLength(2));
    expect(writes[0].body.permission_id).toBe(writes[1].body.permission_id);
    expect(writes[0].body.permission_id).not.toBe(key);
    expect(writes[0].body.organization_code).toBeNull();
    expect(writes[0].body.user_id).toBeNull();
  });
  it("메뉴 구조는 선택 ID와 정수 순서를 보존한다", async () => {
    open("menu-structure");
    await selectRow();
    await userEvent.clear(screen.getByLabelText("표시순서"));
    await userEvent.type(screen.getByLabelText("표시순서"), "3");
    await userEvent.click(screen.getByRole("button", { name: "구조 저장" }));
    await waitFor(() => expect(writes[0]?.body.display_order).toBe(3));
    expect(writes[0].body.menu_id).toBe(key);
  });
  it("메뉴 실행정보를 독립 저장하고 위험 URL은 링크로 실행하지 않는다", async () => {
    open("menu-info");
    await querySelected("메뉴 조회");
    await screen.findByLabelText("URL");
    await userEvent.clear(screen.getByLabelText("URL"));
    await userEvent.type(screen.getByLabelText("URL"), "javascript:alert(1)");
    await userEvent.click(screen.getByRole("button", { name: "저장" }));
    await waitFor(() =>
      expect(writes[0]?.path).toBe("/api/menu-execution-info"),
    );
    expect(document.querySelector('a[href^="javascript:"]')).toBeNull();
  });
  it("코드그룹은 그룹 식별자를 잠그고 설명의 명시적 빈 문자열을 저장한다", async () => {
    open("code-groups");
    await selectRow();
    expect(screen.getByRole("textbox", { name: "그룹ID" })).toHaveAttribute(
      "readonly",
    );
    await userEvent.clear(screen.getByLabelText("설명"));
    await userEvent.click(screen.getByRole("button", { name: "저장" }));
    await waitFor(() => expect(writes[0]?.body.description).toBe(""));
  });
  it("상세코드 조회는 선택 그룹과 서버 순서를 사용한다", async () => {
    open("detail-codes");
    await querySelected("코드그룹 조회");
    await selectRow();
    await userEvent.click(screen.getByRole("button", { name: "저장" }));
    await waitFor(() => expect(writes[0]?.body.group_id).toBe(key));
    expect(writes[0].body.code).toBe(key);
  });
  it.each(["변경", "회수"] as const)(
    "%s은 선택 ID와 명령·별도 승인자를 그대로 전송한다",
    async (action) => {
      open("user-roles");
      await selectRow();
      const assignment_id = records["/api/user-role-assignments"].assignment_id;
      await userEvent.selectOptions(screen.getByLabelText("승인자"), key);
      await userEvent.click(screen.getByRole("button", { name: action }));
      await waitFor(() => expect(writes[0]?.body.action).toBe(action));
      expect(writes[0].body.assignment_id).toBe(assignment_id);
      expect(writes[0].body.approver_id).toBe(key);
      expect(writes[0].body).not.toHaveProperty("is_current");
      expect(writes[0].body).not.toHaveProperty("origin");
    },
  );
  it("승인자가 비어 있으면 자동 지정하거나 서버에 제출하지 않는다", async () => {
    open("user-roles");
    await screen.findByRole("heading", { name: "사용자 역할 관리", level: 1 });
    await waitFor(() =>
      expect(
        screen.queryByText("선택 후보를 불러오는 중입니다."),
      ).not.toBeInTheDocument(),
    );
    await userEvent.click(screen.getByRole("button", { name: "부여" }));
    expect(
      await screen.findByText("승인자 항목을 입력해 주세요."),
    ).toBeVisible();
    expect(writes).toHaveLength(0);
  });
  it("저장 403은 이전 보호 데이터와 편집을 숨긴다", async () => {
    open("roles");
    await selectRow();
    denied = "/api/roles";
    await userEvent.click(screen.getByRole("button", { name: "저장" }));
    expect(await screen.findByText("접근 권한이 없습니다.")).toBeVisible();
    expect(screen.queryByLabelText("목적")).not.toBeInTheDocument();
    expect(screen.queryByRole("table")).not.toBeInTheDocument();
  });
});
