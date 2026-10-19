export interface ApiResponse<T> {
  success: true;
  meta: { request_id?: string };
  data: T;
}
export interface PageMeta {
  page: number;
  size: number;
  total: number;
}
export interface Page<T> extends ApiResponse<T[]> {
  pagination: PageMeta;
}
export interface Paging {
  page: number;
  size: number;
}
export interface User {
  user_id: string;
  employee_number: string;
  person_name: string;
  organization_code: string;
  job_grade: string;
  employment_status: string;
  business_roles: string[];
  enabled: boolean;
  position: string;
  retired_on: string | null;
  last_synced_at: string | null;
}
export interface Organization {
  organization_code: string;
  organization_type: string;
  parent_code: string;
  child_codes: string[];
  valid_from: string | null;
  valid_to: string | null;
}
export interface Role {
  role_id: string;
  purpose: string;
  grant_criteria: string;
  default_data_scope: string;
}
export type AssignmentAction = "부여" | "변경" | "회수";
export interface UserRoleAssignment {
  assignment_id: string;
  user_id: string;
  role_id: string;
  valid_from: string | null;
  valid_to: string | null;
  approver_id: string;
  origin: "보직 기반" | "수동";
  action: AssignmentAction;
  is_current: boolean;
}
export interface MenuPermission {
  permission_id: string;
  role_id: string | null;
  organization_code: string | null;
  user_id: string | null;
  menu_id: string;
  allowed: boolean;
}
export interface MenuStructure {
  menu_id: string;
  parent_menu_id: string;
  level: number;
  display_order: number;
}
export interface MenuExecutionInfo {
  menu_id: string;
  screen_id: string;
  url: string;
}
export interface CodeGroup {
  group_id: string;
  name: string;
  description: string;
  managing_department: string;
}
export interface DetailCode {
  group_id: string;
  code: string;
  sort_order: number;
  parent_code: string;
}
export interface OrganizationRelation {
  relation_id: string;
  organization_code: string;
  parent_code: string;
  valid_from: string | null;
  valid_to: string | null;
}
export type UserInput = Pick<User, "user_id" | "enabled">;
export type OrganizationInput = Pick<
  Organization,
  "organization_code" | "parent_code" | "valid_from" | "valid_to"
>;
export type RoleInput = Role;
export type UserRoleAssignmentInput = Omit<
  UserRoleAssignment,
  "origin" | "is_current"
>;
export type MenuPermissionInput = Pick<
  MenuPermission,
  "permission_id" | "menu_id" | "allowed"
> &
  Partial<Pick<MenuPermission, "role_id" | "organization_code" | "user_id">>;
export type MenuStructureInput = MenuStructure;
export type MenuExecutionInfoInput = MenuExecutionInfo;
export type CodeGroupInput = CodeGroup;
export type DetailCodeInput = Omit<DetailCode, "parent_code"> & {
  parent_code?: string;
};
export interface AccessibleMenu {
  menu_id: string;
  parent_menu_id: string;
  display_order: number;
  screen_id: string;
  url: string;
}
export interface AccessibleMenuResult {
  menus: AccessibleMenu[];
}
export type Entity =
  | User
  | Organization
  | Role
  | UserRoleAssignment
  | MenuPermission
  | MenuStructure
  | MenuExecutionInfo
  | CodeGroup
  | DetailCode
  | OrganizationRelation;
