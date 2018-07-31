export enum UserState {
  PERSISTED,
  NEW,
  UPDATED,
  REMOVED
}

export class PluginUserRO {
  entityId: number;
  username: string;
  passwd: string;
  certificateId: string;
  originalUser: string;
  authRoles: string;
  authenticationType: string;
  status: string;
}
