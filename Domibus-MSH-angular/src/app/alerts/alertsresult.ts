import {AlertsEntry} from "./alertsentry";

export class AlertsResult {

  constructor(public alertsEntries: Array<AlertsEntry>,
              public count: number) {

  }
}
