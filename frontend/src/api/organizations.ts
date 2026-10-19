import { list, put } from "./client";
import type {
  Organization,
  OrganizationInput,
  OrganizationRelation,
  Paging,
} from "./types";
export interface OrganizationQuery extends Paging {
  organization_code?: string;
}
export const getOrganizationRelations = (
  q: OrganizationQuery,
  signal?: AbortSignal,
) => list<Organization>("/api/organizations", { ...q }, signal);
export const saveOrganizationRelation = (
  input: OrganizationInput,
  signal?: AbortSignal,
) => put<Organization, OrganizationInput>("/api/organizations", input, signal);
export const listOrganizationRelationHistory = (
  q: Paging & { organization_code: string; filter?: string },
  signal?: AbortSignal,
) =>
  list<OrganizationRelation>(
    "/api/organization-relation-history",
    { ...q },
    signal,
  );
