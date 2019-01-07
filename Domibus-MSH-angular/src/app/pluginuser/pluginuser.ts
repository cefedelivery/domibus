export enum UserState {
  PERSISTED,
  NEW,
  UPDATED,
  REMOVED
}

export class PluginUserRO {
  entityId: number;
  userName: string;
  password: string;
  certificateId: string;
  originalUser: string;
  authRoles: string;
  authenticationType: string;
  status: string;
  active: boolean;
  suspended: boolean;
}
