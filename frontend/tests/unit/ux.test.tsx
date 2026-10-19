import { act, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, expect, it, vi } from "vitest";
import App from "../../src/App";

const menu = {
  menu_id: "role-menu",
  parent_menu_id: "",
  display_order: 1,
  screen_id: "SCR-ROLES",
  url: "/admin/roles",
};
function response(data: unknown) {
  return new Response(JSON.stringify({ success: true, meta: {}, data }));
}
function emptyPage() {
  return new Response(
    JSON.stringify({
      success: true,
      meta: {},
      data: [],
      pagination: { page: 1, size: 20, total: 0 },
    }),
  );
}
function open() {
  window.history.replaceState({}, "", "/admin/roles");
  return render(<App />);
}
afterEach(() => vi.unstubAllGlobals());

it("모바일 메뉴는 열림 상태를 알리고 Escape로 닫혀 트리거로 초점을 돌린다", async () => {
  vi.stubGlobal(
    "fetch",
    vi.fn(async (input: RequestInfo | URL) =>
      String(input) === "/api/accessible-menus"
        ? response({ menus: [menu] })
        : emptyPage(),
    ),
  );
  open();
  const trigger = screen.getByRole("button", { name: "업무 메뉴 열기" });
  expect(trigger).toHaveAttribute("aria-expanded", "false");
  await userEvent.click(trigger);
  expect(trigger).toHaveAttribute("aria-expanded", "true");
  expect(trigger).toHaveAttribute("aria-controls", "work-navigation");
  await userEvent.keyboard("{Escape}");
  expect(trigger).toHaveAttribute("aria-expanded", "false");
  expect(trigger).toHaveFocus();
});

it("목록 조회 중에는 접근 가능한 skeleton을 표시한다", async () => {
  vi.stubGlobal(
    "fetch",
    vi.fn(async (input: RequestInfo | URL) => {
      if (String(input) === "/api/accessible-menus")
        return response({ menus: [menu] });
      return new Promise<Response>(() => {});
    }),
  );
  open();
  expect(await screen.findByLabelText("조회 중입니다.")).toHaveAttribute(
    "aria-busy",
    "true",
  );
});

it("빈 결과에는 복구 안내를 제공하고 재조회 중에는 이전 empty를 표시하지 않는다", async () => {
  let reads = 0;
  let finish!: (result: Response) => void;
  vi.stubGlobal(
    "fetch",
    vi.fn(async (input: RequestInfo | URL) => {
      const url = new URL(String(input), "http://localhost");
      if (url.pathname === "/api/accessible-menus")
        return response({ menus: [menu] });
      if (url.searchParams.get("size") === "100") return emptyPage();
      reads += 1;
      if (reads === 1) return emptyPage();
      return new Promise<Response>((resolve) => {
        finish = resolve;
      });
    }),
  );
  open();
  expect(
    await screen.findByText("조회 조건에 맞는 결과가 없습니다."),
  ).toBeVisible();
  expect(
    screen.getByText("조회 조건을 변경한 뒤 다시 조회해 주세요."),
  ).toBeVisible();
  await userEvent.click(screen.getByRole("button", { name: "조회" }));
  await waitFor(() => expect(reads).toBe(2));
  expect(
    screen.queryByText("조회 조건에 맞는 결과가 없습니다."),
  ).not.toBeInTheDocument();
  await act(async () => finish(emptyPage()));
});
