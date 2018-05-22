import {AlertsEntry} from "./alertsentry";

export class AlertsResult {

  constructor(public alertsEntries: Array<AlertsEntry>,
              public pageSize: number,
              public count: number,
              public filter: any,
              public alertsType: Array<string>,
              public alertsLevels: Array<string>
  ) {

  }
}
