export enum UserState {
  PERSISTED,
  NEW,
  UPDATED,
  REMOVED
}

export class PluginUserRO {
  entityId: number;
  username: string;
  password: string;
  certificateId: string;
  originalUser: string;
  authRoles: string;
  authenticationType: string;
  status: string; 
}
