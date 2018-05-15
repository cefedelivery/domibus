export class AlertsEntry {
  constructor(public processed: boolean,
              public alertId: string,
              public alertType: string,
              public alertLevel: string,
              public alertText: string,
              public creationTime: Date,
              public reportingTime: Date,
              public parameters: string[])
  {

  }
}
