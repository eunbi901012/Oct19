import { render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, describe, expect, it, vi } from "vitest";
import App from "../../src/App";
import type { MenuPermission, MenuPermissionInput } from "../../src/api/types";

function json(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), { status });
}
function page(data: unknown[], total = data.length) {
  return json({
    success: true,
    meta: {},
    data,
    pagination: { page: 1, size: 20, total },
  });
}
function access(screenId: string, route: string) {
  return json({
    success: true,
    meta: {},
    data: {
      menus: [
        {
          menu_id: "contract-menu",
          parent_menu_id: "",
          display_order: 1,
          screen_id: screenId,
          url: route,
        },
      ],
    },
  });
}
function open(route: string) {
  window.history.replaceState({}, "", route);
  render(<App />);
}
afterEach(() => vi.unstubAllGlobals());

it("조회 field_errors는 검색 필드에 연결하고 입력과 GET 재시도를 보존한다", async () => {
  let fail = false;
  const fetcher = vi.fn(
    async (input: RequestInfo | URL, init?: RequestInit) => {
      expect(init?.method).not.toBe("PUT");
      const url = new URL(String(input), "http://localhost");
      if (url.pathname === "/api/accessible-menus")
        return access("SCR-USERS", "/admin/users");
      if (url.pathname === "/api/users" && fail) {
        return json(
          {
            success: false,
            meta: {},
            message: "검색 조건 오류",
            field_errors: [
              {
                field: "employee_number",
                message: "교번 형식을 확인해 주세요.",
              },
              { field: "enabled", message: "사용여부 조건을 확인해 주세요." },
            ],
          },
          400,
        );
      }
      return page([]);
    },
  );
  vi.stubGlobal("fetch", fetcher);
  open("/admin/users");
  await screen.findByText("조회 조건에 맞는 결과가 없습니다.");
  await userEvent.type(screen.getByLabelText("교번 조회"), "invalid");
  await userEvent.selectOptions(screen.getByLabelText("사용여부 조회"), "true");
  fail = true;
  await userEvent.click(screen.getByRole("button", { name: "검색" }));
  await screen.findByText("검색 조건 오류");
  expect(screen.getByLabelText("교번 조회")).toHaveAttribute(
    "aria-invalid",
    "true",
  );
  expect(screen.getByLabelText("교번 조회")).toHaveAccessibleDescription(
    "교번 형식을 확인해 주세요.",
  );
  expect(screen.getByLabelText("사용여부 조회")).toHaveAttribute(
    "aria-invalid",
    "true",
  );
  expect(screen.getByLabelText("교번 조회")).toHaveValue("invalid");
  fail = false;
  await userEvent.click(screen.getByRole("button", { name: "조회 재시도" }));
  await screen.findByText("조회 조건에 맞는 결과가 없습니다.");
  expect(screen.getByLabelText("교번 조회")).toHaveAttribute(
    "aria-invalid",
    "false",
  );
  expect(
    screen.queryByText("교번 형식을 확인해 주세요."),
  ).not.toBeInTheDocument();
  expect(screen.getByLabelText("교번 조회")).toHaveValue("invalid");
});

const permission: MenuPermission = {
  permission_id: "saved-permission",
  role_id: "contract-role",
  organization_code: null,
  user_id: null,
  menu_id: "contract-menu",
  allowed: false,
};
function permissionApi(
  rows: MenuPermission[],
  total = rows.length,
  rejectWrite = false,
) {
  const writes: MenuPermissionInput[] = [];
  vi.stubGlobal(
    "fetch",
    vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
      const url = new URL(String(input), "http://localhost");
      if (url.pathname === "/api/accessible-menus")
        return access("SCR-MENU-PERMISSIONS", "/admin/menu-permissions");
      if (url.pathname === "/api/menu-permissions") {
        if (init?.method === "PUT") {
          const body = JSON.parse(String(init.body)) as MenuPermissionInput;
          writes.push(body);
          if (rejectWrite)
            return json(
              {
                success: false,
                meta: {},
                message: "저장 조건 오류",
                field_errors: [{ field: "menu_id", message: "메뉴 저장 오류" }],
              },
              400,
            );
          return json({ success: true, meta: {}, data: body });
        }
        return page(rows, total);
      }
      if (url.pathname === "/api/roles")
        return page([
          {
            role_id: "contract-role",
            purpose: "",
            grant_criteria: "",
            default_data_scope: "",
          },
        ]);
      if (url.pathname === "/api/menu-structure")
        return page([
          {
            menu_id: "contract-menu",
            parent_menu_id: "",
            level: 1,
            display_order: 1,
          },
        ]);
      return page([]);
    }),
  );
  return writes;
}

