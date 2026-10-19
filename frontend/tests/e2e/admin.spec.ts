import { expect, test } from "@playwright/test";
import type { APIRequestContext, Page } from "@playwright/test";

type Row = Record<string, string | number | boolean | string[] | null>;
const screens = [
  ["SCR-USERS", "/admin/users", "사용자 관리", "/api/users"],
  [
    "SCR-ORGANIZATIONS",
    "/admin/organizations",
    "조직 관리",
    "/api/organizations",
  ],
  ["SCR-ROLES", "/admin/roles", "역할 관리", "/api/roles"],
  [
    "SCR-USER-ROLES",
    "/admin/user-roles",
    "사용자 역할 관리",
    "/api/user-role-assignments",
  ],
  [
    "SCR-MENU-PERMISSIONS",
    "/admin/menu-permissions",
    "메뉴 권한 관리",
    "/api/menu-permissions",
  ],
  [
    "SCR-MENU-STRUCTURE",
    "/admin/menu-structure",
    "메뉴 구조 관리",
    "/api/menu-structure",
  ],
  [
    "SCR-MENU-INFO",
    "/admin/menu-info",
    "메뉴 정보 관리",
    "/api/menu-execution-info",
  ],
  [
    "SCR-CODE-GROUPS",
    "/admin/code-groups",
    "코드그룹 관리",
    "/api/code-groups",
  ],
  [
    "SCR-DETAIL-CODES",
    "/admin/detail-codes",
    "상세코드 관리",
    "/api/detail-codes",
  ],
] as const;
const fields: Record<string, string[]> = {
  "/api/users": ["user_id", "enabled"],
  "/api/organizations": [
    "organization_code",
    "parent_code",
    "valid_from",
    "valid_to",
  ],
  "/api/roles": ["role_id", "purpose", "grant_criteria", "default_data_scope"],
  "/api/user-role-assignments": [
    "assignment_id",
    "user_id",
    "role_id",
    "valid_from",
    "valid_to",
    "approver_id",
    "action",
  ],
  "/api/menu-permissions": [
    "permission_id",
    "role_id",
    "organization_code",
    "user_id",
    "menu_id",
    "allowed",
  ],
  "/api/menu-structure": [
    "menu_id",
    "parent_menu_id",
    "level",
    "display_order",
  ],
  "/api/menu-execution-info": ["menu_id", "screen_id", "url"],
  "/api/code-groups": [
    "group_id",
    "name",
    "description",
    "managing_department",
  ],
  "/api/detail-codes": ["group_id", "code", "sort_order", "parent_code"],
};
async function accessible(request: APIRequestContext) {
  const response = await request.get("/api/accessible-menus");
  expect(response.ok(), "승인된 인증 컨텍스트가 필요합니다.").toBeTruthy();
  return (await response.json()).data.menus as {
    screen_id: string;
    url: string;
  }[];
}
async function rows(
  request: APIRequestContext,
  endpoint: string,
  extra: Record<string, string> = {},
) {
  const result: Row[] = [];
  let page = 1;
  let total: number;
  do {
    const response = await request.get(endpoint, {
      params: { page, size: 100, ...extra },
    });
    expect(response.ok()).toBeTruthy();
    const body = await response.json();
    expect(body.pagination.page).toBe(page);
    expect(Number.isInteger(body.pagination.total)).toBe(true);
    total = body.pagination.total;
    const batch = body.data as Row[];
    expect(Array.isArray(batch)).toBe(true);
    expect(
      batch.length > 0 || result.length >= total,
      "페이지 중간의 빈 결과는 부재 증거가 아닙니다.",
    ).toBe(true);
    result.push(...batch);
    page += 1;
  } while (result.length < total);
  return result;
}
async function query(page: Page, label: string, value: string) {
  await expect(
    page.getByLabel(label).locator(`option[value="${value}"]`),
  ).toHaveCount(1);
  await page.getByLabel(label).selectOption(value);
  await page.getByRole("button", { name: "조회", exact: true }).click();
}
async function setText(page: Page, label: string, value: string) {
  await page.getByLabel(label, { exact: true }).fill(value);
}
async function saved(page: Page, endpoint: string, button: string) {
  const response = page.waitForResponse(
    (r) => r.url().includes(endpoint) && r.request().method() === "PUT",
  );
  await page.getByRole("button", { name: button, exact: true }).click();
  const result = await response;
  expect(result.ok()).toBeTruthy();
  const payload = result.request().postDataJSON() as Row;
  await expect(
    page.getByText("저장 후 재조회가 완료되었습니다."),
  ).toBeVisible();
  return payload;
}

