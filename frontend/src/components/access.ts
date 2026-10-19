import { createContext, useContext } from "react";
import type { AccessibleMenu } from "../api/types";

export interface AccessState {
  menus: AccessibleMenu[];
  refresh: () => Promise<void>;
  allowed: (screen_id: string, path?: string) => boolean;
}
export const AccessContext = createContext<AccessState>({
  menus: [],
  refresh: async () => {},
  allowed: () => false,
});
export const useAccess = () => useContext(AccessContext);
export function safeInternalUrl(value: string): boolean {
  if (
    !value.startsWith("/") ||
    value.startsWith("//") ||
    /[\\\s\u0000-\u001f]/.test(value)
  )
    return false;
  try {
    const parsed = new URL(value, window.location.origin);
    return (
      parsed.origin === window.location.origin &&
      !/%(?:2f|5c|0[0-9a-f])/i.test(value)
    );
  } catch {
    return false;
  }
}
