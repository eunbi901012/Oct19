import { list, put } from "./client";
import type {
  Paging,
  UserRoleAssignment,
  UserRoleAssignmentInput,
} from "./types";
export interface AssignmentQuery extends Paging {
  user_id?: string;
  filter?: string;
}
export const listUserRoleAssignments = (
  q: AssignmentQuery,
  signal?: AbortSignal,
) => list<UserRoleAssignment>("/api/user-role-assignments", { ...q }, signal);
export const saveUserRoleAssignment = (
  input: UserRoleAssignmentInput,
  signal?: AbortSignal,
) =>
  put<UserRoleAssignment, UserRoleAssignmentInput>(
    "/api/user-role-assignments",
    input,
    signal,
  );
