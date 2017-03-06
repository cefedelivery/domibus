import {ErrorLogEntry} from "./errorlogentry";
export class ErrorLogResult {

  constructor(public errorLogEntries: Array<ErrorLogEntry>,
              public count: number,
              public mshRoles: Array<string>,
              public errorCodes: Array<string>) {
  }
}
