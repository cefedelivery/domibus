import {ErrorLogEntry} from "./errorlogentry";
export class ErrorLogResult {

  constructor(public errorLogEntries: Array<ErrorLogEntry>,
              // public offset: number,
              public pageSize: number,
              // public orderBy: string,
              // public asc: boolean,
              public count: number,
              public filter: any,
              public mshRoles: Array<string>,
              public errorCodes: Array<string>) {
  }
}
