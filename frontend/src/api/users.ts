import { list, put } from "./client";
import type { Paging, User, UserInput } from "./types";
export interface UserQuery extends Paging {
  employee_number?: string;
  person_name?: string;
  organization_code?: string;
  job_grade?: string;
  employment_status?: string;
  business_roles?: string;
  enabled?: boolean;
  filter?: string;
}
export const searchUsers = (q: UserQuery, signal?: AbortSignal) =>
  list<User>("/api/users", { ...q }, signal);
export const updateUserSettings = (input: UserInput, signal?: AbortSignal) =>
  put<User, UserInput>("/api/users", input, signal);
