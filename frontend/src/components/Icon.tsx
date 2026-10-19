import type { SVGProps } from "react";

const paths = {
  users:
    "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2m20 0v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75M13 7a4 4 0 1 1-8 0 4 4 0 0 1 8 0Z",
  organizations: "M9 3h6v6H9zM2 16h6v5H2zM16 16h6v5h-6zM12 9v4M5 16v-3h14v3",
  roles: "m12 3 8 3v6c0 5-8 9-8 9s-8-4-8-9V6l8-3Zm-4 9 3 3 5-5",
  assignments:
    "M15 7a4 4 0 1 1-8 0 4 4 0 0 1 8 0ZM3 21v-2a5 5 0 0 1 5-5h4m6 1v6m-3-3h6",
  permissions: "M5 10h14v11H5zM8 10V7a4 4 0 0 1 8 0v3m-4 5v2",
  structure: "M3 3h6v6H3zM15 15h6v6h-6zM3 15h6v6H3zM6 9v6m3-9h9v9",
  execution: "M14 3h7v7m0-7L10 14M10 3H3v18h18v-7",
  groups: "M3 7h7l2 2h9v12H3V7Zm0 0V3h7l2 2h9v4",
  codes: "m8 5-6 7 6 7m8-14 6 7-6 7M14 3l-4 18",
  search: "M21 21l-5-5M18 10a8 8 0 1 1-16 0 8 8 0 0 1 16 0Z",
  save: "m19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h12l4 4v12a2 2 0 0 1-2 2ZM7 3v6h10V3M7 21v-8h10v8",
  refresh:
    "M20 7v5h-5M4 17v-5h5m-4-4a8 8 0 0 1 13-4l2 3M4 17l2 3a8 8 0 0 0 13-4",
  menu: "M3 6h18M3 12h18M3 18h18",
  close: "m6 6 12 12M6 18 18 6",
  info: "M12 8h.01M12 11v6M22 12a10 10 0 1 1-20 0 10 10 0 0 1 20 0Z",
  check: "m8 12 3 3 5-6M22 12a10 10 0 1 1-20 0 10 10 0 0 1 20 0Z",
  alert: "M12 8v5m0 4h.01M10 3 1 20h22L14 3Z",
  arrow: "M5 12h14m-5-5 5 5-5 5",
} as const;
export type IconName = keyof typeof paths;
export function Icon({
  name,
  ...props
}: SVGProps<SVGSVGElement> & { name: IconName }) {
  return (
    <svg
      width="18"
      height="18"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.7"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      focusable="false"
      {...props}
    >
      <path d={paths[name]} />
    </svg>
  );
}
