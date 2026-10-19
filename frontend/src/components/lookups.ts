import { useEffect, useState } from "react";
import { allPages, messageOf } from "../api/client";
import { searchUsers } from "../api/users";
import { getOrganizationRelations } from "../api/organizations";
import { listRoles } from "../api/roles";
import { getMenuStructure } from "../api/menus";
import { listCodeGroups } from "../api/code_groups";

export type Source = "users" | "organizations" | "roles" | "menus" | "groups";
export interface Option {
  value: string;
  label: string;
}
const readers: Record<Source, (signal: AbortSignal) => Promise<Option[]>> = {
  users: async (signal) =>
    (await allPages(searchUsers, signal)).map((row) => ({
      value: row.user_id,
      label: `${row.person_name} · ${row.employee_number}`,
    })),
  organizations: async (signal) =>
    (await allPages(getOrganizationRelations, signal)).map((row) => ({
      value: row.organization_code,
      label: row.organization_code,
    })),
  roles: async (signal) =>
    (await allPages(listRoles, signal)).map((row) => ({
      value: row.role_id,
      label: row.role_id,
    })),
  menus: async (signal) =>
    (await allPages(getMenuStructure, signal)).map((row) => ({
      value: row.menu_id,
      label: row.menu_id,
    })),
  groups: async (signal) =>
    (await allPages(listCodeGroups, signal)).map((row) => ({
      value: row.group_id,
      label: `${row.name} · ${row.group_id}`,
    })),
};
export function useLookups(sources: Source[]) {
  const [options, setOptions] = useState<Partial<Record<Source, Option[]>>>({});
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [retry, setRetry] = useState(0);
  const key = [...new Set(sources)].sort().join(",");
  useEffect(() => {
    const controller = new AbortController();
    setLoading(Boolean(key));
    setError("");
    setOptions({});
    Promise.all(
      key
        ? key.split(",").map(async (source) => {
            const typed = source as Source;
            return [typed, await readers[typed](controller.signal)] as const;
          })
        : [],
    )
      .then((entries) => {
        if (!controller.signal.aborted) setOptions(Object.fromEntries(entries));
      })
      .catch((reason) => {
        if (!controller.signal.aborted)
          setError(`후보 조회 실패: ${messageOf(reason)}`);
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false);
      });
    return () => controller.abort();
  }, [key, retry]);
  return {
    options,
    error,
    loading,
    retry: () => setRetry((value) => value + 1),
  };
}
