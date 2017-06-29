export class User {
  id: number;
  username: string;
  authorities: Array<string>;
  defaultPasswordUsed:boolean;

  constructor(id: number, login: string, profile: string, authorities: Array<string>,defaultPasswordUsed:boolean) {
  }
}
