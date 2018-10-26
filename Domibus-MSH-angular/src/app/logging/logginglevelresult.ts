import {LoggingLevelEntry} from "./logginglevelentry";

export class LoggingLevelResult {

  constructor(public loggingEntries: Array<LoggingLevelEntry>,
              public pageSize: number,
              public count: number,
              public filter: any,
              public levels: Array<string>) {
  }
}