describe("메뉴 권한의 기존 선택과 최초 저장 경계", () => {
  it("기존 결과를 미설정으로 표시하지 않고 행 선택 후 saved ID로 저장한다", async () => {
    const writes = permissionApi([permission]);
    open("/admin/menu-permissions");
    const select = await screen.findByRole("button", { name: "선택" });
    expect(
      screen.queryByRole("button", { name: "허용 설정 저장" }),
    ).not.toBeInTheDocument();
    expect(
      screen.queryByText("미설정 — 불허 판정이 아닙니다."),
    ).not.toBeInTheDocument();
    await userEvent.click(select);
    await waitFor(() =>
      expect(
        screen.getByRole("button", { name: "허용 설정 저장" }),
      ).toBeEnabled(),
    );
    await userEvent.selectOptions(
      screen.getByLabelText("변경할 접근 허용 여부"),
      "true",
    );
    await userEvent.click(
      screen.getByRole("button", { name: "허용 설정 저장" }),
    );
    await screen.findByText("저장 후 재조회가 완료되었습니다.");
    expect(writes).toEqual([{ ...permission, allowed: true }]);
  });

  it("빈 페이지라도 전체 결과가 있으면 미설정 신규 폼을 열지 않는다", async () => {
    permissionApi([], 1);
    open("/admin/menu-permissions");
    await screen.findByText("조회 조건에 맞는 결과가 없습니다.");
    expect(
      screen.queryByRole("button", { name: "허용 설정 저장" }),
    ).not.toBeInTheDocument();
  });

  it("실제 0건 조회는 신규 UUID와 null 대상을 유지하고 저장 오류를 검색에 표시하지 않는다", async () => {
    const writes = permissionApi([], 0, true);
    open("/admin/menu-permissions");
    const editor = await screen.findByRole("region", {
      name: "선택 대상과 메뉴의 단일 설정",
    });
    await waitFor(() =>
      expect(
        within(editor).getByRole("button", { name: "허용 설정 저장" }),
      ).toBeEnabled(),
    );
    await userEvent.selectOptions(
      within(editor).getByLabelText("역할"),
      "contract-role",
    );
    await userEvent.selectOptions(
      within(editor).getByLabelText("메뉴"),
      "contract-menu",
    );
    await userEvent.selectOptions(
      screen.getByLabelText("변경할 접근 허용 여부"),
      "true",
    );
    await userEvent.click(
      within(editor).getByRole("button", { name: "허용 설정 저장" }),
    );
    await screen.findByText("메뉴 저장 오류");
    expect(within(editor).getByLabelText("메뉴")).toHaveAttribute(
      "aria-invalid",
      "true",
    );
    expect(screen.getByLabelText("메뉴 조회")).toHaveAttribute(
      "aria-invalid",
      "false",
    );
    await userEvent.click(screen.getByRole("button", { name: "저장 재시도" }));
    await waitFor(() => expect(writes).toHaveLength(2));
    expect(writes[0]).toEqual({
      permission_id: expect.any(String),
      role_id: "contract-role",
      organization_code: null,
      user_id: null,
      menu_id: "contract-menu",
      allowed: true,
    });
    expect(writes[0].permission_id).not.toBe("");
    expect(writes[1]).toEqual(writes[0]);
  });
});
