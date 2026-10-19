import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import type { ComponentType } from "react";
import { BrowserRouter, Link, useLocation } from "react-router-dom";
import { getAccessibleMenus } from "./api/menus";
import type { AccessibleMenu } from "./api/types";
import { isDenied, messageOf } from "./api/client";
import { AccessContext, safeInternalUrl } from "./components/access";
import { EmptyState, LoadingState, Notice } from "./components/primitives";
import { Icon, type IconName } from "./components/Icon";
import UserPage from "./pages/UserPage";
import OrganizationPage from "./pages/OrganizationPage";
import RolePage from "./pages/RolePage";
import UserRoleAssignmentPage from "./pages/UserRoleAssignmentPage";
import MenuPermissionPage from "./pages/MenuPermissionPage";
import MenuStructurePage from "./pages/MenuStructurePage";
import MenuExecutionInfoPage from "./pages/MenuExecutionInfoPage";
import CodeGroupPage from "./pages/CodeGroupPage";
import DetailCodePage from "./pages/DetailCodePage";

interface Screen {
  id: string;
  path: string;
  title: string;
  group: string;
  page: ComponentType;
}
const screens: Screen[] = [
  {
    id: "SCR-USERS",
    path: "/admin/users",
    title: "사용자 관리",
    group: "인사·권한",
    page: UserPage,
  },
  {
    id: "SCR-ORGANIZATIONS",
    path: "/admin/organizations",
    title: "조직 관리",
    group: "인사·권한",
    page: OrganizationPage,
  },
  {
    id: "SCR-ROLES",
    path: "/admin/roles",
    title: "역할 관리",
    group: "인사·권한",
    page: RolePage,
  },
  {
    id: "SCR-USER-ROLES",
    path: "/admin/user-roles",
    title: "사용자 역할 관리",
    group: "인사·권한",
    page: UserRoleAssignmentPage,
  },
  {
    id: "SCR-MENU-PERMISSIONS",
    path: "/admin/menu-permissions",
    title: "메뉴 권한 관리",
    group: "메뉴",
    page: MenuPermissionPage,
  },
  {
    id: "SCR-MENU-STRUCTURE",
    path: "/admin/menu-structure",
    title: "메뉴 구조 관리",
    group: "메뉴",
    page: MenuStructurePage,
  },
  {
    id: "SCR-MENU-INFO",
    path: "/admin/menu-info",
    title: "메뉴 정보 관리",
    group: "메뉴",
    page: MenuExecutionInfoPage,
  },
  {
    id: "SCR-CODE-GROUPS",
    path: "/admin/code-groups",
    title: "코드그룹 관리",
    group: "코드",
    page: CodeGroupPage,
  },
  {
    id: "SCR-DETAIL-CODES",
    path: "/admin/detail-codes",
    title: "상세코드 관리",
    group: "코드",
    page: DetailCodePage,
  },
];
const screenIcons: Record<string, IconName> = {
  "SCR-USERS": "users",
  "SCR-ORGANIZATIONS": "organizations",
  "SCR-ROLES": "roles",
  "SCR-USER-ROLES": "assignments",
  "SCR-MENU-PERMISSIONS": "permissions",
  "SCR-MENU-STRUCTURE": "structure",
  "SCR-MENU-INFO": "execution",
  "SCR-CODE-GROUPS": "groups",
  "SCR-DETAIL-CODES": "codes",
};
function matches(menu: AccessibleMenu, screen: Screen): boolean {
  if (menu.screen_id !== screen.id || !safeInternalUrl(menu.url)) return false;
  return new URL(menu.url, window.location.origin).pathname === screen.path;
}
function Console() {
  const location = useLocation();
  const [menus, setMenus] = useState<AccessibleMenu[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [denied, setDenied] = useState(false);
  const [mobileNav, setMobileNav] = useState(false);
  const navTrigger = useRef<HTMLButtonElement>(null);
  const navClose = useRef<HTMLButtonElement>(null);
  const navigation = useRef<HTMLElement>(null);
  const closeNav = useCallback(() => {
    setMobileNav(false);
    navTrigger.current?.focus();
  }, []);
  useEffect(() => {
    if (!mobileNav) return;
    navClose.current?.focus();
    const previousOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    const keydown = (event: KeyboardEvent) => {
      if (event.key === "Escape") closeNav();
      if (event.key !== "Tab") return;
      const items =
        navigation.current?.querySelectorAll<HTMLElement>("button, a[href]");
      if (!items?.length) return;
      const first = items[0];
      const last = items[items.length - 1];
      if (event.shiftKey && document.activeElement === first) {
        event.preventDefault();
        last.focus();
      } else if (!event.shiftKey && document.activeElement === last) {
        event.preventDefault();
        first.focus();
      }
    };
    window.addEventListener("keydown", keydown);
    return () => {
      document.body.style.overflow = previousOverflow;
      window.removeEventListener("keydown", keydown);
    };
  }, [mobileNav, closeNav]);
  const sequence = useRef(0);
  const pending = useRef<AbortController>();
  const refresh = useCallback(async () => {
    const epoch = ++sequence.current;
    pending.current?.abort();
    const controller = new AbortController();
    pending.current = controller;
    try {
      const result = await getAccessibleMenus(controller.signal);
      if (epoch !== sequence.current || controller.signal.aborted) return;
      setMenus(result.menus);
      setError("");
      setDenied(false);
    } catch (reason) {
      if (epoch !== sequence.current || controller.signal.aborted) return;
      setMenus([]);
      setError(messageOf(reason));
      setDenied(isDenied(reason));
      throw reason;
    } finally {
      if (epoch === sequence.current && !controller.signal.aborted)
        setLoading(false);
    }
  }, []);
  useEffect(() => {
    void refresh().catch(() => {});
    return () => pending.current?.abort();
  }, [refresh]);
  useEffect(() => setMobileNav(false), [location.pathname]);
  const access = useMemo(
    () => ({
      menus,
      refresh,
      allowed: (screen_id: string, path?: string) => {
        const screen = screens.find(
          (item) => item.id === screen_id && (!path || item.path === path),
        );
        return Boolean(screen && menus.some((menu) => matches(menu, screen)));
      },
    }),
    [menus, refresh],
  );
  const active = screens.find((screen) => screen.path === location.pathname);
  const authorized = active && access.allowed(active.id, active.path);
  const Page = active?.page;
  const sorted = [...menus].sort(
    (left, right) => left.display_order - right.display_order,
  );
  function tree(parent: string, visited: Set<string>): React.ReactNode {
    return sorted
      .filter(
        (menu) => menu.parent_menu_id === parent && !visited.has(menu.menu_id),
      )
      .map((menu) => {
        const next = new Set(visited).add(menu.menu_id);
        const screen = screens.find((item) => matches(menu, item));
        return (
          <li key={menu.menu_id}>
            {screen ? (
              <Link
                to={screen.path}
                aria-current={active?.id === screen.id ? "page" : undefined}
              >
                <Icon name={screenIcons[screen.id]} />
                {screen.title}
              </Link>
            ) : (
              <span className="nav-parent">{menu.menu_id}</span>
            )}
            <ul>{tree(menu.menu_id, next)}</ul>
          </li>
        );
      });
  }
  const roots = sorted.filter(
    (menu) => !menus.some((parent) => parent.menu_id === menu.parent_menu_id),
  );
  const hasHierarchy = sorted.some((menu) =>
    menus.some((parent) => parent.menu_id === menu.parent_menu_id),
  );
  return (
    <AccessContext.Provider value={access}>
      <a className="skip-link" href="#main">
        본문으로 이동
      </a>
      <div className="app-shell">
        {mobileNav ? (
          <button
            className="nav-backdrop"
            aria-label="업무 메뉴 배경 닫기"
            onClick={closeNav}
          />
        ) : null}
        <aside
          id="work-navigation"
          ref={navigation}
          className={`sidebar ${mobileNav ? "open" : ""}`}
          aria-label="업무 탐색"
        >
          <div className="brand">
            <span>
              <Icon name="structure" width="23" height="23" />
            </span>
            <div>
              공통 관리<small>업무 운영 콘솔</small>
            </div>
          </div>
          <button
            ref={navClose}
            className="nav-close"
            aria-label="업무 메뉴 닫기"
            onClick={closeNav}
          >
            <Icon name="close" />
          </button>
          <div className="nav-caption">허용된 업무</div>
          <nav aria-label="업무 메뉴">
            {hasHierarchy ? (
              <ul className="nav-tree">
                {[...new Set(roots.map((menu) => menu.parent_menu_id))].map(
                  (root) => (
                    <li key={root}>
                      <ul>{tree(root, new Set())}</ul>
                    </li>
                  ),
                )}
              </ul>
            ) : (
              ["인사·권한", "메뉴", "코드"].map((group) => {
                const allowedMenus = sorted.flatMap((menu) => {
                  const screen = screens.find(
                    (item) => item.group === group && matches(menu, item),
                  );
                  return screen ? [screen] : [];
                });
                if (!allowedMenus.length) return null;
                return (
                  <section className="nav-group" key={group}>
                    <h2>{group}</h2>
                    {allowedMenus.map((screen) => (
                      <Link
                        key={screen.id}
                        to={screen.path}
                        aria-current={
                          active?.id === screen.id ? "page" : undefined
                        }
                      >
                        <Icon name={screenIcons[screen.id]} />
                        {screen.title}
                      </Link>
                    ))}
                  </section>
                );
              })
            )}
          </nav>
          <div className="sidebar-foot">
            서버 접근 판정 적용
            <br />
            <small>인사 · 메뉴 · 기준정보</small>
          </div>
        </aside>
        <div className="workspace">
          <header className="topbar">
            <button
              ref={navTrigger}
              className="nav-toggle"
              aria-label="업무 메뉴 열기"
              aria-expanded={mobileNav}
              aria-controls="work-navigation"
              onClick={() => setMobileNav((value) => !value)}
            >
              <Icon name="menu" />
            </button>
            <div className="breadcrumb">
              공통 관리 <span>/</span> {active?.group || "업무 선택"}
              {active ? (
                <>
                  <span>/</span>
                  {active.title}
                </>
              ) : null}
            </div>
            <div className="header-right">
              <span className="environment">관리 콘솔</span>
              <button
                disabled={loading}
                onClick={() => void refresh().catch(() => {})}
              >
                <Icon name="refresh" /> 메뉴 새로고침
              </button>
            </div>
          </header>
          <main id="main" tabIndex={-1}>
            {loading ? (
              <LoadingState label="접근 가능한 메뉴를 확인 중입니다." />
            ) : error ? (
              <Notice error>
                {denied ? "접근 권한이 없습니다." : error}
                <button onClick={() => void refresh().catch(() => {})}>
                  재시도
                </button>
              </Notice>
            ) : location.pathname === "/" ? (
              <EmptyState
                title={
                  menus.length
                    ? "허용된 업무 메뉴를 선택해 주세요."
                    : "사용 가능한 메뉴가 없습니다."
                }
                description="업무 메뉴에서 관리할 항목을 선택해 주세요."
              />
            ) : !active ? (
              <Notice error>요청한 화면을 찾을 수 없습니다.</Notice>
            ) : !authorized ? (
              <Notice error>접근 권한이 없습니다.</Notice>
            ) : Page ? (
              <Page key={active.id} />
            ) : null}
          </main>
          <footer>
            공통 관리 <span>변경사항은 저장 후 서버 재조회로 확인합니다.</span>
          </footer>
        </div>
      </div>
    </AccessContext.Provider>
  );
}
export default function App() {
  return (
    <BrowserRouter>
      <Console />
    </BrowserRouter>
  );
}
