import { act, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, describe, expect, it, vi } from "vitest";
import App from "../../src/App";

const menu = {
  menu_id: "fixture-menu",
  parent_menu_id: "",
  display_order: 1,
  screen_id: "SCR-ROLES",
  url: "/admin/roles",
};
const row = {
  role_id: "fixture-key",
  purpose: "조회값",
  grant_criteria: "",
  default_data_scope: "",
};
function json(data: unknown, status = 200) {
  return new Response(JSON.stringify(data), { status });
}
function page(rows: unknown[]) {
  return json({
    success: true,
    meta: {},
    data: rows,
    pagination: { page: 1, size: 20, total: rows.length },
  });
}
function open() {
  window.history.replaceState({}, "", "/admin/roles");
  return render(<App />);
}
afterEach(() => vi.unstubAllGlobals());

describe("공통 상태와 경합 복구", () => {
  it("접근 판정 중에는 목록을 요청하지 않고 로딩만 표시한다", () => {
    const read = vi.fn(() => new Promise<Response>(() => {}));
    vi.stubGlobal("fetch", read);
    open();
    expect(screen.getByText("접근 가능한 메뉴를 확인 중입니다.")).toBeVisible();
    expect(screen.queryByRole("table")).not.toBeInTheDocument();
    expect(read).toHaveBeenCalledTimes(1);
  });
  it("명세 외 접근 응답을 허용으로 해석하지 않는다", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn(async () =>
        json({ success: true, meta: {}, data: { menus: [{}] } }),
      ),
    );
    open();
    expect(
      await screen.findByText("메뉴 접근 판정 형식이 올바르지 않습니다."),
    ).toBeVisible();
    expect(
      screen.queryByRole("link", { name: "역할 관리" }),
    ).not.toBeInTheDocument();
  });
  it("빈 조회는 이전 선택과 폼을 없애고 실패 재시도는 읽기만 수행한다", async () => {
    let count = 0;
    const read = vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
      expect(init?.method).not.toBe("PUT");
      if (String(input) === "/api/accessible-menus") {
        return json({ success: true, meta: {}, data: { menus: [menu] } });
      }
      const q = new URL(String(input), "http://localhost").searchParams;
      if (q.get("size") === "100") return page([row]);
      count += 1;
      if (count === 2)
        return json({ success: false, meta: {}, message: "조회 실패" }, 500);
      return page(count === 1 ? [row] : []);
    });
    vi.stubGlobal("fetch", read);
    open();
    await userEvent.click(await screen.findByRole("button", { name: "선택" }));
    await userEvent.click(screen.getByRole("button", { name: "조회" }));
    await screen.findByText("조회 실패");
    await userEvent.click(screen.getByRole("button", { name: "조회 재시도" }));
    expect(
      await screen.findByText("조회 조건에 맞는 결과가 없습니다."),
    ).toBeVisible();
    expect(screen.queryByLabelText("목적")).not.toBeInTheDocument();
  });
  it("늦은 이전 검색 응답은 취소되고 새 조회의 빈 상태를 덮지 않는다", async () => {
    let resolve!: (response: Response) => void;
    let oldSignal: AbortSignal | null | undefined;
    let count = 0;
    vi.stubGlobal(
      "fetch",
      vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
        if (String(input) === "/api/accessible-menus") {
          return json({ success: true, meta: {}, data: { menus: [menu] } });
        }
        const q = new URL(String(input), "http://localhost").searchParams;
        if (q.get("size") === "100") return page([row]);
        count += 1;
        if (count === 1) {
          oldSignal = init?.signal;
          return new Promise<Response>((done) => {
            resolve = done;
          });
        }
        return page([]);
      }),
    );
    open();
    await screen.findByText("조회 중입니다.");
    await waitFor(() =>
      expect(
        (screen.getByLabelText("역할 식별정보 조회") as HTMLSelectElement)
          .disabled,
      ).toBe(false),
    );
    await userEvent.selectOptions(
      screen.getByLabelText("역할 식별정보 조회"),
      row.role_id,
    );
    // A filter-context change may supersede an in-flight read; stale data must not reappear.
    await userEvent.click(screen.getByRole("button", { name: "조회" }));
    await waitFor(() => expect(oldSignal?.aborted).toBe(true));
    expect(
      await screen.findByText("조회 조건에 맞는 결과가 없습니다."),
    ).toBeVisible();
    await act(async () => resolve(page([row])));
    expect(
      screen.queryByRole("button", { name: "선택" }),
    ).not.toBeInTheDocument();
  });
});
