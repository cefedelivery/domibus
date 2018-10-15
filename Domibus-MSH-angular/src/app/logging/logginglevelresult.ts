import {LoggingLevelEntry} from "./logginglevelentry";

export class LoggingLevelResult {

  constructor(public loggingEntries: Array<LoggingLevelEntry>,
              // public offset: number,
              public pageSize: number,
              // public orderBy: string,
              // public asc: boolean,
              public count: number,
              public filter: any) {
  }
}