test("실제 서버 판정으로 허용 탐색과 미등재 직접 진입을 일치시킨다", async ({
  page,
  request,
}) => {
  const menus = await accessible(request);
  for (const [id, path, title] of screens) {
    const response = await page.goto(path);
    const allowed = menus.some(
      (menu) =>
        menu.screen_id === id &&
        new URL(menu.url, "http://local").pathname === path,
    );
    if (allowed) {
      await expect(
        page.getByRole("heading", { name: title, level: 1 }),
      ).toBeVisible();
      await expect(
        page.getByRole("navigation").getByRole("link", { name: title }),
      ).toBeVisible();
      await page
        .getByRole("navigation")
        .getByRole("link", { name: title })
        .click();
      await expect(page).toHaveURL(new RegExp(path));
    } else {
      expect(
        response?.status(),
        "nginx와 backend가 직접 화면 URL을 차단해야 합니다.",
      ).toBe(403);
      await expect(page.getByRole("table")).toHaveCount(0);
    }
  }
});

for (const [id, path, title, endpoint] of screens) {
  test(`${title}: 실제 fixture 변경·서버 재조회·복구`, async ({
    page,
    request,
  }) => {
    test.skip(
      process.env.E2E_MUTATION_APPROVED !== "true",
      "전용 DB·합성 fixture·기간/승인 정책과 쓰기/복구 승인을 먼저 확인해야 합니다.",
    );
    const menus = await accessible(request);
    test.skip(
      !menus.some((menu) => menu.screen_id === id),
      "승인된 화면 접근 주체가 필요합니다.",
    );
    let records: Row[];
    let group_id = "";
    if (id === "SCR-DETAIL-CODES") {
      const groups = await rows(request, "/api/code-groups");
      expect(
        groups.length,
        "승인된 코드그룹 fixture가 필요합니다.",
      ).toBeGreaterThan(0);
      group_id = String(groups[0].group_id);
      records = await rows(request, endpoint, { group_id });
    } else records = await rows(request, endpoint);
    expect(
      records.length,
      `${title} 승인 fixture가 필요합니다.`,
    ).toBeGreaterThan(0);
    const original = records[0];
    const optional =
      endpoint === "/api/menu-permissions"
        ? ["role_id", "organization_code", "user_id"]
        : endpoint === "/api/detail-codes"
          ? ["parent_code"]
          : [];
    const payloadFields = fields[endpoint].filter(
      (field) =>
        !optional.includes(field) ||
        (original[field] !== null &&
          original[field] !== undefined &&
          original[field] !== ""),
    );
    const restore = Object.fromEntries(
      payloadFields.map((field) => [field, original[field]]),
    );
    if (id === "SCR-USER-ROLES") restore.action = "변경";
    await page.goto(path);
    await expect(
      page.getByRole("heading", { name: title, level: 1 }),
    ).toBeVisible();
    if (id === "SCR-ORGANIZATIONS")
      await query(page, "조직코드 조회", String(original.organization_code));
    else if (id === "SCR-MENU-INFO")
      await query(page, "메뉴 조회", String(original.menu_id));
    else {
      if (group_id) await query(page, "코드그룹 조회", group_id);
      await page
        .getByRole("button", { name: "선택", exact: true })
        .first()
        .click();
    }
    await page.getByRole("button", { name: "취소", exact: true }).click();
    let button = "저장";
    let changed: Row = {};
    if (id === "SCR-USERS") {
      changed = { enabled: !original.enabled };
      await page
        .getByLabel("시스템 사용여부")
        .selectOption(String(changed.enabled));
      button = "사용여부 저장";
    } else if (id === "SCR-ORGANIZATIONS") {
      const parent = process.env.E2E_ORG_PARENT_CODE;
      test.skip(
        !parent,
        "OQ-ORG-001 승인된 비순환 대체 상위조직 E2E_ORG_PARENT_CODE가 필요합니다.",
      );
      changed = { parent_code: parent! };
      await page.getByLabel("변경할 상위조직").selectOption(parent!);
      button = "상위관계 저장";
    } else if (id === "SCR-ROLES") {
      changed = { purpose: `${original.purpose} · 검증` };
      await setText(page, "목적", String(changed.purpose));
    } else if (id === "SCR-USER-ROLES") {
      const approver = process.env.E2E_APPROVER_ID;
      test.skip(
        !approver,
        "OQ-ROLE-001 승인된 승인자 E2E_APPROVER_ID가 필요합니다.",
      );
      changed = { approver_id: approver!, action: "변경" };
      await page.getByLabel("승인자", { exact: true }).selectOption(approver!);
      button = "변경";
    } else if (id === "SCR-MENU-PERMISSIONS") {
      test.skip(
        process.env.E2E_PERMISSION_WRITE_APPROVED !== "true",
        "자기 접근 회수 위험이 없는 fixture와 OQ-AUTH 승인 후 쓰기를 허용합니다.",
      );
      changed = { allowed: !original.allowed };
      await page
        .getByLabel("변경할 접근 허용 여부")
        .selectOption(String(changed.allowed));
      button = "허용 설정 저장";
    } else if (id === "SCR-MENU-STRUCTURE") {
      changed = { display_order: Number(original.display_order) + 1 };
      await setText(page, "표시순서", String(changed.display_order));
      button = "구조 저장";
    } else if (id === "SCR-MENU-INFO") {
      changed = {
        url: `${String(original.url).split("?")[0]}?ui_verification=1`,
      };
      await setText(page, "URL", String(changed.url));
    } else if (id === "SCR-CODE-GROUPS") {
      changed = { description: `${original.description} · 검증` };
      await setText(page, "설명", String(changed.description));
    } else {
      changed = { sort_order: Number(original.sort_order) + 1 };
      await setText(page, "정렬순서", String(changed.sort_order));
    }
    try {
      const payload = await saved(page, endpoint, button);
      expect(payload).toMatchObject(changed);
      expect(Object.keys(payload).sort()).toEqual([...payloadFields].sort());
      const reload = await rows(
        request,
        endpoint,
        group_id ? { group_id } : {},
      );
      const identity = fields[endpoint][0];
      const current = reload.find(
        (row) =>
          row[identity] === original[identity] &&
          (id !== "SCR-DETAIL-CODES" || row.code === original.code),
      );
      expect(current).toMatchObject(changed);
      if (id === "SCR-ORGANIZATIONS") {
        const historyResponse = page.waitForResponse((r) =>
          r.url().includes("/api/organization-relation-history?"),
        );
        await page.getByRole("button", { name: "이력 조회" }).click();
        expect((await historyResponse).ok()).toBeTruthy();
        await expect(page.getByRole("table")).toBeVisible();
      }
      if (id === "SCR-MENU-INFO") {
        const link = page
          .getByRole("region", { name: "사용 가능한 메뉴" })
          .getByRole("link")
          .first();
        await expect(link).toBeVisible();
        const href = await link.getAttribute("href");
        expect(href?.startsWith("/")).toBe(true);
        await link.click();
        await expect(page).toHaveURL(new RegExp(String(href).split("?")[0]));
      }
    } finally {
      // Organization restoration appends history; use disposable fixtures, never a live organizational dataset.
      const response = await request.put(endpoint, { data: restore });
      expect(
        response.ok(),
        "fixture 복원 실패: 서버 정책/승인 상태를 확인해 주세요.",
      ).toBeTruthy();
    }
  });
}
