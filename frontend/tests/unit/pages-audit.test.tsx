import { render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import App from "../../src/App";
import type { DetailCode, DetailCodeInput } from "../../src/api/types";

let rows: DetailCode[];
let writes: DetailCodeInput[];

function response(data: unknown, status = 200) {
  return new Response(JSON.stringify(data), { status });
}

beforeEach(() => {
  rows = [
    { group_id: "audit-group", code: "root", parent_code: "", sort_order: 1 },
    {
      group_id: "audit-group",
      code: "child",
      parent_code: "root",
      sort_order: 2,
    },
  ];
  writes = [];
  vi.stubGlobal(
    "fetch",
    vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
      const url = new URL(String(input), "http://localhost");
      if (url.pathname === "/api/accessible-menus") {
        return response({
          success: true,
          meta: {},
          data: {
            menus: [
              {
                menu_id: "audit-menu",
                parent_menu_id: "",
                display_order: 1,
                screen_id: "SCR-DETAIL-CODES",
                url: "/admin/detail-codes",
              },
            ],
          },
        });
      }
      if (url.pathname === "/api/detail-codes" && init?.method === "PUT") {
        const body = JSON.parse(String(init.body)) as DetailCodeInput;
        writes.push(body);
        if (
          body.parent_code !== undefined &&
          !rows.some((row) => row.code === body.parent_code)
        ) {
          return response(
            {
              success: false,
              meta: {},
              message: "Parent code must exist in the selected group",
            },
            400,
          );
        }
        rows = rows.map((row) =>
          row.code === body.code ? { ...row, ...body } : row,
        );
        return response({
          success: true,
          meta: {},
          data: rows.find((row) => row.code === body.code),
        });
      }
      const data =
        url.pathname === "/api/code-groups"
          ? [
              {
                group_id: "audit-group",
                name: "audit-group",
                description: "",
                managing_department: "",
              },
            ]
          : url.pathname === "/api/detail-codes"
            ? rows
            : [];
      return response({
        success: true,
        meta: {},
        data,
        pagination: {
          page: Number(url.searchParams.get("page")),
          size: 100,
          total: data.length,
        },
      });
    }),
  );
  window.history.replaceState({}, "", "/admin/detail-codes");
});
afterEach(() => vi.unstubAllGlobals());

async function openCode(code: string) {
  render(<App />);
  const group = await screen.findByLabelText("코드그룹 조회");
  await waitFor(() =>
    expect(
      within(group).getByRole("option", { name: /audit-group/ }),
    ).toBeInTheDocument(),
  );
  await userEvent.selectOptions(group, "audit-group");
  await userEvent.click(screen.getByRole("button", { name: "조회" }));
  const cell = (
    await screen.findAllByRole("cell", {
      name: code === "child" ? "↳ child" : "root",
    })
  )[0];
  await userEvent.click(
    within(cell.closest("tr")!).getByRole("button", { name: "선택" }),
  );
  await waitFor(() =>
    expect(
      screen.queryByText("상위코드 후보를 불러오는 중입니다."),
    ).not.toBeInTheDocument(),
  );
}

async function save() {
  await userEvent.click(screen.getByRole("button", { name: "저장" }));
  await waitFor(() => expect(writes).toHaveLength(1));
}

describe("상세코드 optional 부모 계약 audit", () => {
  it("기존 루트의 순서만 저장할 때 미변경 빈 부모를 보내지 않는다", async () => {
    await openCode("root");
    expect(screen.getByRole("textbox", { name: "코드그룹" })).toHaveAttribute(
      "readonly",
    );
    expect(screen.getByLabelText("상세코드")).toHaveAttribute("readonly");
    await userEvent.clear(screen.getByLabelText("정렬순서"));
    await userEvent.type(screen.getByLabelText("정렬순서"), "3");
    await save();
    expect(writes[0]).toEqual({
      group_id: "audit-group",
      code: "root",
      sort_order: 3,
    });
    expect(
      await screen.findByText("저장 후 재조회가 완료되었습니다."),
    ).toBeVisible();
  });

  it("실제 변경한 부모는 saved 복합 식별자와 함께 전송한다", async () => {
    await openCode("root");
    await userEvent.selectOptions(screen.getByLabelText("상위코드"), "child");
    await save();
    expect(writes[0]).toEqual({
      group_id: "audit-group",
      code: "root",
      parent_code: "child",
      sort_order: 1,
    });
  });

  it("부모 변경 취소 후 기존 부모를 보존하는 생략 payload로 저장한다", async () => {
    await openCode("child");
    await userEvent.selectOptions(screen.getByLabelText("상위코드"), "");
    await userEvent.click(screen.getByRole("button", { name: "취소" }));
    expect(screen.getByLabelText("상위코드")).toHaveValue("root");
    await save();
    expect(writes[0]).toEqual({
      group_id: "audit-group",
      code: "child",
      sort_order: 2,
    });
  });

  it("명시적 부모 해제는 생략으로 숨기지 않고 서버 검증 오류를 유지한다", async () => {
    await openCode("child");
    await userEvent.selectOptions(screen.getByLabelText("상위코드"), "");
    await save();
    expect(writes[0].parent_code).toBe("");
    expect(
      await screen.findByText("Parent code must exist in the selected group"),
    ).toBeVisible();
    expect(screen.getByLabelText("상위코드")).toHaveValue("");
  });
});
