import {MessageLogEntry} from "./messagelogentry";
export class MessageLogResult {

  constructor(public messageLogEntries: Array<MessageLogEntry>,
              public pageSize: number,
              public count: number,
              public filter: any,
              public mshRoles: Array<string>,
              public msgTypes: Array<string>,
              public msgStatus: Array<string>,
              public notifStatus: Array<string>
              ) {

  }
}
