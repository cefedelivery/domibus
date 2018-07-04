import {AlertsEntry} from "./alertsentry";

export class AlertsResult {

  constructor(public alertsEntries: Array<AlertsEntry>,
              public pageSize: number,
              public count: number) {

  }
}
