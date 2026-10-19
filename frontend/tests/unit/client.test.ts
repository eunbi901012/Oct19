import { describe, expect, it, vi } from "vitest";
import { allPages, ApiFailure, queryString } from "../../src/api/client";
import { safeInternalUrl } from "../../src/components/access";
import type { Page } from "../../src/api/types";

describe("API 경계", () => {
  it("빈 선택 필터만 생략하고 boolean false를 보존한다", () => {
    expect(
      queryString({
        page: 1,
        size: 20,
        role_id: "",
        user_id: undefined,
        enabled: false,
      }),
    ).toBe("page=1&size=20&enabled=false");
  });
  it("실제 후보의 모든 페이지를 서버 순서대로 이어 읽는다", async () => {
    const read = vi.fn(
      async ({
        page,
      }: {
        page: number;
        size: number;
      }): Promise<Page<{ id: string }>> => ({
        success: true,
        meta: {},
        data: [{ id: `page-${page}` }],
        pagination: { page, size: 1, total: 2 },
      }),
    );
    expect(await allPages(read)).toEqual([{ id: "page-1" }, { id: "page-2" }]);
    expect(read).toHaveBeenCalledTimes(2);
  });
  it("불완전한 후보 페이지를 정상 빈 목록으로 숨기지 않는다", async () => {
    const read = async (): Promise<Page<never>> => ({
      success: true,
      meta: {},
      data: [],
      pagination: { page: 1, size: 20, total: 1 },
    });
    await expect(allPages(read)).rejects.toBeInstanceOf(ApiFailure);
  });
  it.each([
    "javascript:alert(1)",
    "//outside.invalid",
    "/\\outside",
    "/%2foutside",
    "/\nunsafe",
  ])("위험 실행 URL을 링크로 제공하지 않는다: %s", (value) => {
    expect(safeInternalUrl(value)).toBe(false);
  });
  it("동일 출처 내부 URL만 실행 fallback으로 제공한다", () => {
    expect(safeInternalUrl("/admin/users?user_id=selected")).toBe(true);
  });
});
